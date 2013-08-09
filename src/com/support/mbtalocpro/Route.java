package com.support.mbtalocpro;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Route {
	public String routeTag;
	public String routeTitle;
	public double routeLatMax;
	public double routeLatMin;
	public double routeLngMax;
	public double routeLngMin;
	
	public LinkedHashMap<String, Stop> stopList;
	public ArrayList<Direction> directionList;
	public ArrayList<Path> routePath;
	
	public Route() {
		stopList = new LinkedHashMap<String, Stop>();
		directionList = new ArrayList<Direction>();
		routePath = new ArrayList<Path>();
	}
}
