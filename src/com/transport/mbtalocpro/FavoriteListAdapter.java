package com.transport.mbtalocpro;

import java.util.ArrayList;

import com.support.mbtalocpro.FavoriteListItemObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.sax.StartElementListener;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FavoriteListAdapter extends BaseAdapter {
	
	ArrayList<FavoriteListItemObject> favoriteObjectList;
	Context context;
	ImageButton routeImage;
	
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
		
		//Route name in bold
		TextView favouriteRouteName = (TextView) view.findViewById(R.id.favRouteName);
		favouriteRouteName.setText(favoriteListItemObject.routeTitle);
		
		//Direction name
		TextView favouriteDirectionName = (TextView) view.findViewById(R.id.favDirectionItem);
		favouriteDirectionName.setText(favoriteListItemObject.directionTitle);
		
		//Stop name
		TextView favouriteStopName = (TextView) view.findViewById(R.id.favStopName);
		favouriteStopName.setText(favoriteListItemObject.stopTitle);
		
		//Image view for a bus or a train or a subway
		ImageView transpoImage = (ImageView) view.findViewById(R.id.transpoImage);
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Bus")) {
			transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bus));
		}
		
		if(favoriteListItemObject.transportationType.equalsIgnoreCase("Commuter Rail")) {
			transpoImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_trains));
		}
		
		//Image button
		routeImage = (ImageButton) view.findViewById(R.id.pinImage);
		routeImage.setOnClickListener(new ImageOnClickListener());
				
		return view;
	}
	
	/*
	 * Image button click listener for all the items in the list 
	 */
	private class ImageOnClickListener extends FragmentActivity implements OnClickListener {

		public final int IMAGE_PICK_CODE = 1;
		@Override
		public void onClick(View view) {
			Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			imageIntent.setType("image/*");
			startActivity(imageIntent);//, IMAGE_PICK_CODE);
		}
		
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null) {
				Uri _uri = data.getData();

		        //User had pick an image.
		        Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
		        cursor.moveToFirst();

		        //Link to the image
		        final String imageFilePath = cursor.getString(0);
		        routeImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePath));
		        cursor.close();
			}
		}
		
	}

}
