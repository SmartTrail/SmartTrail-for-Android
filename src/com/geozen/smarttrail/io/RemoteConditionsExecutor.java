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
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.model.Trail;

/**

 */
public class RemoteConditionsExecutor {
	private final ContentResolver mResolver;

	public RemoteConditionsExecutor(ContentResolver resolver) {
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
			int limit) throws CredentialsException,
			IOException, SmartTrailException, JSONException {
		boolean clearedCredentials = false;

		if ( !api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}

		JSONArray conditions = api.pullConditionsByRegion(regionId, afterTimestamp,
				limit);


		for (int i = 0; i < conditions.length(); i++) {

			Condition condition = new Condition(conditions.getJSONObject(i));
			
			Trail.purgeConditionsToLimit(mResolver, condition.mTrailId, 9);
			
			condition.upsert(mResolver);
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	

}
