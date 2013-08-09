package com.transport.mbtalocpro;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.support.mbtalocpro.DirectionPrediction;
import com.support.mbtalocpro.Prediction;
import com.support.mbtalocpro.RoutePrediction;

public class PredictionsParser {
	
	
	
	/*
	 * Gets the predictions object with all the info and makes a List 
	 * input - stream object
	 */
	public ArrayList<RoutePrediction> getPredictions(InputStream xmlString) throws IOException {		
		try {
			RoutePrediction routePredictions = null;
			ArrayList<RoutePrediction> routePredictionsList = new ArrayList<RoutePrediction>();
			XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			XmlPullParser xmlParser = parserFactory.newPullParser();			
			xmlParser.setInput(xmlString, null);
			xmlParser.nextTag();
			while(xmlParser.next() != XmlPullParser.END_TAG) {
				if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
				String tagName = xmlParser.getName();
				if(tagName.equalsIgnoreCase("predictions")) {
					routePredictions = readPredictionsFeed(xmlParser);
					routePredictionsList.add(routePredictions);
				}							
				xmlParser.next();
			}		
			return routePredictionsList;			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} 				
		return null;
	} 
	
	/*
	 * Reads the predictions for a certain route which is in the form of RoutePrediction Object 
	 * RoutePrediction object has a list of DirectionPrediction Objects, which is the list of directions for the route
	 */
	private RoutePrediction readPredictionsFeed(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		RoutePrediction routePredictions = new RoutePrediction();
		routePredictions.routeTag = xmlParser.getAttributeValue(null, "routeTag");
		routePredictions.routeTitle = xmlParser.getAttributeValue(null, "routeTitle");
		routePredictions.stopTag = xmlParser.getAttributeValue(null, "stopTag");
		routePredictions.stopTitle = xmlParser.getAttributeValue(null, "stopTitle");
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("direction")) {				
				DirectionPrediction dirPredict = readDirectionPrediction(xmlParser);
				routePredictions.dirForPredictions.add(dirPredict);
			}
			xmlParser.next();
		}
		
		return routePredictions;
	}
	
	/*
	 * Reads the direction tag to get the direction of the route
	 * DirectionPrediction Object has a list of predictions that has the time of arrival for the buses	
	 */
	private DirectionPrediction readDirectionPrediction(XmlPullParser xmlParser) throws XmlPullParserException, IOException {
		DirectionPrediction dirPredict = new DirectionPrediction();
		dirPredict.directionTitle = xmlParser.getAttributeValue(null, "title");
		while(xmlParser.next() != XmlPullParser.END_TAG) {
			if(xmlParser.getEventType() != XmlPullParser.START_TAG) continue;
			String tagName = xmlParser.getName();
			if(tagName.equalsIgnoreCase("prediction")) {				
				Prediction prediction = readPrediction(xmlParser);
				dirPredict.predictionList.add(prediction);
			}
			xmlParser.next();
		}		
		return dirPredict;
	}
	
	/*
	 * Prediction Object has the time of arrival, epoch time, direction etc... 
	 */
	private Prediction readPrediction(XmlPullParser xmlParser) {
		Prediction prediction = new Prediction();
		prediction.epochTime = Long.valueOf(xmlParser.getAttributeValue(null, "epochTime"));
		prediction.minutes = Integer.valueOf(xmlParser.getAttributeValue(null, "minutes"));
		prediction.seconds = Integer.valueOf(xmlParser.getAttributeValue(null, "seconds"));
		prediction.directionTag = xmlParser.getAttributeValue(null, "dirTag");
		prediction.vehicleId = xmlParser.getAttributeValue(null, "vehicle");	
		return prediction;
	} 
	

}
