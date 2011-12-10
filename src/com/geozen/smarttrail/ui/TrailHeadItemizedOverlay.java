/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

public class TrailHeadItemizedOverlay extends BalloonItemizedOverlay<TrailHead> {
//	private static final String CLASSTAG = TrailHeadItemizedOverlay.class
//			.getSimpleName();

	ArrayList<TrailHead> mTrailHeads;



	private OnTrailHeadClickListener mOnTrailHeadClickListener;



	private TrailMapActivity mMapActivity;

	

	public TrailHeadItemizedOverlay(Drawable defaultMarker, TrailMapActivity mapActivity,
			ArrayList<TrailHead> trailHeads) {
		super(boundCenterBottom(defaultMarker), mapActivity.getMapView());
		mMapActivity = mapActivity;
		
		mTrailHeads = trailHeads;

		populate();
	}

	/**
	 * Creates the balloon view. Override to create a sub-classed view that can
	 * populate additional sub-views.
	 */
	@Override
	protected BalloonOverlayView<TrailHead> createBalloonOverlayView() {
		return new TrailBalloonOverlayView(mMapActivity,
				getBalloonBottomOffset());
	}

	@Override
	protected TrailHead createItem(int i) {

		TrailHead th = mTrailHeads.get(i);
		
		return th;

	}

	@Override
	public int size() {

		if (mTrailHeads == null) {
			return 0;
		}
		else {
			return mTrailHeads.size();
		}
	}



	public void setOnTrailHeadClickListener(OnTrailHeadClickListener listener) {
		mOnTrailHeadClickListener = listener;

	}

	@Override
	protected boolean onBalloonTap(int index, TrailHead item) {

		if (mOnTrailHeadClickListener != null) {
			return mOnTrailHeadClickListener.onClick(index,
					mTrailHeads.get(index));
		} 
		return true;
	}
}
