/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class AlertsActivity extends BaseSinglePaneActivity {
    @Override
    protected Fragment onCreatePane() {
        return new AlertsFragment();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }    
}
