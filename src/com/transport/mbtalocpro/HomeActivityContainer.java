package com.transport.mbtalocpro;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Path;
import com.support.mbtalocpro.Point;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;
import com.transport.mbtalocpro.PredictionTimeFragment.PredictedTimeFragmentItemSelectedListener;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_time);
        
        routeDir = new LinkedHashMap<String, String>();
		latList = new ArrayList<Double>();
		lngList = new ArrayList<Double>();

        //Prediction time part
        Intent intent = getIntent();
        arrivingBus = (ArrivingTransport) intent.getSerializableExtra("arrivingBus");
        

		
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
	    		gMap.setMyLocationEnabled(true);
	    		//For setting the initial camera bounds for the map
	    		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.36,-71.1), 10));
	    		gMap.setOnMarkerClickListener(this);	 
	    		if(arrivingBus.transportType.equalsIgnoreCase("Bus")) {
	            	getFeeds();	//this is for the buses  
	            }
	            if(arrivingBus.transportType.equalsIgnoreCase("Subway")) {
	            	/*for(Transport train:arrivingBus.vehicles) 
	            		createGpsMarker(train);//this is for trains*/
	            }
	            if(arrivingBus.transportType.equalsIgnoreCase("Commuter Rail")) {
	            	/*for(Transport train:arrivingBus.vehicles) 
	            		createGpsMarker(train);//this is for trains*/
	            }
	    	}
	    }
        
        

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
	private void createGpsMarker(Transport busInfo) {	
		LatLng point = new LatLng(busInfo.lat, busInfo.lng);
		MarkerOptions mOptions = new MarkerOptions();
		mOptions.position(point);
		mOptions.title("Last Reported: " +busInfo.secSinceReport +" seconds");
		
		if(busInfo.dirTag != null)
			mOptions.snippet(busInfo.dirTag);
		else 
			mOptions.snippet("Not Reporting");
		//rotating the bus according to the direction 
		Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.van_bus_icon);
		Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(bmResult);
		int rotationAngle = busInfo.heading - 90;
		//fliping the bus in case
		if(busInfo.heading > 180) {
			tempCanvas.scale(1.0f, -1.0f, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);
		}
		tempCanvas.rotate(rotationAngle, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);		
		tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);		
		mOptions.icon(BitmapDescriptorFactory.fromBitmap(bmResult));
		gMap.addMarker(mOptions);
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
			getFeeds();
		}
		return super.onMenuItemSelected(featureId, item);		
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
	
    

	//Triggered when 
	public void onFragmentItemSelected() {
		gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag)).getMap();
		if(gMap != null) {
			gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41, -83), 10));
		}
	}

    
      
	
	
	
} 