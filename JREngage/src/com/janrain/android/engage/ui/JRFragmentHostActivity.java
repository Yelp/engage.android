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

//import android.R;
import android.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import com.janrain.android.engage.JREngage;
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
    public static final String ACTION_FINISH_FRAGMENT = "com.janrain.android.engage.ACTION_FINISH_FRAGMENT";
    public static final String EXTRA_FINISH_FRAGMENT_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_FRAGMENT_TARGET";
    public static final IntentFilter FINISH_INTENT_FILTER = new IntentFilter(ACTION_FINISH_FRAGMENT);

    private int mFragmentId;
    private JRUiFragment mUiFragment;
    private JRSessionData mSessionData;
    private SharedLayoutHelper mLayoutHelper;

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

        if (!AndroidUtils.isSmallOrNormalScreen()) {
            if (AndroidUtils.getAndroidSdkInt() >= 11) {
                switch (mFragmentId) {
                    case JR_LANDING:
                        /* fall through to provider list */
                    case JR_PROVIDER_LIST:
                        setTheme(16973945); // R.style.Theme_Holo_Light_DialogWhenLarge
                        //setTheme(R.style.Theme_Dialog);
                        break;
                    case JR_WEBVIEW:
                        /* fall through to publish */
                    case JR_PUBLISH:
                        setTheme(16973946); // R.style.Theme_Holo_Light_DialogWhenLarge_NoActionBar
                        //setTheme(R.style.Theme_Dialog);
                        break;
                    default: throw new IllegalFragmentIdException(mFragmentId);
                }
            }
        } else { // Full screen mode
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.FILL_PARENT;
            lp.height = WindowManager.LayoutParams.FILL_PARENT;
            getWindow().setAttributes(lp);

            Context c = JREngage.getContext();
            if (c instanceof Activity) {
                Resources.Theme t = c.getTheme();
                getTheme().setTo(t);
                //setTheme();
            }
            //setTheme(R.style.Theme_Light);

            if (mFragmentId == JR_WEBVIEW) {
                //Request progress indicator
                requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
                //requestWindowFeature(Window.FEATURE_PROGRESS);
            }
        }

        if (savedInstanceState == null) {
            // todo verify this flow control path -- when will savedInstanceState != null?
            switch (mFragmentId) {
                case JR_PROVIDER_LIST:
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
        }

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        //    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mLayoutHelper = mUiFragment.getSharedLayoutHelper();
        autoSetSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        autoSetSize();
    }

    private void autoSetSize() {
        if (!AndroidUtils.isSmallOrNormalScreen()) {
            //View content = findViewById(android.R.id.content);
            //content.setMinimumHeight(AndroidUtils.scaleDipPixels(600));
            //content.setMinimumHeight(AndroidUtils.scaleDipPixels(height));
            //content.setMinimumWidth(AndroidUtils.scaleDipPixels(width));

            int height = (int) (1.0 / Math.sqrt(2.0) * getResources().getDisplayMetrics().heightPixels);
            int width = (int) (1.0 / Math.sqrt(2.0) * getResources().getDisplayMetrics().widthPixels);
            getWindow().setLayout(width, height);

            //if (mFragmentId == JR_PUBLISH) {
            //    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            //    lp.copyFrom(getWindow().getAttributes());
            //    lp.width = WindowManager.LayoutParams.FILL_PARENT;
            //    //lp.height = WindowManager.LayoutParams.FILL_PARENT;
            //    getWindow().setAttributes(lp);
            //}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        return mUiFragment.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
        mUiFragment.onPrepareDialog(id, d);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mLayoutHelper.handleAboutMenu(item) || super.onOptionsItemSelected(item);
    }

    //public static Class<? extends Fragment> getFragmentClassForId(int fragId) {
    //    switch (fragId) {
    //        case JR_PROVIDER_LIST: return JRProviderListFragment.class;
    //        case JR_LANDING: return JRLandingFragment.class;
    //        case JR_WEBVIEW: return JRWebViewFragment.class;
    //        case JR_PUBLISH: return JRPublishFragment.class;
    //        default: throw new IllegalFragmentIdException(fragId);
    //    }
    //}

    public static class IllegalFragmentIdException extends RuntimeException {
        int mFragId;

        public IllegalFragmentIdException(int fragId) {
            mFragId = fragId;
        }

        public String toString() {
            return "Bad fragment ID: " + mFragId;
        }
    }

    public static Intent makeIntentForCurrentScreen(Context c) {
        if (AndroidUtils.isSmallOrNormalScreen()) {
            return new Intent(c, JRFragmentHostActivityFullscreen.class);
        } else {
            return new Intent(c, JRFragmentHostActivity.class);
        }
    }
}