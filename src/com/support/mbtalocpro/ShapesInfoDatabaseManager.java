package com.support.mbtalocpro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ShapesInfoDatabaseManager {
	
	public final String DATABASE_NAME = "shapesInfo.db";
	public String shapeTableName = "shapeInfo";
	private SQLiteDatabase database;
	
	public ShapesInfoDatabaseManager(Context context) {
		ShapesInfoDatabaseHelper helper = new ShapesInfoDatabaseHelper(context, DATABASE_NAME, null, AppConstants.DATABASE_VERSION);
		database = helper.getWritableDatabase();	
	}
	
	/*
	 * Shape Data
	 */
	public Cursor getShapeInfo(String shape_id) {
		return database.rawQuery("SELECT * FROM " + shapeTableName + " WHERE shape_id='" + shape_id + "'", null);
	}
		
	public void closeDb() {
		database.close();
	}
	

}
