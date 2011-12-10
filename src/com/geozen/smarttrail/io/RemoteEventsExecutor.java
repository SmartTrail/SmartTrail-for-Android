/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.net.Uri;

import com.geozen.smarttrail.error.SmartTrailException;
import com.geozen.smarttrail.error.CredentialsException;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Event;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsSchema;

/**

 */
public class RemoteEventsExecutor {
	private final ContentResolver mResolver;

	public RemoteEventsExecutor(ContentResolver resolver) {
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

		JSONArray events = api.pullEventsByRegion(regionId, afterTimestamp,
				limit);

		//remove all previous events from local database.
		Uri uri = EventsSchema.CONTENT_URI;
		mResolver.delete(uri, null, null);
	
		for (int i = 0; i < events.length(); i++) {

			Event event = new Event(events.getJSONObject(i));
			event.upsert(mResolver);
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	
}
