/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.util;

import android.util.Log;

public class AppLog {

	static final String TAG = "SmartTrail";

	public static void d(String msg) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, msg);
		}
	}

	public static void d(String msg, Throwable e) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, msg, e);
		}
	}

	public static void d(String classTag, String msg) {
		d(classTag + ": " + msg);
	}

	public static void d(String classTag, String msg, Throwable e) {
		d(classTag + ": " + msg, e);
	}

	public static void i(String msg) {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, msg);
		}
	}

	public static void i(String classTag, String msg) {
		i(classTag + ": " + msg);
	}

	public static void e(String msg, Throwable ex) {
		if (Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, msg, ex);
		}
	}
	public static void e(String msg) {
		if (Log.isLoggable(TAG, Log.ERROR)) {
			Log.e(TAG, msg);
		}
	}

	public static void e(String classTag, String msg, Throwable ex) {
		e(classTag + ": " + msg, ex);
	}
	public static void e(String classTag, String msg) {
		e(classTag + ": " + msg);
	}

	public static void v(String msg) {
		if (Log.isLoggable(TAG, Log.VERBOSE)) {
			Log.v(TAG, msg);
		}
	}

	public static void v(String classTag, String msg) {
		v(classTag + ": " + msg);
	}

	public static void w(String msg) {
		if (Log.isLoggable(TAG, Log.WARN)) {
			Log.w(TAG, msg);
		}
	}

	public static void w(String classTag, String msg) {
		w(classTag + ": " + msg);
	}
}
