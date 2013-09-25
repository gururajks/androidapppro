package com.support.mbtalocpro;

import java.io.Serializable;

public class Transport implements Serializable, Comparable<Transport> {
	public int id;
	public String routeTag;
	public String routeTitle;
	public String dirTag;
	public String dirTitle;
	public double lat;
	public double lng;
	public int secondsSinceLastReported;
	public boolean isPredictable;
	public int heading;	
	public int timeOfArrival;
	public String vehicleId;
	
	public Transport() {
		routeTag = "";
		routeTitle = "";
		dirTag = "";
		dirTitle = "";		
	}
	
	
	@Override
	public int compareTo(Transport transport) {		
		int transportTime = ((Transport) transport).timeOfArrival;		
		return this.timeOfArrival - transportTime;
	}	
}
