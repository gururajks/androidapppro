package com.support.mbtalocpro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ShapeInfoDbHelper extends SQLiteOpenHelper {
	public String shapeTableName = "shapeInfo";
	Context context;
	
	public ShapeInfoDbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, null, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE if not exists " + shapeTableName + 
				" (id INTEGER PRIMARY KEY, " +
				   "route_id TEXT, " +
				   "shape_lat REAL, " +
				   "shape_lon REAL)");		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + shapeTableName);
		onCreate(db);		
	}
	
	

}
