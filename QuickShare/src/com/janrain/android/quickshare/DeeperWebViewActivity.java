package com.janrain.android.quickshare;

import android.app.Activity;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/25/11
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeeperWebViewActivity extends Activity {
    private static final String TAG = DeeperWebViewActivity.class.getSimpleName();

    private WebView mWebview;
    //private String mUrl;

    public void onCreate(Bundle savedInstanceState) {
        if (Config.LOGD)
            Log.d(TAG, "[onCreate]");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.deeper_webview);

        mWebview = (WebView)findViewById(R.id.deeper_webview);
        mWebview.loadUrl(FeedData.getInstance(this).getUrlToBeLoaded());

        if (Config.LOGD)
            Log.d(TAG, "[onCreate] loading url: " + FeedData.getInstance(this).getUrlToBeLoaded());
    }

//    public void loadUrl(String url) {
//        //mUrl = url;
//
//        mWebview.loadUrl(url);
//    }
}