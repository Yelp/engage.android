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

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;

/**
 * @internal
 *
 * @class JRLandingActivity
 * Landing Page Activity
 **/
public class JRLandingFragment extends Fragment {

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

        private final String TAG = JRLandingFragment.TAG + "-" +
                FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_FRAGMENT_TARGET);
            //if (JRLandingActivity.class.toString().equals(target)) {
            if (JRLandingFragment.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        public void onClick(View view) {
            Log.i(TAG, "[onClick] handled");

            if (view.equals(mSigninButton)) {// || view.equals(mBigSigninButton)) {
                handleSigninClick();
            } else if (view.equals(mSwitchAccountButton)) {
                handleSwitchAccountsClick();
            }
        }
    };

    private static final String TAG = JRLandingFragment.class.getSimpleName();

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSessionData = JRSessionData.getInstance();

        //todo fragments
        //mLayoutHelper = new SharedLayoutHelper(this);

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            getActivity().registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jr_provider_landing, container, false);

        mLayoutHelper = new SharedLayoutHelper(getActivity());

        mImageView = (ImageView)view.findViewById(R.id.jr_landing_logo);
        mEditText = (EditText)view.findViewById(R.id.jr_landing_edit);
        mWelcomeLabel = (TextView)view.findViewById(R.id.jr_landing_welcome_label);
        mSwitchAccountButton = (ColorButton)view.findViewById(R.id.jr_landing_switch_account_button);
        mSigninButton = (ColorButton)view.findViewById(R.id.jr_landing_small_signin_button);

        prepareUserInterface();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);
    }

    private void handleSigninClick() {
        if (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput()) {
            //TODO validate OpenID URLs so they don't hang the WebView
            String text = mEditText.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                //todo turn this string into a resource
                showAlertDialog("Invalid Input",
                        "The input you have entered is not valid. Please try again.");
            } else {
                mSessionData.getCurrentlyAuthenticatingProvider().setUserInput(text);
                JRUserInterfaceMaestro.getInstance().showWebView();
            }
        } else {
            JRUserInterfaceMaestro.getInstance().showWebView();
        }
    }

    private void handleSwitchAccountsClick() {
        Log.i(TAG, "[handleSwitchAccountsClick]");

        mSessionData.getCurrentlyAuthenticatingProvider().setForceReauth(true);
        mSessionData.setReturningBasicProvider("");
        mSessionData.triggerAuthenticationDidRestart();
        //finish();
    }

    private void showAlertDialog(String title, String message) {
        mIsAlertShowing = true;
        new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsAlertShowing = false;
                    if (mIsFinishPending) {
                        mIsFinishPending = false;
                        tryToFinishActivity();
                    }
                }
            }).show();
    }

    private void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            getActivity().finish();
        }
    }

    private void prepareUserInterface() {
        mSwitchAccountButton.setOnClickListener(mButtonListener);
        mSigninButton.setOnClickListener(mButtonListener);

        mSwitchAccountButton.setColor(0xffaaaaaa);
        mSigninButton.setColor(0xff1a557c);

        JRProvider currentlyAuthenticatingProvider =
                mSessionData.getCurrentlyAuthenticatingProvider();
        if (currentlyAuthenticatingProvider.getName().equals("openid")) {
            mEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_URI);
        }

        mLayoutHelper.setHeaderText(getCustomTitle());

        mImageView.setImageDrawable(currentlyAuthenticatingProvider.getProviderLogo(getActivity()));

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

            } else {
                // Will never happen
            }
        }
    }

    private void configureButtonVisibility(boolean isSingleButtonLayout) {
        if (isSingleButtonLayout) {
            mSwitchAccountButton.setVisibility(View.INVISIBLE);
            mSigninButton.setVisibility(View.VISIBLE);
        } else {
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
}