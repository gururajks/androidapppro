package com.transport.mbtalocpro;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.support.mbtalocpro.DatabaseManager;
import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Route;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/*
 * This is a alert dialog for the bus routes that shows all the routes
 * It is a single click list alert dialog 
 */
public class BusStopsDialog extends DialogFragment {
	
	BusStopsDialogListener iDialogListener;
	static LinkedHashMap<String, String> stopList;
	static Direction choosenDirectionInfo;
	static Route routeInfo;
	static int savedCbState[];
	
	//This is the listener interface for the route dialog that gets the item selected on click	
	public interface BusStopsDialogListener {
		public void onSelectStop(int index, String stopName);
	}
	
	public BusStopsDialog newInstance(Route route, Direction choosendirection, LinkedHashMap<String, String> stops, int[] cbState) {
		stopList = stops;
		choosenDirectionInfo = choosendirection;
		routeInfo = route;
		savedCbState = cbState;
		
		BusStopsDialog stopDialog = new BusStopsDialog();
		return stopDialog;
	}
	 
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {		
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		LayoutInflater customLayout = getActivity().getLayoutInflater();
		View view = customLayout.inflate(R.layout.stop_list, null);
		ListView directionListView = (ListView) view.findViewById(R.id.stopList);
		String transportationType = this.getTag();	
		BusListAdapter stopListAdapter = new BusListAdapter(routeInfo, choosenDirectionInfo, stopList, getActivity(), savedCbState, transportationType);
		//INCOMPLETE				
		directionListView.setAdapter(stopListAdapter);
		directionListView.setOnItemClickListener(new BusStopDialogClickListener());
		dialog.setView(view);
		dialog.setTitle("Pick your " + transportationType + " Stop");
		
		return dialog.create();		
	}
	
	class BusStopDialogClickListener implements OnItemClickListener {		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
			TextView stopName = (TextView) view.findViewById(R.id.stopItem);
			iDialogListener.onSelectStop(index, (String) stopName.getText());
		}		
	}
	
	
	//Overriding the interface to confirm the interface has been implemented
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			iDialogListener = (BusStopsDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement BusRouteDialogListener");
		}
	}


}
