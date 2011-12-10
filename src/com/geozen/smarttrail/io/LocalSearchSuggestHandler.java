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

import com.geozen.smarttrail.provider.SmartTrailSchema;
import com.geozen.smarttrail.util.Lists;

public class LocalSearchSuggestHandler extends XmlHandler {

    public LocalSearchSuggestHandler() {
        super(SmartTrailSchema.CONTENT_AUTHORITY);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver)
            throws XmlPullParserException, IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        // Clear any existing suggestion words
       // batch.add(ContentProviderOperation.newDelete(SearchSuggest.CONTENT_URI).build());

        String tag = null;
        int type;
        while ((type = parser.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                tag = parser.getName();
            } else if (type == END_TAG) {
                tag = null;
            } else if (type == TEXT) {
               // final String text = parser.getText();
                if (Tags.WORD.equals(tag)) {
                    // Insert word as search suggestion
//                    batch.add(ContentProviderOperation.newInsert(SearchSuggest.CONTENT_URI)
//                            .withValue(SearchManager.SUGGEST_COLUMN_TEXT_1, text).build());
                }
            }
        }

        return batch;
    }

    private interface Tags {
        String WORD = "word";
    }
}
