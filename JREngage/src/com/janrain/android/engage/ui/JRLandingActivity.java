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
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;

/**
 * @internal
 *
 * @class JRLandingActivity
 * Landing Page Activity
 **/
public class JRLandingActivity extends Activity {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JRUserInterfaceMaestro.  A facility
     * for iPhone-like ability to close this activity from the maestro class.
     **/
    private class FinishReceiver extends BroadcastReceiver {

        private final String TAG = JRLandingActivity.TAG + "-" +
                FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRLandingActivity.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    private class ButtonEventListener implements
            View.OnClickListener {//}, View.OnFocusChangeListener, View.OnTouchListener {

        private final String TAG = JRLandingActivity.TAG + "-" +
                ButtonEventListener.class.getSimpleName();

        public void onClick(View view) {
            Log.i(TAG, "[onClick] handled");

            if (view.equals(mSigninButton)) {// || view.equals(mBigSigninButton)) {
                handlePrimaryButtonClick();
            } else if (view.equals(mSwitchAccountButton)) {
                handleSecondaryButtonClick();
            }
        }

        //public void onFocusChange(View view, boolean hasFocus) {
        //    Log.i(TAG, "[onFocusChange] hasFocus = " + (hasFocus ? "true" : "false"));
        //
        //    if (hasFocus)
        //        view.getBackground().clearColorFilter();
        //    else
        //        if (view == mSwitchAccountButton)
        //            view.getBackground().setColorFilter(0xFFAAAAAA, PorterDuff.Mode.MULTIPLY);
        //        else if (view == mSigninButton)
        //            view.getBackground().setColorFilter(0xFF1A557C, PorterDuff.Mode.MULTIPLY);
        //}

        //public boolean onTouch(View view, MotionEvent motionEvent) {
        //    Log.i(TAG, "[onTouch] motionEvent = " + motionEvent.toString());
        //
        //    if (view == mSwitchAccountButton)
        //        mSwitchAccountButton.getBackground().setColorFilter(0xFFAAAAAA, PorterDuff.Mode.MULTIPLY);
        //    else if (view == mSigninButton)
        //        mSigninButton.getBackground().setColorFilter(0xFF1A557C, PorterDuff.Mode.MULTIPLY);
        //
        //    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        //        view.getBackground().clearColorFilter();
        //
        //    return false;
        //}
    }


    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRLandingActivity.class.getSimpleName();

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private SharedLayoutHelper mLayoutHelper;
    private JRSessionData mSessionData;
    private FinishReceiver mFinishReceiver;

    private boolean mIsAlertShowing = false;
    private boolean mIsFinishPending = false;

    private ImageView mImageView;
    private EditText mEditText;

    private TextView mWelcomeLabel;

    private ColorButton mSwitchAccountButton;
    private ColorButton mSigninButton;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     *      If the activity is being re-initialized after previously being shut down then this
     *      Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *      Note: Otherwise it is null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionData = JRSessionData.getInstance();

        // For the case when this activity is relaunched after the process was killed
        if (mSessionData == null) {
            Log.e(TAG, "JRLandingActivity bailing out after a process kill/restart");
            finish();
            return;
        }

        setContentView(R.layout.jr_provider_landing);
        mLayoutHelper = new SharedLayoutHelper(this);

        mImageView = (ImageView)findViewById(R.id.jr_landing_logo);
        mEditText = (EditText)findViewById(R.id.jr_landing_edit);

        mWelcomeLabel = (TextView)findViewById(R.id.jr_landing_welcome_label);

        ButtonEventListener bel = new ButtonEventListener();

        mSwitchAccountButton = (ColorButton)findViewById(R.id.jr_landing_switch_account_button);
        mSwitchAccountButton.setOnClickListener(bel);
        mSigninButton = (ColorButton)findViewById(R.id.jr_landing_small_signin_button);
        mSigninButton.setOnClickListener(bel);

