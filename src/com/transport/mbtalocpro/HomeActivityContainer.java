package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.CommuterRailParser;
import com.support.mbtalocpro.DatabaseQueryService;
import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.ParcelablePoint;

import com.support.mbtalocpro.SubwayJsonParser;
import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Path;
import com.support.mbtalocpro.Point;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;
import com.transport.mbtalocpro.PredictionTimeFragment.PredictedTimeFragmentItemSelectedListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivityContainer extends UrlConnector implements PredictedTimeFragmentItemSelectedListener, 
																LocationListener, OnMarkerClickListener {

	GoogleMap gMap = null;
	public final String agency="mbta";
	LinkedHashMap<String, String> routeDir;
	String routeTag;
	String stopTag;
	ArrivingTransport arrivingBus;
	ArrayList<Double> lngList;
	ArrayList<Double> latList;
	 
	private final String GPS_MENU_KEY = "gps_setting";
	private final String TRAFFIC_MENU_KEY = "map_traffic";
	private boolean gps_menu_setting;
	private boolean traffic_menu_setting;
	SharedPreferences sharedPref;
	RoutesPointReceiver routesReceiver;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_time);
        
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        routeDir = new LinkedHashMap<String, String>();
		latList = new ArrayList<Double>();
		lngList = new ArrayList<Double>();

        //Prediction time part
        Intent intent = getIntent();
        arrivingBus = (ArrivingTransport) intent.getSerializableExtra("arrivingBus");
        
        IntentFilter intentFilter = new IntentFilter(RoutesPointReceiver.POINT_RECEIVER_FLAG);        
        routesReceiver = new RoutesPointReceiver();
        registerReceiver(routesReceiver, intentFilter);
        
        populateFragments(arrivingBus);                
	}
    
    
    
    public void populateFragments(ArrivingTransport arrivingBus) { 
    	PredictionTimeFragment predictedTime = (PredictionTimeFragment) getSupportFragmentManager().findFragmentById(R.id.listFragment);
        predictedTime.setArrivingBusDetails(getApplicationContext(), arrivingBus);        
        stopTag = arrivingBus.stopTag;        
        if(arrivingBus.routeTag != null) {
        	if(!arrivingBus.routeTag.isEmpty()) {
        		routeTag = arrivingBus.routeTag.get(0);        		
        	}
        }
        
        
        //map part 
        if(gMap == null) {
	    	gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag)).getMap();
	    	if(gMap != null) {
	    		gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    		
	    		gps_menu_setting = sharedPref.getBoolean(GPS_MENU_KEY, true);
	    		traffic_menu_setting = sharedPref.getBoolean(TRAFFIC_MENU_KEY, false);
	    		gMap.setMyLocationEnabled(gps_menu_setting);
	    		gMap.setTrafficEnabled(traffic_menu_setting);
	    		//For setting the initial camera bounds for the map
	    		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.36,-71.1), 10));
	    		gMap.setOnMarkerClickListener(this);	 
	    		if(arrivingBus.transportType.equalsIgnoreCase("Bus")) {
	            	getFeeds();	//this is for the buses  
	            }
	            if(arrivingBus.transportType.equalsIgnoreCase("Subway")) {
	            	for(Transport train:arrivingBus.vehicles) {
	            		createGpsMarker(train);//this is for trains
	            	}	            		
	            	createStopMarker(arrivingBus);
	            	if(routeTag!=null) displayTrainRouteLines(routeTag);           			            		
	            }
	            if(arrivingBus.transportType.equalsIgnoreCase("Commuter Rail")) {
	            	for(Transport train:arrivingBus.vehicles) 
	            		createGpsMarker(train);//this is for trains
	            	if(routeTag!=null) displayTrainRouteLines(routeTag);   
	            }
	    	} 
	    } 
    } 
    
    public void displayTrainRouteLines(String routeTag) {
    	
    	Intent intent = new Intent(this, DatabaseQueryService.class);
    	intent.putExtra(DatabaseQueryService.INCOMING_INTENT,routeTag);
    	startService(intent);	
    }

	public void getFeeds() {
        
        //Getting the routes data from the feed
        if(routeTag != null) {
	    	//busDisplay.setText(routeTitle);	    
	    	getBusRouteData(routeTag);
	    	getBusLocationData(routeTag);	    	    	
	    }
    }
    
    

	public void getBusLocationData(String routeTag) {				
		URL url;
		try {
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a="+agency+"&r=" +routeTag+"&t=0");		
			new DownloadVehicleLocation().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}	
	}
	
	public void getBusRouteData(String routeTag) {
		 URL url;
		try {			
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&verbose&a="+agency+"&r="+routeTag);
			new DownloadRoutes().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}
	}
	
	private class DownloadVehicleLocation extends AsyncTask<URL, Integer, ArrayList<Object>> {
			
		@Override
		protected ArrayList<Object> doInBackground(URL... urls) {
			try {
            	return downloadUrl(urls[0], 1);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
			return null;
		}		
		
		protected void onPostExecute(ArrayList<Object> result) {
			if(result != null) {
				if(!result.isEmpty()) { 
					Object resultArray[] = result.toArray();
					for(int i = 0 ; i < result.size(); i++) {
						Transport busInfo = (Transport) resultArray[i];
						//Saving to menu
						if(busInfo.isPredictable == true) {
							latList.add(busInfo.lat);
							lngList.add(busInfo.lng);
							if(arrivingBus.dirTag.equalsIgnoreCase(busInfo.dirTag)) createGpsMarker(busInfo);
						}														
					}
					//Check if the lists are not empty , move the camera to accomodate all the buses visible
					if(!latList.isEmpty() && !lngList.isEmpty()) {
						Double maxLat = Collections.max(latList);
		    	    	Double maxLng = Collections.max(lngList);		    	    	
		    	    	LatLng maxCoord = new LatLng(maxLat, maxLng);
		    	    	Double minLat = Collections.min(latList);
		    	    	Double minLng = Collections.min(lngList);
		    	    	LatLng minCoord = new LatLng(minLat, minLng);
		    	    	LatLngBounds bounds = new LatLngBounds(minCoord, maxCoord);
		    	    	if(latList.size() > 1) gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
					}
					latList.clear();
					lngList.clear();
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
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
					//Gets the path tags and the points for the polyline that draws the bus route on the map
					Route routeInfo = (Route) result.get(0);
					PolylineOptions pOptions = new PolylineOptions();
					ArrayList<Path> paths = routeInfo.routePath;
					for(int i = 0; i < paths.size(); i++) {
						Path segment = paths.get(i);
						ArrayList<Point> routePointList = segment.routePoints;
						for(int j = 0 ; j < routePointList.size(); j++) {
							Point routePoint = routePointList.get(j);
							pOptions.add(new LatLng(routePoint.lat, routePoint.lng));
						}
						pOptions.zIndex(-1);
						Polyline busRoutePline = gMap.addPolyline(pOptions);
						busRoutePline.setWidth(3);						
						pOptions = new PolylineOptions();
					}
					
					//Getting the list of directions that each route has
					ArrayList<Direction> directionList = routeInfo.directionList;
					for(int i = 0; i < directionList.size(); i++) {
						Direction direction = directionList.get(i);
						routeDir.put(direction.directionTag, direction.directionTitle);
					}
					Stop stopDetails = routeInfo.stopList.get(stopTag);
					gMap.addMarker(new MarkerOptions().position(new LatLng(stopDetails.stopLocation.lat, stopDetails.stopLocation.lng)).title(stopDetails.stopTitle));
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	 
	}  
	
	
	//creates the gps marker
    //has an image that shows the direction of the bus using the heading
	private void createGpsMarker(Transport transportInfo) {	
		LatLng point = new LatLng(transportInfo.lat, transportInfo.lng);
		MarkerOptions mOptions = new MarkerOptions();
		mOptions.position(point);
		if(transportInfo.secSinceReport > 0) {
			mOptions.title("GPS Position Last Reported: " +transportInfo.secSinceReport +" seconds"); 
		}
		else {
			mOptions.title("GPS Position Last Reported: Not known");
		}
		if(transportInfo.dirTag != null)
			mOptions.snippet(transportInfo.dirTag);
		else 
			mOptions.snippet("Not Reporting");
		//rotating the bus according to the direction 
		Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.van_bus_icon);
		Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(bmResult);
		int rotationAngle = transportInfo.heading - 90;
		//fliping the bus in case
		if(transportInfo.heading > 180) {
			tempCanvas.scale(1.0f, -1.0f, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);
		}
		tempCanvas.rotate(rotationAngle, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);		
		tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);		
		mOptions.icon(BitmapDescriptorFactory.fromBitmap(bmResult));
		gMap.addMarker(mOptions);
	}

	//Creates a stop marker for trains stops 
    private void createStopMarker(ArrivingTransport trainStop) {    	
    	gMap.addMarker(new MarkerOptions().position(new LatLng(trainStop.stopLat, trainStop.stopLng)).title(trainStop.stopTitle));    		
	}
	
	
	//Refresh the gps feed to check for the new location of the bus
	public void refreshGpsFeed(View view) {
		gMap.clear();
		latList.clear();
		lngList.clear();
		getBusRouteData(routeTag);
		getBusLocationData(routeTag);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu_maptime, menu);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.bus_list_menu) {
			Intent intent = new Intent(this,MbtaBusList.class);
			startActivity(intent);
			finish();
		}		
		if(item.getItemId() == R.id.settings_menu) {
			Intent intent = new Intent(this,Settings.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}		
		if(item.getItemId() == R.id.fav_list_menu) {
			finish();
		}
		if(item.getItemId() == R.id.refresh_menu) {
			if(gMap != null) gMap.clear();
			if(arrivingBus.transportType.equalsIgnoreCase("Bus")) {
				downloadBusPredictions();
				getFeeds();		//feeds are exclusive for bus predictions only
			}  
			if(arrivingBus.transportType.equalsIgnoreCase("Subway")) {
				new SubwayPrediction().execute(arrivingBus.stopTag);
			}
			if(arrivingBus.transportType.equalsIgnoreCase("Commuter Rail")) {
				new CommuterRailPrediction().execute(arrivingBus.stopTag);
			}			
		}
		return super.onMenuItemSelected(featureId, item);		
	}
	
	
	public void downloadBusPredictions() {
		URL url; 
		try {			
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=mbta&s="+arrivingBus.stopTag+"&r="+routeTag);			
			new DownloadPredictions().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
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
					ArrivingTransport arrivingTransport = new ArrivingTransport();
					arrivingTransport.transportType = "Bus";
					for(int i = 0 ; i < result.size(); i++) {		//Iterate through multiple predictions tag
						RoutePrediction predictedRoute = (RoutePrediction) result.get(i);
						ArrayList<DirectionPrediction> predictedDirections = predictedRoute.dirForPredictions;
						for(int j = 0 ; j < predictedDirections.size(); j++) {	//Iterate through multiple direction tag
							DirectionPrediction predictedDirection = predictedDirections.get(j);
							if(predictedDirection.directionTitle.equalsIgnoreCase(arrivingBus.direction)) {	//checking for the choosen direction
								ArrayList<Prediction> predictions = predictedDirection.predictionList;
								for(int k = 0 ; k < predictions.size(); k++) {		//Iterate through multiple prediction tags
									Prediction busPrediction = predictions.get(k);
									arrivingTransport.minutes.add(busPrediction.minutes);
									arrivingTransport.routeTag.add(predictedRoute.routeTag);									
									arrivingTransport.vehicleIds.add(busPrediction.vehicleId);
								}
								arrivingTransport.direction = arrivingBus.direction;
								arrivingTransport.dirTag = arrivingBus.dirTag;
							}							
						}
						arrivingTransport.routeTitle = predictedRoute.routeTitle;
						arrivingTransport.stopTitle = predictedRoute.stopTitle;
						arrivingTransport.stopTag = predictedRoute.stopTag;
					}					
					populateFragments(arrivingTransport);					
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}	 
	}  
	
	/*
	 * Subway prediction with parsing of the subway information provided by the Subway 2.0
	 */
	class SubwayPrediction extends AsyncTask<String, Integer, ArrivingTransport> {
		
		protected ArrivingTransport doInBackground(String... params) {
			SubwayJsonParser subwayParser = new SubwayJsonParser(arrivingBus.routeTag.get(0) , params[0], arrivingBus.direction, arrivingBus.routeTitle);
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport(); 
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			if(arrivingTransport != null) {				
				arrivingTransport.stopLat = arrivingBus.stopLat;
				arrivingTransport.stopLng = arrivingBus.stopLng;
				populateFragments(arrivingTransport);				
			}
		}		 
	}
	
	/*
	 * Commuter Rail prediction with there JSON feed
	 */
	class CommuterRailPrediction extends AsyncTask<String, Integer, ArrivingTransport> {

		@Override
		protected ArrivingTransport doInBackground(String... params) {		 
			CommuterRailParser commRailParser = new CommuterRailParser(arrivingBus.routeTag.get(0), params[0], arrivingBus.direction, arrivingBus.routeTitle);
			commRailParser.parseCommuterRailInfo();
			return commRailParser.getArrivingTransport();
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			if(arrivingTransport != null) {				
				arrivingTransport.stopLat = arrivingBus.stopLat;
				arrivingTransport.stopLng = arrivingBus.stopLng;
				populateFragments(arrivingTransport);
			}
		}	
		
	}

	
	
	@Override
	public void onLocationChanged(Location location) {
		gMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
		gMap.animateCamera(CameraUpdateFactory.zoomTo(14));
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {		
		String direction = marker.getSnippet();		
		
		if(routeDir.get(direction) != null) {
			marker.setSnippet("Direction: "+ routeDir.get(direction));
		}
		return false;
	}
	
	
	private boolean checkIfNumber(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
    @Override
    protected void onPause() {
    	super.onPause();
    	if(routesReceiver != null) {
    		unregisterReceiver(routesReceiver);
    		routesReceiver = null;
    	}    	
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if(routesReceiver != null) {
    		unregisterReceiver(routesReceiver);
    		routesReceiver = null;
    	}
    }

	//Triggered when 
	public void onFragmentItemSelected() {
		gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag)).getMap();
		if(gMap != null) {
			gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41, -83), 10));
		}
	}

    /*
     * Broadcast receiver for maps data coming in
     */
      
	public class RoutesPointReceiver extends BroadcastReceiver {

		public final static String POINT_RECEIVER_FLAG = "ACTION_RECEIVED";
		@Override
		public void onReceive(Context context, Intent intent) {
			if(gMap != null) {				
				Bundle bundle = intent.getExtras();				
				ArrayList<ParcelablePoint> points = bundle.getParcelableArrayList("points");
				PolylineOptions pOptions = new PolylineOptions();
				for(int i = 0 ; i < points.size(); i++) {					
					ParcelablePoint point = points.get(i);
					pOptions.add(new LatLng(point.getLat(), point.getLng()));					
				}
				Polyline trainPolyline = gMap.addPolyline(pOptions);
				trainPolyline.setWidth(3);
			}
			
		}
		
	}
	
	
	
} 