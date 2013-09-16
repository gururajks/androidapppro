package com.transport.mbtalocpro;


import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.android.gms.maps.GoogleMap;
import com.support.mbtalocpro.ArrivingTransport;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PredictionTimeFragment extends Fragment {
	  private PredictedTimeFragmentItemSelectedListener listener;
	  View view;
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		  view = inflater.inflate(R.layout.prediction_time, container, false);		  
		  return view;
	  }
	  
	  public void setArrivingBusDetails(Context context, ArrivingTransport arrivingBus, String prediction_time_format) {
		  
		  
		  ListView listView = (ListView) view.findViewById(R.id.predictedItem);
		  
		  PredictionTimeListAdapter minutesAdapter = new PredictionTimeListAdapter(context, arrivingBus, prediction_time_format);
		  
		  listView.setAdapter(minutesAdapter);
		  
		  listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
				
			}
		});
	  }

	  public interface PredictedTimeFragmentItemSelectedListener {
	      public void onFragmentItemSelected();
	  }
	  
	  @Override
	  public void onAttach(Activity activity) {
		  super.onAttach(activity);
		  try {
			  listener = (PredictedTimeFragmentItemSelectedListener) activity;
		  } catch(InflateException e) {
		    e.printStackTrace();
		  }
	  }

  
	  public void triggerFunction() {
		  listener.onFragmentItemSelected();
	  }

  
} 