/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.model.Condition;
import com.geozen.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import com.geozen.smarttrail.util.TimeUtil;

public class ConditionsAdapter extends CursorAdapter {

	private Activity mActivity;


	class ViewHolder {
		ImageView status;
		TextView nickname;
		TextView comment;
		TextView time;
	}

	public ConditionsAdapter(Activity activity) {
		super(activity, null);
		mActivity = activity;
	}


	/** {@inheritDoc} */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view =  mActivity.getLayoutInflater().inflate(R.layout.list_item_condition,
				parent, false);
		
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.status = (ImageView) view.findViewById(R.id.condition);
		viewHolder.nickname = (TextView) view.findViewById(R.id.name);
		viewHolder.comment = (TextView) view.findViewById(R.id.comment);
		viewHolder.time = (TextView) view.findViewById(R.id.time);
		view.setTag(viewHolder);
		
		return view;
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		ViewHolder vh = (ViewHolder) v.getTag();
		String condition = c.getString(ConditionsQuery.CONDITION_STATUS);
		String name = c.getString(ConditionsQuery.CONDITION_NICKNAME);
		String comment = c.getString(ConditionsQuery.CONDITION_COMMENT);
		long submittedAt = c.getLong(ConditionsQuery.CONDITION_SUBMITTED_AT);
		long now = new GregorianCalendar().getTimeInMillis();

		if (condition != null) {
			vh.status.setImageDrawable(Condition.getStatusDrawable(context, condition));
			//vh.status.setBackgroundDrawable(Condition.getConditionBackground(context, condition));
		}
		if (name != null) {
			vh.nickname.setText(name);
		}
		if (comment != null) {
			if (TextUtils.isEmpty(comment)) {
				vh.comment.setVisibility(View.GONE);
			} else {
				vh.comment.setVisibility(View.VISIBLE);
			vh.comment.setText(comment);
			}
		}
	
	    vh.time.setText(TimeUtil.ago(now - submittedAt));

	}


	/** {@link com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema} query parameters. */
	public interface ConditionsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = {BaseColumns._ID, ConditionsColumns.STATUS,
				ConditionsColumns.USERNAME, ConditionsColumns.COMMENT, ConditionsColumns.UPDATED_AT };

		
		int _ID = 0;
		int CONDITION_STATUS = 1;
		int CONDITION_NICKNAME = 2;
		int CONDITION_COMMENT = 3;
		int CONDITION_SUBMITTED_AT = 4;
	}
}
