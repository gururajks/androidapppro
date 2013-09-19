package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.support.mbtalocpro.AppConstants;
import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.CommuterRailParser;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.RealTimeMbtaDirectionsListParser;
import com.support.mbtalocpro.RealTimeMbtaRoutesListParser;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;
import com.support.mbtalocpro.SubwayJsonParser;
import com.support.mbtalocpro.TransportModes;
import com.transport.mbtalocpro.BusStopsDialog.BusStopsDialogListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CommRailDirectionList extends FragmentActivity implements BusStopsDialogListener{
	
	Direction choosenDirection = null;
	String commRailTitle = null;
	String commRailTag = null;
	String transportationType = null;
	int BUSLIST = 1;
	String destinationDirectionStop;
	double stopLat;
	double stopLng;
	ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_direction_list);
		
		Intent intent = getIntent();
		commRailTag = intent.getStringExtra("commrailid");
		commRailTitle = intent.getStringExtra("commrailtitle"); 
		transportationType = intent.getStringExtra("transportationType"); 
		getCommuterRailDirection(commRailTag);	
		
	}
	
	private void getCommuterRailDirection(String commRailTag) {
		URL url;
		try {
			url = new URL("http://realtime.mbta.com/developer/api/v1/stopsbyroute?api_key=" + AppConstants.API_KEY +"&route=" + commRailTag);
			new DownloadCommuterRailDirection().execute(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}	
	}

	class DownloadCommuterRailDirection extends AsyncTask<URL, Integer, Route> {
		
		Route route = null;
		@Override
		protected Route doInBackground(URL... urls) {
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		protected void onPostExecute(Route result) {
			if(result != null) {
				route = result;
				ArrayList<String> directionList = new ArrayList<String>();
				if(!result.directionList.isEmpty()) {
					route.routeTitle = commRailTitle;
					route.routeTag = commRailTag;
					for(int i = 0 ; i < result.directionList.size(); i++) {			
						Direction destinationDirection = result.directionList.get(i);		
						//Direction + last stop name (which becomes the destination)
						int lastStopIndex = destinationDirection.stopList.size() - 1;
						destinationDirectionStop = destinationDirection.stopList.get(lastStopIndex).stopTitle;
						directionList.add(destinationDirection.directionTitle + ": " + destinationDirectionStop);
						if(transportationType.equalsIgnoreCase("Subway")) {				//Station suffix is not needed as it cannot compared
							String[] directionStripped = destinationDirectionStop.split(" ");						
							result.directionList.get(i).directionTag = directionStripped[0];
						}
						else if(transportationType.equalsIgnoreCase("Commuter Rail")) {	//station suffix is needed											
							result.directionList.get(i).directionTag = destinationDirectionStop;
						}					
					} 
				}
				ListView listView = (ListView) findViewById(R.id.bus_dir_list);
				ArrayAdapter<String> commDirectionAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.direction_item, R.id.dirItem, directionList);					
				listView.setAdapter(commDirectionAdapter);	
				 
				listView.setOnItemClickListener(new OnItemClickListener() { 
					public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
						choosenDirection = route.directionList.get(index);
						
						ArrayList<Stop> stops = choosenDirection.stopList;
						LinkedHashMap<String, String> stopList = new LinkedHashMap<String, String>();
						for(int i = 0; i < stops.size(); i++) {
							String stopId = stops.get(i).stopId;							
							String pairedStop = stops.get(i).stopTitle;		
							stopList.put(stopId, pairedStop);							
						}
						getBusStopsData(route, choosenDirection, stopList);			
					} 
				});
			}
		}
	}
	
	public void getBusStopsData(Route route, Direction choosenDirection, LinkedHashMap<String, String> stopNames) {
		DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
		int savedCbState[] = null; 
		Cursor checkBoxCursor = dbManager.getData(route.routeTag, choosenDirection.directionTag);
		if(checkBoxCursor != null && checkBoxCursor.moveToFirst()) {
			savedCbState = new int[checkBoxCursor.getCount()];
			int counter = 0;
			do {
				savedCbState[counter] = checkBoxCursor.getInt(7);
				counter++;
			}while(checkBoxCursor.moveToNext());			
		}		
		checkBoxCursor.close();
		dbManager.closeDb();
		DialogFragment routeDialog = new BusStopsDialog().newInstance(route, choosenDirection, stopNames, savedCbState);
		routeDialog.show(getSupportFragmentManager(), transportationType);
	}
			
	
	public Route downloadUrl(URL url) throws IOException {
    	InputStream is = null;
    	HttpURLConnection conn = null;
    	try {    		
    		conn = (HttpURLConnection) url.openConnection(); 
    		conn.setReadTimeout(10000);
    		conn.setConnectTimeout(10000);
    		conn.setRequestMethod("GET");
    		conn.setRequestProperty("Content-Type", "application/xml");
    		conn.setDoInput(true);
    		
    		//Starting the query
    		conn.connect(); 
    		int responseCode = conn.getResponseCode();
    		System.out.println("The response is: " + responseCode);    		
    		is = conn.getInputStream();
    		        		
			RealTimeMbtaDirectionsListParser realtimeDirectionsList = new RealTimeMbtaDirectionsListParser();
			Route routeInfo = realtimeDirectionsList.getDirectionsList(is);
    		return routeInfo;   		
    	}
    	catch(IOException e) {
    		int responseCode = conn.getResponseCode();
    		System.out.println("Error response is: " + responseCode);    		
    	}    	
		return null;    	
    }

	/*
	 * On Selecting the direction list 
	 */
	@Override
	public void onSelectStop(int index, String stopName) {
		String stopId = choosenDirection.stopList.get(index).stopId;
		stopLat = choosenDirection.stopList.get(index).stopLocation.lat;
		stopLng = choosenDirection.stopList.get(index).stopLocation.lng;
		progressDialog = ProgressDialog.show(this, "Loading...", "Getting Data");
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);		
		if (transportationType.equalsIgnoreCase("Subway")) {			
			new SubwayPrediction().execute(stopId);
		}
		else {
			new CommuterRailPrediction().execute(stopId);
		}
	} 
	
	/*
	 * Subway prediction with parsing of the subway information provided by the Subway 2.0
	 */
	class SubwayPrediction extends AsyncTask<String, Integer, ArrivingTransport> {
		
		protected ArrivingTransport doInBackground(String... params) {
			String choosenDestinationDirectionStopStripped = null;
			if(choosenDirection != null) {
				int lastStopIndex = choosenDirection.stopList.size() - 1;
				String choosenDestinationDirectionStop = choosenDirection.stopList.get(lastStopIndex).stopTitle;
				String tempStringArray[] = choosenDestinationDirectionStop.split(" ");
				choosenDestinationDirectionStopStripped = tempStringArray[0];
			}
			
			SubwayJsonParser subwayParser = new SubwayJsonParser(commRailTag, params[0], choosenDestinationDirectionStopStripped, commRailTitle);
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport();
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {			
			if(arrivingTransport != null) {
				progressDialog.dismiss();
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				arrivingTransport.stopLat = stopLat;
				arrivingTransport.stopLng = stopLng;
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}		 
	}
	
	/*
	 * Commuter Rail prediction with there JSON feed
	 */
	class CommuterRailPrediction extends AsyncTask<String, Integer, ArrivingTransport> {

		@Override
		protected ArrivingTransport doInBackground(String... params) {
			String choosenDestinationDirectionStop = null;
			if(choosenDirection != null) {
				int lastStopIndex = choosenDirection.stopList.size() - 1;
				choosenDestinationDirectionStop = choosenDirection.stopList.get(lastStopIndex).stopTitle;				
			}
			
			CommuterRailParser commRailParser = new CommuterRailParser(commRailTag, params[0], choosenDestinationDirectionStop, commRailTitle);
			commRailParser.parseCommuterRailInfo();
			return commRailParser.getArrivingTransport();
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			
			if(arrivingTransport != null) {
				progressDialog.dismiss();
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				arrivingTransport.stopLat = stopLat;
				arrivingTransport.stopLng = stopLng;
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	
		
	}
	
	
	
	
	
	
	
	/******************* Menu options **************************************/
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.bus_list_menu) {
			Intent intent = new Intent(this,MbtaBusList.class);
			startActivityForResult(intent, BUSLIST);
		}		
		if(item.getItemId() == R.id.comm_rail_list_menu) {
			Intent intent = new Intent(this,CommRailList.class);
			intent.putExtra("transportationType", "Commuter Rail");
			startActivityForResult(intent, BUSLIST);
		}		
		if(item.getItemId() == R.id.subway_list_menu) {
			Intent intent = new Intent(this,CommRailList.class);
			intent.putExtra("transportationType", "Subway");
			startActivityForResult(intent, BUSLIST);
		}		
		if(item.getItemId() == R.id.settings_menu) {
			Intent intent = new Intent(this,Settings.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}			
		if(item.getItemId() == R.id.map_menu) {
			Intent intent = new Intent(this,RouteStopMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}
		return super.onMenuItemSelected(featureId, item);		
	}
}
