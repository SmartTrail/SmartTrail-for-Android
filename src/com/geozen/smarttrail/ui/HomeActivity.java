/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.util.AnalyticsUtils;
import com.geozen.smarttrail.util.EulaHelper;

/**
 * Front-door {@link Activity} that displays high-level features the schedule
 * application offers to users. Depending on whether the device is a phone or an
 * Android 3.0+ tablet, different layouts will be used. For example, on a phone,
 * the primary content is a {@link DashboardFragment}, whereas on a tablet, both
 * a {@link DashboardFragment} and a {@link TagStreamFragment} are displayed.
 */
public class HomeActivity extends BaseActivity {

	private TagStreamFragment mTagStreamFragment;
	private UpcomingEventsFragment mEventsFragment;
	private int SET_REGION_REQUEST= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!EulaHelper.hasAcceptedEula(this)) {
			EulaHelper.showEula(false, this);
		} else {
//			Toast toast = Toast.makeText(this, "This is a beta release for testing purposes only.",Toast.LENGTH_SHORT );
//			toast.setGravity(Gravity.CENTER, 0, 0);
//			toast.show();
		}

		AnalyticsUtils.getInstance(this).trackPageView("/Home");

		setContentView(R.layout.activity_home);
		getActivityHelper().setupActionBar(null, 0);

		FragmentManager fm = getSupportFragmentManager();

		mTagStreamFragment = (TagStreamFragment) fm
				.findFragmentById(R.id.fragment_tag_stream);
		mEventsFragment = (UpcomingEventsFragment) fm
		.findFragmentById(R.id.fragment_now_playing);
		
		triggerRefresh();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupHomeActivity();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
		getMenuInflater().inflate(R.menu.setregion_menu_item, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			triggerRefresh();
			return true;
		} else if (item.getItemId() == R.id.menu_setregion) {
			 Intent intent = new Intent(this, RegionsActivity.class);
	            startActivityForResult(intent, SET_REGION_REQUEST);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void triggerRefresh() {

		super.triggerRefresh();

		if (mTagStreamFragment != null) {
			mTagStreamFragment.refresh();
		}
		
	}

	
	public void onRefreshStatusChange(boolean status) {
		super.onRefreshStatusChange(status);
		if (status == false) {
			if (mEventsFragment != null) {
				mEventsFragment.refreshQuery();
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == SET_REGION_REQUEST) {
            if (resultCode == RESULT_OK) {
               triggerRefresh();
            }
        }
    }
}
