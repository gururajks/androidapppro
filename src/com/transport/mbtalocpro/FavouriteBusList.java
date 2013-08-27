package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.CommuterRailParser;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.FavoriteListItemObject;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.SubwayJsonParser;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
	ArrayAdapter<String> favoritesAdapter;
	ArrayList<FavoriteListItemObject> favoriteObjectList;
	ProgressDialog progressDialog = null;
	
	
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

		favoriteObjectList = new ArrayList<FavoriteListItemObject>();
		DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
		Cursor favRoutes = dbManager.getAllData();
		if(favRoutes != null && favRoutes.moveToFirst()) {			
			do {
				FavoriteListItemObject favoriteObjectListItem = new FavoriteListItemObject(); 
				favBusRoutes.add(favRoutes.getString(1) + "-" + favRoutes.getString(3) +"@" + favRoutes.getString(5));
				favoriteObjectListItem.routeTitle = favRoutes.getString(1);
				favoriteObjectListItem.routeTag = (favRoutes.getString(2));
				favoriteObjectListItem.directionTitle = (favRoutes.getString(3));
				favoriteObjectListItem.directionTag = (favRoutes.getString(4)); 
				favoriteObjectListItem.stopTag = (favRoutes.getString(6));
				favoriteObjectListItem.transportationType = favRoutes.getString(8);
				favoriteObjectList.add(favoriteObjectListItem);
			} while(favRoutes.moveToNext());			
		}
		 
		ListView listView = (ListView) findViewById(R.id.fav_bus_list);
		favoritesAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.fav_item, R.id.favBusItem, favBusRoutes);
		
		listView.setAdapter(favoritesAdapter); 
		
		//Touch event on the favorite pane
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {				
				FavoriteListItemObject favoriteListItemObject = favoriteObjectList.get(index);
				String choosenRouteTag = favoriteListItemObject.routeTag;
				String choosenRouteTitle = favoriteListItemObject.routeTitle;
				choosenDirection = favoriteListItemObject.directionTitle;
				String choosenDirectionTag = favoriteListItemObject.directionTag;				
				choosenStop = favoriteListItemObject.stopTag;
				progressDialog = ProgressDialog.show(FavouriteBusList.this, "Loading...", "Getting Data");
				//Bus Transportation predictions
				if(favoriteListItemObject.transportationType.equalsIgnoreCase("Bus")) { 
					URL url;
					try {			 
						url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=mbta&s="+choosenStop+"&r="+choosenRouteTag);
						new DownloadBusPredictions().execute(url);
					} catch (MalformedURLException e) {	
						e.printStackTrace();
					}
				}
				
				//Subway Transportation predictions 
				if(favoriteListItemObject.transportationType.equalsIgnoreCase("Subway")) {
					new DownloadSubwayPredictions().execute(choosenRouteTitle, choosenDirectionTag, choosenStop);
				}
				
				//Commuter Rail Predictions 
				if(favoriteListItemObject.transportationType.equalsIgnoreCase("Commuter Rail")) {
					System.out.println("TRANSPORTATION: Fav" + choosenRouteTitle + choosenStop + choosenDirectionTag);
					new DownloadCommuterRailPredictions().execute(choosenRouteTitle, choosenDirectionTag, choosenStop);
				}
			}
		});
		
		//Long click for context menu
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				registerForContextMenu(parent);
				return false;
			}
		});
 
		dbManager.closeDb();
	}
	
	/* Private classes for predictions - Bus*/	
	private class DownloadBusPredictions extends AsyncTask<URL, Integer, ArrayList<Object>> {    	
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
	
	
	/* Private classes for predictions - Subway
	 * choosenRouteTag, choosenDirection, choosenStop */	
	private class DownloadSubwayPredictions extends AsyncTask<String, Integer, ArrivingTransport> {

		@Override
		protected ArrivingTransport doInBackground(String... params) {
			
			
			SubwayJsonParser subwayParser = new SubwayJsonParser(params[0], params[2], params[1]);
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport();
			
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			progressDialog.dismiss();
			if(arrivingTransport != null) {
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
		}
		
		
		
	}
		
	/* Private classes for predictions - Commuter Rail*/
	private class DownloadCommuterRailPredictions extends AsyncTask<String, Integer, ArrivingTransport> {

		@Override
		protected ArrivingTransport doInBackground(String... params) {
			/*String choosenDestinationDirectionStop = null;
			if(choosenDirection != null) {
				int lastStopIndex = choosenDirection.stopList.size() - 1;
				choosenDestinationDirectionStop = choosenDirection.stopList.get(lastStopIndex).stopTitle;				
			}*/
			
			CommuterRailParser commRailParser = new CommuterRailParser(params[0], params[2], params[1]);
			commRailParser.parseCommuterRailInfo();
			return commRailParser.getArrivingTransport(); 			
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			progressDialog.dismiss();
			if(arrivingTransport != null) {
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
		}
		
		
		
	}
	
	
	
	
	/************************************* MENU OPTIONS ********************************/
	
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.fav_activity_contextual_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {	        
	        case R.id.deleteBookmark:
	            // delete the bookmark
	        	deleteBookmark(info.id);
	        	Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	
	//Deleting bookmark
	private void deleteBookmark(long id) {		
		int index = (int) id;
		if(favoritesAdapter != null) {
			//delete in the database as well 
			DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
			FavoriteListItemObject favoriteListItemObject = favoriteObjectList.get(index);
			dbManager.deleteDataByStop(favoriteListItemObject.routeTag, favoriteListItemObject.directionTag, favoriteListItemObject.stopTag);
			favoriteObjectList.remove(index);
			favoritesAdapter.remove(favBusRoutes.get(index));
			favoritesAdapter.notifyDataSetChanged();
			dbManager.closeDb();
		}
	}
	
	
	
	/*public void bookmarkbus(View view) {
		Intent intent = new Intent(this,MbtaBusList.class);
		startActivityForResult(intent, BUSLIST);
	}*/
	
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
