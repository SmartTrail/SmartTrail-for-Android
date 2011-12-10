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

package com.geozen.smarttrail.ui.phone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.ui.BaseSinglePaneActivity;
import com.geozen.smarttrail.ui.TrailDetailFragment;
import com.geozen.smarttrail.ui.AddConditionFragment.OnAddConditionListener;

public class TrailDetailActivity extends BaseSinglePaneActivity implements
		OnAddConditionListener {
	private TrailDetailFragment mTrailDetailFrag;

	@Override
	protected Fragment onCreatePane() {
		return mTrailDetailFrag = new TrailDetailFragment();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getActivityHelper().setupSubActivity();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_add_condition:
			mTrailDetailFrag.startAddConditionFragment(R.id.root_container);

			return true;

		}
		return super.onOptionsItemSelected(item);
	}
}
