/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geozen.smarttrail.ui.tablet;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.ui.AreasAdapter;
import com.geozen.smarttrail.ui.BaseActivity;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;
import com.geozen.smarttrail.util.UIUtils;

/**
 * A tablet-specific fragment that is a giant {@link android.widget.Spinner}
 * -like widget. It shows a {@link ListPopupWindow} containing a list of tracks,
 * using {@link AreasAdapter}.
 * 
 * Requires API level 11 or later since {@link ListPopupWindow} is API level
 * 11+.
 */
public class AreasDropdownFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener,
		AdapterView.OnItemClickListener, PopupWindow.OnDismissListener {

	private boolean mAutoloadTarget = true;
	private Cursor mCursor;
	private AreasAdapter mAdapter;

	private ListPopupWindow mListPopupWindow;
	private ViewGroup mRootView;
	private TextView mTitle;
	private TextView mAbstract;

	private NotifyingAsyncQueryHandler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		mAdapter = new AreasAdapter(getActivity());

		if (savedInstanceState != null) {
			// Prevent auto-load behavior on orientation change.
			mAutoloadTarget = false;
		}

		reloadFromArguments(getArguments());
	}

	public void reloadFromArguments(Bundle arguments) {
		// Teardown from previous arguments
		if (mListPopupWindow != null) {
			mListPopupWindow.setAdapter(null);
		}
		if (mCursor != null) {
			getActivity().stopManagingCursor(mCursor);
			mCursor = null;
		}
		mHandler.cancelOperation(AreasAdapter.AreasQuery._TOKEN);

		// Load new arguments
		final Intent intent = BaseActivity.fragmentArgumentsToIntent(arguments);
		final Uri areaUri = intent.getData();
		if (areaUri == null) {
			return;
		}

		// Filter our tracks query to only include those with valid results
		String[] projection = AreasAdapter.AreasQuery.PROJECTION;
		String selection = null;
		// Only show tracks with at least one trail
		projection = AreasAdapter.AreasQuery.PROJECTION;
	

		// Start background query to load trail areas
		mHandler.startQuery(AreasAdapter.AreasQuery._TOKEN, null, areaUri,
				projection, selection, null, AreasSchema.DEFAULT_SORT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_areas_dropdown, null);
		mTitle = (TextView) mRootView.findViewById(R.id.area_title);
		mAbstract = (TextView) mRootView.findViewById(R.id.area_abstract);

		mRootView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mListPopupWindow = new ListPopupWindow(getActivity());
				mListPopupWindow.setAdapter(mAdapter);
				mListPopupWindow.setModal(true);
				mListPopupWindow.setContentWidth(400);
				mListPopupWindow.setAnchorView(mRootView);
				mListPopupWindow
						.setOnItemClickListener(AreasDropdownFragment.this);
				mListPopupWindow.show();
				mListPopupWindow
						.setOnDismissListener(AreasDropdownFragment.this);
			}
		});
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null || cursor == null) {
			return;
		}

		mCursor = cursor;
		getActivity().startManagingCursor(mCursor);

		// If there was a last-opened track, load it. Otherwise load the first
		// track.
		cursor.moveToFirst();
		String lastAreaID = UIUtils.getLastUsedAreaId(getActivity());
		if (lastAreaID != null) {
			while (!cursor.isAfterLast()) {
				if (lastAreaID.equals(cursor
						.getString(AreasAdapter.AreasQuery.AREA_ID))) {
					break;
				}
				cursor.moveToNext();
			}

			if (cursor.isAfterLast()) {
				loadArea(null, mAutoloadTarget);
			} else {
				loadArea(cursor, mAutoloadTarget);
			}
		} else {
			loadArea(null, mAutoloadTarget);
		}

		mAdapter.changeCursor(mCursor);
	}

	/** {@inheritDoc} */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		loadArea(cursor, true);

		if (cursor != null) {
			UIUtils.setLastUsedAreaId(getActivity(),
					cursor.getString(AreasAdapter.AreasQuery.AREA_ID));
		} else {
			UIUtils.setLastUsedAreaId(getActivity(), AreasSchema.ALL_AREAS_ID);
		}

		if (mListPopupWindow != null) {
			mListPopupWindow.dismiss();
		}
	}

	public void loadArea(Cursor cursor, boolean loadTargetFragment) {
		//final String areaId;
		final int areaColor;
		final Resources res = getResources();

		if (cursor != null) {
			String colorStr = cursor.getString(AreasAdapter.AreasQuery.AREA_COLOR);
			areaColor = Color.parseColor(colorStr);
			//areaId = cursor.getString(AreasAdapter.AreasQuery.AREA_ID);

			mTitle.setText(cursor.getString(AreasAdapter.AreasQuery.AREA_NAME));
			// mAbstract.setText(cursor.getString(AreasAdapter.AreasQuery.TRACK_ABSTRACT));

		} else {
			areaColor = res.getColor(R.color.all_area_color);
			//areaId = AreasSchema.ALL_AREAS_ID;

			mTitle.setText(R.string.all_trails_title);
			mAbstract.setText(R.string.all_trails_subtitle);
		}

		boolean isDark = UIUtils.isColorDark(areaColor);
		mRootView.setBackgroundColor(areaColor);

		if (isDark) {
			mTitle.setTextColor(res.getColor(R.color.body_text_1_inverse));
			mAbstract.setTextColor(res.getColor(R.color.body_text_2_inverse));
			mRootView
					.findViewById(R.id.area_dropdown_arrow)
					.setBackgroundResource(R.drawable.area_dropdown_arrow_light);
		} else {
			mTitle.setTextColor(res.getColor(R.color.body_text_1));
			mAbstract.setTextColor(res.getColor(R.color.body_text_2));
			mRootView.findViewById(R.id.area_dropdown_arrow)
					.setBackgroundResource(R.drawable.area_dropdown_arrow_dark);
		}

		if (loadTargetFragment) {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			
			intent.setData(AreasSchema.CONTENT_URI);

			((BaseActivity) getActivity()).openActivityOrFragment(intent);
		}
	}

	public void onDismiss() {
		mListPopupWindow = null;
	}
}
