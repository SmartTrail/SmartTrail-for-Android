/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import com.geozen.smarttrail.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

public class TagStreamActivity extends BaseSinglePaneActivity {
    private TagStreamFragment mTagStreamFragment;

	@Override
    protected Fragment onCreatePane() {
    	mTagStreamFragment= new TagStreamFragment();
        return mTagStreamFragment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
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

		case R.id.menu_refresh:
			mTagStreamFragment.refresh();

			return true;

		}
		return super.onOptionsItemSelected(item);
	}
}
