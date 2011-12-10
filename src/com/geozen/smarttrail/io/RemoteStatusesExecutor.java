/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.model.Trail;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;

/**

 */
public class RemoteStatusesExecutor {
	private final ContentResolver mResolver;

	public RemoteStatusesExecutor(ContentResolver resolver) {
		mResolver = resolver;
	}

	/**
	 * 
	 * Update all trail status for a trail.
	 * 
	 * @throws JSONException
	 * @throws SmartTrailException
	 * @throws IOException
	 * @throws CredentialsException
	 * 
	 */
	public void executeByRegion(SmartTrailApi api, String regionId, long afterTimestamp, int limit) throws CredentialsException, IOException,
			SmartTrailException, JSONException {
		boolean clearedCredentials = false;

		if (!api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}

		JSONArray statuses = api.pullStatusesByRegion(regionId, afterTimestamp, limit);

		JSONObject status;
		ContentValues values = new ContentValues();
		String trailId;
		
		for (int i = 0; i < statuses.length(); i++) {
			
			status = statuses.getJSONObject(i);
			values.clear();
			trailId = status.getString(Condition.TRAIL_ID);
			Uri uri = TrailsSchema.buildUri(trailId);
			
			values.put(TrailsColumns.CONDITION, status.getString(Trail.CONDITION));
			values.put(TrailsColumns.DIRECTION,
					status.getString(Trail.DIRECTION));
			values.put(TrailsColumns.STATUS_UPDATED_AT,
					status.getString(Trail.JSON_STATUS_UPDATED_AT));
			
			mResolver.update(uri, values, null, null);
		}
		

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	

}
