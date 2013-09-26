package com.transport.mbtalocpro;

import com.support.mbtalocpro.ArrivingTransport;
import com.support.mbtalocpro.Transport;

import android.content.Context;
import android.text.format.Time;
import android.util.TypedValue;
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
			viewHolder.timeItemDirection = (TextView) view.findViewById(R.id.predictedTimeItemDirection);
			viewHolder.timeItemStop = (TextView) view.findViewById(R.id.predictedTimeItemStop);
			viewHolder.timeDisplayItem = (TextView) view.findViewById(R.id.predictedTimeDisplayItem);
			viewHolder.routeNoDisplay = (TextView) view.findViewById(R.id.routedisplay);			
			viewHolder.currentTimeDisplay = (TextView) view.findViewById(R.id.currentTime);
			view.setTag(viewHolder);
		}	
		else {
			viewHolder = (ViewHolder) view.getTag();			
		}
		String directionInfo = arrivingBus.direction;
		String stopInfo = arrivingBus.stopTitle;
		Transport transport = arrivingBus.vehicles.get(index);
		String eta = String.valueOf(transport.timeOfArrival);
		String routeNo = arrivingBus.routeTitle;
				 
		viewHolder.timeItemDirection.setText(directionInfo);
		viewHolder.timeItemStop.setText(stopInfo);
		
		eta = formatTime(eta);
		viewHolder.timeDisplayItem.setText(eta);
		//Reduce font if the predicted time format is mm:ss
		if(prediction_time_format.equalsIgnoreCase("1")) {				//Minutes with seconds
			viewHolder.timeDisplayItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		}		
		viewHolder.routeNoDisplay.setText(routeNo);
		
		Time currentTime = new Time();
		currentTime.setToNow();
		viewHolder.currentTimeDisplay.setText("Updated :" + currentTime.format("%H:%M:%S"));
		
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
		public TextView timeItemDirection;
		public TextView timeItemStop;
		public TextView timeDisplayItem;
		public TextView routeNoDisplay;
		public TextView currentTimeDisplay;
	}

}
