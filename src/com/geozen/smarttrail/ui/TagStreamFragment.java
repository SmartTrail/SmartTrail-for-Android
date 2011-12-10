/**
 * User: matt
 *
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package com.geozen.smarttrail.ui;

import com.geozen.smarttrail.R;
import com.geozen.smarttrail.app.SmartTrailApplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A {@link WebView}-based fragment that shows Google Realtime Search results for a given query,
 * provided as the {@link TagStreamFragment#EXTRA_QUERY} extra in the fragment arguments. If no
 * search query is provided, the conference hashtag is used as the default query.
 */
public class TagStreamFragment extends Fragment {

    private static final String TAG = "TagStreamFragment";

    public static final String EXTRA_QUERY = "com.geozen.smarttrail.extra.QUERY";

   // public static final String CONFERENCE_HASHTAG = "#boco_trails OR #valmontbikepark";
   // public static final String CONFERENCE_HASHTAG = "#boco_trails";

    private String mSearchString;
    private WebView mWebView;
    private View mLoadingSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
        mSearchString = intent.getStringExtra(EXTRA_QUERY);
        if (TextUtils.isEmpty(mSearchString)) {
        	
        	SmartTrailApplication app = ((SmartTrailApplication) getActivity().getApplication());
            mSearchString = app.getSponsorTwitterQuery();
        }
        if (!mSearchString.startsWith("#")) {
            mSearchString = "#" + mSearchString;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_webview_with_spinner, null);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
        mWebView = (WebView) root.findViewById(R.id.webview);
        mWebView.setWebViewClient(mWebViewClient);

        mWebView.post(new Runnable() {
            public void run() {
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
                try {
//                	mWebView.loadUrl(
//                			"http://www.google.com/search?tbs="
//                			+ "mbl%3A1&hl=en&source=hp&q="
//                			+ URLEncoder.encode(mSearchString, "UTF-8")
//                			+ "&btnG=Search");
//                    mWebView.loadUrl(
//                            "http://www.google.com/search?tbs="
//                            + "mbl%3A1&hl=en&source=hp&biw=1170&bih=668&q="
//                            + URLEncoder.encode(mSearchString, "UTF-8")
//                            + "&btnG=Search");
                	String url = "https://mobile.twitter.com/searches?q="
						+ URLEncoder.encode(mSearchString, "UTF-8");
                    mWebView.loadUrl(url);
                    
                    
                    
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Could not construct the realtime search URL", e);
                }
            }
        });

        return root;
    }

    public void refresh() {
        mWebView.reload();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mLoadingSpinner.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mLoadingSpinner.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("javascript")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    };
}
