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
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.RealTimeMbtaDirectionsListParser;
import com.support.mbtalocpro.RealTimeMbtaRoutesListParser;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;
import com.support.mbtalocpro.SubwayJsonParser;
import com.support.mbtalocpro.TransportModes;
import com.transport.mbtalocpro.BusStopsDialog.BusStopsDialogListener;
import com.transport.mbtalocpro.SubwayDirectionList.SubwayPrediction;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
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

public class CommRailDirectionList extends FragmentActivity implements BusStopsDialogListener{
	
	Direction choosenDirection = null;
	String commRailTitle = null;
	String commRailTag = null;
	int BUSLIST = 1;
	String destinationDirectionStop;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_direction_list);
		
		Intent intent = getIntent();
		commRailTag = intent.getStringExtra("commrailid");
		commRailTitle = intent.getStringExtra("commrailtitle"); 
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
						directionList.add(destinationDirection.directionTitle + "-" + destinationDirectionStop);						
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
		routeDialog.show(getSupportFragmentManager(), "railStops");
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

	@Override
	public void onSelectStop(int index, String stopName) {
		String stopId = choosenDirection.stopList.get(index).stopId;
		new SubwayPrediction().execute(stopId);		
	}
	
	class SubwayPrediction extends AsyncTask<String, Integer, ArrivingTransport> {
		
		protected ArrivingTransport doInBackground(String... params) {
			String choosenDestinationDirectionStopStripped = null;
			if(choosenDirection != null) {
				int lastStopIndex = choosenDirection.stopList.size() - 1;
				String choosenDestinationDirectionStop = choosenDirection.stopList.get(lastStopIndex).stopTitle;
				String tempStringArray[] = choosenDestinationDirectionStop.split(" ");
				choosenDestinationDirectionStopStripped = tempStringArray[0];
			}
			
			SubwayJsonParser subwayParser = new SubwayJsonParser(commRailTitle, params[0], choosenDestinationDirectionStopStripped);
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport();
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			if(arrivingTransport != null) {
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
		}
		
	}
	
	
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
