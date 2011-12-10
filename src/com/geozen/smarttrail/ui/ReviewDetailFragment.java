/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.model.Review;
import com.geozen.smarttrail.util.ImageLoader;

public class ReviewDetailFragment extends Fragment {

	public static final String EXTRA_USERNAME = "com.geozen.smarttrail.extra.USERNAME";
	public static final String EXTRA_REVIEW = "com.geozen.smarttrail.extra.REVIEW";
	public static final String EXTRA_PHOTO_ID = "com.geozen.smarttrail.extra.PHOTO_ID";
	public static final String EXTRA_RATING = "com.geozen.smarttrail.extra.RATING";
	public static final String EXTRA_UPDATED_AT_STR = "com.geozen.smarttrail.extra.UPDATED_AT_STR";
	public static final String EXTRA_NUM_REVIEWS = "com.geozen.smarttrail.extra.NUM_REVIEWS";

	private Review mReview;
	private ImageLoader mImageLoader;
	private String mBasePhotoUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reloadFromArguments(getArguments());

	}

	public void reloadFromArguments(Bundle arguments) {
		// Teardown from previous arguments

		// Load new arguments
		mReview = new Review();
		mReview.mUsername = arguments.getString(EXTRA_USERNAME);
		mReview.mPhotoId = arguments.getString(EXTRA_PHOTO_ID);
		mReview.mUserNumReviews = arguments.getInt(EXTRA_NUM_REVIEWS);
		mReview.mReview = arguments.getString(EXTRA_REVIEW);
		mReview.mUpdatedAtStr = arguments.getString(EXTRA_UPDATED_AT_STR);
		mReview.mRating = arguments.getFloat(EXTRA_RATING);
		mImageLoader = new ImageLoader(getActivity().getApplicationContext());
		
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		mBasePhotoUrl = app.getApi().getApiUri().buildUpon().appendPath("photos").build().toString()+ "/";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_review, container, false);

		//
		// TextView title = (TextView) v.findViewById(R.id.title);
		// title.setText(mTrailName);

		TextView usernameTextView = (TextView) v.findViewById(R.id.name);
		usernameTextView.setText(mReview.mUsername);

		TextView numReviewsTextView = (TextView) v
				.findViewById(R.id.numReviews);
	
		numReviewsTextView.setText(Integer.toString(mReview.mUserNumReviews)+ " "+getString(R.string.reviews));

		TextView reviewTextView = (TextView) v.findViewById(R.id.review);
		reviewTextView.setText(Html.fromHtml(mReview.mReview));

		RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
		ratingBar.setRating(mReview.mRating);

		TextView dateTextView = (TextView) v.findViewById(R.id.date);
		dateTextView.setText(mReview.mUpdatedAtStr);

		ImageView image = (ImageView) v.findViewById(R.id.photo);
		String url = mBasePhotoUrl + mReview.mPhotoId;
		image.setTag(url);

		mImageLoader.DisplayImage(url, getActivity(), image);
		return v;
	}

}
