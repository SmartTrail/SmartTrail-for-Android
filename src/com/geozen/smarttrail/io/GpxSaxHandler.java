/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.geozen.smarttrail.model.TrailDataSet;
import com.geozen.smarttrail.util.GeoUtil;
import com.google.android.maps.GeoPoint;

public class GpxSaxHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================

	public TrailDataSet mTrailDataSet;

	private StringBuffer buf = new StringBuffer();
	private double lat;
	private double lon;
	@SuppressWarnings("unused")
	private double ele;
	 private List<GeoPoint> track;

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		mTrailDataSet = new TrailDataSet();
		track = new ArrayList<GeoPoint>();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
		mTrailDataSet.mPoints = track.toArray(new GeoPoint[track.size()]);
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes attributes) throws SAXException {
		buf.setLength(0);
		if (qName.equals("trkpt")) {
			lat = Double.parseDouble(attributes.getValue("lat"));
			lon = Double.parseDouble(attributes.getValue("lon"));
		}

	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		 if (qName.equals("trkpt")) {
	            //track.add(Trackpoint.fromWGS84(lat, lon, ele, time));
			 GeoPoint point = new GeoPoint(GeoUtil.doubleToE6(lat),
						GeoUtil.doubleToE6(lon));
			 track.add(point);
	        } else if (qName.equals("ele")) {
	            ele = Double.parseDouble(buf.toString());
	        } 
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char[] chars, int start, int length) {
		buf.append(chars, start, length);
	}
}