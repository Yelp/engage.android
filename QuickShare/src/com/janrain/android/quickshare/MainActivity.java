/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 Copyright (c) 2011, Janrain, Inc.

 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation and/or
   other materials provided with the distribution.
 * Neither the name of the Janrain, Inc. nor the names of its
   contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 File:   MainActivity.java
 Author: Lilli Szafranski - lilli@janrain.com
 Date:   April 22, 2011
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
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

        if (FeedData.getInstance(this).getFeed().isEmpty()) {
            FeedData.getInstance(this).asyncLoadJanrainBlog(this);
        }
        else {
            mViewFeedSummary.setEnabled(true);
            mFeedHasLoaded = true;
            mViewFeedSummary.setText(R.string.view_janrain_blog);
        }
    }

    public void onClick(View view) {
        if (Config.LOGD)
            Log.d(TAG, "[onClick] button clicked, " +
                    (mFeedHasLoaded ? "view feed summary" : "reloading blog"));

        if (mFeedHasLoaded) {
            this.startActivity(new Intent(this, FeedSummaryActivity.class));
        }
        else {
            mViewFeedSummary.setText(R.string.loading_janrain_blog);
            FeedData.getInstance(this).asyncLoadJanrainBlog(this);
        }
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
