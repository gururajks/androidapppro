package com.transport.mbtalocpro;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.Transport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PredictionTimeListAdapter extends BaseAdapter {
	Context context;
	ArrivingTransport arrivingBus;
	String prediction_time_format;
	
	public PredictionTimeListAdapter(Context context, ArrivingTransport arrivingBus, String prediction_time_format) {
		this.context = context;
		this.arrivingBus = arrivingBus;
		this.prediction_time_format = prediction_time_format;		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrivingBus.vehicles.size();
	}

	@Override
	public Transport getItem(int index) {
		// TODO Auto-generated method stub
		return arrivingBus.vehicles.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		if(view == null) {
			LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
			view = inflator.inflate(R.layout.predicted_time_item, viewGroup, false);
			
			viewHolder = new ViewHolder();
			viewHolder.timeItem = (TextView) view.findViewById(R.id.predictedTimeItem);
			viewHolder.timeDisplayItem = (TextView) view.findViewById(R.id.predictedTimeDisplayItem);
			viewHolder.routeNoDisplay = (TextView) view.findViewById(R.id.routedisplay);			
			view.setTag(viewHolder);
		}	
		else {
			viewHolder = (ViewHolder) view.getTag();			
		}
		String routeInfo = arrivingBus.direction + " \n@ " + arrivingBus.stopTitle;
		Transport transport = arrivingBus.vehicles.get(index);
		String eta = String.valueOf(transport.timeOfArrival);
		String routeNo = arrivingBus.routeTitle;
				 
		viewHolder.timeItem.setText(routeInfo);
		
		eta = formatTime(eta);
		viewHolder.timeDisplayItem.setText(eta);
		
		viewHolder.routeNoDisplay.setText(routeNo);
		return view;
	}
	
	public String formatTime(String eta) {
		int intEta = Integer.parseInt(eta);
		if(prediction_time_format.equalsIgnoreCase("0")) {				//Minutes	
			int minutes = (int) Math.ceil((intEta)/60);
			eta = String.valueOf(minutes);
		}
		if(prediction_time_format.equalsIgnoreCase("1")) {				//Minutes with seconds
			int minutes = intEta/60;	//Gives the minutes
			int seconds = intEta % 60; 		//Gives the seconds after the minutes
			eta = String.valueOf(minutes) + "m " + String.valueOf(seconds) + "s";
		}
		
		return eta;
	}
	
	private static class ViewHolder {
		public TextView timeItem;
		public TextView timeDisplayItem;
		public TextView routeNoDisplay;
		
	}

}
