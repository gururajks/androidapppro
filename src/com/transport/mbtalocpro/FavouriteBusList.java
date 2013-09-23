package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.CommuterRailParser;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.DatabaseQueryService;
import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.FavoriteListItemObject;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.Transport;

import com.support.mbtalocpro.SubwayJsonParser;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
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
import android.widget.TextView;
import android.widget.Toast;

public class FavouriteBusList extends UrlConnector {
	
	ArrayList<String> favBusRoutes;
	String choosenDirection;
	String choosenStop;
	int BUSLIST = 1;
	FavoriteListAdapter favoritesAdapter;
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
		
		installShapesData();
	}
	
	private void installShapesData() {
		Intent intent = new Intent(this, DatabaseQueryService.class);
    	startService(intent);			
	}

	//Displays the routes in the sql database
	public void displayFavoriteRoutes() {
		favBusRoutes = new ArrayList<String>();
		favoriteObjectList = new ArrayList<FavoriteListItemObject>();
		DatabaseManager dbManager = new DatabaseManager(this);
		Cursor favRoutes = dbManager.getAllData();
		if(favRoutes != null && favRoutes.moveToFirst()) {			
			do {
				FavoriteListItemObject favoriteObjectListItem = new FavoriteListItemObject(); 
				favBusRoutes.add(favRoutes.getString(1) + "-" + favRoutes.getString(3) +"@" + favRoutes.getString(5));
				favoriteObjectListItem.routeTitle = favRoutes.getString(1);
				favoriteObjectListItem.routeTag = (favRoutes.getString(2));
				favoriteObjectListItem.directionTitle = (favRoutes.getString(3));
				favoriteObjectListItem.directionTag = (favRoutes.getString(4)); 
				favoriteObjectListItem.stopTitle = (favRoutes.getString(5));
				favoriteObjectListItem.stopTag = (favRoutes.getString(6));
				favoriteObjectListItem.transportationType = favRoutes.getString(8);
				favoriteObjectListItem.imagePath = favRoutes.getString(9);
				favoriteObjectList.add(favoriteObjectListItem);
			} while(favRoutes.moveToNext());			
		}
		  
		ListView listView = (ListView) findViewById(R.id.fav_bus_list);
		TextView emptyView = (TextView) findViewById(R.id.empty);
		favoritesAdapter = new FavoriteListAdapter(this, favoriteObjectList);
				
		listView.setAdapter(favoritesAdapter); 
		listView.setEmptyView(emptyView);
		
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
				progressDialog.setCancelable(true);
				progressDialog.setCanceledOnTouchOutside(false);				
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
					new DownloadSubwayPredictions().execute(choosenRouteTitle, choosenDirectionTag, choosenStop, choosenRouteTag);
				}
				
				//Commuter Rail Predictions 
				if(favoriteListItemObject.transportationType.equalsIgnoreCase("Commuter Rail")) {
					System.out.println("TRANSPORTATION: Fav" + choosenRouteTitle + choosenStop + choosenDirectionTag);
					new DownloadCommuterRailPredictions().execute(choosenRouteTitle, choosenDirectionTag, choosenStop, choosenRouteTag);
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
									Transport transport = new Transport();
									transport.timeOfArrival = (busPrediction.seconds);																	
									transport.vehicleId = (busPrediction.vehicleId); 
									arrivingBus.vehicles.add(transport);
									arrivingBus.dirTag = busPrediction.directionTag;
								}
								arrivingBus.direction = choosenDirection;
							} 
						} 
						arrivingBus.routeTag = (predictedRoute.routeTag);	
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
			
			SubwayJsonParser subwayParser = new SubwayJsonParser(params[3], params[2], params[1], params[0]);			
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport();
			
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {			
			if(arrivingTransport != null) {
				progressDialog.dismiss();
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
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
			
			CommuterRailParser commRailParser = new CommuterRailParser(params[3], params[2], params[1], params[0]);
			commRailParser.parseCommuterRailInfo();
			return commRailParser.getArrivingTransport(); 			
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {			
			if(arrivingTransport != null) {
				progressDialog.dismiss();
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
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
			favoritesAdapter.notifyDataSetChanged();
			dbManager.closeDb();
		}
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
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, BUSLIST);
		}		
		if(item.getItemId() == R.id.comm_rail_list_menu) {
			Intent intent = new Intent(this,CommRailList.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("transportationType", "Commuter Rail");
			startActivityForResult(intent, BUSLIST);  
		}		
		if(item.getItemId() == R.id.subway_list_menu) {
			Intent intent = new Intent(this,CommRailList.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
		return super.onMenuItemSelected(featureId, item);		
	}
	
		
	/* On activity result from image button */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == FavoriteListAdapter.IMAGE_PICK_CODE && data != null && data.getData() != null && resultCode == FragmentActivity.RESULT_OK) {
			Uri _uri = data.getData();
			
	        //User had pick an image.
	        Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
	        cursor.moveToFirst();

	        //Link to the image
	        String imageFilePath = cursor.getString(0);
	        int rowPosition = favoritesAdapter.getClickedIndex();
	        			
			//Notify the adapter to update the image
	        //favoriteObjectList.get(rowPosition).imagePath = imageFilePath;
	        
	        DatabaseManager dbManager = new DatabaseManager(this);
	        FavoriteListItemObject favoriteListItemObject = favoriteObjectList.get(rowPosition);
	        String dirTag = favoriteListItemObject.directionTag;
	        String stopTag = favoriteListItemObject.stopTag;
	        String routeTag = favoriteListItemObject.routeTag;
	        dbManager.updateImageFilePath(imageFilePath, routeTag, dirTag, stopTag);        
	        dbManager.closeDb();
	        favoritesAdapter.notifyDataSetChanged();
	          
	        
	        cursor.close();  
		}
	}		
	
}
