package com.geozen.smarttrail.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.maps.GeoPoint;

public class DatabaseUtil {
		
	public static boolean exists(ContentResolver provider, Uri contentUri, long id) {
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		Cursor cur = provider.query(uri, null, null, null, null);
		boolean isExist = false;
		if (cur != null) {
			
			if (cur.getCount() > 0) {
				
				isExist = true;
			}
			cur.close();
		}
		return isExist;
	}
	
	public static int update(ContentProvider provider, Uri contentUri, long id,
			ContentValues values) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		return provider.update(uri, values, null, null);
	}
	public static int update(ContentResolver provider, Uri contentUri, long id,
			ContentValues values) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		return provider.update(uri, values, null, null);
	}
	
	//
	// Long
	//
	public static long getLong(ContentProvider provider, Uri uri, String key) {
		long value = -1L;

		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			value = getLong(cur, key);
			cur.close();
		}
		return value;
	}

	public static long getLong(ContentProvider provider, Uri contentUri,
			long id, String key) {
		return getLong(provider, ContentUris.withAppendedId(contentUri, id),
				key);
	}

	public static long getLong(ContentResolver resolver, Uri uri, String key) {
		long value = -1L;

		Cursor cur = resolver.query(uri, null, null, null, null);
		if (cur != null) {
			value = getLong(cur, key);
			cur.close();
		}
		return value;
	}

	public static long getLong(ContentResolver resolver, Uri contentUri,
			long id, String key) {
		return getLong(resolver, ContentUris.withAppendedId(contentUri, id),
				key);
	}

	public static long getLong(Cursor cur, String key) {
		if (cur.moveToNext())
			return cur.getLong(cur.getColumnIndex(key));
		else
			return -1L;
	}

	public static int update(ContentProvider provider, Uri contentUri, long id,
			String key, long value) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		ContentValues values = new ContentValues();

		values.put(key, value);
		return provider.update(uri, values, null, null);
	}
	
	public static int update(ContentResolver provider, Uri contentUri, long id,
			String key, long value) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		ContentValues values = new ContentValues();
		
		values.put(key, value);
		return provider.update(uri, values, null, null);
	}

	//
	// Int
	//
	public static int getInt(ContentProvider provider, Uri uri, String key) {
		int value = -1;

		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			value = getInt(cur, key);
			cur.close();
		}
		return value;
	}

	public static int getInt(ContentProvider provider, Uri contentUri, long id,
			String key) {
		return getInt(provider, ContentUris.withAppendedId(contentUri, id), key);
	}

	public static int getInt(ContentResolver resolver, Uri uri, String key) {
		int value = -1;

		Cursor cur = resolver.query(uri, null, null, null, null);
		if (cur != null) {
			value = getInt(cur, key);
			cur.close();
		}
		return value;
	}

	public static int getInt(ContentResolver resolver, Uri contentUri, long id,
			String key) {
		return getInt(resolver, ContentUris.withAppendedId(contentUri, id), key);
	}

	public static int getInt(Cursor cur, String key) {
		if (cur.moveToNext())
			return cur.getInt(cur.getColumnIndex(key));
		else
			return -1;
	}

	public static int update(ContentProvider provider, Uri contentUri, long id,
			String key, int value) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		ContentValues values = new ContentValues();

		values.put(key, value);
		return provider.update(uri, values, null, null);
	}
	public static int update(ContentResolver provider, Uri contentUri, long id,
			String key, int value) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		ContentValues values = new ContentValues();
		
		values.put(key, value);
		return provider.update(uri, values, null, null);
	}

	//
	// GeoPoint
	//
	public static GeoPoint getPoint(ContentProvider provider, Uri uri,
			String latKeyE6, String longKeyE6) {
		int latE6 = 0;
		int longE6 = 0;

		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			if (cur.moveToNext()) {
				latE6 = cur.getInt(cur.getColumnIndex(latKeyE6));
				longE6 = cur.getInt(cur.getColumnIndex(longKeyE6));
			}
			cur.close();
		}
		return new GeoPoint(latE6, longE6);
	}

	public static GeoPoint getPoint(ContentProvider provider, Uri contentUri,
			long id, String latKeyE6, String longKeyE6) {
		return getPoint(provider, ContentUris.withAppendedId(contentUri, id),
				latKeyE6, longKeyE6);
	}

	public static int updatePoint(ContentProvider provider, Uri contentUri,
			long id, String latKeyE6, String longKeyE6, GeoPoint point) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		ContentValues values = new ContentValues();

		values.put(latKeyE6, point.getLatitudeE6());
		values.put(longKeyE6, point.getLongitudeE6());
		return provider.update(uri, values, null, null);
	}
	public static GeoPoint getPoint(ContentResolver provider, Uri uri,
			String latKeyE6, String longKeyE6) {
		int latE6 = 0;
		int longE6 = 0;
		
		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			if (cur.moveToNext()) {
				latE6 = cur.getInt(cur.getColumnIndex(latKeyE6));
				longE6 = cur.getInt(cur.getColumnIndex(longKeyE6));
			}
			cur.close();
		}
		return new GeoPoint(latE6, longE6);
	}
	
	public static GeoPoint getPoint(ContentResolver provider, Uri contentUri,
			long id, String latKeyE6, String longKeyE6) {
		return getPoint(provider, ContentUris.withAppendedId(contentUri, id),
				latKeyE6, longKeyE6);
	}
	
	public static int updatePoint(ContentResolver provider, Uri contentUri,
			long id, String latKeyE6, String longKeyE6, GeoPoint point) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		ContentValues values = new ContentValues();
		
		values.put(latKeyE6, point.getLatitudeE6());
		values.put(longKeyE6, point.getLongitudeE6());
		return provider.update(uri, values, null, null);
	}

	//
	// String
	//
	public static String getString(ContentProvider provider, Uri uri, String key) {
		String value = "";
		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			value = getString(cur, key);
			cur.close();
		}
		return value;
	}

	public static String getString(ContentProvider provider, Uri contentUri,
			long id, String key) {
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		return getString(provider, uri, key);
	}

	public static String getString(ContentResolver resolver, Uri uri, String key) {
		String value = "";
		Cursor cur = resolver.query(uri, null, null, null, null);
		if (cur != null) {
			value = getString(cur, key);
			cur.close();
		}
		return value;
	}

	public static String getString(ContentResolver resolver, Uri contentUri,
			long id, String key) {
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		return getString(resolver, uri, key);
	}

	public static String getString(Cursor cur, String key) {
		if (cur.moveToNext())
			return cur.getString(cur.getColumnIndex(key));
		else
			return "";
	}

	public static int update(ContentProvider provider, Uri contentUri, long id,
			String key, String value) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		ContentValues values = new ContentValues();

		values.put(key, value);
		return provider.update(uri, values, null, null);
	}
	public static int update(ContentResolver provider, Uri contentUri, long id,
			String key, String value) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		ContentValues values = new ContentValues();
		
		values.put(key, value);
		return provider.update(uri, values, null, null);
	}

	//
	// Boolean
	//
	public static Boolean getBoolean(ContentProvider provider, Uri uri,
			String key) {
		Boolean value = false;

		Cursor cur = provider.query(uri, null, null, null, null);
		if (cur != null) {
			value = getBoolean(cur, key);
			cur.close();
		}
		return value;
	}

	public static boolean getBoolean(ContentProvider provider, Uri contentUri,
			long id, String key) {
		return getBoolean(provider, ContentUris.withAppendedId(contentUri, id),
				key);
	}

	public static boolean getBoolean(ContentResolver resolver, Uri uri,
			String key) {
		Boolean value = false;

		Cursor cur = resolver.query(uri, null, null, null, null);
		if (cur != null) {
			value = getBoolean(cur, key);
			cur.close();
		}
		return value;
	}

	public static boolean getBoolean(ContentResolver resolver, Uri contentUri,
			long id, String key) {
		return getBoolean(resolver, ContentUris.withAppendedId(contentUri, id),
				key);
	}

	public static Boolean getBoolean(Cursor cur, String key) {
		if (cur.moveToNext())
			return (cur.getInt(cur.getColumnIndex(key)) == 1 ? true : false);
		else
			return false;
	}

	public static int update(ContentProvider provider, Uri contentUri, long id,
			String key, Boolean value) {

		Uri uri = ContentUris.withAppendedId(contentUri, id);

		ContentValues values = new ContentValues();

		values.put(key, value ? 1 : 0);
		return provider.update(uri, values, null, null);
	}
	
	public static int update(ContentResolver provider, Uri contentUri, long id,
			String key, Boolean value) {
		
		Uri uri = ContentUris.withAppendedId(contentUri, id);
		
		ContentValues values = new ContentValues();
		
		values.put(key, value ? 1 : 0);
		return provider.update(uri, values, null, null);
	}

}
