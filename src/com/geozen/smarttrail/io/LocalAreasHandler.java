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
import android.graphics.Color;

import com.geozen.smarttrail.provider.SmartTrailSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.util.Lists;

public class LocalAreasHandler extends XmlHandler {

	public LocalAreasHandler() {
		super(SmartTrailSchema.CONTENT_AUTHORITY);
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(XmlPullParser parser,
			ContentResolver resolver) throws XmlPullParserException,
			IOException {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		batch.add(ContentProviderOperation.newDelete(AreasSchema.CONTENT_URI)
				.build());

		int type;
		while ((type = parser.next()) != END_DOCUMENT) {
			if (type == START_TAG && Tags.AREA.equals(parser.getName())) {
				batch.add(parseArea(parser));
			}
		}

		return batch;
	}

	private static ContentProviderOperation parseArea(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		final int depth = parser.getDepth();
		final ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(AreasSchema.CONTENT_URI);

		String tag = null;
		int type;
		while (((type = parser.next()) != END_TAG || parser.getDepth() > depth)
				&& type != END_DOCUMENT) {
			if (type == START_TAG) {
				tag = parser.getName();
			} else if (type == END_TAG) {
				tag = null;
			} else if (type == TEXT) {
				final String text = parser.getText();
				if (AreasColumns.REGION_ID.equals(tag)) {
					builder.withValue(AreasColumns.REGION_ID, text);
				} else if (AreasColumns.AREA_ID.equals(tag)) {
					builder.withValue(AreasColumns.AREA_ID, text);
				} else if (AreasColumns.NAME.equals(tag)) {
					builder.withValue(AreasColumns.NAME, text);
				} else if (AreasColumns.DESCRIPTION.equals(tag)) {
					builder.withValue(AreasColumns.DESCRIPTION, text);
				} else if (AreasColumns.COLOR.equals(tag)) {
					final int color = Color.parseColor(text);
					builder.withValue(AreasColumns.COLOR, color);
				} else if (AreasColumns.UPDATED_AT.equals(tag)) {
					builder.withValue(ConditionsColumns.UPDATED_AT,
							Long.parseLong(text));
				}
			}
		}

		return builder.build();
	}

	interface Tags {
		String AREA = "area";

	}
}