        mSwitchAccountButton.setColor(0xffaaaaaa);
        mSigninButton.setColor(0xff1a557c);
        prepareUserInterface();

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        JREngage.setContext(this);
    }


    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        mSessionData.triggerAuthenticationDidRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFinishReceiver != null) unregisterReceiver(mFinishReceiver);
    }

    private void handlePrimaryButtonClick() {
        if (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput())
        {
            //TODO validate OpenID URLs so they don't hang the WebView
            String text = mEditText.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                //todo turn this string into a resource
                showAlertDialog("Invalid Input",
                        "The input you have entered is not valid. Please try again.");
            } else {
//                showHideKeyboard(false);
                mSessionData.getCurrentlyAuthenticatingProvider().setUserInput(text);
                JRUserInterfaceMaestro.getInstance().showWebView();
            }
        } else {
//            showHideKeyboard(false);
            JRUserInterfaceMaestro.getInstance().showWebView();
        }
    }

    private void handleSecondaryButtonClick() {
        Log.i(TAG, "[handleSecondaryButtonClick]");
        //todo this should also call the forget function or no ... ?
        mSessionData.getCurrentlyAuthenticatingProvider().setForceReauth(true);
        mSessionData.setReturningBasicProvider("");
        finish();
    }

    private void showAlertDialog(String title, String message) {
        mIsAlertShowing = true;
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsAlertShowing = false;
                    if (mIsFinishPending) {
                        mIsFinishPending = false;
                        finish();
                    }
                }
            })
            .show();
    }

    private void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            finish();
        }
    }

    private void prepareUserInterface() {
        JRProvider currentlyAuthenticatingProvider =
                mSessionData.getCurrentlyAuthenticatingProvider();
        if (currentlyAuthenticatingProvider.getName().equals("openid")) {
            mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
        }

        mLayoutHelper.setHeaderText(getCustomTitle());

        mImageView.setImageDrawable(currentlyAuthenticatingProvider.getProviderLogo(this));

        if (currentlyAuthenticatingProvider.getName().equals(
                mSessionData.getReturningBasicProvider())) {
            configureButtonVisibility(false);

            if (currentlyAuthenticatingProvider.requiresInput()) {
                if (Config.LOGD) {
                    Log.d(TAG, "[prepareUserInterface] current provider requires input");
                }

                mEditText.setVisibility(View.VISIBLE);
                mWelcomeLabel.setVisibility(View.GONE);

                String userInput = currentlyAuthenticatingProvider.getUserInput();
                if (!TextUtils.isEmpty(userInput)) {
                    mEditText.setText(userInput);
                    //configureButtonVisibility(false);
                } else { // Will probably never happen
                    mEditText.setText("");
                    configureButtonVisibility(true);
                }

                mEditText.setHint(currentlyAuthenticatingProvider.getPlaceholderText());
            } else {
                if (Config.LOGD) {
                    Log.d(TAG, "[prepareUserInterface] current provider doesn't require input");
                }

                mEditText.setVisibility(View.GONE);
                mWelcomeLabel.setVisibility(View.VISIBLE);

                mWelcomeLabel.setText(currentlyAuthenticatingProvider.getWelcomeString());
            }
        } else {
             configureButtonVisibility(true);

            if (currentlyAuthenticatingProvider.requiresInput()) {
                if (Config.LOGD) {
                    Log.d(TAG, "[prepareUserInterface] current provider requires input");
                }

                mEditText.setVisibility(View.VISIBLE);
                mWelcomeLabel.setVisibility(View.GONE);

                String userInput = currentlyAuthenticatingProvider.getUserInput();
                if (!TextUtils.isEmpty(userInput)) {
                    mEditText.setText(userInput);
                } else {
                    mEditText.setText("");
                }

                mEditText.setHint(currentlyAuthenticatingProvider.getPlaceholderText());

            } else { // Will never happen

            }
        }
    }

    private void configureButtonVisibility(boolean isSingleButtonLayout) {
        if (isSingleButtonLayout) {
            // TODO: If we go with Gabe/Alexis's suggestions, big button will always be gone and
            // small button will always be visible... Clean this code up...
            mSwitchAccountButton.setVisibility(View.INVISIBLE);//(View.GONE);
            mSigninButton.setVisibility(View.VISIBLE);//(View.GONE);
            //mBigSigninButton.setVisibility(View.VISIBLE);
        } else {
            //mBigSigninButton.setVisibility(View.GONE);
            mSwitchAccountButton.setVisibility(View.VISIBLE);
            mSigninButton.setVisibility(View.VISIBLE);
        }
    }

    private String getCustomTitle() {
        JRProvider provider = mSessionData.getCurrentlyAuthenticatingProvider();
        return provider.requiresInput()
                ? provider.getShortText()
                : getString(R.string.jr_landing_default_custom_title);
    }

//    private void showHideKeyboard(boolean show) {
//        if (show) {
//            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                .showSoftInput(mEditText, 0);
//        } else {
//            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
//                .hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//        }
//    }

//    private void focusEditAndPopKeyboard() {
//        if ((TextUtils.isEmpty(mEditText.getText()) && (mEditText.requestFocus()))) {
//            if (SHOW_KEYBOARD_ON_LAUNCH) {
//                InputMethodManager imm =
//                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//
//                imm.toggleSoftInput(
//                    InputMethodManager.SHOW_FORCED,
//                    InputMethodManager.HIDE_IMPLICIT_ONLY);
//            }
//        } else {
//            Log.d(TAG, "[focusEditAndPopKeyboard] FAIL");
//        }
//    }
}