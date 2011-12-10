/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Region is a location category. All trail areas are in a specific region. For
 * instance Boulder, CO is a region, that contains the Marshall Mesa and Walker
 * Ranch trail areas.
 * 
 * @author matt
 * 
 */
public class Region {

	public static final String REGION_ID = "id";
	public static final String NAME = "name";
	public static final String SPONSOR_ID = "sponsorId";
	public static final String SPONSOR_NAME = "sponsorName";
	public static final String SPONSOR_ALERTS_URL = "sponsorAlertsUrl";
	public static final String SPONSOR_TWITTER = "sponsorTwitter";

	public static final String UPDATED_AT = "updatedAt";

	public String mId;

	public String mName;
	public long mUpdatedAt;
	public String mSponsorId;
	public String mSponsorName;
	public String[] mSponsorTwitter;
	public String mSponsorAlertsUrl;
	public String mSponsorTwitterQuery;

	public Region() {

	}

	public Region(JSONObject region) throws JSONException {
		//
		// Required
		//
		mId = region.getString(REGION_ID);
		mName = region.getString(NAME);
		mUpdatedAt = region.getLong(UPDATED_AT);
		mSponsorId = region.getString(SPONSOR_ID);
		mSponsorName = region.getString(SPONSOR_NAME);
		mSponsorAlertsUrl = region.getString(SPONSOR_ALERTS_URL);

		JSONArray twitterArray = region.getJSONArray(SPONSOR_TWITTER);
		final int N = twitterArray.length();
		mSponsorTwitter = new String[N];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < N; i++) {
			mSponsorTwitter[i] = twitterArray.getString(i);
			sb.append(mSponsorTwitter[i]);
			if (i != N - 1) {
				sb.append(" OR ");
			}
		}
		mSponsorTwitterQuery = sb.toString();

	}

	public JSONObject toJson() throws JSONException {

		JSONObject area = new JSONObject();

		area.put(REGION_ID, mId);
		area.put(NAME, mName);
		area.put(SPONSOR_ID, mSponsorId);
		area.put(SPONSOR_NAME, mSponsorName);

		return area;
	}

}
