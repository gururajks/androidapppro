package com.transport.mbtalocpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpConnection;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Routes;

public class CommuterRailParser { 
	InputStream is;
	SQLiteDatabase database;
	String commRailTableName;
	public interface CommuterRailRoutesInterface {
		public void iCommRailRoutesDownloadedData(Routes routes);		
	}
	
	public CommuterRailParser(SQLiteDatabase database, String commRailTableName) {
		this.commRailTableName = commRailTableName;
		this.database = database;
		try {			
			URL url = new URL("http://www.mbta.com/uploadedfiles/Rider_Tools/Developer_Page/CommuterRailStationLineOrdering.csv");
			new DownloadRailInfo().execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	 
	
	class DownloadRailInfo extends AsyncTask<URL, Integer, Routes> {

		@Override
		protected Routes doInBackground(URL... url) {
			return downloadData(url[0]);			
		}
				
	}
	private Routes downloadData(URL url) {
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
    			
    		return getRoutesInfo(is);
    		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			url = null;
		}
		
		return null;
	}
	 
	
	
	//get the route info of commuter rail
	public Routes getRoutesInfo(InputStream is) throws IOException, IndexOutOfBoundsException {		
		String line = null;
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(is));
		buffReader.readLine();		//ignoring the first line
		while((line = buffReader.readLine()) != null) {
			System.out.println(line);
			if(!line.equalsIgnoreCase(",,,,,,")) {		//the csv terminates with this .. badly formatted csv
				String[] csvElements = line.split(",");
				database.execSQL("INSERT INTO commRailTableName " +
						"(route, dirTitle, stopSeqId, stopTitle, stopLag, stopLng) " +
						"VALUES('"+csvElements[0]+"', '"+csvElements[1]+"', '"+csvElements[2]+"', '"+csvElements[3] 
								+"', '"+csvElements[4]+"', '"+csvElements[5]+"')");			 
			}
		}		
		return null;
	}

}
