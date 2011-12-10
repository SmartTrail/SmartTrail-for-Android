/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.Constants;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;

public class SigninActivity extends Activity {
	private static final String CLASSTAG = SigninActivity.class.getSimpleName();

	private ProgressDialog mProgressDialog;
	private TextView mUsernameEditText;
	private TextView mPasswordEditText;
	private AsyncTask<Void, Void, Boolean> mSigninTask;
	private Boolean mShow = false;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin);

		setTitle(R.string.signIn);
		SmartTrailApplication app = (SmartTrailApplication) getApplication();
		app.signoutUser();

		setupUi();

		// Re-task if the request was cancelled.
		mSigninTask = (SigninTask) getLastNonConfigurationInstance();
		if (mSigninTask != null && mSigninTask.isCancelled()) {
			AppLog.d(CLASSTAG, "LoginTask previously cancelled, trying again.");
			mSigninTask = new SigninTask().execute();
		}
	}

	private ProgressDialog showProgressDialog() {
		if (mProgressDialog == null) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(R.string.login_dialog_title);
			dialog.setMessage(getString(R.string.login_dialog_message));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			mProgressDialog = dialog;
		}
		mProgressDialog.show();
		return mProgressDialog;
	}

	private void dismissProgressDialog() {
		try {
			mProgressDialog.dismiss();
		} catch (IllegalArgumentException e) {
			// We don't mind. android cleared it for us.
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
	
		if (mSigninTask != null) {
			mSigninTask.cancel(true);
		}
		return mSigninTask;
	}

	private void setupUi() {
		
		
		
		SmartTrailApplication app = (SmartTrailApplication) getApplication();
		final Button signinButton = (Button) findViewById(R.id.signIn);
		signinButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// close the keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(),
						0);
				mSigninTask = new SigninTask().execute();
			}
		});
		signinButton.setTypeface(app.mTf);

		Button signupButton = (Button) findViewById(R.id.signUp);
		signupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SigninActivity.this,
						SignupActivity.class);

				// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
				// Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

				SigninActivity.this.finish();
			}
		});
		signupButton.setTypeface(app.mTf);

		TextView alreadyAMemberTextView = ((TextView) findViewById(R.id.notMember));
		alreadyAMemberTextView.setTypeface(app.mTf);
		
		mUsernameEditText = ((EditText) findViewById(R.id.username));
		mUsernameEditText.setTypeface(app.mTf);
		mPasswordEditText = ((EditText) findViewById(R.id.password));
		mPasswordEditText.setTypeface(app.mTf);
		
		mUsernameEditText.setText(app.getUsername());
		mPasswordEditText.setText(app.getPassword());
		if (app.getUsername() != null) {
			mPasswordEditText.requestFocus();
		}

		final ImageButton showPassword = (ImageButton) findViewById(R.id.showPassword);
		showPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mShow = !mShow;
				int position = mPasswordEditText.getSelectionStart();
				if (mShow) {
					mPasswordEditText
							.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					mPasswordEditText
							.setTransformationMethod(new SingleLineTransformationMethod());
					Editable etext = (Editable) mPasswordEditText.getText();
					Selection.setSelection(etext, position);
				} else {
					mPasswordEditText
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					mPasswordEditText
							.setTransformationMethod(new PasswordTransformationMethod());
					Editable etext = (Editable) mPasswordEditText.getText();
					Selection.setSelection(etext, position);
				}

			}
		});

	}

	/**
	 * 
	 * @author Matt Tucker (matt@geozen.com)
	 * 
	 */
	private class SigninTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "SigninTask";

		private Exception mReason;

		@Override
		protected void onPreExecute() {
	
			showProgressDialog();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			SmartTrailApplication app = (SmartTrailApplication) getApplication();

			try {
				String username = mUsernameEditText.getText().toString();
				String password = mPasswordEditText.getText().toString();

				boolean signedin = app.signin(username, password);
				if (signedin) {
					JSONObject info = app.getApi().getUserInfo();
					if (info.has("isPatroller")) {
						app.setIsPatroller(info.getBoolean("isPatroller"));
					}
				}
				
				return signedin;

			} catch (Exception e) {
					AppLog.d(CLASSTAG,"Caught Exception logging in.", e);
				mReason = e;
				app.signoutUser();

				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean signedIn) {
			AppLog.d(CLASSTAG,"onPostExecute(): " + signedIn);

			if (signedIn) {

				sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_IN));

				// Be done with the activity.
				finish();

			} else {
				sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_OUT));
				NotificationsUtil.ToastReasonForFailure(SigninActivity.this,
						mReason);
			}
			dismissProgressDialog();
		}

		@Override
		protected void onCancelled() {
			dismissProgressDialog();
		}
	}

}