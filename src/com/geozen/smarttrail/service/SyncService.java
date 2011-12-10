/**
 * Adapted from code from Google IO scheduler.
 * 
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.service;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.io.RemoteAreasExecutor;
import com.geozen.smarttrail.io.RemoteConditionsExecutor;
import com.geozen.smarttrail.io.RemoteEventsExecutor;
import com.geozen.smarttrail.io.RemoteStatusesExecutor;
import com.geozen.smarttrail.io.RemoteTrailsExecutor;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.TimeUtil;
import com.geozen.smarttrail.util.Util;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link ScheduleProvider}. Reads data from both local {@link Resources} and
 * from remote sources, such as a spreadsheet.
 */
public class SyncService extends IntentService {
	private static final String TAG = "SyncService";

	public static final String EXTRA_STATUS_RECEIVER = "com.google.geozen.smarttrail.extra.STATUS_RECEIVER";
	public static final String INTENT_ACTION_SYNC_COMPLETE = "com.google.geozen.smarttrail.action.SYNC_COMPLETE";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;

//	private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
//
//	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
//	private static final String ENCODING_GZIP = "gzip";

	private RemoteConditionsExecutor mRemoteConditionsExecutor;

	private RemoteEventsExecutor mRemoteEventsExecutor;

	private RemoteAreasExecutor mRemoteAreasExecutor;

	private RemoteTrailsExecutor mRemoteTrailsExecutor;

	private RemoteStatusesExecutor mRemoteStatusExecutor;

	public SyncService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final ContentResolver resolver = getContentResolver();

		mRemoteEventsExecutor = new RemoteEventsExecutor(resolver);
		mRemoteAreasExecutor = new RemoteAreasExecutor(resolver);
		mRemoteTrailsExecutor = new RemoteTrailsExecutor(resolver);
		mRemoteConditionsExecutor = new RemoteConditionsExecutor(resolver);
		mRemoteStatusExecutor = new RemoteStatusesExecutor(resolver);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);

		// final Context context = this;
		final SharedPreferences prefs = getSharedPreferences(
				Prefs.SMARTTRAIL_SYNC, Context.MODE_PRIVATE);
		final long lastUpdate = prefs.getLong(Prefs.LAST_UPDATE, 0);

		try {

			// Always hit remote database for any updates if network available
			if (Util.isConnectedToNetwork(this)) {
				final long startRemote = System.currentTimeMillis();
				final long now = startRemote;
				long afterTimestamp = lastUpdate;
				// afterTimestamp = -1;
				SmartTrailApplication app = ((SmartTrailApplication) getApplication());
				SmartTrailApi api = app.getApi();

				int limit = -1;
				String regionId = app.getRegion();

				// download new or changed areas
//				mRemoteAreasExecutor.executeByRegion(api, regionId,
//						afterTimestamp, limit);
//
//				// download new or changed trails for all areas
//				mRemoteTrailsExecutor.executeByRegion(api, regionId,
//						afterTimestamp, limit);
//
//				// download new or changed statuses for all trails
//				mRemoteStatusExecutor.executeByRegion(api, regionId,
//						afterTimestamp, limit);
//
//				// download new or changed conditions for all trails. limit 10
//				// per trail
//				mRemoteConditionsExecutor.executeByRegion(api, regionId,
//						afterTimestamp, 10);
//
//				// download new or changed events limit 10 per trail
//				mRemoteEventsExecutor.executeByRegion(api, regionId, now - 2
//						* TimeUtil.DAY_MS, 20);

				// Save last update time
				prefs.edit().putLong(Prefs.LAST_UPDATE, now).commit();

				AppLog.d(TAG, "remote sync took "
						+ (System.currentTimeMillis() - startRemote) + "ms");
			}
		} catch (Exception e) {
			AppLog.e(TAG, "Problem while syncing", e);

			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
		}

		// Announce success to any surface listener
		AppLog.d(TAG, "sync finished");
		if (receiver != null)
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);

		Intent syncCompleteIntent = new Intent(INTENT_ACTION_SYNC_COMPLETE);
		sendBroadcast(syncCompleteIntent);
	}

//	/**
//	 * Generate and return a {@link HttpClient} configured for general use,
//	 * including setting an application-specific user-agent string.
//	 */
//	public static HttpClient getHttpClient(Context context) {
//		final HttpParams params = new BasicHttpParams();
//
//		// Use generous timeouts for slow mobile networks
//		HttpConnectionParams
//				.setConnectionTimeout(params, 20 * SECOND_IN_MILLIS);
//		HttpConnectionParams.setSoTimeout(params, 20 * SECOND_IN_MILLIS);
//
//		HttpConnectionParams.setSocketBufferSize(params, 8192);
//		HttpProtocolParams.setUserAgent(params, buildUserAgent(context));
//
//		final DefaultHttpClient client = new DefaultHttpClient(params);
//
//		client.addRequestInterceptor(new HttpRequestInterceptor() {
//			public void process(HttpRequest request, HttpContext context) {
//				// Add header to accept gzip content
//				if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
//					request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
//				}
//			}
//		});
//
//		client.addResponseInterceptor(new HttpResponseInterceptor() {
//			public void process(HttpResponse response, HttpContext context) {
//				// Inflate any responses compressed with gzip
//				final HttpEntity entity = response.getEntity();
//				final Header encoding = entity.getContentEncoding();
//				if (encoding != null) {
//					for (HeaderElement element : encoding.getElements()) {
//						if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
//							response.setEntity(new InflatingEntity(response
//									.getEntity()));
//							break;
//						}
//					}
//				}
//			}
//		});
//
//		return client;
//	}
//
//	/**
//	 * Build and return a user-agent string that can identify this application
//	 * to remote servers. Contains the package name and version code.
//	 */
//	private static String buildUserAgent(Context context) {
//		try {
//			final PackageManager manager = context.getPackageManager();
//			final PackageInfo info = manager.getPackageInfo(
//					context.getPackageName(), 0);
//
//			// Some APIs require "(gzip)" in the user-agent string.
//			return info.packageName + "/" + info.versionName + " ("
//					+ info.versionCode + ") (gzip)";
//		} catch (NameNotFoundException e) {
//			return null;
//		}
//	}
//
//	/**
//	 * Simple {@link HttpEntityWrapper} that inflates the wrapped
//	 * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
//	 */
//	private static class InflatingEntity extends HttpEntityWrapper {
//		public InflatingEntity(HttpEntity wrapped) {
//			super(wrapped);
//		}
//
//		@Override
//		public InputStream getContent() throws IOException {
//			return new GZIPInputStream(wrappedEntity.getContent());
//		}
//
//		@Override
//		public long getContentLength() {
//			return -1;
//		}
//	}
//
	
	private interface Prefs {
		String LAST_UPDATE = "lastUpdate";
		String SMARTTRAIL_SYNC = "iosched_sync";
		// String LOCAL_VERSION = "local_version";
	}
}
