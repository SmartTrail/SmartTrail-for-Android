/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.util;



import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GeoUtil {
	
	static double DEG_PER_MIN = 1.0 / 60.0;
	static double MIN_PER_SEC = 1.0 / 60.0;
	static double DEG_PER_SEC = MIN_PER_SEC * DEG_PER_MIN;
	static final double EARTH_RADIUS_M = 6371010;
	private static final String LOGTAG = "GeoUtil";

	public static int toDegreesE6(double deg, double min, double sec) {
		if (deg > 0) {
			return (int) ((deg + min * DEG_PER_MIN + sec * DEG_PER_SEC) * 1e6);
		} else {
			return (int) ((deg - min * DEG_PER_MIN - sec * DEG_PER_SEC) * 1e6);
		}
	}

	public static GeoPoint parseCoordinate(String coordStr) {
		String[] coord = coordStr.split(",");
		int longE6 = (int) (Float.parseFloat(coord[0]) * 1e6f);
		int latE6 = (int) (Float.parseFloat(coord[1]) * 1e6f);

		return new GeoPoint(latE6, longE6);
	}

	public static GeoPoint parseCoordinateE6(String coordStr) {
		String[] coord = coordStr.split(",");
		int longE6 = Integer.parseInt(coord[0].trim());
		int latE6 = Integer.parseInt(coord[1].trim());

		return new GeoPoint(latE6, longE6);
	}

	static ArrayList<GeoPoint> parseCoordinates(String pointsStr) {
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

		String[] points = pointsStr.split(" ");
		for (String aPoint : points) {
			geoPoints.add(parseCoordinate(aPoint));
		}
		return geoPoints;
	}

	public static float getLatitude(GeoPoint point) {
		return ((float) point.getLatitudeE6()) / 1e6f;
	}

	public static float getLongitude(GeoPoint point) {
		return ((float) point.getLongitudeE6()) / 1e6f;
	}

	/**
	 * Distance to destination in meters.
	 * 
	 * Uses Vincenty formula from:
	 * http://en.wikipedia.org/wiki/Great-circle_distance
	 * 
	 * @param dest
	 * @return distance in meters.
	 */
	public static int distanceTo(GeoPoint start, GeoPoint finish) {

		double phi_s = Math.toRadians(start.getLatitudeE6() / 1E6);
		double lambda_s = Math.toRadians(start.getLongitudeE6() / 1E6);
		double phi_f = Math.toRadians(finish.getLatitudeE6() / 1E6);
		double lambda_f = Math.toRadians(finish.getLongitudeE6() / 1E6);

		double deltaLambda = lambda_f - lambda_s;

		double cosPhi_f = Math.cos(phi_f);
		double sinPhi_f = Math.sin(phi_f);
		double cosPhi_s = Math.cos(phi_s);
		double sinPhi_s = Math.sin(phi_s);
		double cosDeltaLambda = Math.cos(deltaLambda);
		double sinDeltaLambda = Math.sin(deltaLambda);

		double num = Math.sqrt(Math.pow(cosPhi_f * sinDeltaLambda, 2.0)
				+ Math.pow(cosPhi_s * sinPhi_f - sinPhi_s * cosPhi_f
						* cosDeltaLambda, 2.0));
		double den = sinPhi_s * sinPhi_f + cosPhi_s * cosPhi_f * cosDeltaLambda;

		double sigma = Math.atan(num / den);

		double ddist = sigma * EARTH_RADIUS_M;
		int dist = (int) ddist;
		return dist;

	}
	/**
	 * New point given starting point, bearing, and distance.
	 * 
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * Assumes perfect sphere.
	 * 
	 * @param dest
	 * @return distance in meters.
	 */
	public static GeoPoint destinationPoint(GeoPoint start, double bearing, double distance) {
		double lat1 = Math.toRadians(start.getLatitudeE6() / 1E6);
		double lon1 = Math.toRadians(start.getLongitudeE6() / 1E6);
		
		double rRatio = distance/EARTH_RADIUS_M;
		double sinRRatio = Math.sin(rRatio);
		double cosRRatio = Math.cos(rRatio);
		double sinLat1 = Math.sin(lat1);
		double cosLat1 = Math.cos(lat1);
		double lat2 = Math.asin(sinLat1*cosRRatio + cosLat1*sinRRatio*Math.cos(bearing));
		
		double lon2 = lon1 + Math.atan2(Math.sin(bearing)*sinRRatio*cosLat1, cosRRatio-sinLat1*Math.sin(lat2));
		int lat2E6 = (int) (Math.toDegrees(lat2)*1E6d);
		int lon2E6 = (int) (Math.toDegrees(lon2)*1E6d);
		return new GeoPoint(lat2E6, lon2E6);
		
	}

	public static boolean isNear(GeoPoint start, GeoPoint finish, int radius) {
		return (distanceTo(start, finish) < radius);
	}

	public static GeoPoint locationToGeoPoint(Location loc) {
		if (loc != null) {
			return new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc
					.getLongitude() * 1E6));
		} else {
			return null;
		}
	}

	public static String toCoordinatesStringE6(GeoPoint center) {
		StringBuilder sb = new StringBuilder();
		if (center == null) {
			sb.append("null,null");
		} else {
			sb.append(center.getLongitudeE6());
			sb.append(",");
			sb.append(center.getLatitudeE6());
		}
		return sb.toString();
	}

	public static Location geoPointToLocation(GeoPoint point, String provider) {
		if (point != null) {
			Location loc = new Location(provider);
			loc.setLatitude(point.getLatitudeE6() / 1e6f);
			loc.setLongitude(point.getLongitudeE6() / 1e6f);
			return loc;
		} else {
			return null;
		}
	}

	public static Drawable getIconDrawable(Context context, String iconName) {

		Drawable drawable = null;
		int idx = iconName.indexOf(":");

		if (idx != -1) {
			String app = iconName.substring(0, idx);

			try {
				Resources resources = context.getPackageManager()
						.getResourcesForApplication(app);
				int id = resources.getIdentifier(iconName, null, null);
				if (id != 0)
					drawable = resources.getDrawable(id);
			} catch (NameNotFoundException e) {

				Log.e(LOGTAG, e.getMessage());
			}
		} else {

		}

		return drawable;

	}

	public static String getIconPackage(Context context, String iconName) {

		int idx = iconName.indexOf(":");

		if (idx != -1) {
			return iconName.substring(0, idx);

		} else {
			return null;
		}

	}

	
	public static String toCoordinatesString(GeoPoint point) {
		float latitude = point.getLatitudeE6() / 1e6f;
		float longitude = point.getLongitudeE6() / 1e6f;
		
		return String.format("%f, %f", latitude, longitude);
	}

	public static float e6ToFloat(int geoE6) {	
		return geoE6/1e6f;
	}
	public static int floatToE6(float coordinate) {	
		return (int) (coordinate*1e6f);
	}

	public static int doubleToE6(double coordinate) {
		return (int) (coordinate*1e6d);
	}

	public static double e6ToDouble(int e6) {
		return e6/1e6d;
	}

	public static JSONObject geoPointToJson(GeoPoint geoPoint,String latTag, String lonTag) throws JSONException {
		JSONObject point = new JSONObject();
		point.put(latTag, e6ToDouble(geoPoint.getLatitudeE6()) );
		point.put(lonTag, e6ToDouble(geoPoint.getLongitudeE6()) );
		return point;
	}

	public static GeoPoint jsonToGeoPoint(JSONObject json, String latTag, String lonTag) throws JSONException {
		
		int latE6 = doubleToE6(json.getDouble(latTag));
		int lonE6 = doubleToE6(json.getDouble(lonTag));
		
		return new GeoPoint(latE6, lonE6);
	}

}
