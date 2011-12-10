/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

/**
 * A simple {@link ListFragment} that renders a list of areas with available
 * trails using a {@link AreasAdapter}.
 */
public class AreasFragment extends ListFragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {


	private AreasAdapter mAdapter;
	private NotifyingAsyncQueryHandler mHandler;
	private boolean mHasSetEmptyText = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity
				.fragmentArgumentsToIntent(getArguments());
		final Uri areasUri = intent.getData();

		mAdapter = new AreasAdapter(getActivity());
		setListAdapter(mAdapter);

		// Filter our areas query to only include those with valid results
		String[] projection = AreasAdapter.AreasQuery.PROJECTION;
		String selection = null;

		// Only show areas with at least one trail
		projection = AreasAdapter.AreasQuery.PROJECTION;

		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Areas");

		// Start background query to load areas
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		mHandler.startQuery(areasUri, projection, selection, null,
				AreasSchema.DEFAULT_SORT);
	}

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//		ViewGroup root = (ViewGroup) inflater.inflate(
//				R.layout.fragment_list_with_spinner, null);
//
//		// For some reason, if we omit this, NoSaveStateFrameLayout thinks we
//		// are FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the
//		// top of the activity.
//		root.setLayoutParams(new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.FILL_PARENT,
//				ViewGroup.LayoutParams.FILL_PARENT));
//		return root;
//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		if (!mHasSetEmptyText) {
			// Could be a bug, but calling this twice makes it become visible
			// when it shouldn't
			// be visible.
			setEmptyText(getString(R.string.empty_areas));
			mHasSetEmptyText = true;
		}
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		//int cnt = cursor.getCount();
		getActivity().startManagingCursor(cursor);
		mAdapter.changeCursor(cursor);
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String areaId;

		if (cursor != null) {
			areaId = cursor.getString(AreasAdapter.AreasQuery.AREA_ID);
			final Uri areaUri = AreasSchema.buildAreaUri(areaId);

			final Intent intent = new Intent(Intent.ACTION_VIEW, areaUri);

			// open AreaDetailActivity
			((BaseActivity) getActivity()).openActivityOrFragment(intent);

			getListView().setItemChecked(position, true);

		}
	}
}
