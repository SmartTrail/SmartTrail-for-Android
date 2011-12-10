/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Review;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.service.SyncService;
import com.geozen.smarttrail.ui.TrailsAdapter.TrailsQuery;
import com.geozen.smarttrail.ui.phone.ReviewDetailActivity;
import com.geozen.smarttrail.util.ActivityHelper;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.FractionalTouchDelegate;
import com.geozen.smarttrail.util.NotificationsUtil;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

/**
 * A fragment that shows detail information for a trail, including trail title,
 * abstract, time information, etc.
 */
public class AreaDetailFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener,
		CompoundButton.OnCheckedChangeListener {

	int TRAILS_TOKEN = 0x1;
	int AREA_TOKEN = 0x2;

	/**
	 * Since trails belong to areas, the parent activity can send this extra
	 * specifying an area URI that should be used for coloring the title-bar.
	 */
	public static final String EXTRA_AREA = "com.geozen.smarttrail.extra.AREA";

	private static final String TAG_INFO = "info";
	private static final String TAG_TRAILS = "trails";
	private static final String TAG_REVIEWS = "reviews";

	// private static StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);
	private ArrayList<Review> mReviews;

	private String mAreaId;

	private Uri mAreaUri;

	private String mAreaName;

	private ViewGroup mRootView;
	private TabHost mTabHost;
	private TextView mTitle;
	private RatingBar mAreaRating;
	private TextView mReviewsLabel;
	private CompoundButton mStarred;

	private NotifyingAsyncQueryHandler mHandler;

	private Cursor mTrailsCursor;
	private TrailsAdapter mTrailsAdapter;
	private BroadcastReceiver mSyncReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			mTrailsCursor.requery();
			mTrailsAdapter.notifyDataSetChanged();

			// start the query for the area
			mHandler.startQuery(AREA_TOKEN, mAreaUri, AreasQuery.PROJECTION);

			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();

			app.mSyncReviews = true;
			if (app.mLastTabTag.equals(TAG_REVIEWS)) {
				loadReviews();
				app.mSyncReviews = false;
			}

		}

	};

	// private TextView mDescription;
	private ReviewsAdapter mReviewsAdapter;
	private ListView mReviewListView;
	private PullReviewsTask mPullReviewsTask;

	private ProgressBar mReviewsProgressBar;
	private TextView mEmptyView;
	private TextView mOwnerTextView;
	private TextView mNumTrailsTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity
				.fragmentArgumentsToIntent(getArguments());
		mAreaUri = intent.getData();
		mAreaId = AreasSchema.getAreaId(mAreaUri);

		if (mAreaUri == null) {
			return;
		}

		setHasOptionsMenu(true);
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		mReviews = new ArrayList<Review>();
		String basePhotoUrl = app.getApi().getApiUri().buildUpon()
				.appendPath("photos").build().toString()
				+ "/";
		mReviewsAdapter = new ReviewsAdapter(getActivity(), mReviews,
				basePhotoUrl);

		this.setRetainInstance(true);

		app.mSyncReviews = true;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(mSyncReceiver,
				new IntentFilter(SyncService.INTENT_ACTION_SYNC_COMPLETE));

		if (mTrailsCursor != null) {
			mTrailsCursor.requery();
		}

		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();

		if (app.mLastTabTag.equals(TAG_REVIEWS)) {
			if (app.mSyncReviews) {
				loadReviews();
				app.mSyncReviews = false;
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mSyncReceiver);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mAreaUri == null) {
			return;
		}

		if (mTrailsCursor != null) {
			getActivity().stopManagingCursor(mTrailsCursor);
			mTrailsCursor = null;
		}

		// Start background queries to load areas/trails

		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);

		// start the query for the area
		mHandler.startQuery(AREA_TOKEN, mAreaUri, AreasQuery.PROJECTION);

		// start the query for the trails in the area
		final Uri trailsUri = AreasSchema.buildTrailsUri(mAreaId);

		// some info:
		// startQuery(int token, Object cookie, Uri uri, String[] projection,
		// String selection, String[] selectionArgs, String orderBy)

		mHandler.startQuery(TRAILS_TOKEN, null, trailsUri,
				TrailsAdapter.TrailsQuery.PROJECTION, null, null,
				TrailsColumns.NAME + " ASC");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();

		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_area_detail,
				null);

		mTabHost = (TabHost) mRootView.findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTitle = (TextView) mRootView.findViewById(R.id.trail_title);
		mTitle.setTypeface(app.mTf);
		mAreaRating = (RatingBar) mRootView.findViewById(R.id.areaRating);
		mReviewsLabel = (TextView) mRootView.findViewById(R.id.reviewsLabel);
		mStarred = (CompoundButton) mRootView.findViewById(R.id.star_button);
		// mStarred.setVisibility(View.GONE);
		mStarred.setFocusable(true);
		mStarred.setClickable(true);

		// Larger target triggers star toggle
		final View starParent = mRootView.findViewById(R.id.header_trail);
		FractionalTouchDelegate.setupDelegate(starParent, mStarred, new RectF(
				0.6f, 0f, 1f, 0.8f));

		// mDescription = (TextView) mTabHost.findViewById(R.id.description);

		setupInfoTab();
		setupReviewsTab();
		setupTrailsTab();

		if (!TextUtils.isEmpty(app.mLastTabTag)) {
			mTabHost.setCurrentTabByTag(app.mLastTabTag);
		}

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				app.mLastTabTag = tabId;

				if (tabId.equals(TAG_REVIEWS)) {
					if (app.mSyncReviews) {
						loadReviews();
						app.mSyncReviews = false;
					}
				}
			}
		});

		ActivityHelper activityHelper = ((BaseActivity) getActivity())
				.getActivityHelper();
		activityHelper.setActionBarTitle("");

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
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		indicator.setTypeface(app.mTf);
		indicator.setText(textRes);
		return indicator;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (token == TRAILS_TOKEN) {
			onTrailsQueryComplete(cursor);
		} else if (token == AREA_TOKEN) {
			onAreaQueryComplete(cursor);
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	private void onAreaQueryComplete(Cursor cursor) {
		if (cursor != null) {
			try {
				if (!cursor.moveToFirst()) {
					return;
				}

				mAreaName = cursor.getString(AreasQuery.NAME);
				mTitle.setText(mAreaName + " " + getString(R.string.area));
				String owner = cursor.getString(AreasQuery.OWNER);
				mOwnerTextView.setText(owner);

				int numReviews = cursor.getInt(AreasQuery.NUM_REVIEWS);
				float rating = cursor.getFloat(AreasQuery.RATING);
				mAreaRating.setRating(rating);
				mReviewsLabel.setText(Integer.toString(numReviews) + " "
						+ getString(R.string.reviews));
				// Unregister around setting checked state to avoid triggering
				// listener since change isn't user generated.
				mStarred.setOnCheckedChangeListener(null);

				int intval = cursor.getInt(AreasQuery.STARRED);
				boolean val = !(intval == 0);
				// boolean val = (intval != 0); huh? doesn't work
				mStarred.setChecked(val);
				mStarred.setOnCheckedChangeListener(this);

				// final String description = cursor
				// .getString(AreasQuery.DESCRIPTION);

				AnalyticsUtils.getInstance(getActivity()).trackPageView(
						"/Areas/" + mAreaName);

				// mDescription.setText(description);

			} finally {
				cursor.close();
			}
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	private void onTrailsQueryComplete(Cursor cursor) {
		mTrailsCursor = cursor;
		getActivity().startManagingCursor(mTrailsCursor);
		mTrailsAdapter.changeCursor(mTrailsCursor);

		int numTrails = cursor.getCount();
		mNumTrailsTextView.setText(Integer.toString(numTrails));
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
			intent.putExtra(TrailMapActivity.EXTRA_TRAIL_ID, "");
			startActivity(intent);
			return true;

		case R.id.menu_add_condition:

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.addAreaContent);
			CharSequence[] items;
			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();
			if (app.getIsPatroller()) {
				items = getActivity().getResources().getTextArray(
						R.array.addContentOptionsPatroller);
			} else {
				items = getActivity().getResources().getTextArray(
						R.array.addContentOptions);
			}

			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {

					case 0:
						startAddReviewFragment(R.id.root_container);
						break;

					default:
					case 1:
						startAddConditionFragment(R.id.root_container);
						break;

					case 2:
						startAddPatrolFragment(R.id.root_container);
						break;

					}

				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void startAddConditionFragment(int containerId) {
		final Intent intent;
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddConditionFragment();

			Bundle args = new Bundle();
			args.putString(AddConditionFragment.EXTRA_TRAILS_ID, "");
			args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
					AreasSchema.buildTrailsUri(mAreaId));
			frag.setArguments(args);

			FragmentTransaction transaction = getActivity()
					.getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack
			transaction.replace(containerId, frag);
			// transaction.replace(R.id.root_container, frag);
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

	public void startAddReviewFragment(int containerId) {
		final Intent intent;
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddReviewFragment();

			Bundle args = new Bundle();
			args.putString(AddReviewFragment.EXTRA_AREA_ID, mAreaId);
			args.putString(AddReviewFragment.EXTRA_AREA_NAME, mAreaName);

			frag.setArguments(args);

			FragmentTransaction transaction = getActivity()
					.getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment, and add the transaction to the back stack
			transaction.replace(containerId, frag);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
			app.mSyncReviews = true;
		} else {
			if (app.signedInBefore()) {
				intent = new Intent(getActivity(), SigninActivity.class);
			} else {
				intent = new Intent(getActivity(), SignupActivity.class);
			}
			startActivity(intent);
		}
	}

	public void startAddPatrolFragment(int containerId) {
		final Intent intent;
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddPatrolFragment();

			Bundle args = new Bundle();
			args.putString(AddConditionFragment.EXTRA_TRAILS_ID, "");
			args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
					AreasSchema.buildTrailsUri(mAreaId));
			frag.setArguments(args);

			FragmentTransaction transaction = getActivity()
					.getSupportFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack
			transaction.replace(containerId, frag);
			// transaction.replace(R.id.root_container, frag);
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
		values.put(AreasSchema.STARRED, isChecked ? 1 : 0);
		mHandler.startUpdate(mAreaUri, values);

	}

	/**
	 * Build and add "info" tab.
	 */
	private void setupInfoTab() {
		// Summary content comes from existing layout
		mTabHost.addTab(mTabHost.newTabSpec(TAG_INFO)
				.setIndicator(buildIndicator(R.string.trail_info))
				.setContent(R.id.tab_area_info));

		mOwnerTextView = (TextView) mRootView.findViewById(R.id.owner);
		mNumTrailsTextView = (TextView) mRootView.findViewById(R.id.numTrails);
	}

	private void setupReviewsTab() {
		// Summary content comes from existing layout
		mTabHost.addTab(mTabHost.newTabSpec(TAG_REVIEWS)
				.setIndicator(buildIndicator(R.string.reviewsTitle))
				.setContent(R.id.tab_area_reviews));

		mReviewListView = (ListView) mRootView.findViewById(R.id.reviewsList);
		mReviewListView.setAdapter(mReviewsAdapter);
		mReviewListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {

				final Review review = (Review) mReviewsAdapter
						.getItem(position);

				final Intent intent = new Intent(getActivity(),
						ReviewDetailActivity.class);

				intent.putExtra(ReviewDetailFragment.EXTRA_USERNAME,
						review.mUsername);
				intent.putExtra(ReviewDetailFragment.EXTRA_REVIEW,
						review.mReview);
				intent.putExtra(ReviewDetailFragment.EXTRA_RATING,
						review.mRating);
				intent.putExtra(ReviewDetailFragment.EXTRA_PHOTO_ID,
						review.mPhotoId);
				intent.putExtra(ReviewDetailFragment.EXTRA_NUM_REVIEWS,
						review.mUserNumReviews);
				intent.putExtra(ReviewDetailFragment.EXTRA_UPDATED_AT_STR,
						review.mUpdatedAtStr);

				((BaseActivity) getActivity()).openActivityOrFragment(intent);

			}
		});

		mEmptyView = (TextView) mRootView.findViewById(R.id.emptyReviews);
		mReviewListView.setEmptyView(mEmptyView);

		mReviewsProgressBar = (ProgressBar) mRootView
				.findViewById(R.id.reviewsProgressBar);
	}

	void loadReviews() {
		mPullReviewsTask = new PullReviewsTask();
		mPullReviewsTask.execute();
	}

	/**
	 * Build and add "conditions" tab.
	 */
	private void setupTrailsTab() {

		mTrailsAdapter = new TrailsAdapter(getActivity());

		ListView listView = (ListView) mRootView
				.findViewById(R.id.conditionsList);
		listView.setAdapter(mTrailsAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {

				final Cursor cursor = (Cursor) mTrailsAdapter.getItem(position);
				final String trailId = cursor.getString(cursor
						.getColumnIndex(TrailsColumns.TRAIL_ID));
				final Uri trailUri = TrailsSchema.buildUri(trailId);
				final Intent intent = new Intent(Intent.ACTION_VIEW, trailUri);
				final String areaId = cursor.getString(cursor
						.getColumnIndex(TrailsColumns.AREA_ID));
				Uri areaUri = AreasSchema.buildAreaUri(areaId);
				intent.putExtra(TrailDetailFragment.EXTRA_AREA, areaUri);
				((BaseActivity) getActivity()).openActivityOrFragment(intent);

			}
		});

		View emptyView = mRootView.findViewById(R.id.empty);
		listView.setEmptyView(emptyView);

		// Setup tab
		mTabHost.addTab(mTabHost.newTabSpec(TAG_TRAILS)
				.setIndicator(buildIndicator(R.string.title_trails))
				.setContent(R.id.tab_trail_conditions));

	}

	public void fireConditionsEvent(int actionId) {
		AnalyticsUtils.getInstance(getActivity()).trackEvent("Trail Details",
				getActivity().getString(actionId), mAreaName, 0);
	}

	/**
	 * // * Build and add "summary" tab. //
	 */

	/**
	 * {@link com.geozen.smarttrail.provider.ScheduleContract.Tracks} query
	 * parameters.
	 */
	private interface AreasQuery {

		String[] PROJECTION = { AreasColumns.NAME, AreasColumns.STARRED,
				AreasColumns.DESCRIPTION, AreasColumns.NUM_REVIEWS,
				AreasColumns.RATING, AreasColumns.OWNER };

		int NAME = 0;
		int STARRED = 1;
		@SuppressWarnings("unused")
		int DESCRIPTION = 2;
		int NUM_REVIEWS = 3;
		int RATING = 4;
		int OWNER = 5;
	}

	private class PullReviewsTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "PullReviewsTask";

		private Exception mReason;

		private ArrayList<Review> mReviewsList;

		@Override
		protected void onPreExecute() {

			mEmptyView.setVisibility(View.INVISIBLE);
			mReviewsProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				SmartTrailApi api = app.getApi();

				JSONArray reviews = api.pullReviewsByArea(mAreaId, -1, 20);

				mReviewsList = new ArrayList<Review>(reviews.length());

				for (int i = 0; i < reviews.length(); i++) {

					Review review = new Review(reviews.getJSONObject(i));
					mReviewsList.add(review);

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
				mReviews = mReviewsList;
				mReviewsAdapter.setData(mReviews);

				mReviewsAdapter.notifyDataSetChanged();

			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);
				// ((OnAddConditionListener)
				// getActivity()).onConditionCancelled();
			}

			mReviewsProgressBar.setVisibility(View.GONE);

		}

		@Override
		protected void onCancelled() {
			mReviewsProgressBar.setVisibility(View.GONE);

		}
	}

}
