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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.KeyEvent;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.utils.AndroidUtils;

public class JRFragmentHostActivity extends FragmentActivity {
    private static final String TAG = JRFragmentHostActivity.class.getSimpleName();
    public static final String JR_FRAGMENT_ID = "com.janrain.android.engage.JR_FRAGMENT_ID";
    public static final int JR_PROVIDER_LIST = 4;
    public static final int JR_LANDING = 1;
    public static final int JR_WEBVIEW = 2;
    public static final int JR_PUBLISH = 3;
    public static final String ACTION_FINISH_FRAGMENT = "com.janrain.android.engage.ACTION_FINISH_FRAGMENT";
    public static final String EXTRA_FINISH_FRAGMENT_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_FRAGMENT_TARGET";
    public static final String FINISH_TARGET_ALL = "JR_FINISH_ALL";
    public static final IntentFilter FINISH_INTENT_FILTER = new IntentFilter(ACTION_FINISH_FRAGMENT);

    private int mFragmentId;
    private JRUiFragment mUiFragment;
    private JRSessionData mSessionData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentId = getIntent().getExtras().getInt(JR_FRAGMENT_ID);
        Log.d(TAG, "[onCreate]: " + mFragmentId);

        mSessionData = JRSessionData.getInstance();
        /* For the case when this activity is relaunched after the process was killed */
        if (mSessionData == null) {
            Log.e(TAG, "bailing out after a process kill/restart");
            finish();
            return;
        }

        switch (mFragmentId) {
            case JR_PROVIDER_LIST:
                /* check and see whether we should start the landing page */
                String rbpName = mSessionData.getReturningBasicProvider();
                JRProvider rbp = mSessionData.getProviderByName(rbpName);
                if (!TextUtils.isEmpty(rbpName)
                        && mSessionData.getAuthenticatedUserForProvider(rbp) != null
                        && !mSessionData.getAlwaysForceReauth()
                        && !rbp.getForceReauth()) {
                    // These other conditions are in the iPhone UI Maestro
                    //&& !sessionData.socialSharing
                    //&& ![((NSArray*)[customInterface objectForKey:kJRRemoveProvidersFromAuthentication]) containsObject:sessionData.returningBasicProvider]
                    //&& [sessionData.basicProviders containsObject:sessionData.returningBasicProvider])

                    JRProvider provider = mSessionData.getProviderByName(rbpName);
                    mSessionData.setCurrentlyAuthenticatingProvider(provider);
                    Intent i = createIntentForCurrentScreen(this, true);
                    i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, JR_LANDING);
                    startActivityForResult(i, JRUiFragment.REQUEST_LANDING);
                }

                mUiFragment = new JRProviderListFragment();
                break;
            case JR_LANDING:
                mUiFragment = new JRLandingFragment();
                break;
            case JR_WEBVIEW:
                mUiFragment = new JRWebViewFragment();
                break;
            case JR_PUBLISH:
                mUiFragment = new JRPublishFragment();
                break;
            default:
                throw new IllegalFragmentIdException(mFragmentId);
        }


        setContentView(R.layout.jr_fragment_host_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.jr_fragment_container, mUiFragment)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();

        if (savedInstanceState != null) {
            throw new IllegalStateException("unexpected not null savedInstanceState");

            // This control flow path is not reached  because this activity handles configuration changes
            // and doesn't implement onSaveInstanceState
        }
    }

    @Override
    public void setTheme(int r) {
        Log.d(TAG, "setTheme: " + r);
        super.setTheme(r);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Config.LOGD) Log.d(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode <= 1<<16) mUiFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (AndroidUtils.isCupcake()
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            onBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        switch (mFragmentId) {
            case JR_PROVIDER_LIST:
                mSessionData.triggerAuthenticationDidCancel();
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case JR_LANDING:
                mSessionData.triggerAuthenticationDidRestart();
                setResult(JRLandingFragment.RESULT_RESTART);
                finish();
                break;
            case JR_WEBVIEW:
                mSessionData.triggerAuthenticationDidRestart();
                setResult(JRWebViewFragment.RESULT_RESTART);
                finish();
                break;
            case JR_PUBLISH:
                mSessionData.triggerPublishingDidComplete();
                setResult(Activity.RESULT_OK);
                finish();
                break;
            default:
                throw new IllegalStateException(new IllegalFragmentIdException(mFragmentId));
        }
    }

    public static class IllegalFragmentIdException extends RuntimeException {
        int mFragId;

        public IllegalFragmentIdException(int fragId) {
            mFragId = fragId;
        }

        public String toString() {
            return "Bad fragment ID: " + mFragId;
        }
    }

    public static Intent createIntentForCurrentScreen(Context c, boolean showTitleBar) {
        if (AndroidUtils.isSmallNormalOrLargeScreen()) {
            if (showTitleBar) {
                return new Intent(c, Fullscreen.class);
            } else {
                return new Intent(c, FullscreenNoTitleBar.class);
            }
        } else { // Honeycomb (because the screen is large+)
            // ignore showTitleBar, this activity dynamically enables and disables its title
            return new Intent(c, JRFragmentHostActivity.class);
        }
    }

    /* ~aliases for alternative activity declarations for this activity */
    public static class Fullscreen extends JRFragmentHostActivity {}

    public static class FullscreenNoTitleBar extends Fullscreen {}
}