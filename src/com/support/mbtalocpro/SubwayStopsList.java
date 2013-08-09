package com.support.mbtalocpro;

import java.util.LinkedHashMap;

public class SubwayStopsList {	
	
	public LinkedHashMap<String, String> getTrainStops(String trainNo, String direction) {
		LinkedHashMap<String, String> stopNames = new LinkedHashMap<String, String>();
		//Stops for the red line
		if(trainNo.equalsIgnoreCase("Red Line")) {
			stopNames.put("Alewife", "Alewife");
			stopNames.put("Davis", "Davis Square");
			stopNames.put("Porter", "Porter Square");
			stopNames.put("Harvard", "Harvard Square");
			stopNames.put("Central", "Central Square");
			stopNames.put("Kendal/MIT", "Kendal/MIT");
			stopNames.put("Charles/MGH", "Charles/MGH");
			stopNames.put("Park St", "Park St");
			stopNames.put("Downtown Crossing", "Downtown Crossing");
			stopNames.put("South Station", "South Station");
			stopNames.put("Broadway", "Broadway");
			stopNames.put("Andrew", "Andrew");
			stopNames.put("JFK/UMass", "JFK/UMass");
			if(direction.equalsIgnoreCase("Braintree")) {
				stopNames.put("North Quincy", "North Quincy");
				stopNames.put("Wollaston", "Wollaston");
				stopNames.put("Quincy Center", "Quincy Center");
				stopNames.put("Quincy Adams", "Quincy Adams");
				stopNames.put("Greenbush Line", "Greenbush Line");
				stopNames.put("Braintree", "Braintree");
			}
			if(direction.equalsIgnoreCase("Ashmont")) {
				stopNames.put("Savin Hill", "Savin Hill");
				stopNames.put("Fields Corner", "Fields Corner");
				stopNames.put("Shawmut", "Shawmut");
				stopNames.put("Ashmont", "Ashmont");
			}
		}	
		
		//Stops for Blue line
		
		//Stops for Orange line
		
		
		
		return stopNames;
	}
	

}
