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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.*;
//import android.view.inputmethod.InputMethodManager;
//import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.utils.AndroidUtils;

/**
 * @internal
 *
 * @class JRWebViewActivity
 * Landing Page Activity
 **/
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

        if (!AndroidUtils.isSmallOrNormalScreen()) {
            if (AndroidUtils.getAndroidSdkInt() >= 11) {
                /* Fiddle with the theme */
                switch (mFragmentId) {
                    case JR_LANDING:
                        /* fall through to provider list */
                    case JR_PROVIDER_LIST:
                        setTheme(16973945); // R.style.Theme_Holo_Light_DialogWhenLarge
                        break;
                    case JR_WEBVIEW:
                        /* fall through to publish */
                    case JR_PUBLISH:
                        setTheme(16973946); // R.style.Theme_Holo_Light_DialogWhenLarge_NoActionBar
                        break;
                    default: throw new IllegalFragmentIdException(mFragmentId);
                }
            } else { // else less than Honeycomb and bigger than normal screen
                //switch (mFragmentId) {
                //    case JR_LANDING:
                //    case JR_PROVIDER_LIST:
                //        setTheme(android.R.style.Theme_NoTitleBar);
                //        //this.getTheme().
                //        break;
                //    case JR_WEBVIEW:
                //    case JR_PUBLISH:
                //        setTheme(android.R.style.Theme_Light_NoTitleBar);
                //        break;
                //    default: throw new IllegalFragmentIdException(mFragmentId);
                //}
            }
        } else { // else small or normal screen -> full screen mode
        }

        if (savedInstanceState == null) {
            switch (mFragmentId) {
                case JR_PROVIDER_LIST:
                    /* check and see whether we should start the landing page */
                    String rbpName = mSessionData.getReturningBasicProvider();
                    if (!TextUtils.isEmpty(rbpName)) {
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

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mUiFragment)
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                    .commit();
        } else {
            //savedInstanceState != null

            // todo verify this flow control path -- when will savedInstanceState != null?
        }
    }

    @Override
    public void setTheme(int r) {
        Log.d(TAG, "setTheme: " + r);
        super.setTheme(r);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        autoSetSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        autoSetSize();
    }

    private void autoSetSize() {
        if (!AndroidUtils.isSmallOrNormalScreen()) {
            int height = (int) (1.0 / Math.sqrt(2.0) * getResources().getDisplayMetrics().heightPixels);
            int width = (int) (1.0 / Math.sqrt(2.0) * getResources().getDisplayMetrics().widthPixels);

            //View v = findViewById(android.R.id.content);
            //ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            //layoutParams.
            //v.setLayoutParams(layoutParams);
            //v.setMinimumHeight(height);
            //v.setMinimumWidth(width);
            getWindow().setLayout(width, height);
        }
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
        if (AndroidUtils.isSmallOrNormalScreen()) {
            if (showTitleBar) {
                return new Intent(c, JRFragmentHostActivityFullscreen.class);
            } else {
                return new Intent(c, JRFragmentHostActivityFullscreenNoTitleBar.class);
            }
        } else {
            // ignore showTitleBar, this activity dynamically enables and disables its title
            return new Intent(c, JRFragmentHostActivity.class);
        }
    }
}