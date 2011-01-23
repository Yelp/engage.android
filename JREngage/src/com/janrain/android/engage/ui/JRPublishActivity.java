/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 Copyright (c) 2010, Janrain, Inc.

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
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSessionData;

import java.util.HashMap;
import java.util.Set;

/**
 * Publishing UI
 */
public class JRPublishActivity extends Activity implements View.OnClickListener {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    /**
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     */
    private class FinishReceiver extends BroadcastReceiver {

        private final String TAG = JRPublishActivity.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRPublishActivity.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    private static class ProviderArrayItem {

        private boolean mIsActivityInfoVisible;
        private int mIconResId;
        private int mShareBgColorResId;
        private int mShareButtonResId;

        ProviderArrayItem(boolean isInfoVisible, int iconResId, int shareBgColorResId, int shareBtnResId) {
            mIsActivityInfoVisible = isInfoVisible;
            mIconResId = iconResId;
            mShareBgColorResId = shareBgColorResId;
            mShareButtonResId = shareBtnResId;
        }

        boolean getIsActivityInfoVisible() {
            return mIsActivityInfoVisible;
        }

        int getIconResId() {
            return mIconResId;
        }

        int getShareBgColorResId() {
            return mShareBgColorResId;
        }

        int getShareBtnResId() {
            return mShareButtonResId;
        }
    }


    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            ProviderArrayItem item = PROVIDER_MAP.get(selected);

            if (item.getIsActivityInfoVisible()) {
                mActivityInfoContainer.setVisibility(View.VISIBLE);
            } else {
                mActivityInfoContainer.setVisibility(View.GONE);
            }

            mProviderIcon.setImageResource(item.getIconResId());
            mShareButtonContainer.setBackgroundResource(item.getShareBgColorResId());
            mShareButton.setBackgroundResource(item.getShareBtnResId());
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRPublishActivity.class.getSimpleName();

    private static HashMap<String, ProviderArrayItem> PROVIDER_MAP;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    static {
        PROVIDER_MAP = new HashMap<String, ProviderArrayItem>();
        PROVIDER_MAP.put("Facebook",
                new ProviderArrayItem(
                        true,
                        R.drawable.icon_facebook_30x30,
                        R.color.bg_clr_facebook,
                        R.drawable.button_facebook_280x40));
        PROVIDER_MAP.put("Twitter",
                new ProviderArrayItem(
                        false,
                        R.drawable.icon_twitter_30x30,
                        R.color.bg_clr_twitter,
                        R.drawable.button_twitter_280x40));
        PROVIDER_MAP.put("MySpace",
                new ProviderArrayItem(
                        false,
                        R.drawable.icon_myspace_30x30,
                        R.color.bg_clr_myspace,
                        R.drawable.button_myspace_280x40));
        PROVIDER_MAP.put("LinkedIn",
                new ProviderArrayItem(
                        false,
                        R.drawable.icon_linkedin_30x30,
                        R.color.bg_clr_linkedin,
                        R.drawable.button_linkedin_280x40));
    }
    
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private FinishReceiver mFinishReceiver;
    private SharedLayoutHelper mLayoutHelper;
    private JRSessionData mSessionData;

    private Spinner mSpinner;

    private LinearLayout mActivityInfoContainer;
    private ImageView mProviderIcon;
    private LinearLayout mShareButtonContainer;
    private Button mShareButton;


    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JRPublishActivity() {

    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_activity);

        mSessionData = JRSessionData.getInstance();
        mLayoutHelper = new SharedLayoutHelper(this);
        //mLayoutHelper.setHeaderText("Share");
        TextView title = (TextView)findViewById(R.id.header_text);
        title.setText("Share");

        mSpinner = (Spinner) findViewById(R.id.provider_spinner);

        Set<String> keySet = PROVIDER_MAP.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keyArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

        mActivityInfoContainer = (LinearLayout) findViewById(R.id.activity_info_container);
        mProviderIcon = (ImageView) findViewById(R.id.provider_icon);
        mShareButtonContainer = (LinearLayout) findViewById(R.id.share_button_container);
        mShareButton = (Button) findViewById(R.id.share_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mFinishReceiver);
    }
    
    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        finish();
    }

    public void onClick(View view) {
    }


}
