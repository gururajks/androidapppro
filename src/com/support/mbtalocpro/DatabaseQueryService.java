package com.support.mbtalocpro;

import java.util.ArrayList;

import com.transport.mbtalocpro.HomeActivityContainer.RoutesPointReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public class DatabaseQueryService extends IntentService {
	public final static String INCOMING_INTENT = "DbIncomingIntent";
	
	public DatabaseQueryService() {
		super("DbQueryService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String routeTag = intent.getStringExtra(INCOMING_INTENT);		
		String shape_id = AppConstants.ROUTE_SHAPE().get(routeTag);
		ArrayList<ParcelablePoint> pointsArray = new ArrayList<ParcelablePoint>();
    	DatabaseManager dbManager = new DatabaseManager(getApplicationContext());
		Cursor dbCursor = dbManager.getShapeInfo(shape_id);		
		
		if(dbCursor != null && dbCursor.moveToFirst()) {	 		 
			do { 				
				double lat = dbCursor.getDouble(dbCursor.getColumnIndex("shape_lat"));
				double lng = dbCursor.getDouble(dbCursor.getColumnIndex("shape_lon"));				
				pointsArray.add(new ParcelablePoint(lat, lng));				
			}
			while(dbCursor.moveToNext());

		} 
		dbManager.closeDb(); 	
		Intent broadCast = new Intent();		
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("points", pointsArray);
		broadCast.setAction(RoutesPointReceiver.POINT_RECEIVER_FLAG);
		broadCast.putExtras(bundle);
		sendBroadcast(broadCast);
	}

}
