/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.Constants;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;

public class SignupActivity extends Activity {
	private static final String CLASSTAG = SignupActivity.class.getSimpleName();

	private ProgressDialog mProgressDialog;
	private TextView mEmailEditText;
	private TextView mPasswordEditText;
	private AsyncTask<Void, Void, Boolean> mSignupTask;

	private Boolean mShow = false;

	private EditText mNicknameEditText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);

		setTitle(R.string.signUp);
		// Re-task if the request was cancelled.
		mSignupTask = (SignupTask) getLastNonConfigurationInstance();
		if (mSignupTask != null && mSignupTask.isCancelled()) {
			AppLog.d(CLASSTAG, "SignupTask previously cancelled, trying again.");
			mSignupTask = new SignupTask().execute();
		}

		setupUi();
	}

	private ProgressDialog showProgressDialog() {
		if (mProgressDialog == null) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(R.string.joinProgressTitle);
			dialog.setMessage(getString(R.string.joinProgressMessage));
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
		if (mSignupTask != null) {
			mSignupTask.cancel(true);
		}
		return mSignupTask;
	}

	private void setupUi() {
//		Toast toast = Toast.makeText(this, "We're working out the bugs, so signup is invite only.",Toast.LENGTH_LONG );
//		toast.setGravity(Gravity.CENTER, 0, 0);
//		toast.show();
		SmartTrailApplication app = (SmartTrailApplication) getApplication();
		TextView alreadyMember = (TextView) findViewById(R.id.notMember);
		alreadyMember.setText(R.string.alreadyMember);

		Button signInButton = (Button) findViewById(R.id.signUp);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SignupActivity.this,
						SigninActivity.class));

				SignupActivity.this.finish();
			}
		});
		signInButton.setText(R.string.signInLink);
		signInButton.setTypeface(app.mTf);
		
		TextView alreadyAMemberTextView = ((TextView) findViewById(R.id.notMember));
		alreadyAMemberTextView.setTypeface(app.mTf);
		
		mEmailEditText = ((EditText) findViewById(R.id.username));
		mEmailEditText.setTypeface(app.mTf);
		mNicknameEditText = ((EditText) findViewById(R.id.nickname));
		mNicknameEditText.setTypeface(app.mTf);
		mPasswordEditText = ((EditText) findViewById(R.id.password));
		mPasswordEditText.setTypeface(app.mTf);
		
		 CharSequence username = app.getUsername();
		 if (username != null) {
		
			if (username.toString().contains("@")) {
				mEmailEditText.setText(username);
			} else {
				mNicknameEditText.setText(username);
			}
		 }

		mPasswordEditText.setText(app.getPassword());
		mPasswordEditText.setHint(R.string.createPasswordHint);
		if (app.getUsername() != null) {
			mPasswordEditText.requestFocus();
		}

		final ImageButton showPassword = (ImageButton) findViewById(R.id.showPassword);
		showPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mShow = !mShow;
				if (mShow) {
					mPasswordEditText
							.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					mPasswordEditText
							.setTransformationMethod(new SingleLineTransformationMethod());

				} else {
					mPasswordEditText
							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					mPasswordEditText
							.setTransformationMethod(new PasswordTransformationMethod());
				}

			}
		});

		final Button signupButton = (Button) findViewById(R.id.signIn);
		signupButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!usernameEditTextFieldIsValid()) {
					Toast.makeText(SignupActivity.this, R.string.invalidEmail,
							Toast.LENGTH_LONG).show();
				} else if (!passwordEditTextFieldIsValid()) {
					Toast.makeText(SignupActivity.this,
							R.string.invalidPassword, Toast.LENGTH_LONG).show();
				} else {
					// close the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(
							mPasswordEditText.getWindowToken(), 0);

					mSignupTask = new SignupTask().execute();
				}

			}
		});
		signupButton.setText(R.string.signUp);
		signupButton.setTypeface(app.mTf);
		
		

	}

	private boolean usernameEditTextFieldIsValid() {
		String email = mEmailEditText.getText().toString();
		return !TextUtils.isEmpty(email) && email.contains("@");
	}

	private boolean passwordEditTextFieldIsValid() {
		if (mPasswordEditText.getText() == null) {
			return false;
		} else {
			return (mPasswordEditText.getText().length() > 5);
		}
	}

	private class SignupTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "SignupTask";

		private Exception mReason;

		private String mEmail;

		private String mPassword;

		@Override
		protected void onPreExecute() {
			showProgressDialog();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			SmartTrailApplication app = (SmartTrailApplication) getApplication();

			try {

				mEmail = mEmailEditText.getText().toString().toLowerCase();
				mPassword = mPasswordEditText.getText().toString();
				String nickname = mNicknameEditText.getText().toString()
						.toLowerCase();

				app.getApi().signup(nickname, mEmail, mPassword);

				app.signin(mEmail, mPassword);

				return true;

			} catch (Exception e) {
				AppLog.d(CLASSTAG, "Caught Exception signing up user.", e);
				mReason = e;

				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean signedup) {

			if (signedup) {

				sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_IN));
				Toast.makeText(SignupActivity.this,
						getString(R.string.login_welcome_toast),
						Toast.LENGTH_LONG).show();

				// Be done with you.
				finish();

			} else {

				NotificationsUtil.ToastReasonForFailure(SignupActivity.this,
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