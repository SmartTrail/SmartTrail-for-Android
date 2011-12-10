/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import static com.geozen.smarttrail.util.UIUtils.buildStyledSnippet;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.ui.TrailsAdapter.TrailsQuery;
import com.geozen.smarttrail.util.ActivityHelper;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

/**
 * A {@link ListFragment} showing a list of trails.
 */
public class TrailsFragment extends ListFragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	int TRAILS_TOKEN = 0x1; 
	int AREA_TOKEN = 0x2;
	int SEARCH_TOKEN = 0x3;

	private static final String STATE_CHECKED_POSITION = "checkedPosition";

	private Uri mAreaUri;
	private Cursor mCursor;
	private CursorAdapter mAdapter;
	private int mCheckedPosition = -1;
	private boolean mHasSetEmptyText = false;

	private NotifyingAsyncQueryHandler mHandler;
	private Handler mMessageQueueHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		reloadFromArguments(getArguments());
	}

	public void reloadFromArguments(Bundle arguments) {
		// Teardown from previous arguments
		if (mCursor != null) {
			getActivity().stopManagingCursor(mCursor);
			mCursor = null;
		}

		mCheckedPosition = -1;
		setListAdapter(null);

		mHandler.cancelOperation(SearchQuery._TOKEN);
		mHandler.cancelOperation(TRAILS_TOKEN);
		mHandler.cancelOperation(AREA_TOKEN);

		// Load new arguments
		final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
		final Uri trailsUri = intent.getData();
		final int trailQueryToken;

		if (trailsUri == null) {
			return;
		}

		String[] projection;
		if (TrailsSchema.isSearchUri(trailsUri)) {
			mAdapter = new SearchAdapter(getActivity());
			projection = SearchQuery.PROJECTION;
			trailQueryToken = SEARCH_TOKEN;

		} else {
			mAdapter = new TrailsAdapter(getActivity());
			projection = TrailsAdapter.TrailsQuery.PROJECTION;
			trailQueryToken = TRAILS_TOKEN;
			
		}

		setListAdapter(mAdapter);

		// Start background query to load trails
		mHandler.startQuery(trailQueryToken, null, trailsUri, projection,
				null, null, TrailsSchema.DEFAULT_SORT);

		// If caller launched us with specific track hint, pass it along when
		// launching trail details. Also start a query to load the track info.
		mAreaUri = intent.getParcelableExtra(TrailDetailFragment.EXTRA_AREA);
		if (mAreaUri != null) {
			mHandler.startQuery(AREA_TOKEN, mAreaUri,
					AreasQuery.PROJECTION);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		if (savedInstanceState != null) {
			mCheckedPosition = savedInstanceState.getInt(
					STATE_CHECKED_POSITION, -1);
		}

		if (!mHasSetEmptyText) {
			// Could be a bug, but calling this twice makes it become visible
			// when it shouldn't
			// be visible.
			setEmptyText(getString(R.string.empty_trails));
			mHasSetEmptyText = true;
		}
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (token == TRAILS_TOKEN || token == SEARCH_TOKEN) {
			onTrailOrSearchQueryComplete(cursor);
		} else if (token == AREA_TOKEN) {
			onAreaQueryComplete(cursor);
		} else {
			Log.d("SessionsFragment/onQueryComplete",
					"Query complete, Not Actionable: " + token);
			cursor.close();
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	private void onTrailOrSearchQueryComplete(Cursor cursor) {
		mCursor = cursor;
		getActivity().startManagingCursor(mCursor);
		mAdapter.changeCursor(mCursor);
		if (mCheckedPosition >= 0 && getView() != null) {
			getListView().setItemChecked(mCheckedPosition, true);
		}
	}

	/**
	 * Handle {@link AreasQuery} {@link Cursor}.
	 */
	private void onAreaQueryComplete(Cursor cursor) {
		if (cursor != null) {
			try {
				if (!cursor.moveToFirst()) {
					return;
				}

				// Use found track to build title-bar
				ActivityHelper activityHelper = ((BaseActivity) getActivity())
						.getActivityHelper();
				String areaName = cursor.getString(AreasQuery.AREA_NAME);
				activityHelper.setActionBarTitle(areaName);
				activityHelper.setActionBarColor(cursor
						.getInt(AreasQuery.AREA_COLOR));

				AnalyticsUtils.getInstance(getActivity()).trackPageView(
						"/Areas/" + areaName);
			} finally {
				cursor.close();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mMessageQueueHandler.post(mRefreshTrailsRunnable);
		getActivity().getContentResolver().registerContentObserver(
				TrailsSchema.CONTENT_URI, true, mTrailChangesObserver);
		if (mCursor != null) {
			mCursor.requery();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mMessageQueueHandler.removeCallbacks(mRefreshTrailsRunnable);
		getActivity().getContentResolver().unregisterContentObserver(
				mTrailChangesObserver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_CHECKED_POSITION, mCheckedPosition);
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Launch viewer for specific trail, passing along any trail area knowledge
		// that should influence the title-bar.
		
		//
		// remember this is shared between trailsadapter and searchadapter.
		//
		
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String trailId = cursor.getString(cursor.getColumnIndex(TrailsColumns.TRAIL_ID));
		final Uri trailUri = TrailsSchema.buildUri(trailId);
		final Intent intent = new Intent(Intent.ACTION_VIEW, trailUri);
		final String areaId = cursor.getString(cursor.getColumnIndex(TrailsColumns.AREA_ID));
		Uri areaUri = AreasSchema.buildAreaUri(areaId);
		intent.putExtra(TrailDetailFragment.EXTRA_AREA, areaUri);
		((BaseActivity) getActivity()).openActivityOrFragment(intent);

		getListView().setItemChecked(position, true);
		mCheckedPosition = position;
	}

	public void clearCheckedPosition() {
		if (mCheckedPosition >= 0) {
			getListView().setItemChecked(mCheckedPosition, false);
			mCheckedPosition = -1;
		}
	}

	
	/**
	 * {@link CursorAdapter} that renders a {@link SearchQuery}.
	 */
	private class SearchAdapter extends CursorAdapter {
		public SearchAdapter(Context context) {
			super(context, null);
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getActivity().getLayoutInflater().inflate(
					R.layout.list_item_trail, parent, false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView) view.findViewById(R.id.trail_title)).setText(cursor
					.getString(SearchQuery.NAME));

			// final String snippet =
			// cursor.getString(SearchQuery.SEARCH_SNIPPET);
			final String snippet = "";

			final Spannable styledSnippet = buildStyledSnippet(snippet);
			((TextView) view.findViewById(R.id.trail_subtitle))
					.setText(styledSnippet);

			final boolean starred = cursor.getInt(SearchQuery.STARRED) != 0;
			view.findViewById(R.id.star_button).setVisibility(
					starred ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private ContentObserver mTrailChangesObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (mCursor != null) {
				mCursor.requery();
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	private Runnable mRefreshTrailsRunnable = new Runnable() {
		public void run() {
			if (mAdapter != null) {
				// This is used to refresh trail title colors.
				mAdapter.notifyDataSetChanged();
			}

			// Check again on the next quarter hour, with some padding to
			// account for network
			// time differences.
			long nextQuarterHour = (SystemClock.uptimeMillis() / 900000 + 1) * 900000 + 5000;
			mMessageQueueHandler.postAtTime(mRefreshTrailsRunnable,
					nextQuarterHour);
		}
	};

	

	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema} query parameters.
	 */
	private interface AreasQuery {
		

		String[] PROJECTION = { BaseColumns._ID, AreasColumns.NAME, AreasColumns.COLOR };

		@SuppressWarnings("unused")
		int _ID = 0;
		int AREA_NAME = 1;
		int AREA_COLOR = 2;
	}

	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema} search query
	 * parameters.
	 */
	private interface SearchQuery {
		int _TOKEN = 0x3;

		String[] PROJECTION = { BaseColumns._ID, TrailsColumns.TRAIL_ID, TrailsColumns.AREA_ID, TrailsColumns.NAME,
				TrailsColumns.STARRED, };

		@SuppressWarnings("unused")
		int _ID = 0;
		@SuppressWarnings("unused")
		int TRAIL_ID = 1;
		@SuppressWarnings("unused")
		int AREA_ID = 2;
		int NAME = 3;
		int STARRED = 4;
	}
}
