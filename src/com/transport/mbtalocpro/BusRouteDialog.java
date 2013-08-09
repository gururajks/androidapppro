package com.transport.mbtalocpro;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


/*
 * This is a alert dialog for the bus routes that shows all the routes
 * It is a single click list alert dialog 
 */
public class BusRouteDialog extends DialogFragment {
	
	BusRouteDialogListener iDialogListener;
	static CharSequence[] routeList;
	
	//This is the listener interface for the route dialog that gets the item selected on click	
	public interface BusRouteDialogListener {
		public void onSelectRoute(CharSequence item);
	}
	
	public BusRouteDialog newInstance(CharSequence[] routes) {
		routeList = routes;		
		BusRouteDialog routeDialog = new BusRouteDialog();
		return routeDialog;
	}
	
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle("Pick your Bus Route");		
		dialog.setItems(routeList, new BusRouteDialogClickListener());		
		return dialog.create();		
	}
	
	class BusRouteDialogClickListener implements android.content.DialogInterface.OnClickListener {		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			iDialogListener.onSelectRoute(routeList[which]);			
		}		
	}
	
	
	//Overriding the interface to confirm the interface has been implemented
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the BusRouteDialogListener so we can send events to the host
			iDialogListener = (BusRouteDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement BusRouteDialogListener");
		}
	}


}
