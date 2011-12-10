/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.text.TextUtils;

import com.geozen.smarttrail.app.Constants;
import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Trail;

/**

 */
public class RemoteTrailsExecutor {
	private final ContentResolver mResolver;

	public RemoteTrailsExecutor(ContentResolver resolver) {
		mResolver = resolver;
	}

	/**
	 * @throws JSONException
	 * @throws SmartTrailException
	 * @throws IOException
	 * @throws CredentialsException
	 * 
	 */
	public void executeByRegion(SmartTrailApi api, String regionId,
			long afterTimestamp, int limit) throws CredentialsException,
			IOException, SmartTrailException, JSONException {
		boolean clearedCredentials = false;

		if (!api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}
		File cacheDir = new File(
				android.os.Environment.getExternalStorageDirectory(), Constants.CACHE_DIR+"/"
						+ regionId);
		

		if (!cacheDir.exists())
			cacheDir.mkdirs();

		JSONArray trails = api.pullTrailsByRegion(regionId, afterTimestamp,
				limit);

		for (int i = 0; i < trails.length(); i++) {

			Trail trail = new Trail(trails.getJSONObject(i));
			trail.upsert(mResolver);

			// download trail map
			if ( !TextUtils.isEmpty(trail.mMapId)) {
				File mapFile = new File(cacheDir, trail.mMapId + ".gpx");
				api.pullMap(trail.mMapId, mapFile);
			}
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

}
