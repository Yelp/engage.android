/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.simpledemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.janrain.android.engage.*;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.*;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DIALOG_JR_ENGAGE_ERROR = 1;

    private String readAsset(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer); // buffer is exactly the right size, a guarantee of asset files
            return new String(buffer);
        } catch (IOException e) {
            return null;
        }
    }

    private JREngage mEngage;
    private JRActivityObject mActivity;

    private Button mBtnTestAuth;
    private Button mBtnTestPub;

    // Activity object variables
    private String mTitleText = "title text";
    private String mActionLink = "http://www.janrain.com/feed/blogs";
    private String mDescriptionText = "description text";
    private String mImageUrl = "http://www.janrain.com/sites/default/themes/janrain/logo.png";
    private String mDescriptionHtml = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mEngage.showAuthenticationDialog();
            }
        });

        mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
        mBtnTestPub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mEngage.showSocialPublishingDialog(mActivity);
            }
        });
        mBtnTestPub.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (findViewById(R.id.jr_publish_fragment) != null) {
                    mEngage.showSocialPublishingFragment(
                            mActivity,
                            MainActivity.this,
                            com.janrain.android.engage.R.id.jr_publish_fragment,
                            false,
                            null,
                            null,
                            null,
                            null);
                }

                return true;
            }
        });

        String engageAppId = null;
        String engageTokenUrl = null;
        try {
            engageAppId = readAsset("app_id.txt").trim();
            engageTokenUrl = readAsset("token_url.txt").trim();
        } catch (NullPointerException e) {
            if (engageAppId == null) {
                new AlertDialog.Builder(this).setTitle(
                        "You need to create assets/app_id.txt, then recompile and reinstall.")
                        .create().show();
                return;
            }
        }

        mEngage = JREngage.initInstance(this, engageAppId, engageTokenUrl, mJREngageDelegate);
        //mEngage.setEnabledAuthenticationProviders(new String[] { "google" });
        //mEngage.setEnabledSharingProviders(new String[] { "twitter" });

        //mActivity = new JRActivityObject("shared an article from the Janrain Blog!",
        //    "");
        mActivity = new JRActivityObject("shared an article from the Janrain Blog!",
            mActionLink);

        mActivity.setTitle(mTitleText);
        mActivity.setDescription(mDescriptionText);
        mActivity.addMedia(new JRImageMediaObject(mImageUrl, "http://developer.android.com"));

        String smsBody = "Check out this article!\n" + mActionLink;
        String emailBody = mActionLink + "\n" + mDescriptionText;

        mActivity.addActionLink(new JRActionLink("test action", "http://android.com"));

        JRSmsObject sms = new JRSmsObject(smsBody);
        JREmailObject email = new JREmailObject("Check out this article!", emailBody);
        sms.addUrl(mActionLink);
        email.addUrl(mActionLink);
        mActivity.setEmail(email);
        mActivity.setSms(sms);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    private JREngageDelegate mJREngageDelegate  = new JREngageDelegate() {
        public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
            String message = "Simpledemo:\nJREngage dialog failed to show.\nError: " +
                    ((error == null) ? "unknown" : error.getMessage());

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
            JRDictionary profile = (authInfo == null) ? null : authInfo.getAsDictionary("profile");
            String displayName = (profile == null) ? null : profile.getAsString("displayName");
            String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                    ? "" : (" for user: " + displayName));

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                     HttpResponseHeaders response,
                                                     String tokenUrlPayload,
                                                     String provider) {
            Toast.makeText(MainActivity.this, "Authentication did reach token URL: " + tokenUrlPayload,
                    Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidNotComplete() {
            Toast.makeText(MainActivity.this, "Authentication did not complete", Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
            String message = "Authentication failed, error: " +
                    ((error == null) ? "unknown" : error.getMessage());

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                          JREngageError error,
                                                          String provider) {
            Toast.makeText(MainActivity.this, "Failed to reach token URL", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidNotCompletePublishing() {
            Toast.makeText(MainActivity.this, "Sharing did not complete", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidCompletePublishing() {
            //Toast.makeText(this, "Sharing dialog did complete", Toast.LENGTH_LONG).show();
        }

        public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
            Toast.makeText(MainActivity.this, "Activity shared", Toast.LENGTH_LONG).show();
        }

        public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                     JREngageError error,
                                                     String provider) {
            Toast.makeText(MainActivity.this, "Activity failed to share", Toast.LENGTH_LONG).show();
        }
    };
}
