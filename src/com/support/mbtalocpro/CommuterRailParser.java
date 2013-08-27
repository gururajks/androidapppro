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
	
	public void parseCommuterRailInfo() {
		ArrayList<String> commRailMap = AppConstants.COMMUTER_RAIL_TRAINS();
		URL url;
		InputStream is = null;
		try {
			int indexOfTrainName = commRailMap.indexOf(trainNo);
			if(indexOfTrainName > 0) {			//Validity of the index 
				url = new URL("http://developer.mbta.com/lib/RTCR/RailLine_" + indexOfTrainName + ".json");
				is = getJsonFeed(url);
				parseJson(is);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			e.printStackTrace();
		} catch(JSONException e) {
			e.printStackTrace();
		} catch(NumberFormatException e) {
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
	
	private void parseJson(InputStream is) throws IOException, NullPointerException, JSONException, NumberFormatException {
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
			if(stop.equalsIgnoreCase(stopNames) && direction.equalsIgnoreCase(destination)) {
				String scheduledEpochTime = trip.getString("Scheduled");
				String timeStampEpochTime = trip.getString("TimeStamp");
				String lateness = trip.getString("Lateness");
				int scheduledMinutes = getMinutes(scheduledEpochTime, timeStampEpochTime);
				int latenessValue = 0;
				if(lateness != "") latenessValue = Integer.parseInt(lateness);				
				arrivingTransport.minutes.add(scheduledMinutes + latenessValue);
				arrivingTransport.routeTag.add(trainNo);
				arrivingTransport.stopTitle = stop;
				arrivingTransport.stopTag = stop;
				arrivingTransport.direction = destination;
				arrivingTransport.dirTag = destination;
				Transport transport = new Transport();
				
				transport.id = Integer.parseInt(trip.getString("Vehicle"));
				transport.lat = Double.parseDouble(trip.getString("Latitude"));
				transport.lng = Double.parseDouble(trip.getString("Longitude"));
				transport.heading = Integer.parseInt(trip.getString("Heading"));
				arrivingTransport.vehicles.add(transport);
			}
			
		}
		
	}
	
	//Takes the scheduled and the time stamp and returns the minutes left for arrival
	private int getMinutes(String scheduledEpochTime, String timeStampEpochTime) {		
		Date sched = new Date(Long.parseLong(scheduledEpochTime));
		Date timeS = new Date(Long.parseLong(timeStampEpochTime));
		Long diff = Math.abs(sched.getTime() - timeS.getTime());
		int intdiff =  (int) (diff/60); 		
		return intdiff;
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
