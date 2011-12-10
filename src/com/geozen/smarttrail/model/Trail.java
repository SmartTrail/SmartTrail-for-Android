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
import android.net.Uri;
import android.provider.BaseColumns;

import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.util.GeoUtil;
import com.google.android.maps.GeoPoint;

/**
 * Trail is a specific trail with a trail map. Trails are in trail area and
 * areas are in region.
 * 
 * @author matt
 * 
 */
public class Trail implements BaseColumns, TrailsColumns {

	private static final String HEAD = "head";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	public static final String JSON_STATUS_UPDATED_AT = "updatedAt";

	public String mId;
	public String mAreaId;
	public String mRegionId;
	public String mMapId;
	public String mName;
	public String mOwner;
	public String mUrl;
	public String mDescription;
	public int mTechRating;
	public int mAerobicRating;
	public int mCoolRating;
	public int mLength;
	public int mElevationGain;
	public long mUpdatedAt;

	GeoPoint mHead;

	public Trail() {

	}

	public Trail(JSONObject trail) throws JSONException {
		//
		// Required
		//
		mRegionId = trail.getString(REGION_ID);
		mAreaId = trail.getString(AREA_ID);

		mMapId = trail.getString(MAP_ID);
		if (mMapId.equals("null")) {
			mMapId = null;
		}
		mId = trail.getString(TRAIL_ID);
		mName = trail.getString(NAME);
		mOwner = trail.getString(OWNER);
		mUrl = trail.getString(URL);
		mDescription = trail.getString(DESCRIPTION);
		mUpdatedAt = trail.getLong(UPDATED_AT);
		JSONObject head = trail.getJSONObject(HEAD);
		double lat = head.getDouble(LAT);
		double lon = head.getDouble(LON);

		mHead = new GeoPoint(GeoUtil.doubleToE6(lat), GeoUtil.doubleToE6(lon));
		mLength = trail.getInt(LENGTH);
		mElevationGain = trail.getInt(ELEVATION_GAIN);
		mTechRating = trail.getInt(TECH_RATING);
		mAerobicRating = trail.getInt(AEROBIC_RATING);
		mCoolRating = trail.getInt(COOL_RATING);

	}

	public JSONObject toJson() throws JSONException {

		JSONObject trail = new JSONObject();

		//
		// Trail
		//
		trail.put(TRAIL_ID, mId);
		trail.put(REGION_ID, mRegionId);
		trail.put(AREA_ID, mAreaId);
		trail.put(MAP_ID, mMapId);
		trail.put(NAME, mName);
		trail.put(OWNER, mOwner);
		trail.put(DESCRIPTION, mDescription);
		trail.put(URL, mUrl);
		trail.put(UPDATED_AT, mUpdatedAt);

		JSONObject head = new JSONObject();
		head.put(LAT, GeoUtil.e6ToDouble(mHead.getLatitudeE6()));
		head.put(LON, GeoUtil.e6ToDouble(mHead.getLongitudeE6()));

		trail.put(LENGTH, mLength);
		trail.put(ELEVATION_GAIN, mElevationGain);
		trail.put(TECH_RATING, mTechRating);
		trail.put(AEROBIC_RATING, mAerobicRating);
		trail.put(COOL_RATING, mCoolRating);
		return trail;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(TRAIL_ID, mId);
		values.put(REGION_ID, mRegionId);
		values.put(AREA_ID, mAreaId);
		values.put(MAP_ID, mMapId);
		values.put(NAME, mName);
		values.put(OWNER, mOwner);
		values.put(DESCRIPTION, mDescription);
		values.put(URL, mUrl);
		values.put(HEAD_LAT_E6, mHead.getLatitudeE6());
		values.put(HEAD_LON_E6, mHead.getLongitudeE6());
		values.put(LENGTH, mLength);
		values.put(ELEVATION_GAIN, mElevationGain);
		values.put(TECH_RATING, mTechRating);
		values.put(AEROBIC_RATING, mAerobicRating);
		values.put(COOL_RATING, mCoolRating);
		values.put(UPDATED_AT, mUpdatedAt);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = TrailsSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return TrailsSchema.update(provider, mId, getValues());
	}

	public String upsert(ContentResolver provider) {
		Cursor cursor = provider.query(TrailsSchema.CONTENT_URI,
				new String[] { TrailsColumns.TRAIL_ID }, TrailsColumns.TRAIL_ID
						+ "='" + mId + "'", null, null);
		if (cursor == null || cursor.getCount() == 0) {
			return insert(provider);
		} else {
			update(provider);
			return mId;
		}
	}

	public static void purgeConditionsToLimit(ContentResolver resolver,
			String trailId, int limit) {
		Uri uri = TrailsSchema.buildConditionsDirUri(trailId);
		Cursor cursor = resolver.query(uri,
				new String[] { ConditionsColumns.CONDITION_ID }, null, null,
				null);

		if (cursor != null) {

			try {

				int i = 0;
				while (cursor.moveToNext()) {
					if (i >= limit) {
						String conditionId = cursor.getString(0);
						uri = ConditionsSchema.buildUri(conditionId);
						resolver.delete(uri, null, null);
					}
					i++;
				}
			} finally {
				cursor.close();
			}
		}

	}

}
