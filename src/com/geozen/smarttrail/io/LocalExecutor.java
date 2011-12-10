/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.io;

import com.geozen.smarttrail.io.XmlHandler.HandlerException;
import com.geozen.smarttrail.util.ParserUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Opens a local {@link Resources#getXml(int)} and passes the resulting
 * {@link XmlPullParser} to the given {@link XmlHandler}.
 */
public class LocalExecutor {
    private Resources mRes;
    private ContentResolver mResolver;

    public LocalExecutor(Resources res, ContentResolver resolver) {
        mRes = res;
        mResolver = resolver;
    }

    public void execute(Context context, String assetName, XmlHandler handler)
            throws HandlerException {
        try {
            final InputStream input = context.getAssets().open(assetName);
            final XmlPullParser parser = ParserUtils.newPullParser(input);
            handler.parseAndApply(parser, mResolver);
        } catch (HandlerException e) {
            throw e;
        } catch (XmlPullParserException e) {
            throw new HandlerException("Problem parsing local asset: " + assetName, e);
        } catch (IOException e) {
            throw new HandlerException("Problem parsing local asset: " + assetName, e);
        }
    }

    public void execute(int resId, XmlHandler handler) throws HandlerException {
        final XmlResourceParser parser = mRes.getXml(resId);
        try {
            handler.parseAndApply(parser, mResolver);
        } finally {
            parser.close();
        }
    }
}
