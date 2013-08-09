package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Route;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class BusListAdapter extends BaseAdapter{
	
	Route routeInfo;
	Direction choosenDirectionInfo;
	LinkedHashMap<String, String> stopList = new LinkedHashMap<String, String>();
	ArrayList<String> stopsTitleList;
	ArrayList<String> stopsTagList;
	Context context;
	int [] savedCbState;
	ArrayList<Boolean> currCbState;
	DatabaseManager dbManager;
	//Get the list of buses in a given agency
	public BusListAdapter (Route route, Direction choosenDirection,LinkedHashMap<String, String> stopList,Context context, int[] savedCbState) {
		routeInfo = route;		
		choosenDirectionInfo = choosenDirection;
		this.stopList = stopList;
		this.context = context;
		this.savedCbState = savedCbState;
		currCbState = new ArrayList<Boolean>();
		stopsTitleList = new ArrayList<String>(stopList.values());
		stopsTagList = new ArrayList<String>(stopList.keySet());
		for(int i = 0 ; i < this.stopList.size(); i++) {
			currCbState.add(i, false);
		}
		if(savedCbState != null) {
			for(int i = 0; i < savedCbState.length; i++) {
				currCbState.set(savedCbState[i], true);
			}
		}		
	}
	
	@Override
	public int getCount() {
		return stopList.size();
	}

	@Override
	public String getItem(int index) {
		return stopList.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	//Setting the adapter to get the values
	public View getView(final int index, View view, ViewGroup viewGroup) {		
		if(view == null) {
			LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
			view = inflator.inflate(R.layout.stop_item, viewGroup, false);
		}
		String stopTitle = stopsTitleList.get(index);
		TextView textView = (TextView) view.findViewById(R.id.stopItem);
		textView.setText(stopTitle);
		CheckBox fav_check = (CheckBox) view.findViewById(R.id.fav_check);
		fav_check.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View cbView) {
				DatabaseManager dbManager = new DatabaseManager(context);
				CheckBox lcbView = (CheckBox) cbView;
				if(lcbView.isChecked()) {
					currCbState.set(index, true);
					dbManager.saveData(routeInfo.routeTitle, routeInfo.routeTag, choosenDirectionInfo.directionTitle, 
							choosenDirectionInfo.directionTag, stopsTitleList.get(index), stopsTagList.get(index), index);
					Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
				}
				else {
					currCbState.set(index, false);
					dbManager.deleteData(routeInfo.routeTag, choosenDirectionInfo.directionTag, index);
					Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
				}
				dbManager.closeDb();
			}			
		});

		fav_check.setChecked(currCbState.get(index));		
		return view;
	}
	
}
