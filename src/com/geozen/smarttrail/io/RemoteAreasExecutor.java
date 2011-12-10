/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Area;


public class RemoteAreasExecutor {
	private final ContentResolver mResolver;

	public RemoteAreasExecutor(ContentResolver resolver) {
		mResolver = resolver;
	}

	/**
	 * @throws JSONException
	 * @throws SmartTrailException
	 * @throws IOException
	 * @throws CredentialsException
	 * 
	 */
	public void executeByRegion(SmartTrailApi api, String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		boolean clearedCredentials = false;

		if (!api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}

		JSONArray areas = api.pullAreasByRegion(regionId, afterTimestamp,
				limit);

	
		for (int i = 0; i < areas.length(); i++) {

			Area area = new Area(areas.getJSONObject(i));
			area.upsert(mResolver);
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	
}
