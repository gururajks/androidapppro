package com.support.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class RealTimeMbtaDirectionsListParser {

	public Route getDirectionsList(InputStream is) {
		Route route = new Route();
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(is, null);
			xmlParser.nextTag();
			while(xmlParser.next() != XmlPullParser.END_TAG) {
				if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
				String tagName = xmlParser.getName();				
				if(tagName.equalsIgnoreCase("direction")) {
					route.directionList.add(readDirectionFeed(xmlParser));
				}					
			}
			return route;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}

	private Direction readDirectionFeed(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		Direction direction = new Direction();
		direction.directionTitle = xmlParser.getAttributeValue(null, "direction_name");
		direction.directionTag = xmlParser.getAttributeValue(null, "direction_id");
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("stop")) {				
				Stop stop = new Stop();
				stop.stopId = xmlParser.getAttributeValue(null, "stop_id");
				stop.stopTitle = xmlParser.getAttributeValue(null, "stop_name");
				stop.stopLocation.lat = Double.valueOf(xmlParser.getAttributeValue(null, "stop_lat"));
				stop.stopLocation.lng = Double.valueOf(xmlParser.getAttributeValue(null, "stop_lon"));				
				direction.stopList.add(stop);				
			}	
			xmlParser.next();
		}	
		return direction;
	}
	
	
	
	
	
}
