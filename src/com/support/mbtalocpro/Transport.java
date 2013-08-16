package com.support.mbtalocpro;

import java.io.Serializable;

public class Transport implements Serializable {
	public int id;
	public String routeTag;
	public String routeTitle;
	public String dirTag;
	public String dirTitle;
	public double lat;
	public double lng;
	public int secSinceReport;
	public boolean isPredictable;
	public int heading;
}
