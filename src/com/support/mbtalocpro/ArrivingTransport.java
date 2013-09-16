package com.support.mbtalocpro;

import java.io.Serializable;
import java.util.ArrayList;

public class ArrivingTransport implements Serializable {
	
	public ArrayList<Integer> timeInSeconds;
	public String dirTag;
	public String direction;
	public ArrayList<String> routeTag;
	public String routeTitle;
	public String transportType;
	public String stopTag;
	public String stopTitle;
	public double stopLat;
	public double stopLng;
	public ArrayList<String> vehicleIds;
	public ArrayList<Transport> vehicles;
	
	public ArrivingTransport() {
		timeInSeconds = new ArrayList<Integer>();
		routeTag = new ArrayList<String>();
		vehicleIds = new ArrayList<String>();
		vehicles = new ArrayList<Transport>();
	} 

}
