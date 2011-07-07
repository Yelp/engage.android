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

import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
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

    public static final String JR_FRAGMENT_ID = "JR_FRAGMENT_ID";
    public static final int JR_PROVIDER_LIST = 4;
    public static final int JR_LANDING = 1;
    public static final int JR_WEBVIEW = 2;
    public static final int JR_PUBLISH = 3;

    // todo fixme: how does this layout helper get initialized, and/or are there two separate ones for the
    // host activity and the fragment? or just one?
    private SharedLayoutHelper mLayoutHelper = new SharedLayoutHelper(this);
    private int mFragmentId;
    private JRSessionData mSessionData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentId = getIntent().getExtras().getInt(JR_FRAGMENT_ID);

        mSessionData = JRSessionData.getInstance();
        /* For the case when this activity is relaunched after the process was killed */
        if (mSessionData == null) {
            Log.e(TAG, "bailing out after a process kill/restart");
            finish();
            return;
        }

        if (JRUserInterfaceMaestro.getInstance().isDialogMode()) {
            //setTheme(R.style.jr_dialog_no_title);
            if (AndroidUtils.getAndroidSdkInt() >= 11) {
                setTheme(16973943); // Theme_Holo_DialogWhenLarge
            }
        } else if (JRUserInterfaceMaestro.getInstance().isEmbeddedMode()) {
            // 
        } else { // Full screen mode
            if (mFragmentId == JR_WEBVIEW) {
                //Request progress indicator
                requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                requestWindowFeature(Window.FEATURE_PROGRESS);
            }
        }

        if (savedInstanceState == null) {
            // todo verify this flow control path -- when will savedInstanceState != null?
            Fragment f;
            switch (mFragmentId) {
                case JR_PROVIDER_LIST:
                    f = new JRProviderListFragment();
                    break;
                case JR_LANDING:
                    f = new JRLandingFragment();
                    break;
                case JR_WEBVIEW:
                    f = new JRWebViewFragment();
                    break;
                case JR_PUBLISH:
                    f = new JRPublishFragment();
                    break;
                default:
                    throw new IllegalFragmentIdException(mFragmentId);
            }

            getSupportFragmentManager().beginTransaction().add(android.R.id.content, f).commit();

            if (JRUserInterfaceMaestro.getInstance().isDialogMode()) {
                View content = findViewById(android.R.id.content);
                content.setMinimumHeight(AndroidUtils.scaleDipPixels(600));
            }
        } else {
        }
        //todo set mlayouthelper
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

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        switch (mFragmentId) {
            case JR_PROVIDER_LIST:
                mSessionData.triggerAuthenticationDidCancel();
                break;
            case JR_LANDING:
                mSessionData.triggerAuthenticationDidRestart();
                break;
            case JR_WEBVIEW:
                mSessionData.triggerAuthenticationDidRestart();
                break;
            case JR_PUBLISH:
                mSessionData.triggerPublishingDidComplete();
                break;
            default:
                throw new IllegalStateException(new IllegalFragmentIdException(mFragmentId));
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the shared menu
        mLayoutHelper.inflateAboutMenu(menu);
        return true;
    }

    /**
     * Callback for creating dialogs that are managed.
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        return mLayoutHelper.onCreateDialog(id);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mLayoutHelper.handleAboutMenu(item) || super.onOptionsItemSelected(item);
    }

    public static Class<? extends Fragment> getFragmentClassForId(int fragId) {
        switch (fragId) {
            case JR_PROVIDER_LIST: return JRProviderListFragment.class;
            case JR_LANDING: return JRLandingFragment.class;
            case JR_WEBVIEW: return JRWebViewFragment.class;
            case JR_PUBLISH: return JRPublishFragment.class;
            default: throw new IllegalFragmentIdException(fragId);
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
}