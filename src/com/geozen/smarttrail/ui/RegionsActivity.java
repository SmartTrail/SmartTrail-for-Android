/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;


import com.geozen.smarttrail.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RegionsActivity extends BaseSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new RegionsFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
        getActivityHelper().setActionBarTitle(getString(R.string.setRegion));
    }    
}
