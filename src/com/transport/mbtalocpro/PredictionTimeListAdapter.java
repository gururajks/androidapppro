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
	
	public PredictionTimeListAdapter(Context context, ArrivingTransport arrivingBus) {
		this.context = context;
		this.arrivingBus = arrivingBus; 
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrivingBus.minutes.size();
	}

	@Override
	public Integer getItem(int index) {
		// TODO Auto-generated method stub
		return arrivingBus.minutes.get(index);
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
		String eta = String.valueOf(arrivingBus.minutes.get(index));
		String routeNo = arrivingBus.routeTitle;
		TextView timeItem = (TextView) view.findViewById(R.id.predictedTimeItem);
		timeItem.setText(routeInfo);
		TextView timeDisplayItem = (TextView) view.findViewById(R.id.predictedTimeDisplayItem);
		timeDisplayItem.setText(eta);
		TextView routeNoDisplay = (TextView) view.findViewById(R.id.routedisplay);
		routeNoDisplay.setText(routeNo);
		return view;
	}

}
