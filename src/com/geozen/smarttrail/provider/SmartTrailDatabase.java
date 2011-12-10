/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.provider;

import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link ScheduleProvider}.
 */
public class SmartTrailDatabase extends SQLiteOpenHelper {
	private static final String TAG = "SmartTrailDatabase";

	private static final String DATABASE_NAME = "smarttrail.db";

	// NOTE: carefully update onUpgrade() when bumping database versions to make
	// sure user data is saved.

	private static final int DATABASE_VERSION = 26;

	interface Tables {
		String EVENTS = "events";
		String AREAS = "areas";
		String TRAILS = "trails";
		String CONDITIONS = "conditions";
		
		String TRAILS_AREAS = "trails_areas";

		String TRAILS_SEARCH = "trails_search";

		String SEARCH_SUGGEST = "search_suggest";

		String TRAILS_AREAS_JOIN_AREAS = "trails_areas "
				+ "LEFT OUTER JOIN areas ON trails_areas.areaId=areas.id";

		String TRAILS_AREAS_JOIN_TRAILS = "trails_areas "
				+ "LEFT OUTER JOIN trails ON trails_areas.trailId=trails.id ";

		String TRAILS_SEARCH_JOIN_TRAILS = "trails_search "
				+ "LEFT OUTER JOIN trails ON trails_search.trailId=trails.id ";
	}

	private interface Triggers {
		String TRAILS_SEARCH_INSERT = "trails_search_insert";
		String TRAILS_SEARCH_DELETE = "trails_search_delete";
		String TRAILS_SEARCH_UPDATE = "trails_search_update";
	}

	public interface TrailsAreas {
		String TRAIL_ID = "trailId";
		String AREA_ID = "areaId";
	}

	interface TrailsSearchColumns {
		String TRAIL_ID = "trailId";
		String BODY = "body";
	}

	/** Fully-qualified field names. */
	private interface Qualified {
		String TRAILS_SEARCH_SESSION_ID = Tables.TRAILS_SEARCH + "."
				+ TrailsSearchColumns.TRAIL_ID;

		String TRAILS_SEARCH = Tables.TRAILS_SEARCH + "("
				+ TrailsSearchColumns.TRAIL_ID + "," + TrailsSearchColumns.BODY
				+ ")";

	}

	/** {@code REFERENCES} clauses. */
	private interface References {
		String AREA_ID = "REFERENCES " + Tables.AREAS + "("
				+ AreasColumns.AREA_ID + ")";
		String TRAIL_ID = "REFERENCES " + Tables.TRAILS + "("
				+ TrailsColumns.TRAIL_ID + ")";
	}

	private interface Subquery {
		/**
		 * Subquery used to build the {@link TrailsSearchColumns#BODY} string
		 * used for indexing {@link TrailsSchema} content.
		 */
		String TRAILS_BODY = "(new." + TrailsColumns.NAME + "||'; '||new."
				+ TrailsColumns.DESCRIPTION + "||'; '||" + "coalesce(new."
				+ TrailsColumns.KEYWORDS + ", '')" + ")";

	}

