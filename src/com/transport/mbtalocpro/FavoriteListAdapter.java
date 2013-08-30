package com.transport.mbtalocpro;

import java.util.ArrayList;

import com.support.mbtalocpro.FavoriteListItemObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoriteListAdapter extends BaseAdapter {
	
	ArrayList<FavoriteListItemObject> favoriteObjectList;
	Context context;
	
	public FavoriteListAdapter(Context context, ArrayList<FavoriteListItemObject> favoriteObjectList) {
		this.favoriteObjectList = favoriteObjectList;
		this.context = context;
	}

	@Override
	public int getCount() {		
		return favoriteObjectList.size();
	}

	@Override
	public Object getItem(int arg0) {		
		return null;
	}

	@Override
	public long getItemId(int arg0) {		
		return arg0;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		if(view == null) {
			LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
			view = inflator.inflate(R.layout.fav_item, viewGroup, false);
		}	
		FavoriteListItemObject favoriteListItemObject = favoriteObjectList.get(index);
		TextView favouriteRouteName = (TextView) view.findViewById(R.id.favRouteName);
		favouriteRouteName.setText(favoriteListItemObject.routeTitle);
		
		TextView favouriteDirectionName = (TextView) view.findViewById(R.id.favDirectionItem);
		favouriteDirectionName.setText(favoriteListItemObject.directionTitle);
		
		TextView favouriteStopName = (TextView) view.findViewById(R.id.favStopName);
		favouriteStopName.setText(favoriteListItemObject.stopTitle);
		
		
		ImageView transpoImage = (ImageView) view.findViewById(R.id.transpoImage);
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Bus")) {
			transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus));
		}
		
		
		
		return view;
	}

}