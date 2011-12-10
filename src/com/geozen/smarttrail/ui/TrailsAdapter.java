/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.app.Config;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.util.TimeUtil;
import com.geozen.smarttrail.util.UIUtils;

/**
 * {@link CursorAdapter} that renders a {@link TrailsQuery}.
 */
class TrailsAdapter extends CursorAdapter {
	private Activity mActivity;
	private SmartTrailApplication mApp;

	public TrailsAdapter(Activity activity) {
		super(activity, null);
		mActivity = activity;
		mApp = (SmartTrailApplication) activity.getApplication();
	}

	/** {@inheritDoc} */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mActivity.getLayoutInflater().inflate(R.layout.list_item_trail,
				parent, false);
	}

	/** {@inheritDoc} */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView titleView = (TextView) view
				.findViewById(R.id.trail_title);
		final TextView subtitleView = (TextView) view
				.findViewById(R.id.trail_subtitle);
		// Trail condition status summary
		String status = cursor.getString(TrailsQuery.STATUS);
		long updatedAt = cursor.getLong(TrailsQuery.STATUS_UPDATED_AT);
		long now = new GregorianCalendar().getTimeInMillis();

		// int color = Condition.getStatusColor(context, status);
		titleView.setTextColor(Condition.getStatusColor(context, status));
		titleView.setText(cursor.getString(TrailsQuery.NAME));
		titleView.setTypeface(mApp.mTf);
		long delta = now - updatedAt;
		final String subtitle;
		
		if (status.equals(Condition.CLOSED)) {
			subtitle = status;
		} else {
			if (delta < Config.UNKNOWN_WINDOW_MS) {
				//subtitle = status + ": " + TimeUtil.ago(delta);
				subtitle = TimeUtil.ago(delta);
			} else {
				status = Condition.UNKNOWN;
				subtitle = "Unknown";
			}
		}

		subtitleView.setText(subtitle);

		ImageView statusView = (ImageView) view.findViewById(R.id.status);
		statusView.setImageResource(Condition.getStatusImageResource(context,
				status));

		final boolean starred = cursor.getInt(TrailsQuery.STARRED) != 0;
		view.findViewById(R.id.star_button).setVisibility(
				starred ? View.VISIBLE : View.INVISIBLE);

		// TODO
		// Possibly indicate that the trail is closed
		UIUtils.setTrailTitleColor(titleView, subtitleView);
	}

	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema} query parameters.
	 */
	public interface TrailsQuery {

		String[] PROJECTION = { BaseColumns._ID, TrailsColumns.TRAIL_ID,
				TrailsColumns.NAME, TrailsColumns.STARRED,
				TrailsColumns.CONDITION, TrailsColumns.STATUS_UPDATED_AT,
				"trails." + TrailsColumns.AREA_ID };

		int _ID = 0;
		int TRAIL_ID = 1;
		int NAME = 2;
		int STARRED = 3;
		int STATUS = 4;
		int STATUS_UPDATED_AT = 5;
		int AREA_ID = 6;

	}
}
