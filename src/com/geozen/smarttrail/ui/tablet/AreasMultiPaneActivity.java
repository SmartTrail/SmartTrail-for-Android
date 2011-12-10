/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geozen.smarttrail.ui.tablet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.ui.AreaDetailFragment;
import com.geozen.smarttrail.ui.AreasFragment;
import com.geozen.smarttrail.ui.BaseMultiPaneActivity;
import com.geozen.smarttrail.ui.TrailDetailFragment;
import com.geozen.smarttrail.ui.TrailsFragment;
import com.geozen.smarttrail.ui.AddConditionFragment.OnAddConditionListener;
import com.geozen.smarttrail.ui.phone.AreaDetailActivity;
import com.geozen.smarttrail.ui.phone.AreasActivity;

/**
 * A multi-pane activity, consisting of a {@link AreasDropdownFragment}, a
 * {@link TrailsFragment}, and {@link TrailDetailFragment}.
 * 
 * This activity requires API level 11 or greater because
 * {@link AreasDropdownFragment} requires API level 11.
 */
public class AreasMultiPaneActivity extends BaseMultiPaneActivity implements
		OnAddConditionListener {

	private AreasDropdownFragment mAreasDropdownFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trails);

		Intent intent = new Intent();
		intent.setData(AreasSchema.CONTENT_URI);
		

		final FragmentManager fm = getSupportFragmentManager();
		mAreasDropdownFragment = (AreasDropdownFragment) fm
				.findFragmentById(R.id.fragment_areas_dropdown);
		mAreasDropdownFragment
				.reloadFromArguments(intentToFragmentArguments(intent));


	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();

		ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_trail_detail);
		if (detailContainer != null && detailContainer.getChildCount() > 0) {
			findViewById(R.id.fragment_container_trail_detail)
					.setBackgroundColor(0xffffffff);
		}
	}

	@Override
	public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(
			String activityClassName) {
		if (AreasActivity.class.getName().equals(activityClassName)) {
			return new FragmentReplaceInfo(AreasFragment.class, "areas",
					R.id.fragment_container_trails);
		} else if (AreaDetailActivity.class.getName()
				.equals(activityClassName)) {
			findViewById(R.id.fragment_container_trail_detail)
					.setBackgroundColor(0xffffffff);
			return new FragmentReplaceInfo(AreaDetailFragment.class,
					"area_detail", R.id.fragment_container_trail_detail);
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
		//getMenuInflater().inflate(R.menu.add_condition_menu_item, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}



	@Override
	public void onConditionSubmitted() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();
		triggerRefresh();
	}

	@Override
	public void onConditionCancelled() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();

	}
}
