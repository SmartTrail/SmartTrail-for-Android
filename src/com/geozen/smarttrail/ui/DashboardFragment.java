/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.RegionsSchema;
import com.geozen.smarttrail.ui.tablet.AreasMultiPaneActivity;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.UIUtils;

public class DashboardFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public void fireTrackerEvent(String label) {
		AnalyticsUtils.getInstance(getActivity()).trackEvent(
				"Home Screen Dashboard", "Click", label, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_dashboard3, container);

		final SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();

		root.findViewById(R.id.home_btn_trails).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("trails");
						// Launch trails list
						if (UIUtils.isHoneycombTablet(getActivity())) {
							startActivity(new Intent(getActivity(),
									AreasMultiPaneActivity.class));
						} else {
							String regionId = app.getRegion();
							String regionName = app.getRegionName();
							final Intent intent = new Intent(
									Intent.ACTION_VIEW, RegionsSchema
											.buildAreasUri(regionId));
							intent.putExtra(Intent.EXTRA_TITLE, regionName);
							startActivity(intent);
						}

					}
				});

		((TextView) root.findViewById(R.id.home_btn_trails_label))
				.setTypeface(app.mTf);
		// ((Button)
		// root.findViewById(R.id.home_btn_trails)).setTypeface(app.mTf);

		root.findViewById(R.id.home_btn_starred).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						fireTrackerEvent("Favorites");
						// Launch list of trails and vendors the user has
						// starred
						startActivity(new Intent(getActivity(),
								FavoritesActivity.class));
					}
				});
		// ((Button) root.findViewById(R.id.home_btn_starred))
		// .setTypeface(app.mTf);
		 ((TextView)
		 root.findViewById(R.id.home_btn_starred_label)).setTypeface(app.mTf);


		root.findViewById(R.id.home_btn_map).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						// Launch map of conference venue
						fireTrackerEvent("Map");
						final Intent intent = new Intent(getActivity(),
								TrailMapActivity.class);
						SmartTrailApplication app = (SmartTrailApplication) getActivity()
								.getApplication();
						intent.putExtra(TrailMapActivity.EXTRA_AREA_ID,
								app.getArea());
						startActivity(intent);
					}
				});
		 ((TextView)
		 root.findViewById(R.id.home_btn_map_label)).setTypeface(app.mTf);

		root.findViewById(R.id.home_btn_announcements).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						// splicing in tag streamer
						fireTrackerEvent("Alerts");
						Intent intent = new Intent(getActivity(),
								AlertsActivity.class);
						startActivity(intent);
					}
				});
		// ((Button) root.findViewById(R.id.home_btn_announcements))
		// .setTypeface(app.mTf);
		 ((TextView)
		 root.findViewById(R.id.home_btn_announcements_label)).setTypeface(app.mTf);

		root.findViewById(R.id.home_btn_events).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						// splicing in tag streamer
						fireTrackerEvent("Events");
						Intent intent = new Intent(getActivity(),
								EventsActivity.class);
						startActivity(intent);
					}
				});

		// ((Button) root.findViewById(R.id.home_btn_events))
		// .setTypeface(app.mTf);
		 ((TextView) root.findViewById(R.id.home_btn_events_label))
		 .setTypeface(app.mTf);

		root.findViewById(R.id.home_btn_patrol).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						// splicing in tag streamer
						fireTrackerEvent("Patrol");
						Intent intent;

						SmartTrailApplication app = (SmartTrailApplication) getActivity()
								.getApplication();
						if (app.isSignedin()) {
							if (app.getIsPatroller()) {
								intent = new Intent(getActivity(),
										PatrolActivity.class);
								String areaId = app.getArea();
								intent.putExtra(
										AddConditionFragment.EXTRA_TRAILS_URI,
										AreasSchema.buildTrailsUri(areaId));
								startActivity(intent);
							} else {
								Toast.makeText(getActivity(),
										R.string.patrollersOnly,
										Toast.LENGTH_LONG).show();
							}
						} else {
							if (app.signedInBefore()) {
								intent = new Intent(getActivity(),
										SigninActivity.class);
							} else {
								intent = new Intent(getActivity(),
										SignupActivity.class);
							}
							startActivity(intent);
						}

					}
				});
		// ((Button) root.findViewById(R.id.home_btn_patrol))
		// .setTypeface(app.mTf);
		 ((TextView) root.findViewById(R.id.home_btn_patrol_label))
		 .setTypeface(app.mTf);
		return root;
	}
}
