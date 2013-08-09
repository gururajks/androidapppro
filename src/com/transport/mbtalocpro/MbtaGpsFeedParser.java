package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.support.mbtalocpro.Transport;


public class MbtaGpsFeedParser {
	
	String tagText;
	
	ArrayList<Transport> busInfo;
	//constructor 
	public MbtaGpsFeedParser(InputStream xmlString) {
		busInfo = new ArrayList<Transport>();		
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(xmlString, null);
			int eventType = xmlParser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					String xmlTagName = xmlParser.getName();
					if(xmlTagName.equalsIgnoreCase("vehicle")) {
						Transport bus = new Transport();
						String attributeValue = xmlParser.getAttributeValue(null, "lat");
						if(attributeValue != null) {						
							bus.lat = Double.valueOf(attributeValue);
						}
						attributeValue = xmlParser.getAttributeValue(null, "lon");
						if(attributeValue != null) {						
							bus.lng = Double.valueOf(attributeValue);
						}
						attributeValue = xmlParser.getAttributeValue(null, "id");
						if(attributeValue != null) {						
							bus.id = Integer.valueOf(attributeValue); 
						}
						attributeValue = xmlParser.getAttributeValue(null, "routeTag");
						if(attributeValue != null) {						
							bus.routeTag = attributeValue;
						}
						attributeValue = xmlParser.getAttributeValue(null, "dirTag");
						if(attributeValue != null) {			 			
							bus.dirTag = attributeValue;
						}
						attributeValue = xmlParser.getAttributeValue(null, "secsSinceReport");
						if(attributeValue != null) {						
							bus.secSinceReport = Integer.valueOf(attributeValue);
						}
						attributeValue = xmlParser.getAttributeValue(null, "predictable");
						if(attributeValue != null) {						
							bus.isPredictable = Boolean.valueOf(attributeValue);
						}
						attributeValue = xmlParser.getAttributeValue(null, "heading");
						if(attributeValue != null) {						
							bus.heading = Integer.valueOf(attributeValue);
						}
						busInfo.add(bus);
					}					
					if(xmlTagName.equalsIgnoreCase("route")) {
						Transport bus = new Transport();
						String attributeValue = xmlParser.getAttributeValue(null, "title");
						if(attributeValue != null) {
							bus.routeTitle = attributeValue;
						}
						attributeValue = xmlParser.getAttributeValue(null, "tag");
						if(attributeValue != null) {
							bus.routeTag = attributeValue;
						}
						busInfo.add(bus);
					}
				}					
				eventType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	
	public ArrayList<Transport> getBusInfo() {
		return busInfo;
	}

}
