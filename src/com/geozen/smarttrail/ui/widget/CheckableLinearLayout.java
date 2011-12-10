/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.geozen.smarttrail.R;

public class CheckableLinearLayout extends LinearLayout implements
Checkable {

	private Checkable mCheckableView;
	
	public CheckableLinearLayout(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	public boolean isChecked() {
		return mCheckableView.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		mCheckableView.setChecked(checked);
	}

	@Override
	public void toggle() {
		mCheckableView.toggle();
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mCheckableView = (Checkable) this.findViewById(R.id.checkbox);
	}


}