	public SmartTrailDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + Tables.EVENTS + " (" //
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ EventsColumns.EVENT_ID + " TEXT NOT NULL," //
				+ EventsColumns.REGION_ID + " TEXT NOT NULL," //
				+ EventsColumns.ORG_ID + " TEXT NOT NULL," //
				+ EventsColumns.SNIPPET + " TEXT," //
				+ EventsColumns.DATE_SNIPPET + " TEXT," //
				+ EventsColumns.START_TIMESTAMP + " INT," 
				+ EventsColumns.END_TIMESTAMP + " INT," 
				+ EventsColumns.URL + " TEXT," //
				+ "UNIQUE (" + EventsColumns.EVENT_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.AREAS + " (" //
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ AreasColumns.AREA_ID + " TEXT NOT NULL," //
				+ AreasColumns.REGION_ID + " TEXT NOT NULL," //
				+ AreasColumns.NAME + " TEXT," //
				+ AreasColumns.OWNER + " TEXT," 
				+ AreasColumns.NUM_REVIEWS + " INTEGER NOT NULL DEFAULT 0," 
				+ AreasColumns.RATING + " FLOAT NOT NULL DEFAULT 5," 
				+ TrailsColumns.URL + " TEXT," 
				+ AreasColumns.DESCRIPTION + " TEXT," //
				+ TrailsColumns.KEYWORDS + " TEXT," 
				+ AreasColumns.STARRED + " INTEGER NOT NULL DEFAULT 0," 
				+ AreasColumns.COLOR + " TEXT," //
				+ "UNIQUE (" + AreasColumns.AREA_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.TRAILS
				+ " ("
				+ BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TrailsColumns.TRAIL_ID + " TEXT NOT NULL," 
				+ TrailsColumns.REGION_ID + " TEXT NOT NULL,"
				+ TrailsColumns.AREA_ID + " TEXT NOT NULL,"
				+ TrailsColumns.MAP_ID + " TEXT,"
				+ TrailsColumns.NAME + " TEXT," 
				+ TrailsColumns.OWNER + " TEXT," 
				+ TrailsColumns.URL + " TEXT," 
				+ TrailsColumns.DESCRIPTION + " TEXT," 
				+ TrailsColumns.CONDITION + " TEXT,"
				+ TrailsColumns.DIRECTION + " TEXT,"
				+ TrailsColumns.STATUS_UPDATED_AT + " INT," 
				+ TrailsColumns.UPDATED_AT + " INT," 
				+ TrailsColumns.HEAD_LAT_E6 + " INT," 
				+ TrailsColumns.HEAD_LON_E6 + " INT," 
				+ TrailsColumns.LENGTH + " INT," 
				+ TrailsColumns.ELEVATION_GAIN + " INT," 
				+ TrailsColumns.TECH_RATING + " INT," 
				+ TrailsColumns.AEROBIC_RATING + " INT," 
				+ TrailsColumns.COOL_RATING + " INT," 
				+ TrailsColumns.KEYWORDS + " TEXT," 
				+ TrailsColumns.STARRED + " INTEGER NOT NULL DEFAULT 0," 
				+ "UNIQUE ("
				+ TrailsColumns.TRAIL_ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Tables.CONDITIONS
				+ " ("
				+ BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ConditionsColumns.CONDITION_ID + " TEXT NOT NULL,"
				+ ConditionsColumns.TRAIL_ID + " TEXT NOT NULL," 
				+ ConditionsColumns.TYPE + " INTEGER NOT NULL DEFAULT 0," 
				+ ConditionsColumns.USERNAME + " TEXT NOT NULL,"
				+ ConditionsColumns.USER_TYPE + " TEXT," 
				+ ConditionsColumns.STATUS + " TEXT,"
				+ ConditionsColumns.COMMENT + " TEXT,"
				+ ConditionsColumns.UPDATED_AT + " INT," 
				+ "UNIQUE ("
				+ ConditionsColumns.CONDITION_ID + ") ON CONFLICT REPLACE)");

		db.execSQL("CREATE TABLE " + Tables.TRAILS_AREAS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TrailsAreas.TRAIL_ID + " TEXT NOT NULL "
				+ References.TRAIL_ID + "," + TrailsAreas.AREA_ID
				+ " TEXT NOT NULL " + References.AREA_ID + "," + "UNIQUE ("
				+ TrailsAreas.TRAIL_ID + "," + TrailsAreas.AREA_ID
				+ ") ON CONFLICT REPLACE)");

		createTrailsSearch(db);

		db.execSQL("CREATE TABLE " + Tables.SEARCH_SUGGEST + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT NOT NULL)");

	}

	/**
	 * Create triggers that automatically build {@link Tables#TRAILS_SEARCH} as
	 * values are changed in {@link Tables#TRAILS}.
	 */
	private static void createTrailsSearch(SQLiteDatabase db) {
		// Using the "porter" tokenizer for simple stemming, so that
		// "frustration" matches "frustrated."

		db.execSQL("CREATE VIRTUAL TABLE " + Tables.TRAILS_SEARCH
				+ " USING fts3(" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TrailsSearchColumns.BODY + " TEXT NOT NULL,"
				+ TrailsSearchColumns.TRAIL_ID + " TEXT NOT NULL "
				+ References.TRAIL_ID + "," + "UNIQUE ("
				+ TrailsSearchColumns.TRAIL_ID + ") ON CONFLICT REPLACE,"
				+ "tokenize=porter)");

		// TODO: handle null fields in body, which cause trigger to fail
		// TODO: implement update trigger, not currently exercised

		db.execSQL("CREATE TRIGGER " + Triggers.TRAILS_SEARCH_INSERT
				+ " AFTER INSERT ON " + Tables.TRAILS + " BEGIN INSERT INTO "
				+ Qualified.TRAILS_SEARCH + " " + " VALUES(new."
				+ TrailsColumns.TRAIL_ID + ", " + Subquery.TRAILS_BODY + ");"
				+ " END;");

		db.execSQL("CREATE TRIGGER " + Triggers.TRAILS_SEARCH_DELETE
				+ " AFTER DELETE ON " + Tables.TRAILS + " BEGIN DELETE FROM "
				+ Tables.TRAILS_SEARCH + " " + " WHERE "
				+ Qualified.TRAILS_SEARCH_SESSION_ID + "=old."
				+ TrailsColumns.TRAIL_ID + ";" + " END;");

		db.execSQL("CREATE TRIGGER " + Triggers.TRAILS_SEARCH_UPDATE
				+ " AFTER UPDATE ON " + Tables.TRAILS
				+ " BEGIN UPDATE "+ Tables.TRAILS_SEARCH +" SET " + TrailsSearchColumns.BODY
				+ " = " + Subquery.TRAILS_BODY
				+ " WHERE "+ TrailsSearchColumns.TRAIL_ID + " = old."
				+ TrailsColumns.TRAIL_ID  + " ; END;");

		


	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

		// NOTE: This switch statement is designed to handle cascading database
		// updates, starting at the current version and falling through to all
		// future upgrade cases. Only use "break;" when you want to drop and
		// recreate the entire database.
		int version = oldVersion;


		Log.d(TAG, "after upgrade logic, at version " + version);
		if (version != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");

			db.execSQL("DROP TABLE IF EXISTS " + Tables.EVENTS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.AREAS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.TRAILS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.CONDITIONS);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.TRAILS_AREAS);

			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.TRAILS_SEARCH_INSERT);
			db.execSQL("DROP TRIGGER IF EXISTS "
					+ Triggers.TRAILS_SEARCH_DELETE);
			db.execSQL("DROP TABLE IF EXISTS " + Tables.TRAILS_SEARCH);

			db.execSQL("DROP TABLE IF EXISTS " + Tables.SEARCH_SUGGEST);

			onCreate(db);
		}
	}
}
