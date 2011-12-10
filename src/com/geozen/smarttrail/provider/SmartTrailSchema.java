/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.provider;

import java.util.List;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import com.geozen.smarttrail.util.ParserUtils;

/**
 * Contract class for interacting with {@link SmartTrailProvider}. Unless otherwise
 * noted, all time-based fields are milliseconds since epoch and can be compared
 * against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri}
 * are generated using stronger {@link String} identifiers, instead of
 * {@code int} {@link BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 */
public class SmartTrailSchema {

	/**
	 * Special value for indicating that an entry has never been updated, or
	 * doesn't exist yet.
	 */
	public static final long UPDATED_NEVER = -2;

	/**
	 * Special value for indicating that the last update time is unknown,
	 * usually when inserted from a local file source.
	 */
	public static final long UPDATED_UNKNOWN = -1;

	public interface AreasColumns {
		/** Unique string identifying the area this trail belongs to. */
		String AREA_ID = "id";

		/** Unique string identifying the region this area belongs to. */
		String REGION_ID = "regionId";

		/** Name of the area. */
		String NAME = "name";
		
		/** owner of this area (e.g. city,county, us forest..) */
		String OWNER = "owner";

		String URL = "url";
		
		String NUM_REVIEWS = "numReviews";
		
		String RATING = "rating";
		
		/** Body of text explaining this trail in detail. */
		String DESCRIPTION = "description";
		
		/** User-specific flag indicating starred status. */
		String STARRED = "starred";
		
		/** color representation of area */
		String COLOR = "color";

		String KEYWORDS = "keywords";
		
		/** Timestamp of when condition submitted */
		String UPDATED_AT = "updatedAt";

	}

	public interface TrailsColumns {
		/** Unique string identifying this trail. */
		String TRAIL_ID = "id";
		/** Unique string identifying the area this trail belongs to. */
		String REGION_ID = "regionId";
		
		/** Unique string identifying the area this trail belongs to. */
		String AREA_ID = "areaId";
		
		/** Unique string identifying the area this trail belongs to. */
		String MAP_ID = "mapId";

		/** Title describing this trail. */
		String NAME = "name";
		/** owner of this trail (e.g. city,county, us forest..) */
		String OWNER = "owner";
		/** owner of this trail (e.g. city,county, us forest..) */
		String URL = "url";
		/** Body of text explaining this trail in detail. */
		String DESCRIPTION = "description";
		/** Condition status string */
		String CONDITION = "condition";
		String DIRECTION = "direction";
		String STATUS_UPDATED_AT = "statusUpdatedAt";
		
		/** User-specific flag indicating starred status. */
		String STARRED = "starred";

		String KEYWORDS = "keywords";
		
		// Trail head coordinates
		String HEAD_LAT_E6 = "headLatE6";
		String HEAD_LON_E6 = "headLonE6";
		String LENGTH = "length";
		String ELEVATION_GAIN = "elevationGain";
		String TECH_RATING = "techRating";
		String AEROBIC_RATING = "aerobicRating";
		String COOL_RATING = "coolRating";
		
		
		/** Timestamp of when condition was last updated */
		String UPDATED_AT = "updatedAt";
	}

	public interface ConditionsColumns {
		String CONDITION_ID = "id";
		String TRAIL_ID = "trailId";
		String TYPE = "type";
		String USERNAME = "username";
		String USER_TYPE = "userType";
		String STATUS = "status";
		String COMMENT = "comment";
		String UPDATED_AT = "updatedAt";
	}
	
	public interface EventsColumns {
		/** Unique string identifying the area this trail belongs to. */
		String EVENT_ID = "id";

		/** Unique string identifying the region this area belongs to. */
		String REGION_ID = "regionId";
		
		/** Unique string identifying the region this area belongs to. */
		String ORG_ID = "orgId";

		/** Date/time snippet */
		String SNIPPET = "snippet";
		
		/** Date/time snippet */
		String DATE_SNIPPET = "dateSnippet";

		/** url */
		String URL = "url";

		/** Timestamp of start */
		String START_TIMESTAMP = "startTimestamp";
		String END_TIMESTAMP = "endTimestamp";

		String UPDATED_AT = "updatedAt";
	}

	public static final String CONTENT_AUTHORITY = "com.geozen.smarttrail";

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	// private static final String PATH_AFTER = "after";
	private static final String PATH_EVENTS = "events";
	private static final String PATH_REGIONS = "regions";
	private static final String PATH_AREAS = "areas";
	private static final String PATH_TRAILS = "trails";
	private static final String PATH_CONDITIONS = "conditions";
	private static final String PATH_STARRED = "starred";
	private static final String PATH_SEARCH = "search";
	private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

	/**
	 * 
	 */
	public static class EventsSchema implements EventsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
		.appendPath(PATH_EVENTS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.event";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.event";
		
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = START_TIMESTAMP + " ASC";

		
		/** Build {@link Uri} for requested {@link #EVENT_ID}. */
		public static Uri buildUri(String eventId) {
			return CONTENT_URI.buildUpon().appendPath(eventId).build();
		}
		
		
		/** Read {@link #EVENT_ID} from {@link EventsSchema} {@link Uri}. */
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		
		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);
			
