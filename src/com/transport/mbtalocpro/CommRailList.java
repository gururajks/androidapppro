package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.AppConstants;
import com.support.mbtalocpro.RealTimeMbtaRoutesListParser;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.TransportMode;
import com.support.mbtalocpro.TransportModes;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CommRailList extends FragmentActivity {
	
	ArrayList<String> railList;
	String transportationType;
	int BUSLIST = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mbta_bus_list);
		Intent intent = getIntent();
		transportationType = intent.getStringExtra("transportationType");
		railList = new ArrayList<String>();
		URL url;
		try {			
			url = new URL("http://realtime.mbta.com/developer/api/v1/routes?api_key=" + AppConstants.API_KEY);
			new RealTimeMbtaDownloadRoutesList().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}	
		
		
	}
	
	class RealTimeMbtaDownloadRoutesList extends AsyncTask<URL, Integer, TransportModes> {
		TransportMode commuterRailMode = null;
		@Override
		protected TransportModes doInBackground(URL... urls) {				
			try {
				return downloadUrl(urls[0], 5);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(TransportModes result) {
			if(result != null) {						 
				for(TransportMode mode:result.modes) {
					//mention which form of transportation that should be called
					if(mode.mode_name.equalsIgnoreCase(transportationType)) {
						commuterRailMode = mode; 
					}
				}
				if(commuterRailMode != null) {
					for(int i = 0 ; i < commuterRailMode.routes.routesList.size(); i++) {				
						railList.add(commuterRailMode.routes.routesList.get(i).routeTitle);
					} 
					ListView listView =(ListView) findViewById(R.id.buslist);
					ArrayAdapter<String> railAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.bus_item, R.id.busItem, railList);
					listView.setAdapter(railAdapter);		
					listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view, int index, long id) { 
							Intent intent = new Intent(getApplicationContext(), CommRailDirectionList.class);
							intent.putExtra("commrailid", commuterRailMode.routes.routesList.get(index).routeTag);
							intent.putExtra("transportationType", transportationType);
							intent.putExtra("commrailtitle", commuterRailMode.routes.routesList.get(index).routeTitle);
							startActivity(intent);
						}
					});
				}
			}
		}		
	}	
	

	public TransportModes downloadUrl(URL url, int flag) throws IOException {
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
    		        		
			RealTimeMbtaRoutesListParser realtimeRoutesList = new RealTimeMbtaRoutesListParser();
			TransportModes modes = realtimeRoutesList.getRoutesList(is);    			
    		return modes;   		
    	}
    	catch(IOException e) {
    		int responseCode = conn.getResponseCode();
    		System.out.println("Error response is: " + responseCode);    		
    	}    	
		return null;    	
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
