package com.transport.mbtalocpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.support.mbtalocpro.RealTimeMbtaRoutesListParser;
import com.support.mbtalocpro.Transport;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.RoutePrediction;
import com.support.mbtalocpro.TransportModes;
import com.transport.mbtalocpro.CommRailList.RealTimeMbtaDownloadRoutesList;

import android.support.v4.app.FragmentActivity;

public class UrlConnector extends FragmentActivity {
	

	//Connects to the web url and gets the result
    //flag - 1 : gets the gps data from buses
    //flag - 2: get the route info and direction details
	public ArrayList<Object> downloadUrl(URL url, int flag) throws IOException {
    	InputStream is = null;
    	HttpURLConnection conn = null;
    	ArrayList<Object> busInfo = null;
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
    		
    		if(flag == 1) {			// Mbta bus vehicle location 
    			MbtaGpsFeedParser gpsFeed = new MbtaGpsFeedParser(is);
    			ArrayList<Transport> gpsInfo = gpsFeed.getBusInfo();
    			busInfo = new ArrayList<Object>(gpsInfo);
    		}    		
    		if(flag == 3) {			//Gets the route object with all the route and route lines position for the bus
    			RouteParser routesInfo = new RouteParser();
    			Route routeDirInfoData = routesInfo.getRoute(is);
    			ArrayList<Route> routes = new ArrayList<Route>();
    			routes.add(routeDirInfoData);
    			busInfo = new ArrayList<Object>(routes);
    		}
    		if(flag == 4) {			//Prediction Parser 
    			PredictionsParser preParser = new PredictionsParser();
    			ArrayList<RoutePrediction> routesPredictions = preParser.getPredictions(is);
    			busInfo = new ArrayList<Object>(routesPredictions);
    		}
    		if(flag == 5) {			//Realtime mbta routes parser for subway and commuter rail currently
    			RealTimeMbtaRoutesListParser realtimeRoutesList = new RealTimeMbtaRoutesListParser();
    			TransportModes modes = realtimeRoutesList.getRoutesList(is);
    			ArrayList<TransportModes> modesList = new ArrayList<TransportModes>();
    			modesList.add(modes);
    			busInfo = new ArrayList<Object>(modesList);
    			
    		}
    		return busInfo;   		
    	}
    	catch(IOException e) {
    		int responseCode = conn.getResponseCode();
    		System.out.println("The response is: " + responseCode);    		
    	}    	
		return null;    	
    }
	


}