			// return the new Id
			return getId(uri);
		}
		
		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);
			
			return provider.update(uri, values, null, null);
		}
	}
	
	
	
	
	/**
	 * Areas are overall categories for {@link TrailsSchema} , such as
	 * "Marshall Mesa" or "Dowdy Draw."
	 */
	public static class RegionsSchema implements AreasColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
		.appendPath(PATH_REGIONS).build();
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.region";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.region";
		
		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = NAME + " ASC";
		
		/**
		 * Build {@link Uri} that references any {@link TrailsSchema} associated
		 * with the requested {@link #AREA_ID}.
		 */
		public static Uri buildAreasUri(String regionId) {
			return CONTENT_URI.buildUpon().appendPath(regionId)
					.appendPath(PATH_AREAS).build();
		}
		public static Uri buildTrailsUri(String regionId) {
			return CONTENT_URI.buildUpon().appendPath(regionId)
			.appendPath(PATH_TRAILS).build();
		}
		
		
		/** Read {@link #AREA_ID} from {@link AreasSchema} {@link Uri}. */
		public static String getRegionId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		
	}
	/**
	 * Areas are overall categories for {@link TrailsSchema} , such as
	 * "Marshall Mesa" or "Dowdy Draw."
	 */
	public static class AreasSchema implements AreasColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_AREAS).build();
		public static final Uri CONTENT_STARRED_URI = CONTENT_URI.buildUpon()
		.appendPath(PATH_STARRED).build();


		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.area";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.area";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = NAME + " ASC";

		/** "All tracks" ID. */
		public static final String ALL_AREAS_ID = "all";

		/** Build {@link Uri} for requested {@link #AREA_ID}. */
		public static Uri buildAreaUri(String areaId) {
			return CONTENT_URI.buildUpon().appendPath(areaId).build();
		}

		/**
		 * Build {@link Uri} that references any {@link TrailsSchema} associated
		 * with the requested {@link #AREA_ID}.
		 */
		public static Uri buildTrailsUri(String areaId) {
			return CONTENT_URI.buildUpon().appendPath(areaId)
					.appendPath(PATH_TRAILS).build();
		}

		/** Read {@link #AREA_ID} from {@link AreasSchema} {@link Uri}. */
		public static String getAreaId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		/**
		 * Generate a {@link #AREA_ID} that will always match the requested
		 * {@link Areas} details.
		 */
		public static String generateAreaId(String title) {
			return ParserUtils.sanitizeId(title);
		}

		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getAreaId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildAreaUri(id);

			return provider.update(uri, values, null, null);
		}
	}

	/**
	 * Each trail is under an {@link AreasSchema}
	 */
	public static class TrailsSchema implements TrailsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_TRAILS).build();
		public static final Uri CONTENT_STARRED_URI = CONTENT_URI.buildUpon()
				.appendPath(PATH_STARRED).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.trail";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.trail";

		public static final String SEARCH_SNIPPET = "search_snippet";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = TrailsColumns.NAME + " ASC";

		/** Build {@link Uri} for requested {@link #TRAIL_ID}. */
		public static Uri buildUri(String trailId) {
			return CONTENT_URI.buildUpon().appendPath(trailId).build();
		}

		/**
		 * Build {@link Uri} that references any {@link AreasSchema} associated
		 * with the requested {@link #TRAIL_ID}.
		 */
		public static Uri buildAreasDirUri(String trailId) {
			return CONTENT_URI.buildUpon().appendPath(trailId)
					.appendPath(PATH_AREAS).build();
		}

		// public static Uri buildTrailsAtDirUri(long time) {
		// return CONTENT_URI.buildUpon().appendPath(PATH_AT)
		// .appendPath(String.valueOf(time)).build();
		// }
		public static Uri buildConditionsDirUri(String trailId) {
			return CONTENT_URI.buildUpon().appendPath(trailId)
					.appendPath(PATH_CONDITIONS).build();
		}

		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH)
					.appendPath(query).build();
		}

		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2
					&& PATH_SEARCH.equals(pathSegments.get(1));
		}

		/** Read {@link #TRAIL_ID} from {@link TrailsSchema} {@link Uri}. */
		public static String getTrailId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}

		/**
		 * Generate a {@link #TRAIL_ID} that will always match the requested
		 * {@link TrailsSchema} details.
		 */
		public static String generateTrailId(String name) {
			return ParserUtils.sanitizeId(name);
		}
		
		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getTrailId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}
	}

	/**
	 * Trail conditions
	 */
	public static class ConditionsSchema implements ConditionsColumns,
			BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_CONDITIONS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.condition";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.condition";

		public static final String DEFAULT_SORT = UPDATED_AT + " DSC";


		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getConditionId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}


		public static Uri buildUri(String conditionId) {
			return CONTENT_URI.buildUpon().appendPath(conditionId).build();
		}

		public static String getConditionId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

	}

	public static class SearchSuggest {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SEARCH_SUGGEST).build();

		public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
				+ " COLLATE NOCASE ASC";
	}

	private SmartTrailSchema() {
	}
}
