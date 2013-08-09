package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Path;
import com.support.mbtalocpro.Point;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;
import com.transport.mbtalocpro.BusRouteDialog.BusRouteDialogListener;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RouteStopMap extends UrlConnector implements BusRouteDialogListener {

	public final String agency ="mbta";
	GoogleMap gMap;
	ArrayList<String> busRoutesTitle;
	ArrayList<String> busRoutesTag;
	Button busRouteDisplay;
	ProgressDialog progressDialog;
	
	CharSequence[] routeList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_stop_map);
		setupMap();
		busRouteDisplay = (Button) findViewById(R.id.busNo);
		busRoutesTitle = new ArrayList<String>();
		busRoutesTag = new ArrayList<String>();
	    getBusesList();
	}
	
	//Setting the map
	void setupMap() {
		gMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.routeMap)).getMap();
		if(gMap != null) {
			gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.34332,-71.166687), 10));			
			gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    		gMap.setMyLocationEnabled(true);	    
		} 
	}
	
	 
	
	//get the bus routes into the bus route display
	//Get the list of buses in a given agency
	public void getBusesList() {
		URL url;
		try {
			url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agency);
			new DownloadBusesList().execute(url);
		} catch (MalformedURLException e) {	
			e.printStackTrace();
		}
	} 
	
	public void getBusRouteData(View view) {
		DialogFragment routeDialog = new BusRouteDialog().newInstance(routeList);
		routeDialog.show(getSupportFragmentManager(), "busRoutes");
	}
	
		
	@Override
	public void onSelectRoute(CharSequence item) {
		URL url;
		gMap.clear();		//clearing the map
		busRouteDisplay.setText("Bus Route No: "+item);
		if(!busRoutesTitle.isEmpty()) {
			int index = busRoutesTitle.indexOf((String)item);
			SharedPreferences menuPref = getSharedPreferences("MenuSettings", MODE_PRIVATE);
			SharedPreferences.Editor busNo = menuPref.edit();
			busNo.putString("busNo", (String)item);
			busNo.commit();
			progressDialog = ProgressDialog.show(this, "Loading...", "Getting Data");
			try {			
				url = new URL("http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agency+"&r="+busRoutesTag.get(index));
				new DownloadRoutes().execute(url);
			} catch (MalformedURLException e) {	
				e.printStackTrace();
			} catch(NullPointerException e) {
				e.printStackTrace();
			}		
		}
	}
	
	/*
	 * Downloads the list of buses, titles and tags in xml
	 */
	private class DownloadBusesList extends AsyncTask<URL, Integer, ArrayList<Object>> {
	
		protected ArrayList<Object> doInBackground(URL... urls) {
			try {
            	return downloadUrl(urls[0], 1);
            } catch (IOException e) {
                System.out.println("Unable to retrieve web page. URL may be invalid.");
            }
			return null;
		}
		
		
		protected void onPostExecute(ArrayList<Object> result) {
			if(result != null) {	//check for .isEmpty() condition				
				Object resultArray[] = result.toArray();
				for(int i = 0 ; i < result.size(); i++ ) {
					Transport busInfo = (Transport) resultArray[i];				
					busRoutesTitle.add(busInfo.routeTitle);
					busRoutesTag.add(busInfo.routeTag);
					
				}			
				Object[] busRouteArray = busRoutesTitle.toArray();	
				routeList = new CharSequence[busRoutesTitle.size()];
				for(int i = 0; i < busRouteArray.length; i++) {
					routeList[i] = (CharSequence)busRouteArray[i];
				}
			    SharedPreferences menuPref = getSharedPreferences("MenuSettings", MODE_PRIVATE);
			    String savedBusNo = menuPref.getString("busNo", "1");
				busRouteDisplay.setText("Bus Route No: "+savedBusNo);
				onSelectRoute(savedBusNo);		//display the route as a default
				busRouteDisplay.setClickable(true);
			}
			else {
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
			
		}	 
	}  
	
	/*
	 * Downloads the bus routes with polylines and the stops along the routes with direction
	 */
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
					Collection<Stop> stopCollection = routeInfo.stopList.values();
					Iterator<Stop> stopIterator = stopCollection.iterator();
					while(stopIterator.hasNext()) {
						Stop stop = (Stop) stopIterator.next();
						gMap.addMarker(new MarkerOptions().position(new LatLng(stop.stopLocation.lat, stop.stopLocation.lng)).title(stop.stopTitle));
					}
					progressDialog.dismiss();					
				}
			}
			else {
				progressDialog.dismiss();		//dismiss the dialog if the internet connection disrupts
				Toast.makeText(getApplicationContext(), "Unable to get Data - Possible network disruption", Toast.LENGTH_SHORT).show();
			}
		}
		
		protected void onCancelled() {
        	progressDialog.dismiss();
        }
		
		
	}
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.bus_list_menu) {
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
		return super.onMenuItemSelected(featureId, item);		
	}
	

}
