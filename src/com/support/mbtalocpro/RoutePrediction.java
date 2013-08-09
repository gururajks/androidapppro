package com.support.mbtalocpro;

import java.util.ArrayList;


public class RoutePrediction extends Route {
	
	public String stopTitle;
	public String stopTag;
	
	public ArrayList<DirectionPrediction> dirForPredictions;
	
	public RoutePrediction() {
		dirForPredictions = new ArrayList<DirectionPrediction>();
	}

}
