package com.support.mbtalocpro;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class AppConstants {
	
	public final static String API_KEY ="wX9NwuHnZU2ToO7GmGR9uw";
	
	
	
	public final static ArrayList<String> COMMUTER_RAIL_TRAINS() {
		ArrayList<String> commuterRailMap = new ArrayList<String>();
		
		commuterRailMap.add("");
		commuterRailMap.add("Greenbush Line");
		commuterRailMap.add("Kingston/Plymouth Line");
		commuterRailMap.add("Middleborough/Lakeville Line");
		commuterRailMap.add("Fairmount Line");
		commuterRailMap.add("Providence/Stoughton Line");
		commuterRailMap.add("Franklin Line");
		commuterRailMap.add("Needham Line");
		commuterRailMap.add("Framingham/Worcester Line");
		commuterRailMap.add("Fitchburg/South Acton Line");
		commuterRailMap.add("Lowell Line");
		commuterRailMap.add("Haverhill Line");
		commuterRailMap.add("Newburyport/Rockport Line");
				
		return commuterRailMap;		
	}
	
	public final static LinkedHashMap<String, String> ROUTE_SHAPE() {
		LinkedHashMap<String, String> shapeInfo = new LinkedHashMap<String, String>();
		shapeInfo.put("931_", "933_0002");
		shapeInfo.put("933_", "933_0002");
		shapeInfo.put("948_", "946_0002");
		shapeInfo.put("946_", "946_0002");
		shapeInfo.put("9462", "946_0002");
		shapeInfo.put("9482", "946_0002");
		shapeInfo.put("903_", "903_0012");
		shapeInfo.put("913_", "903_0012");		
		shapeInfo.put("CR-Fairmount","9870002");
		shapeInfo.put("CR-Fitchburg","9840001");
		shapeInfo.put("CR-Worcester","9850001");
		shapeInfo.put("CR-Franklin","9880005");
		shapeInfo.put("CR-Greenbush","9900001");
		shapeInfo.put("CR-Haverhill","9820001");
		shapeInfo.put("CR-Lowell","9830005");
		shapeInfo.put("CR-Needham","9860001");
		shapeInfo.put("CR-Newburyport","9810006");
		shapeInfo.put("CR-Providence","9890009");
		shapeInfo.put("CR-Kingston","9790002");
		shapeInfo.put("CR-Middleborough","9800001");				
		return shapeInfo;
	}
	
	
	
	
	

}
