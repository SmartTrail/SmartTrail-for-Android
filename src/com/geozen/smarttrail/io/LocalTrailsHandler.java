/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.geozen.smarttrail.provider.SmartTrailSchema;
import com.geozen.smarttrail.provider.SmartTrailDatabase.TrailsAreas;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.util.Lists;

public class LocalTrailsHandler extends XmlHandler {

    public LocalTrailsHandler() {
        super(SmartTrailSchema.CONTENT_AUTHORITY);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver)
            throws XmlPullParserException, IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG && Tags.TRAIL.equals(parser.getName())) {
                parseTrail(parser, batch, resolver);
            }
        }

        return batch;
    }

    private static void parseTrail(XmlPullParser parser,
            ArrayList<ContentProviderOperation> batch, ContentResolver resolver)
            throws XmlPullParserException, IOException {
        final int depth = parser.getDepth();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(TrailsSchema.CONTENT_URI);

       
        String trailName = null;
        String trailId = null;
        String areaId = null;

        String tag = null;
        int type;
        while (((type = parser.next()) != END_TAG ||
                parser.getDepth() > depth) && type != END_DOCUMENT) {
            if (type == START_TAG) {
                tag = parser.getName();
            } else if (type == END_TAG) {
                tag = null;
            } else if (type == TEXT) {
                final String text = parser.getText();
                if (TrailsColumns.TRAIL_ID.equals(tag)) {
                    trailId = text;
                } else if (TrailsColumns.NAME.equals(tag)) {
                    trailName = text;
                    builder.withValue(TrailsColumns.NAME, trailName);
                } else if (TrailsColumns.AREA_ID.equals(tag)) {
                	areaId = text;
                    builder.withValue(TrailsColumns.AREA_ID, text);
                }  else if (TrailsColumns.OWNER.equals(tag)) {
                    builder.withValue(TrailsColumns.OWNER, text);
                } else if (TrailsColumns.URL.equals(tag)) {
                    builder.withValue(TrailsColumns.URL, text);
                } else if (TrailsColumns.DESCRIPTION.equals(tag)) {
                    builder.withValue(TrailsColumns.DESCRIPTION, text);
                } else if (TrailsColumns.CONDITION.equals(tag)) {
                    builder.withValue(TrailsColumns.CONDITION, text);
                } else if (TrailsColumns.UPDATED_AT.equals(tag)) {
                	builder.withValue(ConditionsColumns.UPDATED_AT,
							Long.parseLong(text));
                }
            }
        }

        if (trailId == null) {
            trailId = TrailsSchema.generateTrailId(trailName);
        }

        builder.withValue(TrailsColumns.TRAIL_ID, trailId);
        
        
        

        // Use empty strings to make sure SQLite search trigger has valid data
        // for updating search index.
     
        builder.withValue(TrailsColumns.KEYWORDS, "");

  

        // Propagate any existing starred value
        final Uri trailUri = TrailsSchema.buildUri(trailId);
        final int starred = queryTrailsStarred(trailUri, resolver);
        if (starred != -1) {
            builder.withValue(TrailsColumns.STARRED, starred);
        }

        batch.add(builder.build());

        if (areaId != null) {
            final Uri trailAreas = TrailsSchema.buildAreasDirUri(trailId);
            batch.add(ContentProviderOperation.newInsert(trailAreas)
                    .withValue(TrailsAreas.TRAIL_ID, trailId)
                    .withValue(TrailsAreas.AREA_ID, areaId).build());
        }
    }

    public static int queryTrailsStarred(Uri uri, ContentResolver resolver) {
        final String[] projection = { TrailsColumns.STARRED };
        final Cursor cursor = resolver.query(uri, projection, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    interface Tags {
        String TRAIL = "trail";
    }
}
