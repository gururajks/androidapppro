package com.support.mbtalocpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommuterRailParser {
	String trainNo;
	String stopNames;
	String direction;
	ArrivingTransport arrivingTransport;
	
	public CommuterRailParser(String trainNo, String stopNames, String direction) {
		this.trainNo = trainNo;
		this.stopNames = stopNames;
		this.direction = direction;
	}
	
	public void parseSubwayInfo() {
		ArrayList<String> commRailMap = AppConstants.COMMUTER_RAIL_TRAINS();
		URL url;
		InputStream is = null;
		try {
			int indexOfTrainName = commRailMap.indexOf(trainNo);
			url = new URL("http://developer.mbta.com/lib/RTCR/RailLine_" + indexOfTrainName + ".json");
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
		arrivingTransport.routeTitle = trainNo;
		arrivingTransport.transportType = "Commuter Rail";	
		JSONObject json = new JSONObject(jsonString);	
		JSONArray trips = json.getJSONArray("Messages");
		for(int i = 0 ; i < trips.length(); i++) {
			JSONObject trip = (JSONObject) trips.getJSONObject(i);
			String destination = trip.getString("Destination");
			String stop = trip.getString("Stop");
			System.out.println(destination + " " + stopNames + "/" + direction + " " + stop); 
			if(stop.equalsIgnoreCase(stopNames) && direction.equalsIgnoreCase(destination)) {
				String scheduledEpochTime = trip.getString("Scheduled");
				String timeStampEpochTime = trip.getString("TimeStamp");
				int scheduledMinutes = getMinutes(scheduledEpochTime, timeStampEpochTime);
				arrivingTransport.minutes.add(scheduledMinutes);
				arrivingTransport.routeTag.add(trainNo);
				arrivingTransport.stopTitle = stop;
				arrivingTransport.stopTag = stop;
				arrivingTransport.direction = destination;
				arrivingTransport.dirTag = destination;
			}
			
		}
		
	}
	
	//Takes the scheduled and the time stamp and returns the minutes left for arrival
	private int getMinutes(String scheduledEpochTime, String timeStampEpochTime) {
		int diff = Math.abs(Integer.parseInt(scheduledEpochTime) - Integer.parseInt(timeStampEpochTime));
		diff = (diff/1000) / 60;
		return diff;
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
