/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;


import com.geozen.smarttrail.R;
import com.geozen.smarttrail.provider.SmartTrailSchema.AreasSchema;
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.ui.AddConditionFragment.OnAddConditionListener;
import com.geozen.smarttrail.ui.phone.AreaDetailActivity;
import com.geozen.smarttrail.ui.phone.TrailDetailActivity;

/**
 * An activity that shows the user's starred trails. This activity can be
 * either single or multi-pane, depending on the device configuration. We want the multi-pane
 * support that {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class FavoritesActivity extends BaseMultiPaneActivity implements
OnAddConditionListener {

	public static final String TAG_AREAS = "areas";
    public static final String TAG_TRAILS = "trails";

    private TabHost mTabHost;
    private TabWidget mTabWidget;

    private AreasFragment mAreasFragment;
    private TrailsFragment mTrailsFragment;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred);
        getActivityHelper().setupActionBar(getTitle(), 0);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        mTabHost.setup();
        setupAreasTab();
        setupTrailsTab();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_trail_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 1) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
       
        
     
    }

    /**
     * Build and add "trails" tab.
     */
    private void setupAreasTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_areas);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, AreasSchema.CONTENT_STARRED_URI);

        final FragmentManager fm = getSupportFragmentManager();
        mAreasFragment = (AreasFragment) fm.findFragmentByTag(TAG_AREAS);
        if (mAreasFragment == null) {
        	mAreasFragment = new AreasFragment();
        	mAreasFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_areas, mAreasFragment, TAG_AREAS)
                    .commit();
        }

        // Sessions content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_AREAS)
                .setIndicator(buildIndicator(R.string.starred_areas))
                .setContent(R.id.fragment_areas));
    }
    
    /**
     * Build and add "trails" tab.
     */
    private void setupTrailsTab() {
        // TODO: this is very inefficient and messy, clean it up
        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_trails);
        fragmentContainer.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));
        ((ViewGroup) findViewById(android.R.id.tabcontent)).addView(fragmentContainer);

        final Intent intent = new Intent(Intent.ACTION_VIEW, TrailsSchema.CONTENT_STARRED_URI);

        final FragmentManager fm = getSupportFragmentManager();
        mTrailsFragment = (TrailsFragment) fm.findFragmentByTag(TAG_TRAILS);
        if (mTrailsFragment == null) {
            mTrailsFragment = new TrailsFragment();
            mTrailsFragment.setArguments(intentToFragmentArguments(intent));
            fm.beginTransaction()
                    .add(R.id.fragment_trails, mTrailsFragment, TAG_TRAILS)
                    .commit();
        }

        // Sessions content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_TRAILS)
                .setIndicator(buildIndicator(R.string.starred_trails))
                .setContent(R.id.fragment_trails));
    }

    

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     */
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
                mTabWidget, false);
        indicator.setText(textRes);
        return indicator;
    }

    @Override
    public FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(String activityClassName) {
        if (findViewById(R.id.fragment_container_trail_detail) != null) {
            // The layout we currently have has a detail container, we can add fragments there.
            findViewById(android.R.id.empty).setVisibility(View.GONE);
            if (TrailDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        TrailDetailFragment.class,
                        "trail_detail",
                        R.id.fragment_container_trail_detail);
            } else  if (AreaDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new FragmentReplaceInfo(
                        AreaDetailFragment.class,
                        "area_detail",
                        R.id.fragment_container_trail_detail);
            } 
        }
        return null;
    }

    private void clearSelectedItems() {
        if (mTrailsFragment != null) {
            mTrailsFragment.clearCheckedPosition();
        }
//        if (mAreasFragment != null) {
//        	mAreasFragment.clearCheckedPosition();
//        }
        
    }
    @Override
	public void onConditionSubmitted() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();
	}

	@Override
	public void onConditionCancelled() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();

	}
}
