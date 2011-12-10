/**
 * Adapted from code from Joe LaPenna's Foursquare app.
 * 
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

/**
 * 
 */

package com.geozen.smarttrail.util;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.JSONException;

import android.content.Context;
import android.widget.Toast;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;

/**
 * @author Matt Tucker (matt@geozen.com)
 */
public class NotificationsUtil {

	public static void ToastReasonForFailure(Context context, Exception e) {

		if (e instanceof SocketTimeoutException) {
			Toast.makeText(context,
					"Server is over capacity, server request timed out!",
					Toast.LENGTH_LONG).show();

		} else if (e instanceof SocketException) {
			Toast.makeText(context,
					"Server not responding : " + e.getMessage(),
					Toast.LENGTH_LONG).show();

		} else if (e instanceof IOException) {
			Toast.makeText(context,
					"Server not responding : " + e.getMessage(),
					Toast.LENGTH_LONG).show();

		} else if (e instanceof CredentialsException) {
			Toast.makeText(context, "Authorization failed.", Toast.LENGTH_LONG)
					.show();

		} else if (e instanceof SmartTrailException) {
			// GeoZenError is one of these
			String message;
			int toastLength = Toast.LENGTH_LONG;
			if (e.getMessage() == null) {
				message = "Invalid Request";
			} else {
				String extra = ((SmartTrailException) e).getExtra();
				if (extra == null) {
					message = e.getMessage();
				} else {
					message = extra;
				}
				toastLength = Toast.LENGTH_LONG;
			}
			Toast.makeText(context, message, toastLength).show();
		} else if (e instanceof JSONException) {
			Toast.makeText(context, "Error: "+ e.getMessage(), Toast.LENGTH_LONG)
					.show();

		} else {
			Toast.makeText(context, "Error: " + e.getMessage(),
					Toast.LENGTH_LONG).show();

		}
	}
}
