package com.support.mbtalocpro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ShapesInfoDatabaseHelper extends SQLiteOpenHelper {

	Context context;
	public String shapeTableName = "shapeInfo";
	
	public ShapesInfoDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, null, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("CREATE TABLE if not exists " + shapeTableName + 
				" (id INTEGER PRIMARY KEY, " +
				   "shape_id TEXT, " +
				   "shape_lat REAL, " +
				   "shape_lon REAL)");	
	
		insertValues(database);				
	}

	
	private void insertValues(SQLiteDatabase database) {
		System.out.println("inserting");
		if(context!= null) {
			try {
				InputStream csvStream = context.getAssets().open("shapes.csv");
				BufferedReader buffReader = new BufferedReader(new InputStreamReader(csvStream));
				String line;
				while((line = buffReader.readLine()) != null) {					
					ContentValues dbValues = processString(line);
					database.insert(shapeTableName, null, dbValues);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	private ContentValues processString(String line) {
		String[] stringItems = line.split(",");
		ContentValues contentValues = new ContentValues();
		for(int i = 0 ; i < stringItems.length; i++) {
			String stringItem = stringItems[i];			
			if(i == 0) {
				contentValues.put("shape_id", stringItem);
			}										
			if(i == 1) {
				contentValues.put("shape_lat", Double.parseDouble(stringItem));
			}
			if(i == 2) {
				contentValues.put("shape_lon", Double.parseDouble(stringItem));
			}							
		}
		return contentValues;		
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + shapeTableName);
		onCreate(db);		
	}

}
