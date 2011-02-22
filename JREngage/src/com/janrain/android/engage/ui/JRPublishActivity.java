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
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRAuthenticatedUser;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.types.JRActivityObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Publishing UI
 */
public class JRPublishActivity extends TabActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener, TabHost.OnTabChangeListener {

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

    /**
     * UI display attributes for for each of the supported providers.
     */
    private static class ProviderDisplayInfo {
        // whether or not activity info section is displayed
        private boolean mIsMediaContentVisible;
        // provider icon resource id
        private int mIconResId;
        // share section background color
        private int mShareBgColorResId;
        // share button resource id
        private int mShareButtonResId;

        ProviderDisplayInfo(boolean isInfoVisible, int iconResId, int shareBgColorResId, int shareBtnResId) {
            mIsMediaContentVisible = isInfoVisible;
            mIconResId = iconResId;
            mShareBgColorResId = shareBgColorResId;
            mShareButtonResId = shareBtnResId;
        }

        boolean getIsMediaContentVisible() {
            return mIsMediaContentVisible;
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


    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRPublishActivity.class.getSimpleName();

    private static HashMap<String, ProviderDisplayInfo> PROVIDER_MAP;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    static {
        PROVIDER_MAP = new HashMap<String, ProviderDisplayInfo>();
        PROVIDER_MAP.put("Facebook",
                new ProviderDisplayInfo(
                        true,
                        R.drawable.icon_facebook_30x30,
                        R.color.bg_clr_facebook,
                        R.drawable.button_facebook_280x40));
        PROVIDER_MAP.put("Twitter",
                new ProviderDisplayInfo(
                        false,
                        R.drawable.icon_twitter_30x30,
                        R.color.bg_clr_twitter,
                        R.drawable.button_twitter_280x40));
        PROVIDER_MAP.put("MySpace",
                new ProviderDisplayInfo(
                        false,
                        R.drawable.icon_myspace_30x30,
                        R.color.bg_clr_myspace,
                        R.drawable.button_myspace_280x40));
        PROVIDER_MAP.put("LinkedIn",
                new ProviderDisplayInfo(
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
    private JRProvider mSelectedProvider;
    private JRAuthenticatedUser mLoggedInUser;
    
    private JRActivityObject mActivityObject;

//    private Spinner mSpinner;

    private RelativeLayout mMediaContentView;
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
        mActivityObject = mSessionData.getActivity();

        mLayoutHelper = new SharedLayoutHelper(this);

//        mSpinner = (Spinner) findViewById(R.id.provider_spinner);

//        Set<String> keySet = PROVIDER_MAP.keySet();
//        String[] keyArray = keySet.toArray(new String[keySet.size()]);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, keyArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpinner.setAdapter(adapter);
//        mSpinner.setOnItemSelectedListener(this);

        configureTabs();

        if (mSessionData.getHidePoweredBy()) {
            TextView poweredBy = (TextView)findViewById(R.id.powered_by_text);
            poweredBy.setVisibility(View.GONE);
        }
        
        TextView title = (TextView)findViewById(R.id.header_text);
        title.setText(getString(R.string.publish_activity_title));

        mMediaContentView = (RelativeLayout) findViewById(R.id.media_content_view);
        mProviderIcon = (ImageView) findViewById(R.id.provider_icon);
        mShareButtonContainer = (LinearLayout) findViewById(R.id.share_button_container);
        mShareButton = (Button) findViewById(R.id.share_button);
        mShareButton.setOnClickListener(this);
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

    /**
     * Invoked by JRUserInterfaceMaestro via FinishReceiver to close this activity.
     */
    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        finish();
    }

    //
    // View.OnClickListener
    //

    public void onClick(View view) {
    }


    //
    // AdapterView.OnItemSelectedListener
    //

    private static final Map<String, Integer> icon_resources = new HashMap<String, Integer>(){
       {
           put("facebook", R.drawable.ic_facebook_tab);
           put("linkedin", R.drawable.ic_linkedin_tab);
           put("myspace", R.drawable.ic_myspace_tab);
           put("twitter", R.drawable.ic_twitter_tab);
           put("yahoo", R.drawable.ic_yahoo_tab);
       }
    };

    private void configureTabs() {

        // TODO: If no providers


        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab

        int currentIndex = 0, indexOfLastUsedProvider = 0;
        for (JRProvider provider : mSessionData.getSocialProviders())
        {
            // TODO: If provider is NULL
            
            spec = tabHost.newTabSpec(provider.getName()).setIndicator(provider.getFriendlyName(),
                              res.getDrawable(icon_resources.get(provider.getName())))
                          .setContent(R.id.tab_view_content);
            tabHost.addTab(spec);

            if (provider.getName().equals(mSessionData.getReturningSocialProvider()))
                indexOfLastUsedProvider = currentIndex;

            currentIndex++;
        }

        tabHost.setCurrentTab(indexOfLastUsedProvider);

        tabHost.setOnTabChangedListener(this);
    }

    public void onTabChanged(String tabId) {
        Log.d(TAG, "[onTabChange]: " + tabId);

        mSessionData.setCurrentProviderByName(tabId);

        mSelectedProvider = mSessionData.getCurrentProvider();

        String can_share_media = (String)mSelectedProvider.getSocialSharingProperties().get("can_share_media");

        if (can_share_media.equals("YES"))
            mMediaContentView.setVisibility(View.VISIBLE);
        else
            mMediaContentView.setVisibility(View.GONE);



    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String selected = parent.getItemAtPosition(pos).toString();
        ProviderDisplayInfo item = PROVIDER_MAP.get(selected);

        mMediaContentView.setVisibility(item.getIsMediaContentVisible()
                ? View.VISIBLE : View.GONE);
        mProviderIcon.setImageResource(item.getIconResId());
        mShareButtonContainer.setBackgroundResource(item.getShareBgColorResId());
        mShareButton.setBackgroundResource(item.getShareBtnResId());
    }

    public void onNothingSelected(AdapterView parent) {
        // Do nothing.
    }

    //
    // Helper methods
    //

    private void loadActivityObjectToView() {

        // TODO:  check "hasEditedUserContentForActivityAlready"

        if (/*(weAreReady) && */ (mActivityObject.getMedia().size() > 0)
                && providerCanShareMedia(mSelectedProvider.getName())) {

            showHideMediaContentView(true);


        } else {
            showHideMediaContentView(false);
        }

    }

    private boolean providerCanShareMedia(String providerName) {
        return (!TextUtils.isEmpty(providerName)) && ("facebook".equalsIgnoreCase(providerName));
    }

    private void showHideMediaContentView(boolean show) {
        mMediaContentView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
