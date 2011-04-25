package com.janrain.android.quickshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoryDetailActivity extends Activity implements View.OnClickListener {
    private static final String TAG = StoryDetailActivity.class.getSimpleName();

    private FeedData mFeedData;
    private Story mStory;

    private Button mShareStory;
//    private TextView mTitle;
//    private TextView mDate;
//    private TextView mText;

    private WebView mWebview;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_detail_webview);

        mFeedData = FeedData.getInstance(this);

        mShareStory = (Button)findViewById(R.id.share_button);
        mShareStory.setOnClickListener(this);

        mWebview = (WebView)findViewById(R.id.webview_1);
        mWebview.setWebViewClient(mWebviewClient);

//        mTitle = (TextView)findViewById(R.id.title);
//        mDate = (TextView)findViewById(R.id.date);
//        mText = (TextView)findViewById(R.id.text);

        loadCurrentStory();
    }

    public void loadCurrentStory() {
        mStory = mFeedData.getCurrentStory();

        mWebview.loadDataWithBaseURL("http://www.janrain.com/blogs/",
                                            "<html><body>" +
                                            "<h1>" + mStory.getTitle() + "</h1><br />" +
                                            "<h2>" + mStory.getDate() + "</h2><br />" +
                                            "<div class='body'>" + mStory.getDescription() + "</div>" +
                                            "</body></html>",
                                            "text/html", "UTF-8", "");

//        mTitle.setText(mStory.getTitle());
//        mDate.setText(mStory.getDate());
//        mText.setText(mStory.getDescription());
    }

    public void onClick(View view) {
        mFeedData.shareCurrentStory();
    }

    private void openNewWebViewToUrl(String url) {
        mFeedData.setUrlToBeLoaded(url);
        this.startActivity(new Intent(this, DeeperWebViewActivity.class));
    }

    private WebViewClient mWebviewClient = new WebViewClient(){
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            if (Config.LOGD)
                Log.d(TAG, "[shouldOverrideUrlLoading] url: " + url);

            openNewWebViewToUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (Config.LOGD)
                Log.d(TAG, "[onPageStarted] url: " + url);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (Config.LOGD)
                Log.d(TAG, "[onPageFinished] url: " + url);

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            super.onReceivedError(view, errorCode, description, url);
            Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                + " | url: " + url);
        }
    };
}