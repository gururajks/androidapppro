package com.support.mbtalocpro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ShapeInfoDbManager {
	public String shapeTableName = "shapeInfo";
	private SQLiteDatabase database;
	public final String DATABASE_NAME = "mbtaprobusbookmark.db";
	
	public ShapeInfoDbManager(Context context) {
		ShapeInfoDbHelper helper = new ShapeInfoDbHelper(context, DATABASE_NAME, null, 2);
		database = helper.getReadableDatabase();		
	}
	
	public Cursor getShapeInfo(String shape_id) {
		return database.rawQuery("SELECT shape_lat, shape_lat FROM " + shapeTableName + " WHERE shape_id='" + shape_id + "'", null);
	}
	
	public void closeDb() {
		database.close();
	}

}
