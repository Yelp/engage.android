package com.janrain.android.quickshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener, FeedReaderListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mViewFeedSummary;
    private boolean mFeedHasLoaded;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        if (Config.LOGD)
            Log.d(TAG, "[onCreate]");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mViewFeedSummary = (Button)findViewById(R.id.view_feed_summary);
        mViewFeedSummary.setOnClickListener(this);

        FeedData.getInstance(this).asyncLoadJanrainBlog(this);
    }

    public void onClick(View view) {
        if (Config.LOGD)
            Log.d(TAG, "[onClick] button clicked, " +
                    (mFeedHasLoaded ? "view feed summary" : "reloading blog"));

        if (mFeedHasLoaded)
            this.startActivity(new Intent(this, FeedSummaryActivity.class));
        else
            FeedData.getInstance(this).asyncLoadJanrainBlog(this);
    }

    public void AsyncFeedReadSucceeded() {
        if (Config.LOGD)
            Log.d(TAG, "[AsyncFeedReadSucceeded]");

        mViewFeedSummary.setEnabled(true);
        mFeedHasLoaded = true;
        mViewFeedSummary.setText(R.string.view_janrain_blog);
    }

    public void AsyncFeedReadFailed() {
        if (Config.LOGD)
            Log.d(TAG, "[AsyncFeedReadFailed]");

        mViewFeedSummary.setEnabled(true);
        mFeedHasLoaded = false;
        mViewFeedSummary.setText(R.string.reload_janrain_blog);
    }
}
