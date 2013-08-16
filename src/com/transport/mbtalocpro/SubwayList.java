package com.transport.mbtalocpro;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SubwayList extends Activity {

	ArrayList<String> subwayList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mbta_bus_list);
		
		subwayList = new ArrayList<String>();
		subwayList.add("Blue Line");
		subwayList.add("Orange Line");
		subwayList.add("Red Line");
		ListView listView =(ListView) findViewById(R.id.buslist);
		ArrayAdapter<String> subwayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.bus_item, R.id.busItem, subwayList);
		listView.setAdapter(subwayAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
				Intent intent = new Intent(getApplicationContext(), SubwayDirectionList.class);
				intent.putExtra("line", subwayList.get(index));
				startActivity(intent);
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

}
