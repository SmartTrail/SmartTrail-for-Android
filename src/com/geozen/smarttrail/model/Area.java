/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;

/**
 * Area represents a trail area. There can be many trails in an area and many
 * areas in a region.
 * 
 * @author matt
 * 
 */
public class Area implements AreasColumns, BaseColumns {

	public String mId;  // MongoId string of the area.
	public String mRegionId; // MongoId string of the region the area is in.
	public String mName;
	public String mOwner;
	public String mDescription;
	public String mColor;
	public int mNumReviews;
	public float mRating;
	public long mUpdatedAt;

	public Area() {

	}

	public Area(JSONObject area) throws JSONException {
		//
		// Required
		//
		mId = area.getString(AREA_ID);
		mRegionId = area.getString(REGION_ID);
		mName = area.getString(NAME);
		mOwner = area.getString(OWNER);
		mDescription = area.getString(DESCRIPTION);
		mColor = area.getString(COLOR);
		mNumReviews = area.getInt(NUM_REVIEWS);
		mRating = (float) area.getDouble(RATING);
		mUpdatedAt = area.getLong(UPDATED_AT);
	}

	public JSONObject toJson() throws JSONException {

		JSONObject area = new JSONObject();

		area.put(AREA_ID, mId);
		area.put(REGION_ID, mRegionId);
		area.put(NAME, mName);
		area.put(OWNER, mOwner);
		area.put(DESCRIPTION, mDescription);
		area.put(COLOR, mColor);
		area.put(NUM_REVIEWS, mNumReviews);
		area.put(RATING, (double) mRating);

		return area;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(AREA_ID, mId);
		values.put(REGION_ID, mRegionId);
		values.put(NAME, mName);
		values.put(OWNER, mOwner);
		values.put(DESCRIPTION, mDescription);
		values.put(COLOR, mColor);
		values.put(NUM_REVIEWS, mNumReviews);
		values.put(RATING, mRating);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = AreasSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return AreasSchema.update(provider, mId, getValues());
	}

	public String upsert(ContentResolver provider) {

		Cursor cursor = provider.query(AreasSchema.CONTENT_URI,
				new String[] { AreasColumns.AREA_ID }, AreasColumns.AREA_ID
						+ "='" + mId + "'", null, null);

		if (cursor == null || cursor.getCount() == 0) {
			return insert(provider); // database replaces on conflict
		} else {
			update(provider);
			return mId;
		}
	}

}
