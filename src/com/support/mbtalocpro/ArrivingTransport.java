package com.support.mbtalocpro;

import java.io.Serializable;
import java.util.ArrayList;

public class ArrivingTransport implements Serializable {
	
	public ArrayList<Integer> minutes;
	public String dirTag;
	public String direction;
	public ArrayList<String> routeTag;
	public String routeTitle;
	public String transportType;
	public String stopTag;
	public String stopTitle; 
	public ArrayList<String> vehicleIds;
	public ArrayList<Transport> vehicles;
	
	public ArrivingTransport() {
		minutes = new ArrayList<Integer>();
		routeTag = new ArrayList<String>();
		vehicleIds = new ArrayList<String>();
		//vehicles = new ArrayList<Transport>();
	}

}
