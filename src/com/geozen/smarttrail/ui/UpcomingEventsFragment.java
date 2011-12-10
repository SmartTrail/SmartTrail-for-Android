/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.model.Event;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.EventsSchema;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;
import com.geozen.smarttrail.util.UIUtils;

/**
 * A fragment used in {@link HomeActivity} that cycles through upcoming events
 * and shows their countdowns. It also shows a 'Realtime Search' button on
 * phones, as a replacement for the {@link TagStreamFragment} that is visible on
 * tablets on the home screen.
 */
public class UpcomingEventsFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	protected static final long SHOW_EVENT_DURATION_MS = 8 * 1000;

	private Handler mMessageHandler = new Handler();

	private ViewGroup mRootView;

	private NotifyingAsyncQueryHandler mHandler;

	private ArrayList<Event> mEvents;
	private int mCurrentEventIndex;
	private Event mCurrentEvent;

	private long mLastRefreshTimestamp;

	// private TextSwitcher mSwitcher;

	private TextView mEventTextView;

	private int mSwipeMinDistance;

	private int mSwipeThresholdVelocity;

	private OnTouchListener mGestureListener;

	private OnClickListener mOnClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);

		final Uri eventsUri = EventsSchema.CONTENT_URI;
		// Start background query to load events
		mHandler.startQuery(EventsQuery._TOKEN, null, eventsUri,
				EventsQuery.PROJECTION, null, null, EventsSchema.DEFAULT_SORT);

		mEvents = new ArrayList<Event>();

		final ViewConfiguration vc = ViewConfiguration.get(getActivity());
		mSwipeMinDistance = vc.getScaledTouchSlop();
		mSwipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		// Gesture detection
		final GestureDetector gestureDetector = new GestureDetector(
				new MyGestureDetector());
		mGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		mOnClickListener = new View.OnClickListener() {
			public void onClick(View view) {

				if (mCurrentEvent != null) {
					Intent intent = new Intent(getActivity(),
							EventActivity.class);

					intent.putExtra(WebFragment.EXTRA_URL, mCurrentEvent.mUrl);
					startActivity(intent);
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_whats_on,
				container);
		setupView();
		return mRootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mMessageHandler.removeCallbacks(mCountdownRunnable);
	}

	private void setupView() {
		mMessageHandler.removeCallbacks(mCountdownRunnable);
		mRootView.removeAllViews();

		mEventTextView = (TextView) getActivity().getLayoutInflater().inflate(
				R.layout.whats_on_countdown, mRootView, false);
		final SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		mEventTextView.setTypeface(app.mTf);
		mEventTextView.setOnTouchListener(mGestureListener);
		mRootView.addView(mEventTextView);

		// mSwitcher = (TextSwitcher) getActivity().getLayoutInflater().inflate(
		// R.layout.whats_on_switcher, mRootView, false);
		//
		//
		// mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
		// @Override
		// public View makeView() {
		// Resources resources = getActivity().getResources();
		//
		// TextView t = new TextView(getActivity());
		// // TextView t = (TextView) getActivity().getLayoutInflater().inflate(
		// // R.layout.event, null, false);
		// t.setTextColor(resources.getColor(R.color.accent_1));
		// t.setGravity(Gravity.CENTER);
		// // t.setLayoutParams(new LayoutParams( (int)
		// resources.getDimension(R.dimen.whats_on_height),
		// // (int) resources.getDimension(R.dimen.whats_on_height)));
		//
		// t.setLayoutParams(new ViewGroup.LayoutParams(1,
		// ViewGroup.LayoutParams.FILL_PARENT));
		// return t;
		//
		// }
		// });
		//
		// mRootView.addView(mSwitcher);

		// Animation in = AnimationUtils.loadAnimation(getActivity(),
		// android.R.anim.fade_in);
		// Animation out = AnimationUtils.loadAnimation(getActivity(),
		// android.R.anim.fade_out);
		// mSwitcher.setInAnimation(in);
		// mSwitcher.setOutAnimation(out);

		if (!UIUtils.isHoneycombTablet(getActivity())) {
			View separator = new View(getActivity());
			separator.setLayoutParams(new ViewGroup.LayoutParams(1,
					ViewGroup.LayoutParams.FILL_PARENT));
			separator.setBackgroundResource(R.drawable.whats_on_separator);
			mRootView.addView(separator);

			TextView view = (TextView) getActivity().getLayoutInflater()
					.inflate(R.layout.whats_on_stream, mRootView, false);
			view.setTypeface(app.mTf);

			view.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					AnalyticsUtils.getInstance(getActivity()).trackEvent(
							"Home Screen Dashboard", "Click",
							"Realtime Stream", 0);
					// Intent intent = new Intent(getActivity(),
					// TagStreamActivity.class);
					// startActivity(intent);
					// String searchString = "#boco_trails OR #valmontbikepark";
					String searchString = app.getSponsorTwitterQuery();
					String url;
					try {
						url = "https://mobile.twitter.com/searches?q="
								+ URLEncoder.encode(searchString, "UTF-8");
						final Intent intent = new Intent(Intent.ACTION_VIEW)
								.setData(Uri.parse(url));
						startActivity(intent);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			mRootView.addView(view);
		}

	}

	void refresh() {
		mMessageHandler.removeCallbacks(mCountdownRunnable);

		final long currentTimeMillis = UIUtils.getCurrentTime(getActivity());
		mLastRefreshTimestamp = currentTimeMillis;

		if (mCurrentEvent != null) {
			// Show Loading... and load the view corresponding to the current
			// state
			if (currentTimeMillis < mCurrentEvent.mStartTimestamp) {
				setupBefore();
			} else if (currentTimeMillis > mCurrentEvent.mEndTimestamp) {
				setupAfter();
			} else {
				setupDuring();
			}
		} else {
			setupAfter();
		}

	}

	private void setupBefore() {
		// Before conference, show countdown.

		mEventTextView.setOnClickListener(mOnClickListener);
		mMessageHandler.post(mCountdownRunnable);
	}

	private void setupAfter() {
		// After conference, show canned text.
		// getActivity().getLayoutInflater().inflate(R.layout.whats_on_thank_you,
		// mRootView, true);
		mEventTextView.setText(R.string.whats_on_thank_you_title);
		mMessageHandler.post(mCountdownRunnable);
	}

	private void setupDuring() {
		// Event in progress
		mEventTextView.setOnClickListener(mOnClickListener);
	}

	/**
	 * Event that updates countdown timer. Posts itself again to
	 * {@link #mMessageHandler} to continue updating time.
	 */
	private Runnable mCountdownRunnable = new Runnable() {

		public void run() {
			if (mCurrentEvent != null) {
				long now = System.currentTimeMillis();
				int remainingSec = (int) Math.max(0,
						(mCurrentEvent.mStartTimestamp - now) / 1000);
				// boolean refresh = remainingSec == 0;
				boolean refresh = false;

				long delta = now - mLastRefreshTimestamp;
				if (delta > SHOW_EVENT_DURATION_MS && mEvents.size() > 1) {
					goToNextEvent();
					refresh = true;
				}

				if (refresh) {
					// Conference started while in countdown mode, switch modes
					// and
					// bail on future countdown updates.
					mMessageHandler.postDelayed(new Runnable() {
						public void run() {
							refresh();
						}
					}, 100);
					return;
				}

				final int secs = remainingSec % 86400;
				final int days = remainingSec / 86400;
				final String str = getResources().getQuantityString(
						R.plurals.whats_on_countdown_title, days, days,
						DateUtils.formatElapsedTime(secs));

				mEventTextView.setText(mCurrentEvent.mSnippet + " in " + str);

				// Repost ourselves to keep updating countdown
				mMessageHandler.postDelayed(mCountdownRunnable, 1000);
			}
		}

	};

	private void goToNextEvent() {
		if (mEvents.size() > 0) {
			mCurrentEventIndex++;
			if (mCurrentEventIndex >= mEvents.size()) {
				mCurrentEventIndex = 0;
			}
			mCurrentEvent = mEvents.get(mCurrentEventIndex);
		} else {
			mCurrentEventIndex = -1;
			mCurrentEvent = null;
		}
	}

	private void goToPrevEvent() {
		if (mEvents.size() > 0) {
			mCurrentEventIndex--;
			if (mCurrentEventIndex < 0) {
				mCurrentEventIndex = mEvents.size() - 1;
			}
			mCurrentEvent = mEvents.get(mCurrentEventIndex);
		} else {
			mCurrentEventIndex = -1;
			mCurrentEvent = null;
		}
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {

		if (cursor != null) {
			mEvents.clear();
			try {

				while (cursor.moveToNext()) {
					Event event = new Event();

					event.mSnippet = cursor.getString(EventsQuery.SNIPPET);
					event.mStartTimestamp = cursor
							.getLong(EventsQuery.START_TIMESTAMP);
					event.mEndTimestamp = cursor
							.getLong(EventsQuery.END_TIMESTAMP);
					event.mUrl = cursor.getString(EventsQuery.URL);

					mEvents.add(event);
				}
				AppLog.d("num events = " + mEvents.size());
				if (mEvents.size() > 0) {
					mCurrentEventIndex = 0;
					mCurrentEvent = mEvents.get(mCurrentEventIndex);
				} else {
					mCurrentEventIndex = 0;
					mCurrentEvent = null;
				}
				refresh();

			} finally {
				cursor.close();
			}
		}

	}

	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema} query
	 * parameters.
	 */
	private interface EventsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, EventsColumns.SNIPPET,
				EventsColumns.DATE_SNIPPET, EventsColumns.URL,
				EventsColumns.START_TIMESTAMP, EventsColumns.END_TIMESTAMP };

		@SuppressWarnings("unused")
		int _ID = 0;
		int SNIPPET = 1;
		@SuppressWarnings("unused")
		int DATE_SNIPPET = 2;
		int URL = 3;
		int START_TIMESTAMP = 4;
		int END_TIMESTAMP = 5;
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				// if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				// return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > mSwipeMinDistance
						&& Math.abs(velocityX) > mSwipeThresholdVelocity) {
					mMessageHandler.removeCallbacks(mCountdownRunnable);
					goToNextEvent();
					mMessageHandler.post(mCountdownRunnable);
					// Toast.makeText(getActivity(), "Left Swipe",
					// Toast.LENGTH_SHORT).show();
				} else if (e2.getX() - e1.getX() > mSwipeMinDistance
						&& Math.abs(velocityX) > mSwipeThresholdVelocity) {
					mMessageHandler.removeCallbacks(mCountdownRunnable);
					goToPrevEvent();
					mMessageHandler.post(mCountdownRunnable);

					// Toast.makeText(getActivity(), "Right Swipe",
					// Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

		public boolean onDown(MotionEvent e) {

			return true;

		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			mOnClickListener.onClick(mEventTextView);
			return true;
		}

	}

	public void refreshQuery() {
		final Uri eventsUri = EventsSchema.CONTENT_URI;
		// Start background query to load events
		mHandler.startQuery(EventsQuery._TOKEN, null, eventsUri,
				EventsQuery.PROJECTION, null, null, EventsSchema.DEFAULT_SORT);

	}

}
