package com.support.mbtalocpro;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

public class DatabaseQueryService extends IntentService {
	public final static String INCOMING_INTENT = "DbIncomingIntent";
	public final static String OUTCOMING_INTENT = "DbOutcomingIntent";
	public DatabaseQueryService() {
		super("DbQueryService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String routeTag = intent.getStringExtra(INCOMING_INTENT);
		String shape_id = AppConstants.ROUTE_SHAPE().get(routeTag);
    	ShapeInfoDbManager dbManager = new ShapeInfoDbManager(getApplicationContext());
		/*Cursor dbCursor = dbManager.getShapeInfo(shape_id);		
		System.out.println(routeTag + shape_id);
		if(dbCursor != null && dbCursor.moveToFirst()) {	 		 
			do {
				System.out.println("lat:" + dbCursor.getDouble(dbCursor.getColumnIndex("shape_lon")));	            				
			}
			while(dbCursor.moveToNext());
		}*/ 
		dbManager.closeDb(); 		
	}

}
