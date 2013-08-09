package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.support.mbtalocpro.Bus;

public class SubwayDirectionListFeed {
	
String tagText;
	
	ArrayList<Bus> busInfo;
	//constructor 
	public SubwayDirectionListFeed(InputStream xmlString) {
		busInfo = new ArrayList<Bus>();		
		try {
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(xmlString, null);
			
			
			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}		
	}

}
