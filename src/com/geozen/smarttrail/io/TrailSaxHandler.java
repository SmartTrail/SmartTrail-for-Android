/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.geozen.smarttrail.model.TrailDataSet;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.GeoUtil;
import com.google.android.maps.GeoPoint;

public class TrailSaxHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================

	@SuppressWarnings("unused")
	private boolean in_kmltag = false;
	@SuppressWarnings("unused")
	private boolean in_placemarktag = false;
	private boolean in_nametag = false;
	private boolean in_descriptiontag = false;
	@SuppressWarnings("unused")
	private boolean in_geometrycollectiontag = false;
	@SuppressWarnings("unused")
	private boolean in_pointtag = false;
	private boolean in_coordinatestag = false;
	
	private boolean mInGpxCoordinateTag;
	@SuppressWarnings("unused")
	private boolean mInGpxTrackTag;

	public TrailDataSet mTrailDataSet;
	
	private StringBuffer mBuffer;

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		mTrailDataSet = new TrailDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (localName.equals("kml")) {
			this.in_kmltag = true;
		} else if (localName.equals("Placemark")) {
			this.in_placemarktag = true;
		} else if (localName.equals("name")) {
			this.in_nametag = true;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = true;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = true;
		} else if (localName.equals("LineString")) {
		} else if (localName.equals("point")) {
			this.in_pointtag = true;
		} else if (localName.equals("coordinates")) {
			mBuffer = new StringBuffer(1024);
			this.in_coordinatestag = true;
		} else if (qName.equals("gx:coord")) {
			this.mInGpxCoordinateTag = true;
		} else if (qName.equals("gx:Track")) {
			mBuffer = new StringBuffer(1024);
			this.mInGpxTrackTag = true;
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("kml")) {
			this.in_kmltag = false;

		} else if (localName.equals("Placemark")) {

			this.in_placemarktag = false;
			String coordinates;
			if (mBuffer != null) {
				coordinates = mBuffer.toString().trim();
			} else {
				coordinates = null;
			}
			mTrailDataSet.mPoints = pathToPoints(coordinates);

		} else if (localName.equals("name")) {
			this.in_nametag = false;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = false;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = false;
		} else if (localName.equals("LineString")) {
		} else if (localName.equals("point")) {
			this.in_pointtag = false;
		} else if (localName.equals("coordinates")) {
			this.in_coordinatestag = false;
		} else if (qName.equals("gx:coord")) {
			this.mInGpxCoordinateTag = false;
		} else if (qName.equals("gx:Track")) {
			this.mInGpxTrackTag = false;
		}
	}

	private GeoPoint[] pathToPoints(String path) {
		// coordinates are in the form of
		// lat1,long1,elevation1 lat2,long2,elevation2 ...

		GeoPoint[] points;
		GeoPoint point;

		if (path != null) {
			path = path.trim();

			String[] coords = path.trim().split(" ");
			points = new GeoPoint[coords.length];
			String[] lngLatElev;
			double lon;
			double lat;
			try {
				for (int i = 0; i < coords.length; i++) {

					lngLatElev = coords[i].split(",");
					if (lngLatElev.length == 3) {
					lon = Double.parseDouble(lngLatElev[0]);
					lat = Double.parseDouble(lngLatElev[1]);

					point = new GeoPoint(GeoUtil.doubleToE6(lat),
							GeoUtil.doubleToE6(lon));
					points[i] = point;
					} else {
						// weird parse.
						AppLog.w("Bad kml parse at index="+i+", data="+coords[i]);
					}

				}
			} catch (Exception ex) {
				// corrupt file?
				points = new GeoPoint[0];
			}
		} else {
			points = new GeoPoint[0];
		}
		return points;
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.in_nametag) {

		//	mTrailDataSet.mTrailName = new String(ch, start, length);

		} else if (this.in_descriptiontag) {
			//mTrailDataSet.mDescription = new String(ch, start, length);

		} else if (this.in_coordinatestag) {
			if (length == 1) return;
			String str = new String(ch, start, length);
			str = str.trim() + " ";
			mBuffer.append(str);

		} else if (this.mInGpxCoordinateTag) {
			// convert from gpx format to google earth format
			String str = new String(ch, start, length);
			str = str.replace(" ", ",") + " ";
			mBuffer.append(str);
		}
	}
}