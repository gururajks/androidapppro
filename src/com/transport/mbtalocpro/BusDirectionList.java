package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.Stop;
import com.transport.mbtalocpro.BusStopsDialog.BusStopsDialogListener;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BusDirectionList extends UrlConnector implements BusStopsDialogListener{
	
	public final String agency ="mbta";
	String routeTag;
	String routeTitle;
	String routeDir[][];
	Route route = null;
	Direction choosenDirection = null;
	ProgressDialog progressDialog;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_direction_list);
		
		routeDir = new String[10][2];	 
		//Gets the intent from the list of buses
		Intent intent = getIntent();
	    routeTag = intent.getStringExtra("routeTag");
	    routeTitle = intent.getStringExtra("routeTitle");
	    
	    getBusesDirections();

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
		routeDialog.show(getSupportFragmentManager(), "Bus");
	}
	
	private void getBusesDirections() {	
		URL url;
		try {			
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agency+"&r="+routeTag);
			new DownloadRoutes().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}

	}
	
	
	private class DownloadRoutes extends AsyncTask<URL, Integer, ArrayList<Object>> {    	
		@Override
		protected ArrayList<Object> doInBackground(URL... urls) {
			try {
            	return downloadUrl(urls[0], 3);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
			return null;
		}		
		
		protected void onPostExecute(ArrayList<Object> result) {
			if(result != null) {
				if(!result.isEmpty()) {		
					route = (Route) result.get(0);
					ArrayList<String> dirArrayList = new ArrayList<String>();
					if(!route.directionList.isEmpty()) {
						int dirCount = route.directionList.size();
						for(int i = 0; i < dirCount; i++) {
							dirArrayList.add(route.directionList.get(i).directionTitle);
						}
					}
					ListView listView = (ListView) findViewById(R.id.bus_dir_list);
					ArrayAdapter<String> busDirectionAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.direction_item, R.id.dirItem, dirArrayList);					
					listView.setAdapter(busDirectionAdapter);
					
					//Direction item in the list view - click handler
					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int index, long id) {							
							choosenDirection = route.directionList.get(index);
							ArrayList<Stop> stops = choosenDirection.stopList;
							LinkedHashMap<String, String> stopList = new LinkedHashMap<String, String>();
							for(int i = 0; i < stops.size(); i++) {
								String stopTag = stops.get(i).stopTag;
								Stop pairedStop = route.stopList.get(stopTag);								
								stopList.put(stopTag, pairedStop.stopTitle);							
							}
							getBusStopsData(route, choosenDirection, stopList);							
						}						
					});					
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	 
	}  
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	@Override
	public void onSelectStop(int item, String stopName) {
		if(route != null && choosenDirection != null) {
			String choosenStop = choosenDirection.stopList.get(item).stopTag;
			String choosenRoute = route.routeTag;
			progressDialog = ProgressDialog.show(this, "Loading...", "Getting Data");
			progressDialog.setCancelable(true);
			URL url;
			try {			
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=mbta&s="+choosenStop+"&r="+choosenRoute);
				new DownloadPredictions().execute(url);
			} catch (MalformedURLException e) {	
				e.printStackTrace();
			}
		}		
	}
	
	
	private class DownloadPredictions extends AsyncTask<URL, Integer, ArrayList<Object>> {    	
		@Override
		protected ArrayList<Object> doInBackground(URL... urls) {
			try {
            	return downloadUrl(urls[0], 4);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
			return null;
		}		
		
		
		protected void onPostExecute(ArrayList<Object> result) {
			if(result != null) {
				if(!result.isEmpty()) {
					ArrivingTransport arrivingBus = new ArrivingTransport();
					arrivingBus.transportType = "Bus";
					for(int i = 0 ; i < result.size(); i++) {		//Iterate through multiple predictions tag
						RoutePrediction predictedRoute = (RoutePrediction) result.get(i);
						ArrayList<DirectionPrediction> predictedDirections = predictedRoute.dirForPredictions;
						for(int j = 0 ; j < predictedDirections.size(); j++) {	//Iterate through multiple direction tag
							DirectionPrediction predictedDirection = predictedDirections.get(j);
							if(predictedDirection.directionTitle.equalsIgnoreCase(choosenDirection.directionTitle)) {	//checking for the choosen direction
								ArrayList<Prediction> predictions = predictedDirection.predictionList;
								for(int k = 0 ; k < predictions.size(); k++) {		//Iterate through multiple prediction tags
									Prediction busPrediction = predictions.get(k);
									arrivingBus.timeInSeconds.add(busPrediction.seconds);
									arrivingBus.routeTag.add(predictedRoute.routeTag);									
									arrivingBus.vehicleIds.add(busPrediction.vehicleId);
								}
								arrivingBus.direction = choosenDirection.directionTitle;
								arrivingBus.dirTag = choosenDirection.directionTag;
							}							
						}
						arrivingBus.routeTitle = predictedRoute.routeTitle;
						arrivingBus.stopTitle = predictedRoute.stopTitle;
						arrivingBus.stopTag = predictedRoute.stopTag;
					}					
					progressDialog.dismiss();
					Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
					intent.putExtra("arrivingBus", arrivingBus);
					startActivity(intent);
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	 
	}  
	
	

}
