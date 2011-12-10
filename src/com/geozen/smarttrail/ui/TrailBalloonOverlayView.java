/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.ui.phone.TrailDetailActivity;
import com.geozen.smarttrail.util.GeoUtil;
import com.readystatesoftware.mapviewballoons.BalloonOverlayView;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 * 
 */
public class TrailBalloonOverlayView extends BalloonOverlayView<TrailHead> {

	private LinearLayout layout;
	private TextView mTrailTextView;
	private TextView mAreaTextView;
	private RatingBar mAreaRating;
	private ImageButton mInfoButton;
	private TrailMapActivity mMapActivity;
	private ImageButton mDirectionsButton;
	private ImageButton mMapButton;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context
	 *            - The activity context.
	 * @param balloonBottomOffset
	 *            - The bottom padding (in pixels) to be applied when rendering
	 *            this view.
	 */
	public TrailBalloonOverlayView(TrailMapActivity mapActivity, int balloonBottomOffset) {

		super(mapActivity, balloonBottomOffset);

		mMapActivity = mapActivity;
	}

		
	@Override
	public void setupView(Context context, int balloonBottomOffset) {
		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.trail_balloon_overlay, layout);
		mTrailTextView = (TextView) v.findViewById(R.id.trailTitle);
		SmartTrailApplication app = SmartTrailApplication.mInstance;
		mTrailTextView.setTypeface(app.mTf);
		mAreaTextView = (TextView) v.findViewById(R.id.areaTitle);
		mAreaRating = (RatingBar) v.findViewById(R.id.areaRating);

		mInfoButton = (ImageButton) v.findViewById(R.id.button1);
		mMapButton = (ImageButton) v.findViewById(R.id.button3);
		mDirectionsButton = (ImageButton) v.findViewById(R.id.button2);
		
		
		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);
	}
	
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param trailHead
	 *            - The overlay item containing the relevant view data (title
	 *            and snippet).
	 */
	@Override
	public void setData(final TrailHead trailHead) {

		layout.setVisibility(VISIBLE);
		if (trailHead.getTitle() != null) {
			mTrailTextView.setVisibility(VISIBLE);
			mTrailTextView.setText(trailHead.getTitle());
			mAreaTextView.setText(trailHead.mAreaTitle);
			mAreaRating.setRating(trailHead.mAreaRating);
		} else {
			mTrailTextView.setVisibility(GONE);
		}
		if (trailHead.getSnippet() != null) {
			mAreaTextView.setVisibility(VISIBLE);
			mAreaTextView.setText(trailHead.getSnippet());
		} else {
			mAreaTextView.setVisibility(GONE);
		}

		mInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mMapActivity,
						TrailDetailActivity.class);
				final Uri trailsUri = TrailsSchema
						.buildUri(trailHead.mTrailId);
				intent.setData(trailsUri);
				final Uri areaUri = AreasSchema
						.buildAreaUri(trailHead.mAreaId);
				intent.putExtra(TrailDetailFragment.EXTRA_AREA,
						areaUri);
				mMapActivity.startActivity(intent);
				
			}});
		
		mMapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Toast.makeText(v.getContext(), "This will display the trails for this area but it has not been implemented yet.", Toast.LENGTH_SHORT).show();
				mMapActivity.selectArea(trailHead.mAreaId);
			}});
		
		mDirectionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				float lat = GeoUtil.e6ToFloat(trailHead.getPoint().getLatitudeE6());
				float lon = GeoUtil.e6ToFloat(trailHead.getPoint().getLongitudeE6());
				
				String url = "http://maps.google.com/maps?daddr="+Float.toString(lat)+","+Float.toString(lon);
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
				mMapActivity.startActivity(intent);
				
			}});
		
		
		
		
		
	}

}
