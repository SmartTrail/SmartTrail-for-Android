/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;

/**
 * A {@link android.widget.CursorAdapter} that renders a {@link AreasQuery}.
 */
public class AreasAdapter extends CursorAdapter {

	private Activity mActivity;
	private SmartTrailApplication mApp;
	private Object mAreaLabel;


	public AreasAdapter(Activity activity) {
		super(activity, null);
		mActivity = activity;
		mApp = (SmartTrailApplication) activity.getApplication();
		mAreaLabel="";
	}




	/** {@inheritDoc} */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mActivity.getLayoutInflater().inflate(R.layout.list_item_area,
				parent, false);
	}

	/** {@inheritDoc} */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView textView = (TextView) view
				.findViewById(android.R.id.text1);
		textView.setText(cursor.getString(AreasQuery.AREA_NAME)+ mAreaLabel);
		textView.setTypeface(mApp.mTf);
		
		final boolean starred = cursor.getInt(AreasQuery.STARRED) != 0;
		view.findViewById(R.id.star_button).setVisibility(
				starred ? View.VISIBLE : View.INVISIBLE);
	}

	/** {@link com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema} query parameters. */
	public interface AreasQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, AreasColumns.AREA_ID, AreasColumns.NAME,
				AreasColumns.COLOR, AreasColumns.REGION_ID, AreasColumns.STARRED};

		int _ID = 0;
		int AREA_ID = 1;
		int AREA_NAME = 2;
		int AREA_COLOR = 3;
		int REGION_ID = 4;
		int STARRED = 5;
	}
}
