/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Review is submitted user review of a trail area.
 * 
 * @author matt
 * 
 */
public class Review {

	public static final String AREA_ID = "areaId";
	public static final String PHOTO_ID = "photoId";
	public static final String USERNAME = "username";
	public static final String REVIEW = "review";
	public static final String UPDATED_AT = "updatedAt";
	public static final String USER_NUM_REVIEWS = "numReviews";
	public static final String RATING = "rating";

	public String mAreaId;
	public String mPhotoId;
	public String mUsername;
	public int mUserNumReviews;
	public float mRating;
	public String mReview;
	public long mUpdatedAt;
	public String mUpdatedAtStr;

	public Review() {

	}

	public Review(JSONObject review) throws JSONException {

		mPhotoId = review.getString(PHOTO_ID);

		mUsername = review.getString(USERNAME);
		mReview = review.getString(REVIEW);
		mUserNumReviews = review.getInt(USER_NUM_REVIEWS);
		mRating = (float) review.getDouble(RATING);
		mUpdatedAt = review.getLong(UPDATED_AT);

		Date updateAtDate = new Date(mUpdatedAt);
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		mUpdatedAtStr = "on " + format.format(updateAtDate);
	}

	public JSONObject toJson() throws JSONException {

		JSONObject review = new JSONObject();

		review.put(USERNAME, mUsername);
		review.put(AREA_ID, mAreaId);
		review.put(RATING, mRating);
		review.put(REVIEW, mReview);
		review.put(UPDATED_AT, mUpdatedAt);

		// these could be dropped as they are tied to username
		review.put(PHOTO_ID, mPhotoId);
		review.put(USER_NUM_REVIEWS, mUserNumReviews);

		return review;
	}

}