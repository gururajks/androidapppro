package com.support.mbtalocpro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	String tableName = "checkboxState";
	String commRailTableName = "commRailTableName";
	Context context;
	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, null, version);
		this.context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE if not exists " + tableName + 
				" (id INTEGER PRIMARY KEY, " +
				   "route TEXT, " +
				   "routeTag TEXT, " +
				   "dirTitle TEXT, " +
				   "dirTag TEXT, " +
				   "stopTitle TEXT, " +
				   "stopTag TEXT, " +				   
				   "listposition INTEGER, " + 
				   "transportationType TEXT)");
		database.execSQL("CREATE TABLE if not exists " + commRailTableName + 
				" (id INTEGER PRIMARY KEY, " +
				   "route TEXT, " +
				   "dirTitle TEXT, " +
				   "stopSeqId TEXT, " +
				   "stopTitle TEXT, " +
				   "stopLat REAL, " +
				   "stopLng REAL)");
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		//Drop and recreate the table in case the table is upgraded	
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
		onCreate(db);
	}

}
