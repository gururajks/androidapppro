package com.support.mbtalocpro;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

public class DatabaseQueryService extends IntentService {
	public final static String INCOMING_INTENT = "DbIncomingIntent";
	public final static String OUTCOMING_INTENT = "DbOutcomingIntent";
	public DatabaseQueryService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String routeTag = intent.getStringExtra(INCOMING_INTENT);
		String shape_id = AppConstants.ROUTE_SHAPE().get(routeTag);
        System.out.println("Route" + routeTag + " shape_id"+ shape_id);
    	ShapeInfoDbManager dbManager = new ShapeInfoDbManager(getApplicationContext());
		Cursor dbCursor = dbManager.getShapeInfo(shape_id);
		if(dbCursor != null && dbCursor.moveToFirst()) {			
			do {
				System.out.println("lat:" + dbCursor.getDouble(dbCursor.getColumnIndex("shape_lon")));	            				
			}
			while(dbCursor.moveToNext());
		}
		dbManager.closeDb(); 
		
	}

}
