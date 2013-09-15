package com.support.mbtalocpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;


public class SubwayJsonParser {
	String trainNo;
	String stopNames;
	String direction;
	ArrivingTransport arrivingTransport;
	String trainTitle;
	
	public SubwayJsonParser(String trainNo, String stopNames, String direction, String trainTitle) {
		this.trainNo = trainNo;
		this.stopNames = stopNames;
		this.direction = direction;
		this.trainTitle = trainTitle;
	}
	
	public void parseSubwayInfo() {
		String train[] = trainTitle.split(" ");
		URL url;
		InputStream is = null;
		try {
			url = new URL("http://developer.mbta.com/lib/rthr/" + train[0] + ".json");
			
			is = getJsonFeed(url);
			parseJson(is);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
		
	private static InputStream getJsonFeed(URL url) throws IOException, NullPointerException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(10000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		
		//Starting the query
		conn.connect();
		int responseCode = conn.getResponseCode();
		System.out.println("The response is: " + responseCode);    		
		InputStream is = conn.getInputStream();
		return is;		
	}
	 
	private void parseJson(InputStream is) throws IOException, NullPointerException, JSONException {
		String jsonString = getStringFromStream(is);		
		arrivingTransport = new ArrivingTransport();		
		arrivingTransport.routeTitle = trainTitle;
		JSONObject json = new JSONObject(jsonString);		
		JSONObject tripList = json.getJSONObject("TripList");
		arrivingTransport.transportType = "Subway";
		Long currentTime = tripList.getLong("CurrentTime");
		JSONArray trips = tripList.getJSONArray("Trips"); 		
		for(int i = 0 ; i < trips.length(); i++) {
			JSONObject trip = (JSONObject) trips.get(i);					
			if(!trip.isNull("Position")) {
				JSONObject position = (JSONObject) trip.getJSONObject("Position");
				Transport transport = new Transport();
				Long timestamp = position.getLong("Timestamp");
				transport.secSinceReport = (int) Math.abs(timestamp - currentTime);
				transport.lat = position.getDouble("Lat"); 
				transport.lng = position.getDouble("Long");
				transport.heading = position.getInt("Heading");
				arrivingTransport.vehicles.add(transport);
			}
			String destination = trip.getString("Destination");			
			String tempStringArray[] = destination.split(" ");
			destination = tempStringArray[0]; 		//trying to get the first word in a destination
			if(destination.equalsIgnoreCase(direction)) {
				arrivingTransport.dirTag = destination;
				arrivingTransport.direction = destination;
				JSONArray predictions = trip.getJSONArray("Predictions");				
				for(int j = 0 ; j < predictions.length(); j++) {  
					JSONObject prediction = (JSONObject) predictions.get(j);
					if(prediction.getString("StopID").equalsIgnoreCase(stopNames)) {
						arrivingTransport.stopTitle = prediction.getString("Stop");
						arrivingTransport.stopTag = stopNames;
						int seconds = prediction.getInt("Seconds");						
						int minutes = (int) Math.ceil(seconds/60);
						arrivingTransport.minutes.add(minutes);
						arrivingTransport.routeTag.add(trainNo);
					}
				}
			}
		}
	}	
	
	public ArrivingTransport getArrivingTransport() {
		return arrivingTransport;
	}
	
	private String getStringFromStream(InputStream is) {
		StringBuilder sBuilder = new StringBuilder();
		String stream;
		try {
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(is));
			while((stream = buffReader.readLine()) != null) {
				sBuilder.append(stream);
			}
			return sBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

}
