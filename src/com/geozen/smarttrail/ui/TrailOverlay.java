/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.geozen.smarttrail.model.TrailDataSet;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class TrailOverlay extends Overlay {

	private int mRadius = 3;

	public TrailDataSet mTrailDataSet;
	public String mTrailName;
	
	public Paint mPathPaint;
	public Paint mStartPaint;
	public Paint mEndPaint;

	private Paint mStrokePaint;

	private Paint mTextPaint;

//	private String mAreaId;
//	private String mTrailId;

	public TrailOverlay() {
		super();
		mStartPaint = new Paint();
		mStartPaint.setColor(Color.GREEN);

		mPathPaint = new Paint();
		mPathPaint.setDither(true);
		mPathPaint.setColor(Color.BLUE);
		mPathPaint.setAlpha(100);
		mPathPaint.setStyle(Paint.Style.STROKE);
		mPathPaint.setStrokeJoin(Paint.Join.ROUND);
		mPathPaint.setStrokeCap(Paint.Cap.ROUND);
		mPathPaint.setStrokeWidth(6);

		mStrokePaint = new Paint();
		mStrokePaint.setColor(Color.WHITE);
		mStrokePaint.setTextAlign(Paint.Align.CENTER);
		mStrokePaint.setTextSize(18);
		mStrokePaint.setTypeface(Typeface.DEFAULT_BOLD);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mStrokePaint.setStrokeWidth(4);

		mTextPaint = new Paint();
		mTextPaint.setARGB(255, 85, 107, 47);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(18);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

		
	}

	public TrailOverlay(String areaId, String trailId, TrailDataSet dataSet) {
		this();
//		mAreaId = areaId;
//		mTrailId = trailId;
		mTrailDataSet = dataSet;
	
	}

	public void setTrailName(String name) {
		mTrailName = name;
	}
	
	public void setTrailColor(int i) {
		mPathPaint.setColor(i);
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		Projection projection = mapView.getProjection();
		Path path = new Path();
		Point p1 = new Point();
		Point p2 = new Point();

		for (int i = 0; i < mTrailDataSet.mPoints.length; i++) {

			// guard against corrupt files with empty points. wtf?
			if (mTrailDataSet.mPoints[i] != null) {
				if (i == 0) {
					// start point
					projection.toPixels(mTrailDataSet.mPoints[0], p1);
					RectF oval = new RectF(p1.x - mRadius, p1.y - mRadius, p1.x
							+ mRadius, p1.y + mRadius);
					canvas.drawOval(oval, mStartPaint);
					path.moveTo(p1.x, p1.y);
				} else if (i == mTrailDataSet.mPoints.length - 1) {
					// end point

					projection.toPixels(mTrailDataSet.mPoints[i], p1);
					RectF oval = new RectF(p1.x - mRadius, p1.y - mRadius, p1.x
							+ mRadius, p1.y + mRadius);
					canvas.drawOval(oval, mStartPaint);
				} else {
					if (mTrailDataSet.mPoints[i + 1] != null) {
						projection.toPixels(mTrailDataSet.mPoints[i], p1);
						projection.toPixels(mTrailDataSet.mPoints[i + 1], p2);

						path.lineTo(p2.x, p2.y);
					}

				}
			}

		}

		canvas.drawPath(path, mPathPaint);

		projection.toPixels(mTrailDataSet.getCenter(), p1);
		canvas.drawText(mTrailName, p1.x, p1.y, mStrokePaint);
		canvas.drawText(mTrailName, p1.x, p1.y, mTextPaint);

		return super.draw(canvas, mapView, shadow, when);
	}

	public GeoPoint getCenter() {

		return mTrailDataSet.getCenter();
	}

}
