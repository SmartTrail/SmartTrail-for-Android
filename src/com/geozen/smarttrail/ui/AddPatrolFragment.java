/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.http.SmartTrailApi;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.util.AppLog;
import com.geozen.smarttrail.util.NotificationsUtil;
import com.geozen.smarttrail.util.NotifyingAsyncQueryHandler;

public class AddPatrolFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	public static final String EXTRA_TRAILS_URI = "com.geozen.smarttrail.extra.TRAILS_URI";

	public static final String EXTRA_TRAILS_ID = "com.geozen.smarttrail.extra.TRAILS_ID";

	private PushPatrolTask mPushPatrolTask;

	private ArrayAdapter<CharSequence> mStatusAdapter;

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

	private GregorianCalendar mPatrolDate;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("h:mm a");

	private EditText mNumBikers;

	private EditText mNumHikers;

	private EditText mNumDogsUnleashed;

	private EditText mNumDogsLeashed;

	private EditText mNumRunners;

	private EditText mNumEquestrians;

	private EditText mNumAnglers;

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

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
		View v = inflater.inflate(R.layout.fragment_add_patrol, container,
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

		mComment = (EditText) v.findViewById(R.id.comment);

		// used for start time and day
		mPatrolDate = new GregorianCalendar();

		//
		// Patrol Date
		//
		final TextView dateTextView = (TextView) v.findViewById(R.id.date);

		// the callback received when the user "sets" the date in the dialog
		DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mPatrolDate.set(year, monthOfYear, dayOfMonth);
				String str = mDateFormat.format(mPatrolDate.getTime());
				dateTextView.setText(str);
			}

		};

		String str = mDateFormat.format(mPatrolDate.getTime());
		dateTextView.setText(str);

		final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(),
				dateSetListener, mPatrolDate.get(Calendar.YEAR),
				mPatrolDate.get(Calendar.MONTH),
				mPatrolDate.get(Calendar.DAY_OF_MONTH));

		LinearLayout dateContainer = (LinearLayout) v
				.findViewById(R.id.dateContainer);

		dateContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dateDialog.show();
			}
		});

		//
		// Start Time
		//
		final TextView startTimeTextView = (TextView) v
				.findViewById(R.id.startTime);

		TimePickerDialog.OnTimeSetListener startTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mPatrolDate.set(mPatrolDate.get(Calendar.YEAR),
						mPatrolDate.get(Calendar.MONTH),
						mPatrolDate.get(Calendar.DAY_OF_MONTH), hourOfDay,
						minute);
				String str = mTimeFormat.format(mPatrolDate.getTime());
				startTimeTextView.setText(str);
			}

		};

		str = mTimeFormat.format(mPatrolDate.getTime());
		startTimeTextView.setText(str);

		final TimePickerDialog startTimeDialog = new TimePickerDialog(
				getActivity(), startTimeSetListener,
				mPatrolDate.get(Calendar.HOUR),
				mPatrolDate.get(Calendar.MINUTE), false);

		LinearLayout startTimeContainer = (LinearLayout) v
				.findViewById(R.id.startTimeContainer);

		startTimeContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startTimeDialog.show();
			}
		});

		//
		// Trails
		//
		mTrailsTextView = (TextView) v.findViewById(R.id.trails);
		LinearLayout trailsContainer = (LinearLayout) v
				.findViewById(R.id.trailsContainer);

		trailsContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mTrailSelectDialog != null)
					mTrailSelectDialog.show();
			}
		});

		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
		mNumBikers = (EditText) v.findViewById(R.id.numBikers);
		String s = Integer.toString(app.getPatrolNumBikers());
		mNumBikers.setText(s);
		mNumBikers.setSelection(s.length());

		mNumHikers = (EditText) v.findViewById(R.id.numHikers);
		s = Integer.toString(app.getPatrolNumHikers());
		mNumHikers.setText(s);
		mNumHikers.setSelection(s.length());

		mNumDogsUnleashed = (EditText) v.findViewById(R.id.numDogsUnleashed);
		s = Integer.toString(app.getPatrolNumDogsUnleashed());
		mNumDogsUnleashed.setText(s);
		mNumDogsUnleashed.setSelection(s.length());

		mNumDogsLeashed = (EditText) v.findViewById(R.id.numDogsLeashed);
		s = Integer.toString(app.getPatrolNumDogsLeashed());
		mNumDogsLeashed.setText(s);
		mNumDogsLeashed.setSelection(s.length());

		mNumRunners = (EditText) v.findViewById(R.id.numRunners);
		s = Integer.toString(app.getPatrolNumRunners());
		mNumRunners.setText(s);
		mNumRunners.setSelection(s.length());

		mNumEquestrians = (EditText) v.findViewById(R.id.numHorseRiders);
		s = Integer.toString(app.getPatrolNumEquestrians());
		mNumEquestrians.setText(s);
		mNumEquestrians.setSelection(s.length());

		mNumAnglers = (EditText) v.findViewById(R.id.numAnglers);
		s = Integer.toString(app.getPatrolNumAnglers());
		mNumAnglers.setText(s);
		mNumAnglers.setSelection(s.length());

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

				mPushPatrolTask = new PushPatrolTask();
				mPushPatrolTask.execute();

			}

		});

		//
		// Save
		//
		Button save = (Button) v.findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// dismiss softkeyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mComment.getWindowToken(), 0);
				
				saveValues();
				
				// close it down
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
				if (getActivity() instanceof PatrolActivity) {
					getActivity().finish();
				}
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

				
				// close it down
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.popBackStack();
				if (getActivity() instanceof PatrolActivity) {
					getActivity().finish();
				}
			}
		});

		return v;
	}

	private void saveValues() {
		// save values to preferences for next time
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();

		try {
			app.setPatrolNumBikers(Integer.parseInt(mNumBikers.getText()
					.toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumBikers(0);
		}
		try {
			app.setPatrolNumHikers(Integer.parseInt(mNumHikers.getText()
					.toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumHikers(0);
		}
		try {
			app.setPatrolNumDogsUnleashed(Integer.parseInt(mNumDogsUnleashed
					.getText().toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumDogsUnleashed(0);
		}
		try {
			app.setPatrolNumDogsLeashed(Integer.parseInt(mNumDogsLeashed
					.getText().toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumDogsUnleashed(0);
		}
		try {
			app.setPatrolNumRunners(Integer.parseInt(mNumRunners.getText()
					.toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumRunners(0);
		}
		try {
			app.setPatrolNumEquestrians(Integer.parseInt(mNumEquestrians
					.getText().toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumEquestrians(0);
		}
		try {
			app.setPatrolNumAnglers(Integer.parseInt(mNumAnglers.getText()
					.toString()));
		} catch (NumberFormatException ex) {
			app.setPatrolNumAnglers(0);
		}
	}

	private class PushPatrolTask extends AsyncTask<Void, Void, Boolean> {

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
				condition.mUserType = Condition.BIKER;

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

				Activity activity = getActivity();
				((BaseActivity) activity).triggerRefresh();
			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);

			}

			mPushPatrolTask = null;
			this.progressDialog.cancel();

			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();
			if (getActivity() instanceof PatrolActivity) {
				getActivity().finish();
			}

		}

		@Override
		protected void onCancelled() {
			this.progressDialog.cancel();
			FragmentManager fm = getActivity().getSupportFragmentManager();
			fm.popBackStack();

			if (getActivity() instanceof PatrolActivity) {
				getActivity().finish();
			}
		}
	}

	/**
	 * {@link com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema} query parameters.
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
