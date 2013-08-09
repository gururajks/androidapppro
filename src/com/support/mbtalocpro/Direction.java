package com.support.mbtalocpro;

import java.util.ArrayList;

public class Direction {
	public String directionTag;
	public String directionTitle;
	public String directionName;
	public ArrayList<Stop> stopList;
	public boolean useForUI;
	public Direction() {
		stopList = new ArrayList<Stop>();
	}
}
