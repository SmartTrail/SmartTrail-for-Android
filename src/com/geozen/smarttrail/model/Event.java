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

import com.geozen.smarttrail.provider.SmartTrailSchema.EventsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsSchema;
import com.geozen.smarttrail.util.TimeUtil;

/**
 * Event represents an organizational happening for a given region (e.g., a
 * happy hour, trail work, etc.)
 * 
 * @author matt
 * 
 */
public class Event implements EventsColumns, BaseColumns {

	public String mId;
	public String mRegionId;
	public String mOrgId;
	public String mDateSnippet;
	public String mSnippet;
	public String mUrl;
	public long mStartTimestamp;
	public long mEndTimestamp;
	public long mUpdatedAt;

	public Event() {

	}

	public Event(JSONObject event) throws JSONException {
		//
		// Required
		//
		mId = event.getString(EVENT_ID);
		mRegionId = event.getString(REGION_ID);
		mOrgId = event.getString(ORG_ID);
		mSnippet = event.getString(SNIPPET);
		mUrl = event.getString(URL);
		mStartTimestamp = event.getLong(START_TIMESTAMP);
		mEndTimestamp = event.getLong(END_TIMESTAMP);
		//mUpdatedAt = event.getLong(UPDATED_AT); //todo

		mDateSnippet = TimeUtil.eventFormat(mStartTimestamp, mEndTimestamp);
	}

	public JSONObject toJson() throws JSONException {

		JSONObject event = new JSONObject();

		event.put(EVENT_ID, mId);
		event.put(REGION_ID, mRegionId);
		event.put(ORG_ID, mOrgId);
		event.put(SNIPPET, mSnippet);
		event.put(URL, mUrl);
		event.put(START_TIMESTAMP, mStartTimestamp);
		event.put(END_TIMESTAMP, mEndTimestamp);

		return event;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(EVENT_ID, mId);
		values.put(REGION_ID, mRegionId);
		values.put(ORG_ID, mOrgId);
		values.put(SNIPPET, mSnippet);
		values.put(DATE_SNIPPET, mDateSnippet);
		values.put(URL, mUrl);
		values.put(START_TIMESTAMP, mStartTimestamp);
		values.put(END_TIMESTAMP, mEndTimestamp);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = EventsSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return EventsSchema.update(provider, mId, getValues());
	}

	public String upsert(ContentResolver provider) {

		Cursor cursor = provider.query(EventsSchema.CONTENT_URI,
				new String[] { EventsColumns.EVENT_ID }, EventsColumns.EVENT_ID
						+ "='" + mId + "'", null, null);

		if (cursor == null || cursor.getCount() == 0) {
			return insert(provider); // database replaces on conflict
		} else {
			update(provider);
			return mId;
		}
	}

}
