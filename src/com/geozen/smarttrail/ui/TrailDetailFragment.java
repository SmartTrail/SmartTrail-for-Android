/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.service.SyncService;
import com.geozen.smarttrail.util.ActivityHelper;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.FractionalTouchDelegate;
import com.geozen.smarttrail.util.GeoUtil;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;
import com.geozen.smarttrail.util.TimeUtil;
import com.geozen.smarttrail.util.UnitsUtil;

/**
 * A fragment that shows detail information for a trail, including trail title,
 * length, elevation gain, ratings, description, directions, etc.
 */
public class TrailDetailFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener,
		CompoundButton.OnCheckedChangeListener {

	/**
	 * Since trails belong to areas, the parent activity can send this extra
	 * specifying an area URI that should be used for coloring the title-bar.
	 */
	public static final String EXTRA_AREA = "com.geozen.smarttrail.extra.AREA";

	private static final String TAG_INFO = "info";
	private static final String TAG_CONDITIONS = "conditions";

	private String mAreaId;
	private String mTrailId;

	private Uri mTrailUri;
	private Uri mAreaUri;

	private String mTitleString;

	private ViewGroup mRootView;
	private TabHost mTabHost;
	private TextView mTitle;
	private TextView mSubtitle;
	private CompoundButton mStarred;

	private NotifyingAsyncQueryHandler mHandler;

	private Cursor mConditionsCursor;
	private ConditionsAdapter mConditionsAdapter;
	private BroadcastReceiver mSyncReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			mConditionsCursor.requery();
			mConditionsAdapter.notifyDataSetChanged();
		}

	};

	private TextView mDescriptionTextView;

	private RatingBar mTechRating;

	private RatingBar mAerobicRating;

	private RatingBar mCoolRating;

	private LinearLayout mOverviewContainer;

	private View mDirectionsContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity
				.fragmentArgumentsToIntent(getArguments());
		mTrailUri = intent.getData();
		mTrailId = TrailsSchema.getTrailId(mTrailUri);

		mAreaUri = resolveAreaUri(intent);

		if (mTrailUri == null) {
			return;
		}

		mAreaId = AreasSchema.getAreaId(mAreaUri);

		setHasOptionsMenu(true);

	}

	@Override
	public void onResume() {
		super.onResume();
		// updateConditionsTab();

		// Start listening for time updates to adjust "now" bar. TIME_TICK is
		// triggered once per minute, which is how we move the bar over time.
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		filter.addDataScheme("package");
		getActivity().registerReceiver(mPackageChangesReceiver, filter);

		getActivity().registerReceiver(mSyncReceiver,
				new IntentFilter(SyncService.INTENT_ACTION_SYNC_COMPLETE));

		if (mConditionsCursor != null) {
			mConditionsCursor.requery();
		}

		// mTabHost.setCurrentTabByTag(TAG_CONDITIONS);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mPackageChangesReceiver);
		getActivity().unregisterReceiver(mSyncReceiver);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mTrailUri == null) {
			return;
		}

		if (mConditionsCursor != null) {
			getActivity().stopManagingCursor(mConditionsCursor);
			mConditionsCursor = null;
		}

		// Start background queries to load trail/area/conditions details
		final Uri conditionsUri = TrailsSchema.buildConditionsDirUri(mTrailId);

		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		mHandler.startQuery(TrailsQuery._TOKEN, mTrailUri,
				TrailsQuery.PROJECTION);
		mHandler.startQuery(AreasQuery._TOKEN, mAreaUri, AreasQuery.PROJECTION);
		// startQuery(int token, Object cookie, Uri uri, String[] projection,
		// String selection, String[] selectionArgs, String orderBy)
		Date now = new Date();
		String selection = ConditionsColumns.UPDATED_AT + " > "
				+ Long.toString(now.getTime() - 5 * TimeUtil.DAY_MS);
		mHandler.startQuery(ConditionsQuery._TOKEN, null, conditionsUri,
				ConditionsQuery.PROJECTION, selection, null,
				ConditionsColumns.UPDATED_AT + " DESC");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		mRootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_trail_detail, null);

		mTabHost = (TabHost) mRootView.findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTitle = (TextView) mRootView.findViewById(R.id.trail_title);
		mTitle.setTypeface(app.mTf);
		mSubtitle = (TextView) mRootView.findViewById(R.id.trail_subtitle);
		mStarred = (CompoundButton) mRootView.findViewById(R.id.star_button);

		mStarred.setFocusable(true);
		mStarred.setClickable(true);

		// Larger target triggers star toggle
		final View starParent = mRootView.findViewById(R.id.header_trail);
		FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(
				0.6f, 0f, 1f, 0.8f));

		setupInfoTab();
		setupConditionsTab();


		if (!TextUtils.isEmpty(app.mLastTabTag)) {
			mTabHost.setCurrentTabByTag(app.mLastTabTag);
		}

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				app.mLastTabTag = tabId;
			}
		});
		return mRootView;
	}

	/**
	 * Build a {@link View} to be used as a tab indicator, setting the requested
	 * string resource as its label.
	 * 
	 * @param textRes
	 * @return View
	 */
	private View buildIndicator(int textRes) {
		final TextView indicator = (TextView) getActivity().getLayoutInflater()
				.inflate(R.layout.tab_indicator,
						(ViewGroup) mRootView.findViewById(android.R.id.tabs),
						false);
		indicator.setText(textRes);
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		indicator.setTypeface(app.mTf);
		return indicator;
	}

	/**
	 * Derive
	 * {@link com.geozen.smarttrail.provider.ScheduleContract.Tracks#CONTENT_ITEM_TYPE}
	 * {@link Uri} based on incoming {@link Intent}, using {@link #EXTRA_AREA}
	 * when set.
	 * 
	 * @param intent
	 * @return Uri
	 */
	private Uri resolveAreaUri(Intent intent) {
		final Uri areaUri = intent.getParcelableExtra(EXTRA_AREA);
		if (areaUri != null) {
			return areaUri;
		} else {
			return TrailsSchema.buildAreasDirUri(mTrailId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (token == TrailsQuery._TOKEN) {
			onTrailQueryComplete(cursor);
		} else if (token == AreasQuery._TOKEN) {
			onAreaQueryComplete(cursor);
		} else if (token == ConditionsQuery._TOKEN) {
			onConditionsQueryComplete(cursor);
		} else {
			cursor.close();
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	@SuppressWarnings("unused")
	private void onTrailQueryComplete(Cursor cursor) {
		try {
			if (!cursor.moveToFirst()) {
				return;
			}

			final String trailName = cursor.getString(TrailsQuery.NAME);
			mTitle.setText(trailName);

			int lengthMeters = cursor.getInt(TrailsQuery.LENGTH);
			int gainMeters = cursor.getInt(TrailsQuery.ELEVATION_GAIN);

			if (false) {
				if (lengthMeters > 1000) {
					mSubtitle.setText(((float) lengthMeters / 1000)
							+ " km    gain: " + gainMeters + " m");
				} else {
					mSubtitle.setText(lengthMeters + " m    gain: "
							+ gainMeters + " m");
				}
			} else {
				float lengthMiles = UnitsUtil.metersToMiles(lengthMeters);
				float gainFeet = UnitsUtil.metersToFeet(gainMeters);
				String lengthStr = String.format("%2.1f", lengthMiles);
				String gainStr = String.format("%4.0f", gainFeet);
				mSubtitle.setText(lengthStr + " miles    gain: " + gainStr
						+ " ft");
			}

			float techRating = cursor.getInt(TrailsQuery.TECHNICAL_RATING);
			float aerobicRating = cursor.getInt(TrailsQuery.AEROBIC_RATING);
			float coolRating = cursor.getInt(TrailsQuery.COOL_RATING);

			mTechRating.setRating(techRating);
			mAerobicRating.setRating(aerobicRating);
			mCoolRating.setRating(coolRating);

			// Unregister around setting checked state to avoid triggering
			// listener since change isn't user generated.
			mStarred.setOnCheckedChangeListener(null);

			int intval = cursor.getInt(TrailsQuery.STARRED);
			boolean val = !(intval == 0);
			// boolean val = (intval != 0); huh? doesn't work
			mStarred.setChecked(val);
			mStarred.setOnCheckedChangeListener(this);

			final String description = cursor
					.getString(TrailsQuery.DESCRIPTION);

			AnalyticsUtils.getInstance(getActivity()).trackPageView(
					"/Trails/" + mTitleString);

			// mUrl = cursor.getString(TrailsQuery.URL);

			if (TextUtils.isEmpty(description)) {
				mDescriptionTextView.setText(R.string.noDescription);
				mOverviewContainer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
			} else {
				mDescriptionTextView.setText(Html.fromHtml(description));
				mOverviewContainer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Fragment frag = new TrailDescriptionFragment();

						Bundle args = new Bundle();
						args.putString(TrailDescriptionFragment.EXTRA_TRAIL_ID,
								mTrailId);
						args.putString(TrailDescriptionFragment.EXTRA_TRAIL_NAME,
								trailName);
						args.putString(TrailDescriptionFragment.EXTRA_TRAIL_OVERVIEW,
								description);

						frag.setArguments(args);

						FragmentTransaction transaction = getActivity()
								.getSupportFragmentManager().beginTransaction();

						// Replace whatever is in the fragment_container view
						// with
						// this
						// fragment, and add the transaction to the back stack
						transaction.replace(R.id.root_container, frag);
						transaction.addToBackStack(null);

						// Commit the transaction
						transaction.commit();

					}
				});
			}

			final float lat = GeoUtil.e6ToFloat(cursor
					.getInt(TrailsQuery.HEAD_LAT_E6));
			final float lon = GeoUtil.e6ToFloat(cursor
					.getInt(TrailsQuery.HEAD_LON_E6));

			mDirectionsContainer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					String url = "http://maps.google.com/maps?daddr="
							+ Float.toString(lat) + "," + Float.toString(lon);
					Intent intent = new Intent(
							android.content.Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);

				}
			});

		} finally {
			cursor.close();
		}
	}

	/**
	 * Handle {@link AreasQuery} {@link Cursor}.
	 */
	private void onAreaQueryComplete(Cursor cursor) {
		try {
			if (!cursor.moveToFirst()) {
				return;
			}

			// Use found track to build title-bar
			ActivityHelper activityHelper = ((BaseActivity) getActivity())
					.getActivityHelper();
			activityHelper.setActionBarTitle(cursor.getString(AreasQuery.NAME));
			activityHelper.setActionBarColor(cursor.getInt(AreasQuery.COLOR));
		} finally {
			cursor.close();
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	private void onConditionsQueryComplete(Cursor cursor) {
		mConditionsCursor = cursor;
		getActivity().startManagingCursor(mConditionsCursor);
		mConditionsAdapter.changeCursor(mConditionsCursor);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.trail_detail_menu_items, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// final String shareString;
		final Intent intent;

		switch (item.getItemId()) {
		case R.id.menu_map:
			intent = new Intent(getActivity().getApplicationContext(),
					TrailMapActivity.class);
			intent.putExtra(TrailMapActivity.EXTRA_AREA_ID, mAreaId);
			intent.putExtra(TrailMapActivity.EXTRA_TRAIL_ID, mTrailId);
			startActivity(intent);
			return true;

		case R.id.menu_add_condition:

			startAddConditionFragment(R.id.fragment_container_trail_detail);

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void startAddConditionFragment(int containerId) {
		final Intent intent;
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddConditionFragment();

			Bundle args = new Bundle();
			args.putString(AddConditionFragment.EXTRA_TRAILS_ID, mTrailId);
			args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
					AreasSchema.buildTrailsUri(mAreaId));
			frag.setArguments(args);

			FragmentTransaction transaction = getActivity()
					.getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment, and add the transaction to the back stack
			transaction.replace(containerId, frag);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		} else {
			if (app.signedInBefore()) {
				intent = new Intent(getActivity(), SigninActivity.class);
			} else {
				intent = new Intent(getActivity(), SignupActivity.class);
			}
			startActivity(intent);
		}
	}

	/**
	 * Handle toggling of starred checkbox.
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final ContentValues values = new ContentValues();
		values.put(TrailsSchema.STARRED, isChecked ? 1 : 0);
		mHandler.startUpdate(mTrailUri, values);

		// Because change listener is set to null during initialization, these
		// won't fire on pageview.
	}

	/**
	 * Build and add "info" tab.
	 */
	private void setupInfoTab() {
		// Summary content comes from existing layout
		mTabHost.addTab(mTabHost.newTabSpec(TAG_INFO)
				.setIndicator(buildIndicator(R.string.trail_info))
				.setContent(R.id.tab_trail_info));
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		TextView descriptionTitleTextView = (TextView) mTabHost.findViewById(R.id.descripTitle);
		descriptionTitleTextView.setTypeface(app.mTf);
		
		mDescriptionTextView = (TextView) mTabHost.findViewById(R.id.overview);
		mTechRating = (RatingBar) mTabHost.findViewById(R.id.technicalRating);
		mAerobicRating = (RatingBar) mTabHost.findViewById(R.id.aerobicRating);
		mCoolRating = (RatingBar) mTabHost.findViewById(R.id.coolRating);

		mOverviewContainer = (LinearLayout) mTabHost
				.findViewById(R.id.overviewContainer);
		mDirectionsContainer = (LinearLayout) mTabHost
				.findViewById(R.id.directionsContainer);

		TextView directionsTitleTextView = (TextView) mTabHost.findViewById(R.id.directionsTitle);
		directionsTitleTextView.setTypeface(app.mTf);
	}

	private void updateConditionsTab() {

	}

	/**
	 * Build and add "conditions" tab.
	 */
	private void setupConditionsTab() {

		mConditionsAdapter = new ConditionsAdapter(getActivity());

		ListView listView = (ListView) mRootView
				.findViewById(R.id.conditionsList);
		listView.setAdapter(mConditionsAdapter);

		View emptyView = mRootView.findViewById(R.id.empty);
		listView.setEmptyView(emptyView);

		// Setup tab
		mTabHost.addTab(mTabHost.newTabSpec(TAG_CONDITIONS)
				.setIndicator(buildIndicator(R.string.trail_conditions))
				.setContent(R.id.tab_trail_conditions));
	}

	public void fireConditionsEvent(int actionId) {
		AnalyticsUtils.getInstance(getActivity()).trackEvent("Trail Details",
				getActivity().getString(actionId), mTitleString, 0);
	}

	/**
	 * // * Build and add "summary" tab. //
	 */

	private BroadcastReceiver mPackageChangesReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateConditionsTab();
		}
	};

	/**
	 * {@link com.geozen.smarttrail.provider.TrailsSchema} query parameters.
	 */
	private interface TrailsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = {

		TrailsColumns.NAME, TrailsColumns.STARRED, TrailsColumns.DESCRIPTION,
				TrailsColumns.URL, TrailsColumns.OWNER, TrailsColumns.LENGTH,
				TrailsColumns.ELEVATION_GAIN, TrailsColumns.TECH_RATING,
				TrailsColumns.AEROBIC_RATING, TrailsColumns.COOL_RATING,
				TrailsColumns.HEAD_LAT_E6, TrailsColumns.HEAD_LON_E6 };

		int NAME = 0;
		int STARRED = 1;
		int DESCRIPTION = 2;
		@SuppressWarnings("unused")
		int URL = 3;
		@SuppressWarnings("unused")
		int OWNER = 4;
		int LENGTH = 5;
		int ELEVATION_GAIN = 6;
		int TECHNICAL_RATING = 7;
		int AEROBIC_RATING = 8;
		int COOL_RATING = 9;
		int HEAD_LAT_E6 = 10;
		int HEAD_LON_E6 = 11;

	}

	/**
	 * {@link com.geozen.smarttrail.provider.ScheduleContract.Tracks} query parameters.
	 */
	private interface AreasQuery {
		int _TOKEN = 0x2;

		String[] PROJECTION = { AreasColumns.NAME, AreasColumns.COLOR, };

		int NAME = 0;
		int COLOR = 1;
	}

	private interface ConditionsQuery extends ConditionsAdapter.ConditionsQuery {
		int _TOKEN = 0x3;
	}

}
