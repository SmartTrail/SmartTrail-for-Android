/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

public class AddConditionFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	public static final String EXTRA_TRAILS_URI = "com.geozen.smarttrail.extra.TRAILS_URI";

	public static final String EXTRA_TRAILS_ID = "com.geozen.smarttrail.extra.TRAILS_ID";

	private PushConditionTask mPushConditionTask;

	private ArrayAdapter<CharSequence> mStatusAdapter;

	private ArrayAdapter<CharSequence> mUserTypeAdapter;

	private Spinner mUserTypeSpinner;

	private EditText mComment;
	private NotifyingAsyncQueryHandler mHandler;

	private String[] mTrailNames;
	private String[] mTrailIds;
	private boolean[] mTrailSelections;

	private AlertDialog mTrailSelectDialog;

	private TextView mTrailsTextView;

	private String mInitialTrailsId;

	private Button mPoorButton;

	private Button mGoodButton;

	private Button mFairButton;

	private ImageView mPoorPointer;

	private ImageView mGoodPointer;

	private ImageView mFairPointer;
	private String mStatus;

	private OnClickListener mConditionOnClickListener;


	// Container Activity must implement this interface
	public interface OnAddConditionListener {
		public void onConditionSubmitted();

		public void onConditionCancelled();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		reloadFromArguments(getArguments());

	}

	public void reloadFromArguments(Bundle arguments) {
		// Teardown from previous arguments

		mHandler.cancelOperation(TrailsQuery._TOKEN);

		mInitialTrailsId = arguments.getString(EXTRA_TRAILS_ID);
		final Uri trailsUri = (Uri) arguments.getParcelable(EXTRA_TRAILS_URI);
		final int trailQueryToken;

		if (trailsUri == null) {
			return;
		}

		String[] projection = TrailsQuery.PROJECTION;
		trailQueryToken = TrailsQuery._TOKEN;

		// Start background query to load trails
		mHandler.startQuery(trailQueryToken, null, trailsUri, projection, null,
				null, TrailsSchema.DEFAULT_SORT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_add_condition, container,
				false);

		mStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.conditions, android.R.layout.simple_spinner_item);
		mStatusAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mConditionOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (mGoodButton.equals(view)) {
					mStatus = Condition.GOOD;
					mGoodPointer.setVisibility(View.VISIBLE);
					mFairPointer.setVisibility(View.INVISIBLE);
					mPoorPointer.setVisibility(View.INVISIBLE);
				} else if (mFairButton.equals(view)) {
					mStatus = Condition.FAIR;
					mGoodPointer.setVisibility(View.INVISIBLE);
					mFairPointer.setVisibility(View.VISIBLE);
					mPoorPointer.setVisibility(View.INVISIBLE);
				} else if (mPoorButton.equals(view)) {
					mStatus = Condition.POOR;
					mGoodPointer.setVisibility(View.INVISIBLE);
					mFairPointer.setVisibility(View.INVISIBLE);
					mPoorPointer.setVisibility(View.VISIBLE);
				}
			}
		};

		mGoodButton = (Button) v.findViewById(R.id.goodButton);
		mGoodButton.setOnClickListener(mConditionOnClickListener);
		mGoodPointer = (ImageView) v.findViewById(R.id.goodPointer);

		mFairButton = (Button) v.findViewById(R.id.fairButton);
		mFairButton.setOnClickListener(mConditionOnClickListener);
		mFairPointer = (ImageView) v.findViewById(R.id.fairPointer);

		mPoorButton = (Button) v.findViewById(R.id.poorButton);
		mPoorButton.setOnClickListener(mConditionOnClickListener);
		mPoorPointer = (ImageView) v.findViewById(R.id.poorPointer);

		mConditionOnClickListener.onClick(mGoodButton);

		mUserTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.userTypes, android.R.layout.simple_spinner_item);
		mUserTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mUserTypeSpinner = (Spinner) v.findViewById(R.id.userTypeSpinner);
		mUserTypeSpinner.setAdapter(mUserTypeAdapter);

		mComment = (EditText) v.findViewById(R.id.comment);
		mTrailsTextView = (TextView) v.findViewById(R.id.trails);
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
				imm.hideSoftInputFromWindow(mComment.getWindowToken(), 0);

				mPushConditionTask = new PushConditionTask();
				mPushConditionTask.execute();

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
				imm.hideSoftInputFromWindow(mComment.getWindowToken(), 0);

				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
			}
		});

		LinearLayout trailsContainer = (LinearLayout) v
				.findViewById(R.id.trailsContainer);

		trailsContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mTrailSelectDialog != null)
					mTrailSelectDialog.show();
			}
		});

		return v;
	}

	private class PushConditionTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "PushConditionTask";

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
				Condition condition = new Condition();

				condition.mStatus = mStatus;

				int index = mUserTypeSpinner.getSelectedItemPosition();
				condition.mUserType = Condition.getUserTypeByIndex(index);

				condition.mComment = mComment.getText().toString();

				// push all the selected trails
				for (int i = 0; i < mTrailSelections.length; i++) {
					if (mTrailSelections[i]) {
						condition.mTrailId = mTrailIds[i];
						api.pushCondition(condition);
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


			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);

				((OnAddConditionListener) getActivity()).onConditionCancelled();
			}

			mPushConditionTask = null;
			this.progressDialog.cancel();

			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();
			Activity activity = getActivity();
			((BaseActivity) activity).triggerRefresh();
		}

		@Override
		protected void onCancelled() {
			this.progressDialog.cancel();
			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();

			// ((OnAddConditionListener) getActivity()).onConditionCancelled();
		}
	}


	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema}
	 * query parameters.
	 */
	private interface TrailsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, TrailsColumns.TRAIL_ID,
				TrailsColumns.NAME };

		@SuppressWarnings("unused")
		int _ID = 0;
		int TRAIL_ID = 1;
		int NAME = 2;

	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (cursor != null) {
			if (token == TrailsQuery._TOKEN) {

				//
				// Construct arrays from the trails cursor for the trails
				// selection dialog.
				//
				mTrailNames = new String[cursor.getCount()];
				mTrailIds = new String[cursor.getCount()];
				mTrailSelections = new boolean[mTrailNames.length];

				try {
					int i = 0;
					while (cursor.moveToNext()) {
						mTrailIds[i] = cursor.getString(TrailsQuery.TRAIL_ID);
						mTrailNames[i] = cursor.getString(TrailsQuery.NAME);

						if (TextUtils.isEmpty(mInitialTrailsId)) {
							mTrailSelections[i] = true;
						} else if (mTrailIds[i].equals(mInitialTrailsId)) {
							mTrailSelections[i] = true;
						}
						i++;
					}

					final boolean[] tmpSelections = new boolean[mTrailSelections.length];
					System.arraycopy(mTrailSelections, 0, tmpSelections, 0,
							mTrailSelections.length);

					mTrailSelectDialog = new AlertDialog.Builder(getActivity())
							.setTitle(getText(R.string.selectTrailsTitle))
							.setMultiChoiceItems(
									mTrailNames,
									mTrailSelections,
									new DialogInterface.OnMultiChoiceClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which, boolean isChecked) {
											tmpSelections[which] = isChecked;

										}
									})
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mTrailSelections = tmpSelections;
											constructSelectedTrails();
										}

									}).create();

					constructSelectedTrails();
				} finally {
					cursor.close();
				}

			} else {

				cursor.close();
			}
		}

	}

	void constructSelectedTrails() {
		boolean first = true;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < mTrailSelections.length; i++)
			if (mTrailSelections[i]) {
				if (first) {
					first = false;
				} else {
					buf.append(",");
				}
				buf.append(mTrailNames[i]);
			}

		mTrailsTextView.setText(buf.toString());
	}

}
