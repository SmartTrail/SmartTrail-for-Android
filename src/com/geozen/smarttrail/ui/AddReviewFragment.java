/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;

public class AddReviewFragment extends Fragment  {


	public static final String EXTRA_AREA_ID = "com.geozen.smarttrail.extra.AREA_ID";
	public static final String EXTRA_AREA_NAME = "com.geozen.smarttrail.extra.AREA_NAME";

	private PushReviewTask mPushReviewTask;

	private EditText mReviewEditText;


	private String mAreaId;
	private String mAreaName;
	private RatingBar mRating;




	// Container Activity must implement this interface
	public interface OnAddConditionListener {
		public void onConditionSubmitted();

		public void onConditionCancelled();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reloadFromArguments(getArguments());

	}

	public void reloadFromArguments(Bundle arguments) {
		// Teardown from previous arguments


		// Load new arguments
		mAreaId = arguments.getString(EXTRA_AREA_ID);
		mAreaName = arguments.getString(EXTRA_AREA_NAME);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_add_review, container,
				false);


		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText("Review for " + mAreaName);

		mReviewEditText = (EditText) v.findViewById(R.id.review);
		
		mRating = (RatingBar) v.findViewById(R.id.rating);
		//
		// Confirm
		//
		Button submitButton = (Button) v.findViewById(R.id.confirm);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dismiss softkeyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mReviewEditText.getWindowToken(), 0);

				mPushReviewTask = new PushReviewTask();
				mPushReviewTask.execute();
				SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
				app.mSyncReviews = true;
			}

		});

		//
		// Cancel
		//
		Button cancel = (Button) v.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dismiss softkeyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mReviewEditText.getWindowToken(), 0);

				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
			}
		});



		return v;
	}

	private class PushReviewTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "PushReviewTask";

		private final ProgressDialog progressDialog = new ProgressDialog(
				getActivity());

		private Exception mReason;

		

		@Override
		protected void onPreExecute() {
			this.progressDialog
					.setMessage(getText(R.string.submittingCondition));
			this.progressDialog.show();

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				SmartTrailApi api = app.getApi();
		
				
				float rating = mRating.getRating();
				String review = mReviewEditText.getText().toString();
				api.pushReview(mAreaId, rating, review);
				
			} catch (Exception e) {

				AppLog.e(CLASSTAG, "Exception adding review.", e);
				mReason = e;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {

			if (success) {

			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);

				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
			}

			mPushReviewTask = null;
			this.progressDialog.cancel();

			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();
			
			// update the num reviews.
			Activity activity = getActivity();	
			((BaseActivity) activity).triggerRefresh();
			
		}

		@Override
		protected void onCancelled() {
			this.progressDialog.cancel();
			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();
		}
	}

	
	
}
