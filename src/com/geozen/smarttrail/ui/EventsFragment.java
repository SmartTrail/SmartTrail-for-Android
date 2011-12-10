/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsSchema;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

/**
 * A simple {@link ListFragment} that renders a list of tracks with available
 * trails or vendors (depending on {@link EventsFragment#EXTRA_NEXT_TYPE}) using
 * a {@link EventsAdapter}.
 */
public class EventsFragment extends ListFragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	public static final String EXTRA_NEXT_TYPE = "com.google.android.iosched.extra.NEXT_TYPE";

	private EventsAdapter mAdapter;
	private NotifyingAsyncQueryHandler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new EventsAdapter(getActivity());
		setListAdapter(mAdapter);

		// Filter our tracks query to only include those with valid results
		String[] projection = EventsQuery.PROJECTION;
		String selection = null;

		// Only show tracks with at least one trail
		projection = EventsQuery.PROJECTION;
		selection = "";
		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Events");

		// Start background query to load tracks
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);

		mHandler.startQuery(EventsSchema.CONTENT_URI, projection, selection,
				null, EventsSchema.DEFAULT_SORT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_list_with_spinner, null);

		// For some reason, if we omit this, NoSaveStateFrameLayout thinks we
		// are
		// FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top
		// of the activity.
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}
		if (cursor != null) {
			getActivity().startManagingCursor(cursor);
			mAdapter.changeCursor(cursor);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);

		if (cursor != null) {
			//final String eventId = cursor.getString(EventsQuery.EVENT_ID);
			final String url = cursor.getString(EventsQuery.URL);

			Intent intent = new Intent(getActivity(),
					EventActivity.class);

			intent.putExtra(WebFragment.EXTRA_URL, url);
			startActivity(intent);
			
			// open WebFragment
			((BaseActivity) getActivity()).openActivityOrFragment(intent);

			getListView().setItemChecked(position, true);

		}
	}

	public class EventsAdapter extends CursorAdapter {

		private Activity mActivity;

		public EventsAdapter(Activity activity) {
			super(activity, null);
			mActivity = activity;
		}

		/** {@inheritDoc} */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mActivity.getLayoutInflater().inflate(
					R.layout.list_item_event, parent, false);
		}

		/** {@inheritDoc} */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final TextView textView = (TextView) view
					.findViewById(android.R.id.text1);
			textView.setText(cursor.getString(EventsQuery.SNIPPET));

			final TextView date = (TextView) view
					.findViewById(android.R.id.text2);
			date.setText(cursor.getString(EventsQuery.DATE_SNIPPET));

		}

	}

	/** {@link com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema} query parameters. */
	public interface EventsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, EventsColumns.EVENT_ID,
				EventsColumns.ORG_ID, EventsColumns.SNIPPET,
				EventsColumns.DATE_SNIPPET, EventsColumns.URL };

		int _ID = 0;
		int EVENT_ID = 1;
		int ORG_ID = 2;
		int SNIPPET = 3;
		int DATE_SNIPPET = 4;
		int URL = 5;
	}
}
