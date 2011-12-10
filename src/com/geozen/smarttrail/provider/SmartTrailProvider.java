/**
 * Adapted from Google Schedule IO code.
 * 
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.provider;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;

import com.geozen.smarttrail.provider.SmartTrailDatabase.Tables;
import com.geozen.smarttrail.provider.SmartTrailDatabase.TrailsAreas;
import com.geozen.smarttrail.provider.SmartTrailDatabase.TrailsSearchColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.RegionsSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.SearchSuggest;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.service.SyncService;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.SelectionBuilder;

/**
 * Provider that stores {@link SmartTrailSchema} data. Data is usually inserted by
 * {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class SmartTrailProvider extends ContentProvider {
	private static final String TAG = "SmartTrailProvider";

	private SmartTrailDatabase mOpenHelper;

	private static final UriMatcher sUriMatcher = buildUriMatcher();
	
	private static final int REGIONS = 100;
	private static final int REGIONS_ID_EVENTS = 101;
	private static final int REGIONS_ID_AREAS = 102;
	private static final int REGIONS_ID_TRAILS = 103;
	
	private static final int AREAS = 200;
	private static final int AREAS_ID = 201;
	private static final int AREAS_ID_TRAILS = 202;
	private static final int AREAS_STARRED = 203;

	private static final int TRAILS = 400;
	private static final int TRAILS_STARRED = 401;
	private static final int TRAILS_SEARCH = 402;
	private static final int TRAILS_AT = 403;
	private static final int TRAILS_ID = 404;
	private static final int TRAILS_ID_AREAS = 406;
	private static final int TRAILS_ID_CONDITIONS = 408;

	private static final int CONDITIONS = 500;
	private static final int CONDITIONS_ID = 501;
	
	private static final int EVENTS = 600;
	private static final int EVENTS_ID = 601;

	private static final int SEARCH_SUGGEST = 800;
	
	

	// private static final String MIME_XML = "text/xml";

	/**
	 * Build and return a {@link UriMatcher} that catches all {@link Uri}
	 * variations supported by this {@link ContentProvider}.
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = SmartTrailSchema.CONTENT_AUTHORITY;

		matcher.addURI(authority, "regions", REGIONS);
		matcher.addURI(authority, "regions/*/events", REGIONS_ID_EVENTS);
		matcher.addURI(authority, "regions/*/areas", REGIONS_ID_AREAS);
		matcher.addURI(authority, "regions/*/trails", REGIONS_ID_TRAILS);
		
		matcher.addURI(authority, "areas", AREAS);
		matcher.addURI(authority, "areas/starred", AREAS_STARRED);
		matcher.addURI(authority, "areas/*", AREAS_ID);
		matcher.addURI(authority, "areas/*/trails", AREAS_ID_TRAILS);
		

		matcher.addURI(authority, "trails", TRAILS);
		matcher.addURI(authority, "trails/starred", TRAILS_STARRED);
		matcher.addURI(authority, "trails/search/*", TRAILS_SEARCH);
		matcher.addURI(authority, "trails/at/*", TRAILS_AT);
		matcher.addURI(authority, "trails/*", TRAILS_ID);
		matcher.addURI(authority, "trails/*/areas", TRAILS_ID_AREAS);
		matcher.addURI(authority, "trails/*/conditions", TRAILS_ID_CONDITIONS);

		matcher.addURI(authority, "conditions", CONDITIONS);
		matcher.addURI(authority, "conditions/*", CONDITIONS_ID);
		
		matcher.addURI(authority, "events", EVENTS);
		matcher.addURI(authority, "events/*", EVENTS_ID);

		matcher.addURI(authority, "search_suggest_query", SEARCH_SUGGEST);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new SmartTrailDatabase(context);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {

		case AREAS:
			return AreasSchema.CONTENT_TYPE;
		case AREAS_ID:
			return AreasSchema.CONTENT_ITEM_TYPE;
		case AREAS_ID_TRAILS:
			return TrailsSchema.CONTENT_TYPE;
		case AREAS_STARRED:
			return AreasSchema.CONTENT_TYPE;
		case TRAILS:
			return TrailsSchema.CONTENT_TYPE;
		case CONDITIONS:
			return ConditionsSchema.CONTENT_TYPE;
		case CONDITIONS_ID:
			return ConditionsSchema.CONTENT_ITEM_TYPE;
		case TRAILS_STARRED:
			return TrailsSchema.CONTENT_TYPE;
		case TRAILS_SEARCH:
			return TrailsSchema.CONTENT_TYPE;
		case TRAILS_AT:
			return TrailsSchema.CONTENT_TYPE;
		case TRAILS_ID:
			return TrailsSchema.CONTENT_ITEM_TYPE;
		case TRAILS_ID_AREAS:
			return AreasSchema.CONTENT_TYPE;
		case TRAILS_ID_CONDITIONS:
			return ConditionsSchema.CONTENT_TYPE;
		case REGIONS_ID_EVENTS:
			return EventsSchema.CONTENT_TYPE;
		case REGIONS_ID_AREAS:
			return AreasSchema.CONTENT_TYPE;
		case REGIONS_ID_TRAILS:
			return TrailsSchema.CONTENT_TYPE;
		case EVENTS:
			return EventsSchema.CONTENT_TYPE;
		case EVENTS_ID:
			return EventsSchema.CONTENT_ITEM_TYPE;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		AppLog.v(TAG,
				"query(uri=" + uri + ", proj=" + Arrays.toString(projection)
						+ ")");
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		final int match = sUriMatcher.match(uri);
		switch (match) {
		default: {
			// Most cases are handled with simple SelectionBuilder
			final SelectionBuilder builder = buildExpandedSelection(uri, match);
			return builder.where(selection, selectionArgs).query(db,
					projection, sortOrder);
		}
		case SEARCH_SUGGEST: {
			final SelectionBuilder builder = new SelectionBuilder();

			// Adjust incoming query to become SQL text match
			selectionArgs[0] = selectionArgs[0] + "%";
			builder.table(Tables.SEARCH_SUGGEST);
			builder.where(selection, selectionArgs);
			builder.map(SearchManager.SUGGEST_COLUMN_QUERY,
					SearchManager.SUGGEST_COLUMN_TEXT_1);

			projection = new String[] { BaseColumns._ID,
					SearchManager.SUGGEST_COLUMN_TEXT_1,
					SearchManager.SUGGEST_COLUMN_QUERY };

			final String limit = uri
					.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
			return builder.query(db, projection, null, null,
					SearchSuggest.DEFAULT_SORT, limit);
		}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		AppLog.v(TAG, "insert(uri=" + uri + ", values=" + values.toString()
				+ ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match) {

		case EVENTS: {
			db.insertOrThrow(Tables.EVENTS, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return EventsSchema.buildUri(values
					.getAsString(EventsSchema.EVENT_ID));
		}
		case AREAS: {
			db.insertOrThrow(Tables.AREAS, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return AreasSchema.buildAreaUri(values
					.getAsString(AreasSchema.AREA_ID));
		}

		case TRAILS: {
			db.insertOrThrow(Tables.TRAILS, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return TrailsSchema.buildUri(values
					.getAsString(TrailsSchema.TRAIL_ID));
		}

		case CONDITIONS: {
			db.insertOrThrow(Tables.CONDITIONS, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return ConditionsSchema.buildUri(values
					.getAsString(ConditionsSchema.CONDITION_ID));
		}

		case TRAILS_ID_AREAS: {
			db.insertOrThrow(Tables.TRAILS_AREAS, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return AreasSchema.buildAreaUri(values
					.getAsString(TrailsAreas.AREA_ID));
		}

		case SEARCH_SUGGEST: {
			db.insertOrThrow(Tables.SEARCH_SUGGEST, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return SearchSuggest.CONTENT_URI;
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		AppLog.v(TAG, "update(uri=" + uri + ", values=" + values.toString()
				+ ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	/** {@inheritDoc} */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		AppLog.v(TAG, "delete(uri=" + uri + ")");
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	/**
	 * Apply the given set of {@link ContentProviderOperation}, executing inside
	 * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
	 * any single one fails.
	 */
	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Build a simple {@link SelectionBuilder} to match the requested
	 * {@link Uri}. This is usually enough to support {@link #insert},
	 * {@link #update}, and {@link #delete} operations.
	 */
	private SelectionBuilder buildSimpleSelection(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {

		case AREAS: {
			return builder.table(Tables.AREAS);
		}
		case AREAS_ID: {
			final String areaId = AreasSchema.getAreaId(uri);
			return builder.table(Tables.AREAS).where(
					AreasSchema.AREA_ID + "=?", areaId);
		}

		case TRAILS: {
			return builder.table(Tables.TRAILS);
		}
		case TRAILS_ID: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.TRAILS).where(
					TrailsSchema.TRAIL_ID + "=?", trailId);
		}

		case TRAILS_ID_AREAS: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.TRAILS_AREAS).where(
					TrailsSchema.TRAIL_ID + "=?", trailId);
		}
	
		case TRAILS_ID_CONDITIONS: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.CONDITIONS).where(
					ConditionsColumns.TRAIL_ID + "=?", trailId);
		}
		case CONDITIONS: {
			return builder.table(Tables.CONDITIONS);
		}
		case CONDITIONS_ID: {
			final String conditionId = ConditionsSchema.getConditionId(uri);
			return builder.table(Tables.CONDITIONS).where(
					ConditionsSchema.CONDITION_ID + "=?", conditionId);
		}
		case EVENTS: {
			return builder.table(Tables.EVENTS);
		}
		case EVENTS_ID: {
			final String eventId = EventsSchema.getId(uri);
			return builder.table(Tables.EVENTS).where(
					EventsSchema.EVENT_ID + "=?", eventId);
		}
		case SEARCH_SUGGEST: {
			return builder.table(Tables.SEARCH_SUGGEST);
		}
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	/**
	 * Build an advanced {@link SelectionBuilder} to match the requested
	 * {@link Uri}. This is usually only used by {@link #query}, since it
	 * performs table joins useful for {@link Cursor} data.
	 */
	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
		final SelectionBuilder builder = new SelectionBuilder();
		switch (match) {

		case EVENTS: {
			return builder.table(Tables.EVENTS).mapToTable(EventsSchema._ID,
					Tables.EVENTS);
			
		}
		case EVENTS_ID: {
			final String eventId = EventsSchema.getId(uri);
			return builder.table(Tables.EVENTS).where(
					EventsSchema.EVENT_ID + "=?", eventId);
		}
		case AREAS: {
			return builder.table(Tables.AREAS).mapToTable(AreasSchema._ID,
					Tables.AREAS);

		}
		case AREAS_STARRED: {
			return builder.table(Tables.AREAS)
					.mapToTable(AreasSchema._ID, Tables.AREAS)
					.where(AreasSchema.STARRED + "=1");
		}
		case AREAS_ID: {
			final String trackId = AreasSchema.getAreaId(uri);
			return builder.table(Tables.AREAS).where(
					AreasSchema.AREA_ID + "=?", trackId);
		}
		case AREAS_ID_TRAILS: {
			final String areaId = AreasSchema.getAreaId(uri);
			return builder.table(Tables.TRAILS)
			.mapToTable(TrailsSchema._ID, Tables.TRAILS)
			.where(TrailsSchema.AREA_ID + "='"+ areaId+"'");
		}
		
		case REGIONS_ID_AREAS: {
			final String regionId = RegionsSchema.getRegionId(uri);
			return builder.table(Tables.AREAS)
			.mapToTable(AreasSchema._ID, Tables.AREAS)
			.where(AreasSchema.REGION_ID + "='"+ regionId+"'");
		}
		case REGIONS_ID_TRAILS: {
			final String regionId = RegionsSchema.getRegionId(uri);
			return builder.table(Tables.TRAILS)
			.mapToTable(TrailsSchema._ID, Tables.TRAILS)
			.where(TrailsSchema.REGION_ID + "='"+ regionId+"'");
		}
		
		case REGIONS_ID_EVENTS: {
			final String regionId = RegionsSchema.getRegionId(uri);
			return builder.table(Tables.EVENTS)
			.mapToTable(EventsSchema._ID, Tables.EVENTS)
			.where(AreasSchema.REGION_ID + "='"+ regionId+"'");
		}


		case TRAILS: {
			return builder.table(Tables.TRAILS).mapToTable(TrailsSchema._ID,
					Tables.TRAILS);
		}
		case TRAILS_STARRED: {
			return builder.table(Tables.TRAILS)
					.mapToTable(TrailsSchema._ID, Tables.TRAILS)
					.where(TrailsSchema.STARRED + "=1");
		}
		case TRAILS_SEARCH: {
			final String query = TrailsSchema.getSearchQuery(uri);
			return builder.table(Tables.TRAILS_SEARCH_JOIN_TRAILS)
					.map(TrailsSchema.SEARCH_SNIPPET, Subquery.TRAILS_SNIPPET)
					.mapToTable(TrailsSchema._ID, Tables.TRAILS)
					.mapToTable(TrailsSchema.TRAIL_ID, Tables.TRAILS)

					.where(TrailsSearchColumns.BODY + " MATCH ?", query);
		}

		case TRAILS_ID: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.TRAILS)
					.mapToTable(TrailsSchema._ID, Tables.TRAILS)
					.where(Qualified.TRAILS_TRAIL_ID + "=?", trailId);
		}

		case TRAILS_ID_AREAS: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.TRAILS_AREAS_JOIN_AREAS)
					.mapToTable(AreasSchema._ID, Tables.AREAS)
					.mapToTable(AreasSchema.AREA_ID, Tables.AREAS)
					.where(Qualified.TRAILS_AREAS_TRAIL_ID + "=?", trailId);
		}
		case TRAILS_ID_CONDITIONS: {
			final String trailId = TrailsSchema.getTrailId(uri);
			return builder.table(Tables.CONDITIONS).where(
					ConditionsColumns.TRAIL_ID + "=?", trailId);
		}


		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		final int match = sUriMatcher.match(uri);
		switch (match) {
		default: {
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		}
	}

	private interface Subquery {

		// String AREAS_TRAILS_COUNT = "(SELECT COUNT(" +
		// Qualified.TRAILS_AREAS_TRAIL_ID
		// + ") FROM " + Tables.TRAILS_AREAS + " WHERE "
		// + Qualified.TRAILS_AREAS_AREA_ID + "=" + Qualified.AREAS_AREA_ID +
		// ")";

		String TRAILS_SNIPPET = "snippet(" + Tables.TRAILS_SEARCH
				+ ",'{','}','\u2026')";
	}

	/**
	 * {@link SmartTrailSchema} fields that are fully qualified with a specific parent
	 * {@link Tables}. Used when needed to work around SQL ambiguity.
	 */
	private interface Qualified {
		String TRAILS_TRAIL_ID = Tables.TRAILS + "." + TrailsSchema.TRAIL_ID;

		String TRAILS_AREAS_TRAIL_ID = Tables.TRAILS_AREAS + "."
				+ TrailsAreas.TRAIL_ID;
//		String TRAILS_AREAS_AREA_ID = Tables.TRAILS_AREAS + "."
//				+ TrailsAreas.AREA_ID;

		// String TRAILS_STARRED = Tables.TRAILS + "." + TrailsColumns.STARRED;
		//
		// String AREAS_AREA_ID = Tables.AREAS + "." + AreasColumns.AREA_ID;
	}
}
