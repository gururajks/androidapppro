package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.support.mbtalocpro.Direction;
import com.support.mbtalocpro.Path;
import com.support.mbtalocpro.Point;
import com.support.mbtalocpro.Route;
import com.support.mbtalocpro.Stop;


public class RouteParser {
	
	String tagText;
	int pathFlag = 0;
	
	
	/*
	 * Gets the route object with all the info 
	 * input - stream object
	 */
	public Route getRoute(InputStream xmlString) throws IOException {		
		try {
			Route route = null;
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(xmlString, null);
			xmlParser.nextTag();
			while(xmlParser.next() != XmlPullParser.END_TAG) {
				if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
				String tagName = xmlParser.getName();
				if(tagName.equalsIgnoreCase("route")) {
					route = readRouteFeed(xmlParser);
				}			
				xmlParser.next();
			}		
			return route;			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} 				
		return null;
	} 
	
	
	//Read the route feed
	private Route readRouteFeed(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		Route busRoute = new Route();
		busRoute.routeTag = xmlParser.getAttributeValue(null, "tag");		
		busRoute.routeTitle = xmlParser.getAttributeValue(null, "title");		
		busRoute.routeLatMin = Double.valueOf(xmlParser.getAttributeValue(null, "latMin"));		
		busRoute.routeLatMax = Double.valueOf(xmlParser.getAttributeValue(null, "latMax"));		
		busRoute.routeLngMin = Double.valueOf(xmlParser.getAttributeValue(null, "lonMin"));		
		busRoute.routeLngMax = Double.valueOf(xmlParser.getAttributeValue(null, "lonMax"));		
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("stop")) {
				if(xmlParser.getAttributeValue(null, "title") != null) {
					Stop stop = new Stop();
					stop.stopTag = xmlParser.getAttributeValue(null, "tag");
					stop.stopTitle = xmlParser.getAttributeValue(null, "title");			
					stop.stopLocation.lat = Double.valueOf(xmlParser.getAttributeValue(null, "lat"));
					stop.stopLocation.lng = Double.valueOf(xmlParser.getAttributeValue(null, "lon"));
					stop.stopId = xmlParser.getAttributeValue(null, "stopId");
					busRoute.stopList.put(stop.stopTag, stop);					
				}
			}
			if(tagName.equalsIgnoreCase("direction")) {
				Direction direction = readDirection(xmlParser);
				busRoute.directionList.add(direction);
			}
			if(tagName.equalsIgnoreCase("path")) {
				Path path = readPath(xmlParser);
				busRoute.routePath.add(path);
			}
			xmlParser.next();
		}
		
		return busRoute; 
	}
	
	/*
	 * Reads the list of directions and returns a direction object
	 * Direction Object has a list of stop tags. 
	 * Stops can vary depending on the direction
	 */
	private Direction readDirection(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		Direction direction = new Direction();
		direction.directionTag = xmlParser.getAttributeValue(null, "tag");		
		direction.directionTitle = xmlParser.getAttributeValue(null, "title");
		direction.directionName = xmlParser.getAttributeValue(null, "name");
		direction.useForUI = Boolean.valueOf(xmlParser.getAttributeValue(null, "useForUI"));
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("stop")) {
				Stop stop = new Stop();
				stop.stopTag = xmlParser.getAttributeValue(null, "tag");
				direction.stopList.add(stop);
			}
			xmlParser.next();
		}		
		return direction;
	}
	
	
	/*
	 * Reads the path tags and returns a single path object
	 * Path object has a list of points
	 */
	private Path readPath(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		Path path = new Path();
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("point")) {
				Point point = new Point();
				point.lat = Double.valueOf(xmlParser.getAttributeValue(null, "lat"));
				point.lng = Double.valueOf(xmlParser.getAttributeValue(null, "lon"));
				path.routePoints.add(point);
			}
			xmlParser.next();
		}
		
		return path;
	}

}
