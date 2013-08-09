package com.transport.mbtalocpro;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.SubwayJsonParser;
import com.support.mbtalocpro.SubwayStopsList;
import com.transport.mbtalocpro.BusStopsDialog.BusStopsDialogListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SubwayDirectionList extends FragmentActivity implements BusStopsDialogListener {
	
	String trainNo;
	String choosenDirectionTag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_direction_list);
		Intent intent = getIntent();
		trainNo = intent.getStringExtra("line");
		final Route route = new Route();
		route.routeTitle = trainNo;
		route.routeTag = trainNo;
		ArrayList<String> dirArrayList = getDirectionsForTrains(trainNo);		
		ListView listView = (ListView) findViewById(R.id.bus_dir_list);
		ArrayAdapter<String> subwayDirectionAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.direction_item, R.id.dirItem, dirArrayList);					
		listView.setAdapter(subwayDirectionAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
				TextView direction = (TextView) view.findViewById(R.id.dirItem);
				Direction choosenDirection = new Direction();
				choosenDirection.directionTitle = (String) direction.getText();
				choosenDirectionTag = choosenDirection.directionTag = (String) direction.getText();
				LinkedHashMap<String, String> stopNames = new SubwayStopsList().getTrainStops(route.routeTitle, choosenDirection.directionTag);
				getTrainStopsData(route, choosenDirection, stopNames);
			}
		});		
	}		

	public void getTrainStopsData(Route route, Direction choosenDirection, LinkedHashMap<String, String> stopNames) {
		DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
		int savedCbState[] = null;
		Cursor checkBoxCursor = dbManager.getData(route.routeTag, choosenDirection.directionTag);
		if(checkBoxCursor != null && checkBoxCursor.moveToFirst()) {
			savedCbState = new int[checkBoxCursor.getCount()];
			int counter = 0;
			do {
				savedCbState[counter] = checkBoxCursor.getInt(7);
				counter++;
			} while(checkBoxCursor.moveToNext());			
		}		
		checkBoxCursor.close();
		dbManager.closeDb();
		DialogFragment routeDialog = new BusStopsDialog().newInstance(route, choosenDirection, stopNames, savedCbState);
		routeDialog.show(getSupportFragmentManager(), "trainStops");
	}
	
	private ArrayList<String> getDirectionsForTrains(String trainNo) {
		ArrayList<String> dirArrayList = new ArrayList<String>();
		if(trainNo.equalsIgnoreCase("Red Line")) {
			dirArrayList.add("Alewife");
			dirArrayList.add("Braintree");
			dirArrayList.add("Ashmont");
		}
		if(trainNo.equalsIgnoreCase("Blue Line")) {
			dirArrayList.add("Bowdoin");
			dirArrayList.add("Wonderland");
		}
		if(trainNo.equalsIgnoreCase("Orange Line")) {
			dirArrayList.add("Oak Grove");
			dirArrayList.add("Forest Hills");
		}
		return dirArrayList;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.subway_direction_list, menu);
		return true;
	}

	@Override
	public void onSelectStop(int index, String stopName) {
		new SubwayPrediction().execute(stopName);		
	}
	
	class SubwayPrediction extends AsyncTask<String, Integer, ArrivingTransport> {
		
		protected ArrivingTransport doInBackground(String... params) {
			SubwayJsonParser subwayParser = new SubwayJsonParser(trainNo, params[0], choosenDirectionTag);
			subwayParser.parseSubwayInfo();
			return subwayParser.getArrivingTransport();
		}
		
		protected void onPostExecute(ArrivingTransport arrivingTransport) {
			if(arrivingTransport != null) {
				Intent intent = new Intent(getApplicationContext(), HomeActivityContainer.class);
				intent.putExtra("arrivingBus", arrivingTransport);
				startActivity(intent);				
			}
		}
		
	}

}
