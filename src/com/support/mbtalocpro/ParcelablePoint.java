package com.support.mbtalocpro;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePoint extends Point implements Parcelable {
	
		
	public ParcelablePoint(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	public ParcelablePoint(Parcel source) {
		this.lat = source.readDouble();
		this.lng = source.readDouble();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(lat);
		dest.writeDouble(lng);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		@Override
		public ParcelablePoint createFromParcel(Parcel source) {			
			return new ParcelablePoint(source);
		}

		@Override
		public Object[] newArray(int size) {
			
			return new ParcelablePoint[size];
		}
	};
	
	/*
	 * Getter
	 */
	public double getLat() {
		return lat;
	}
	
	public double getLng() {
		return lng;
	}

}
