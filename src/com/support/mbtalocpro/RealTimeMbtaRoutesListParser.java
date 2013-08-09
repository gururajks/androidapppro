package com.support.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/*
XML FORMAT
<route_list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	<mode route_type="0" mode_name="Subway">
		<route route_id="810_" route_name="Green Line"/>
		<route route_id="812_" route_name="Green Line"/>
		<route route_id="822_" route_name="Green Line"/>
		<route route_id="830_" route_name="Green Line"/>
		<route route_id="831_" route_name="Green Line"/>
		<route route_id="840_" route_name="Green Line"/>
		<route route_id="842_" route_name="Green Line"/>
		<route route_id="852_" route_name="Green Line"/>
		<route route_id="880_" route_name="Green Line"/>
		<route route_id="882_" route_name="Green Line"/>
		<route route_id="899_" route_name="Mattapan High-Speed Line"/>
	</mode>
</route>
*/

public class RealTimeMbtaRoutesListParser {
	
	//Gets the routes based on the lines
	public TransportModes getRoutesList(InputStream is) throws IOException {
		try {
			TransportModes modesList = new TransportModes();
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(is, null);
			xmlParser.nextTag();
			while(xmlParser.next() != XmlPullParser.END_TAG) {
				if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
				String tagName = xmlParser.getName();				
				if(tagName.equalsIgnoreCase("mode")) {
					modesList.modes.add(readModeFeed(xmlParser));										
				}					
			}		
			return modesList;			
		} catch(XmlPullParserException e) {
			e.printStackTrace();
		} 				
		return null;
	}
	
	//Get the mode route
	private TransportMode readModeFeed(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		TransportMode mode = new TransportMode();
		mode.mode_type = xmlParser.getAttributeValue(null, "route_type");	
		mode.mode_name = xmlParser.getAttributeValue(null, "mode_name");
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("route")) {				
				Route route = new Route();
				route.routeTag = xmlParser.getAttributeValue(null, "route_id");
				route.routeTitle = xmlParser.getAttributeValue(null, "route_name");
				mode.routes.routesList.add(route);				
			}	
			xmlParser.next();
		}	
		return mode;
	}
	


}






