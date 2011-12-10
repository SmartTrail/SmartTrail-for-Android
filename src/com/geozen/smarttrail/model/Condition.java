/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsSchema;

/**
 * Condition represents a trail condition given by a user. 
 * 
 * @author matt
 *
 */
public class Condition implements ConditionsColumns {

	// conditions
	public static final String UNKNOWN = "Unknown";
	public static final String GOOD = "Good";
	public static final String FAIR = "Fair";
	public static final String POOR = "Poor";
	public static final String CLOSED = "Closed";
	public static final String DEFAULT_MONGO_ID = "";

	public static final String BIKER = "Biker";
	public static final String HIKER = "Hiker";
	public static final String HORSE_RIDER = "Horse Rider";
	public static final String RUNNER = "Runner";
	public static String DEFAULT_USER_TYPE = BIKER;

	public String mId; // MongoId string of the condition.
	public String mTrailId; // MongoId string of the trail.
	public int mType;
	public String mUsername;
	public String mStatus; // UNKNOWN, GOOD, FAIR, POOR, CLOSED
	public String mComment;
	public long mUpdatedAt;
	public String mUserType;

	public Condition() {
		mId = "";
		mType = 0;
	}

	public Condition(JSONObject condition) throws JSONException {
		//
		// Required
		//
		mId = condition.getString(CONDITION_ID);

		mTrailId = condition.getString(TRAIL_ID);

		mType = condition.getInt(TYPE);
		mUsername = condition.getString(USERNAME);
		mStatus = condition.getString(STATUS);
		if (condition.has(USER_TYPE)) {
			mUserType = condition.getString(USER_TYPE);
		}
		mComment = condition.getString(COMMENT);
		mUpdatedAt = condition.getLong(UPDATED_AT);

	}

	public JSONObject toJson() throws JSONException {

		JSONObject condition = new JSONObject();

		condition.put(CONDITION_ID, mId);
		condition.put(TYPE, mType);
		condition.put(TRAIL_ID, mTrailId);
		condition.put(USERNAME, mUsername);
		condition.put(STATUS, mStatus);
		condition.put(USER_TYPE, mUserType);
		condition.put(COMMENT, mComment);
		condition.put(UPDATED_AT, mUpdatedAt);

		return condition;
	}

	

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(CONDITION_ID, mId);
		values.put(TYPE, mType);
		values.put(TRAIL_ID, mTrailId);
		values.put(USERNAME, mUsername);
		values.put(STATUS, mStatus);
		values.put(USER_TYPE, mUserType);
		values.put(COMMENT, mComment);
		values.put(UPDATED_AT, mUpdatedAt);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = ConditionsSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return ConditionsSchema.update(provider, mId, getValues());
	}

	public String upsert(ContentResolver provider) {
		return insert(provider); // database replaces on conflict
	}

	public static String getConditionDisplayName(Context context,
			String condition) {

		if (condition.equals(Condition.GOOD)) {
			return context.getString(R.string.good);
		} else if (condition.equals(Condition.FAIR)) {
			return context.getString(R.string.fair);
		} else if (condition.equals(Condition.POOR)) {
			return context.getString(R.string.poor);
		} else if (condition.equals(Condition.CLOSED)) {
			return context.getString(R.string.closed);
		} else {

			return "";
		}
	}

	public static Drawable getConditionBackground(Context context,
			String condition) {

		if (condition.equals(Condition.GOOD)) {
			return context.getResources().getDrawable(R.color.conditionGood);
		} else if (condition.equals(Condition.FAIR)) {
			return context.getResources().getDrawable(R.color.conditionFair);
		} else if (condition.equals(Condition.POOR)) {
			return context.getResources().getDrawable(R.color.conditionPoor);
		} else if (condition.equals(Condition.CLOSED)) {
			return context.getResources().getDrawable(R.color.conditionClosed);
		} else {

			return null;
		}
	}

	public static String getConditionByIndex(int index) {
		switch (index) {
		default:
		case 0:
			return GOOD;

		case 1:
			return FAIR;

		case 2:
			return POOR;

		case 3:
			return CLOSED;

		}
	}

	public static String getUserTypeByIndex(int index) {
		switch (index) {
		default:
		case 0:
			return BIKER;

		case 1:
			return HIKER;

		case 2:
			return HORSE_RIDER;

		case 3:
			return RUNNER;

		}
	}

	public static int getStatusColor(Context context, String status) {
		if (status == null) {
			return Color.GRAY;
		} else {
			if (status.equals(Condition.GOOD)) {
				return context.getResources().getColor(R.color.conditionGood);
			} else if (status.equals(Condition.FAIR)) {
				return context.getResources().getColor(R.color.conditionFair);
			} else if (status.equals(Condition.POOR)) {
				return context.getResources().getColor(R.color.conditionPoor);
			} else if (status.equals(Condition.CLOSED)) {
				return context.getResources().getColor(R.color.conditionClosed);
			} else {

				return Color.GRAY;
			}
		}
	}

	public static int getStatusImageResource(Context context, String condition) {
		if (condition == null) {
			return R.drawable.condition_unknown;
		} else {
			if (condition.equals(Condition.GOOD)) {
				return R.drawable.condition_cog_good;
			} else if (condition.equals(Condition.FAIR)) {
				return R.drawable.condition_cog_fair;
			} else if (condition.equals(Condition.POOR)) {
				return R.drawable.condition_cog_poor;
			} else if (condition.equals(Condition.CLOSED)) {
				return R.drawable.condition_closed;
			} else if (condition.equals(Condition.UNKNOWN)) {
				return R.drawable.condition_unknown;
			}  else {

				return R.drawable.condition_unknown;
			}
		}
	}

	public static Drawable getStatusDrawable(Context context, String status) {
		int resId = getStatusImageResource(context, status);
		return context.getResources().getDrawable(resId);

	}
}