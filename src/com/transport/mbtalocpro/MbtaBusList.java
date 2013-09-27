package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.DatabaseManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class MbtaBusList extends UrlConnector {
	
	public final String agency ="mbta";
	ListView listView;
	ArrayList<String> busRouteTagList = new ArrayList<String>();
	ArrayList<String> busRouteTitleList = new ArrayList<String>();
	int savedCbState[] = null;
	
	//Suppressing it as the action bar is only used if the phone OS is over Honeycomb
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_mbta_bus_list);
		setProgressBarIndeterminateVisibility(true);
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);			
		}
		
		listView = (ListView) findViewById(R.id.buslist);		
		URL url;
		try {			
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agency);
			new DownloadWebpageText().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}
		
	}
	

	private class DownloadWebpageText extends AsyncTask<URL, Integer, ArrayList<Transport>> {
	
		@Override
		protected ArrayList<Transport> doInBackground(URL... urls) {
			try {
            	return downloadUrl(urls[0]);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
		
			return null;
		}
		
		
		protected void onPostExecute(ArrayList<Transport> result) {
			if(result != null) {
				if(!result.isEmpty()) {
					Object resultArray[] = result.toArray();
					for(int i = 0 ; i < result.size(); i++ ) {
						Transport busInfo = (Transport) resultArray[i];
						busRouteTagList.add(busInfo.routeTag);		
						busRouteTitleList.add(busInfo.routeTitle);		
					}
							
					//BusListAdapter busAdapter = new BusListAdapter(busRouteTitleList, busRouteTagList, getApplicationContext(), savedCbState);
					ArrayAdapter<String> busAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.bus_item, R.id.busItem, busRouteTitleList);
					listView.setAdapter(busAdapter);		 
					
					
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							String busTag = busRouteTagList.get(position);
							String busName = busRouteTitleList.get(position);
							Intent intent = new Intent(getApplicationContext(), BusDirectionList.class);							
							intent.putExtra("routeTag", busTag);
							intent.putExtra("routeTitle", busName);
							startActivity(intent);
						}
					});	
					setProgressBarIndeterminateVisibility(false);
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	 
	}  
	
	//Connects to the web url and gets the result
	private ArrayList<Transport> downloadUrl(URL url) throws IOException {
    	InputStream is = null;
    	HttpURLConnection conn = null;
    	try {   		
    		
    		conn = (HttpURLConnection) url.openConnection(); 
    		conn.setReadTimeout(10000);
    		conn.setConnectTimeout(10000);
    		conn.setRequestMethod("GET");
    		conn.setDoInput(true);
    		
    		//Starting the query
    		conn.connect();
    		int responseCode = conn.getResponseCode();
    		System.out.println("The response is: " + responseCode);    		
    		is = conn.getInputStream();
    		
    		MbtaGpsFeedParser gpsFeed = new MbtaGpsFeedParser(is);
    		ArrayList<Transport> busInfo = gpsFeed.getBusInfo();
    		
    		return busInfo;
    		
    	}
    	catch(IOException e) {
    		int responseCode = conn.getResponseCode();
    		System.out.println("The response is: " + responseCode);    		
    	}    	
		return null;    	
    }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {			
		if(item.getItemId() == R.id.bus_list_menu) {
			Intent intent = new Intent(this,MbtaBusList.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.putExtra("transportationType", "Bus");
			startActivity(intent); 
		}		 
		if(item.getItemId() == R.id.comm_rail_list_menu) {
			Intent intent = new Intent(this,CommRailList.class); 
			intent.putExtra("transportationType", "Commuter Rail");
			startActivity(intent);
		}		
		if(item.getItemId() == R.id.subway_list_menu) {
			Intent intent = new Intent(this,CommRailList.class);
			intent.putExtra("transportationType", "Subway");
			startActivity(intent);
		}		
		if(item.getItemId() == R.id.settings_menu) {
			Intent intent = new Intent(this,Settings.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}			
		if(item.getItemId() == R.id.fav_list_menu) {
			Intent intent = new Intent(this,FavouriteBusList.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		if(item.getItemId() == R.id.map_menu) {
			Intent intent = new Intent(this,RouteStopMap.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
		if(item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);		
	}

}
