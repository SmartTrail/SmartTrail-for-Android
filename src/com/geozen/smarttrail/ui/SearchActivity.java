/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package com.geozen.smarttrail.ui;

import android.app.SearchManager;
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
import com.geozen.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import com.geozen.smarttrail.ui.phone.TrailDetailActivity;

/**
 * An activity that shows trail search results. This activity can be either single
 * or multi-pane, depending on the device configuration. We want the multi-pane support that
 * {@link BaseMultiPaneActivity} offers, so we inherit from it instead of
 * {@link BaseSinglePaneActivity}.
 */
public class SearchActivity extends BaseMultiPaneActivity {

    public static final String TAG_TRAILS = "trails";

    private String mQuery;

    private TabHost mTabHost;
    private TabWidget mTabWidget;

    private TrailsFragment mTrailsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();        
        mQuery = intent.getStringExtra(SearchManager.QUERY);

        setContentView(R.layout.activity_search);

        getActivityHelper().setupActionBar(getTitle(), 0);
        final CharSequence title = getString(R.string.title_search_query, mQuery);
        getActivityHelper().setActionBarTitle(title);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        mTabHost.setup();

        setupTrailsTab();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();

        ViewGroup detailContainer = (ViewGroup) findViewById(R.id.fragment_container_search_detail);
        if (detailContainer != null && detailContainer.getChildCount() > 1) {
            findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        mQuery = intent.getStringExtra(SearchManager.QUERY);

        final CharSequence title = getString(R.string.title_search_query, mQuery);
        getActivityHelper().setActionBarTitle(title);

        mTabHost.setCurrentTab(0);

        mTrailsFragment.reloadFromArguments(getSessionsFragmentArguments());
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

        final FragmentManager fm = getSupportFragmentManager();
        mTrailsFragment = (TrailsFragment) fm.findFragmentByTag("trails");
        if (mTrailsFragment == null) {
            mTrailsFragment = new TrailsFragment();
            mTrailsFragment.setArguments(getSessionsFragmentArguments());
            fm.beginTransaction()
                    .add(R.id.fragment_trails, mTrailsFragment, "trails")
                    .commit();
        }

        // Sessions content comes from reused activity
        mTabHost.addTab(mTabHost.newTabSpec(TAG_TRAILS)
                .setIndicator(buildIndicator(R.string.starred_trails))
                .setContent(R.id.fragment_trails));
    }

    

    private Bundle getSessionsFragmentArguments() {
        return intentToFragmentArguments(
                new Intent(Intent.ACTION_VIEW, TrailsSchema.buildSearchUri(mQuery)));
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
    public BaseMultiPaneActivity.FragmentReplaceInfo onSubstituteFragmentForActivityLaunch(
            String activityClassName) {
        if (findViewById(R.id.fragment_container_search_detail) != null) {
            // The layout we currently have has a detail container, we can add fragments there.
            findViewById(android.R.id.empty).setVisibility(View.GONE);
            if (TrailDetailActivity.class.getName().equals(activityClassName)) {
                clearSelectedItems();
                return new BaseMultiPaneActivity.FragmentReplaceInfo(
                        TrailDetailFragment.class,
                        "trail_detail",
                        R.id.fragment_container_search_detail);
            }
        }
        return null;
    }

    private void clearSelectedItems() {
        if (mTrailsFragment != null) {
            mTrailsFragment.clearCheckedPosition();
        }
    }
}
