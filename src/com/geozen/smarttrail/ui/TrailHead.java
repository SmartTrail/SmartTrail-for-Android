package com.geozen.smarttrail.ui;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class TrailHead extends OverlayItem {

	protected String mTrailId;
	protected String mAreaId;
	float mAreaRating;
	public String mAreaTitle;

	public TrailHead(String trailId, String areaId, GeoPoint point, String trailName, String areaName, float areaRating) {
		super(point, trailName, areaName);
		mTrailId = trailId;
		mAreaTitle = areaName;
		mAreaRating = areaRating;
		mAreaId = areaId;
	}

}
