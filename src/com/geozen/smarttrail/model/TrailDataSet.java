/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.model;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.geozen.smarttrail.io.GpxSaxHandler;
import com.geozen.smarttrail.io.TrailSaxHandler;
import com.geozen.smarttrail.util.GeoUtil;
import com.google.android.maps.GeoPoint;

/**
 * Holds trail map data for displaying on the map.
 * 
 * @author matt
 *
 */
public class TrailDataSet {

	public GeoPoint[] mPoints;
	public float[] mDistance;

	/**
	 * Retrieve trail data set from an input source
	 * 
	 * @param url
	 * @return navigation set
	 */
	public static TrailDataSet parseTrailDataSet(InputSource is)
			throws ParserConfigurationException, SAXException, IOException {

		TrailDataSet trailDataSet = null;

		// Get a SAXParser from the SAXPArserFactory.
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		// Get the XMLReader of the SAXParser we created.
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader
		TrailSaxHandler trailSaxHandler = new TrailSaxHandler();
		xr.setContentHandler(trailSaxHandler);

		// Parse the xml-data from our source
		xr.parse(is);

		// Our TrailSaxHandler now provides the parsed data to us.
		trailDataSet = trailSaxHandler.mTrailDataSet;

		return trailDataSet;
	}

	/**
	 * Retrieve trail data set from an input source
	 * 
	 * @param url
	 * @return navigation set
	 */
	public static TrailDataSet parseGpxDataSet(InputSource is)
			throws ParserConfigurationException, SAXException, IOException {

		TrailDataSet trailDataSet = null;

		// Get a SAXParser from the SAXPArserFactory.
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		// Get the XMLReader of the SAXParser we created.
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader
		GpxSaxHandler trailSaxHandler = new GpxSaxHandler();
		xr.setContentHandler(trailSaxHandler);

		// Parse the xml-data from our source
		xr.parse(is);

		// Our TrailSaxHandler now provides the parsed data to us.
		trailDataSet = trailSaxHandler.mTrailDataSet;

		return trailDataSet;
	}

	public GeoPoint getCenter() {
		int n = mPoints.length;
		int mid = n / 2;
		return mPoints[mid];
	}

	public MinDistance getMinDistance(GeoPoint p0) {

		int N = mDistance.length;
		float distSq;

		int dx;
		int dy;
		float minDistSq = 0;
		int minIndex = -1;

		for (int i = 0; i < N; i++) {
			dx = p0.getLongitudeE6() - mPoints[i].getLongitudeE6();
			dy = p0.getLatitudeE6() - mPoints[i].getLatitudeE6();
			distSq = dx * dx + dy * dy;
			if (distSq < minDistSq || minIndex == -1) {
				minDistSq = distSq;
				minIndex = i;
			}
		}

		float dist = GeoUtil.distanceTo(p0, mPoints[minIndex]);
		MinDistance min = new MinDistance(minIndex, dist);

		return min;
	}

	public class MinDistance {
		public int mIndex;
		public float mDistance;

		public MinDistance(int minIndex, float dist) {
			mIndex = minIndex;
			mDistance = dist;
		}

	}

	public GeoPoint getStart() {
		return mPoints[0];
	}
}
