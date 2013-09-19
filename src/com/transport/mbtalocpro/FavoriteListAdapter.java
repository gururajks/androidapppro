package com.transport.mbtalocpro;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.support.mbtalocpro.FavoriteListItemObject;
import com.support.mbtalocpro.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FavoriteListAdapter extends BaseAdapter {
	
	ArrayList<FavoriteListItemObject> favoriteObjectList;
	Context context;
	ViewHolder viewHolder;
		 
	/*
	 * Holder class for all the views to avoid the expensive findViewById process to recur
	 */
	private static class ViewHolder {
		public TextView favouriteRouteName;
		public TextView favouriteDirectionName;
		public TextView favouriteStopName;
		public ImageView transpoImage;
		public ImageButton routeImage;
	}
	
	
	public final static int IMAGE_PICK_CODE = 0;
	int indexClicked;
	
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
		return favoriteObjectList.get(arg0);
	}

	@Override 
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		
		if(view == null) {
			LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
			view = inflator.inflate(R.layout.fav_item, viewGroup, false);
			viewHolder = new ViewHolder();
			viewHolder.favouriteRouteName = (TextView) view.findViewById(R.id.favRouteName);
			viewHolder.favouriteDirectionName = (TextView) view.findViewById(R.id.favDirectionItem);
			viewHolder.favouriteStopName = (TextView) view.findViewById(R.id.favStopName);
			viewHolder.routeImage = (ImageButton) view.findViewById(R.id.pinImage);
			view.setTag(viewHolder);
		}	 
		else {
			viewHolder = (ViewHolder) view.getTag();
		}
		FavoriteListItemObject favoriteListItemObject = favoriteObjectList.get(index);
		
		//Route name in bold		
		viewHolder.favouriteRouteName.setText(favoriteListItemObject.routeTitle);
		
		//Direction name		
		viewHolder.favouriteDirectionName.setText(favoriteListItemObject.directionTitle);
		
		//Stop name		
		viewHolder.favouriteStopName.setText(favoriteListItemObject.stopTitle);
		
		//Image view for a bus or a train or a subway
		viewHolder.transpoImage = (ImageView) view.findViewById(R.id.transpoImage);
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Bus")) {
			viewHolder.transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus));
		}
		
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Commuter Rail")) {
			viewHolder.transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_trains));
		}
		
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Subway")) {
			viewHolder.transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_subway));
		}
		
		//Image button					
		if(favoriteListItemObject.imagePath != null) {
			
			ImageLoader imgLoader = new ImageLoader();
			imgLoader.loadImage(favoriteListItemObject.imagePath, viewHolder.routeImage, context.getResources(), index);
		}
		
		viewHolder.routeImage.setOnClickListener(new ImageOnClickListener());
		
		viewHolder.routeImage.setTag(index);		
		
		return view; 
	}
	
		
	
	/*
	 * Image button click listener for all the items in the list 
	 */
	private class ImageOnClickListener implements OnClickListener {
				
		@Override
		public void onClick(View view) {
			Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			imageIntent.setType("image/*");
			
			Integer position = (Integer) view.getTag();
			
			setClickedIndex(position.intValue());
			((FragmentActivity)view.getContext()).startActivityForResult(imageIntent, IMAGE_PICK_CODE);
		}		
	}
	
	int index;
	public void setClickedIndex(int index) {
		this.index = index;
	}
	
	public int getClickedIndex() {
		return index;
	}
	

}
