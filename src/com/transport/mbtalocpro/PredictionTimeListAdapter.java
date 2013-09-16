package com.transport.mbtalocpro;

import com.support.mbtalocpro.ArrivingTransport;

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
		return arrivingBus.timeInSeconds.size();
	}

	@Override
	public Integer getItem(int index) {
		// TODO Auto-generated method stub
		return arrivingBus.timeInSeconds.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		if(view == null) {
			LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
			view = inflator.inflate(R.layout.predicted_time_item, viewGroup, false);
		}	
		String routeInfo = arrivingBus.direction + " \n@ " + arrivingBus.stopTitle;
		String eta = String.valueOf(arrivingBus.timeInSeconds.get(index));
		String routeNo = arrivingBus.routeTitle;
		TextView timeItem = (TextView) view.findViewById(R.id.predictedTimeItem);		 
		timeItem.setText(routeInfo);
		TextView timeDisplayItem = (TextView) view.findViewById(R.id.predictedTimeDisplayItem);
		eta = formatTime(eta);
		timeDisplayItem.setText(eta);
		TextView routeNoDisplay = (TextView) view.findViewById(R.id.routedisplay);
		routeNoDisplay.setText(routeNo);
		return view;
	}
	
	public String formatTime(String eta) {
		int intEta = Integer.parseInt(eta);
		if(prediction_time_format.equalsIgnoreCase("0")) {				//Minutes	
			int minutes = (int) Math.ceil((intEta)/60.0);
			eta = String.valueOf(minutes);
		}
		if(prediction_time_format.equalsIgnoreCase("1")) {				//Minutes with seconds
			int minutes = intEta/60;	//Gives the minutes
			int seconds = intEta % 60; 		//Gives the seconds after the minutes
			eta = String.valueOf(minutes) + "m " + String.valueOf(seconds) + "s";
		}
		
		return eta;
	}

}
