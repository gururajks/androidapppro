package com.support.mbtalocpro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {
	String tableName = "checkboxState";
	private SQLiteDatabase database;
	public final String DATABASE_NAME = "mbtaprobusbookmark.db";
	
	public DatabaseManager(Context context) {
		DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME, null, 5);
		database = helper.getWritableDatabase();		
	}
	
	public void saveData(String routeNo, String routeTag, 
		String dirTitle, String dirTag, String stopTitle, String stopTag, int listPosition) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("route", routeNo);
		contentValues.put("listposition", listPosition);
		contentValues.put("routeTag", routeTag);
		contentValues.put("dirTag", dirTag);
		contentValues.put("dirTitle", dirTitle);
		contentValues.put("stopTag", stopTag);
		contentValues.put("stopTitle", stopTitle);
		database.insert(tableName, null, contentValues);
	}
	
	public void deleteData(String routeTag, String dirTag, int listPosition) {
		database.delete(tableName, "routeTag = '"+routeTag+"' AND listposition = " + listPosition + 
							" AND dirTag = '" + dirTag +"'", null);
	}
	
	public Cursor getData(String routeTag, String dirTag) {
		return database.rawQuery("SELECT * FROM " + tableName + " WHERE routeTag = '" + routeTag 
						+ "' AND dirTag = '" + dirTag +"'", null);
	}
	
	public Cursor getAllData() {
		return database.rawQuery("SELECT * FROM " + tableName, null);
	}
	
	public void closeDb() {
		database.close();
	}

}
