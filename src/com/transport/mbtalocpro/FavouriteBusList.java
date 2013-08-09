package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.RoutePrediction;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FavouriteBusList extends UrlConnector {
	
	ArrayList<String> favBusRoutes;
	String choosenDirection;
	String choosenStop;
	int BUSLIST = 1;
	ArrayList<String> favBusRouteTags;
	ArrayList<String> favBusDirectionTitle;
	ArrayList<String> favBusStopTags;
	String mbtaTypes[] = new String[] {"Bus", "Subway", "Commuter Rail"}; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fav_bus_list);
		/*if(android.os.Build.VERSION.SDK_INT >= 11) {
			ArrayAdapter<String> transportationAdapter =new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, mbtaTypes);
			ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			actionBar.setListNavigationCallbacks(transportationAdapter, new OnNavigationListener() {
				
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					Toast.makeText(getBaseContext(), itemPosition, Toast.LENGTH_SHORT).show();
					return false;
				}
			});
		}*/
		displayFavoriteRoutes();		
	}
	
	//Displays the routes in the sql database
	public void displayFavoriteRoutes() {
		favBusRoutes = new ArrayList<String>();
		favBusRouteTags = new ArrayList<String>();
		favBusDirectionTitle = new ArrayList<String>();
		favBusStopTags = new ArrayList<String>();
		DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
		Cursor favRoutes = dbManager.getAllData();
		if(favRoutes != null && favRoutes.moveToFirst()) {			
			do {
				favBusRoutes.add("Route:"+favRoutes.getString(1) + "-" + favRoutes.getString(3) +"@" + favRoutes.getString(5));
				favBusRouteTags.add(favRoutes.getString(2));
				favBusDirectionTitle.add(favRoutes.getString(3));
				favBusStopTags.add(favRoutes.getString(6));
			}while(favRoutes.moveToNext());			
		}
		
		ListView listView = (ListView) findViewById(R.id.fav_bus_list);
		ArrayAdapter<String> favoritesAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.fav_item, R.id.favBusItem, favBusRoutes);
		
		listView.setAdapter(favoritesAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {				
				String busTag = favBusRouteTags.get(index);
				choosenDirection = favBusDirectionTitle.get(index);
				choosenStop = favBusStopTags.get(index);
				URL url;
				try {			
					//url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=mbta&r=86");
					url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=mbta&s="+choosenStop+"&r="+busTag);
					new DownloadPredictions().execute(url);
				} catch (MalformedURLException e) {	
					e.printStackTrace();
				}
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			//Delete the bookmark
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				return false;
			}
		});

		dbManager.closeDb();
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
					for(int i = 0 ; i < result.size(); i++) {		//Iterate through multiple predictions tag
						RoutePrediction predictedRoute = (RoutePrediction) result.get(i);
						ArrayList<DirectionPrediction> predictedDirections = predictedRoute.dirForPredictions;
						for(int j = 0 ; j < predictedDirections.size(); j++) {	//Iterate through multiple direction tag
							DirectionPrediction predictedDirection = predictedDirections.get(j);
							if(predictedDirection.directionTitle.equalsIgnoreCase(choosenDirection)) {	//checking for the choosen direction
								ArrayList<Prediction> predictions = predictedDirection.predictionList;
								for(int k = 0 ; k < predictions.size(); k++) {		//Iterate through multiple prediction tags
									Prediction busPrediction = predictions.get(k);
									arrivingBus.minutes.add(busPrediction.minutes);
									arrivingBus.routeTag.add(predictedRoute.routeTag);									
									arrivingBus.vehicleIds.add(busPrediction.vehicleId);
									arrivingBus.dirTag = busPrediction.directionTag;
								}
								arrivingBus.direction = choosenDirection;
							}
						} 
						arrivingBus.routeTitle = predictedRoute.routeTitle;
						arrivingBus.stopTitle = predictedRoute.stopTitle;
						arrivingBus.stopTag = predictedRoute.stopTag;
					}			
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
	
	
	
	
	
	
	public void bookmarkbus(View view) {
		Intent intent = new Intent(this,MbtaBusList.class);
		startActivityForResult(intent, BUSLIST);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	@Override
	public void onResume() {		
		displayFavoriteRoutes();
		super.onResume();
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
