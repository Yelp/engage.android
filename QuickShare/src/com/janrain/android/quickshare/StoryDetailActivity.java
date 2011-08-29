package com.janrain.android.quickshare;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.janrain.android.engage.utils.AndroidUtils;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 8/23/11
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class StoryDetailActivity extends FragmentActivity {
    private static final String TAG = StoryDetailActivity.class.getSimpleName();
    private boolean mWaitingForChildActivityToFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mWaitingForChildActivityToFinish = savedInstanceState.getBoolean("waiting", false);
        }

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            StoryDetailFragment details = new StoryDetailFragment();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE
                && AndroidUtils.getAndroidSdkInt() >= 11
                && AndroidUtils.isXlarge()
                && !mWaitingForChildActivityToFinish) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("waiting", mWaitingForChildActivityToFinish);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForChildActivityToFinish = false;
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startWaiting() {
        mWaitingForChildActivityToFinish = true;
    }
}
