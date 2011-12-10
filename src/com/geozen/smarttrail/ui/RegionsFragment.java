/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;


import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Region;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;

/**
 * A simple {@link ListFragment} that renders a list of regions using a
 * {@link RegionsAdapter}.
 */
public class RegionsFragment extends ListFragment {

	private RegionsAdapter mRegionsAdapter;
	private ArrayList<Region> mRegions;
	private PullRegionsTask mPullRegionsTask;
	private int mSelectedPosition = -1;
	private LinearLayout mListContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		mRegions = new ArrayList<Region>();
		String basePhotoUrl = app.getApi().getApiUri().buildUpon()
				.appendPath("photos").build().toString()
				+ "/";
		mRegionsAdapter = new RegionsAdapter(getActivity(), mRegions,
				basePhotoUrl);

		setListAdapter(mRegionsAdapter);

		AnalyticsUtils.getInstance(getActivity()).trackPageView("/Regions");

		loadRegions();
		setRetainInstance(true);

	}

	void loadRegions() {
		mPullRegionsTask = new PullRegionsTask();
		mPullRegionsTask.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_list_with_confirm, null);

		// For some reason, if we omit this, NoSaveStateFrameLayout thinks we
		// are FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the
		// top
		// of the activity.
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));

		mListContainer = (LinearLayout) root.findViewById(R.id.listContainer);
		mListContainer.setVisibility(View.GONE);

		//
		// Confirm
		//
		Button submitButton = (Button) root.findViewById(R.id.confirm);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String regionId = mRegions.get(mSelectedPosition).mId;
				SmartTrailApplication app = ((SmartTrailApplication) getActivity()
						.getApplication());
				app.setRegion(regionId);
				app.setRegionName(mRegions.get(mSelectedPosition).mName);
				app.setSponsorName(mRegions.get(mSelectedPosition).mSponsorName);
				app.setSponsorAlertsUrl(mRegions.get(mSelectedPosition).mSponsorAlertsUrl);
				app.setSponsorTwitterQuery(mRegions.get(mSelectedPosition).mSponsorTwitterQuery);

				Activity activity = getActivity();
				// ((BaseActivity) activity).triggerRefresh();

				// close it down
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
				activity.setResult(Activity.RESULT_OK);

				if (activity instanceof RegionsActivity) {
					activity.finish();
				}
			}

		});

		//
		// Cancel
		//
		Button cancel = (Button) root.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// close it down
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
				Activity activity = getActivity();
				if (activity instanceof RegionsActivity) {
					getActivity().finish();
				}

			}
		});

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (mSelectedPosition != -1) {
			getListView().setItemChecked(mSelectedPosition, true);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		getListView().setItemChecked(position, true);

		mSelectedPosition = position;
	}

	private class PullRegionsTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "PullRegionsTask";

		private Exception mReason;

		private ArrayList<Region> mRegionsList;

		private int mCurrentRegionPosition;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				SmartTrailApi api = app.getApi();
				String currentRegion = app.getRegion();

				JSONArray regions = api.pullRegions(-1, 20);

				mRegionsList = new ArrayList<Region>(regions.length());

				mCurrentRegionPosition = -1;
				for (int i = 0; i < regions.length(); i++) {

					Region region = new Region(regions.getJSONObject(i));
					mRegionsList.add(region);
					if (region.mId.equals(currentRegion)) {
						mCurrentRegionPosition = i;
					}

				}

			} catch (Exception e) {

				AppLog.e(CLASSTAG, "Exception adding condition.", e);
				mReason = e;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {

			if (success) {
				mRegions = mRegionsList;
				mRegionsAdapter.setData(mRegions);
				mRegionsAdapter.notifyDataSetChanged();
				mSelectedPosition = mCurrentRegionPosition;
				if (mCurrentRegionPosition != -1) {
					getListView().setItemChecked(mCurrentRegionPosition, true);
				}
				mListContainer.setVisibility(View.VISIBLE);
			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);
			}

		}

		@Override
		protected void onCancelled() {

		}
	}
}
