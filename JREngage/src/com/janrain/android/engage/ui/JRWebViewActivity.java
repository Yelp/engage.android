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
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.*;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.session.JRSessionData;
import com.janrain.android.engage.types.JRDictionary;

import java.net.URL;
import java.util.List;

/**
 * @internal
 *
 * @class JRWebViewActivity
 * Container for authentication web view.  Mimics JRWebViewController iPhone interface.
 */
public class JRWebViewActivity extends Activity {
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = JRWebViewActivity.class.getSimpleName();
    private static final String RPX_RESULT_TAG = "rpx_result";
    private JRProvider mProvider;
    private WebSettings mWebViewSettings;

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

        private final String TAG = JRWebViewActivity.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(
                    JRUserInterfaceMaestro.EXTRA_FINISH_ACTIVITY_TARGET);
            if (JRWebViewActivity.class.toString().equals(target)) {
                tryToFinishActivity();
                Log.i(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                Log.i(TAG, "[onReceive] ignored");
            }
        }
    }

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
    private WebView mWebView;
    private boolean mIsAlertShowing = false;
    private boolean mIsFinishPending = false;
    private FinishReceiver mFinishReceiver;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Request progress indicator
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.jr_provider_webview);

        CookieSyncManager.createInstance(this);

        mSessionData = JRSessionData.getInstance();
        mLayoutHelper = new SharedLayoutHelper(this);

        // For the case when this activity is relaunched after the process was killed
        if (mSessionData == null) {
            finish();
            return;
        }

        mWebView = (WebView)findViewById(R.id.jr_webview);
        mWebViewSettings = mWebView.getSettings();
        // Shim some information about the OS version into the WebView for use by hax ala Yahoo!
        mWebView.addJavascriptInterface(new Object() {
        			// These functions may be invoked via the javascript binding, but they are
        			// never invoked from this Java code, so they will always generate compiler
        			// warnings, so we suppress those warnings safely.
                    @SuppressWarnings("unused")
					String androidIncremental() {
                        return Build.VERSION.INCREMENTAL;
                    }

                    @SuppressWarnings("unused")
					String androidRelease() {
                        return Build.VERSION.RELEASE;
                    }

                    @SuppressWarnings("unused")
					int androidSdkInt() {
                        return Build.VERSION.SDK_INT;
                    }
                }, "jrengage_mobile");

        // Dormant progress bar code.
        //final Activity activity = this;
        //mWebView.setWebChromeClient(new WebChromeClient() {
        //   public void onProgressChanged(WebView view, int progress) {
        //     // Activities and WebViews measure progress with different scales.
        //     // The progress meter will automatically disappear when we reach 100%
        //     activity.setProgress(progress * 100);
        //   }
        //});

        mWebViewSettings.setBuiltInZoomControls(true);
        mWebViewSettings.setLoadsImagesAutomatically(true);
        mWebViewSettings.setJavaScriptEnabled(true);
        mWebViewSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        mWebViewSettings.setSupportZoom(true);

        mProvider = mSessionData.getCurrentlyAuthenticatingProvider();
        mLayoutHelper.setHeaderText(mProvider.getFriendlyName());

        if (mFinishReceiver == null) {
            mFinishReceiver = new FinishReceiver();
            registerReceiver(mFinishReceiver, JRUserInterfaceMaestro.FINISH_INTENT_FILTER);
        }

        mWebView.setWebViewClient(mWebviewClient);
        mWebView.setDownloadListener(mWebViewDownloadListener);

        String customUa = mProvider.getWebViewOptions().getAsString("user_agent");
        if (customUa != null) mWebViewSettings.setUserAgentString(customUa);

        URL startUrl = mSessionData.startUrlForCurrentlyAuthenticatingProvider();
        mWebView.loadUrl(startUrl.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        JREngage.setContext(this);
    }

    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLayoutHelper.dismissProgressDialog();

        if (mFinishReceiver != null) unregisterReceiver(mFinishReceiver);


        // This listener's callback assumes the activity is running, but if the user presses
        // the back button while the WebView is transitioning between pages the activity may
        // not be shown when this listener is fired, which would cause a crash, so we unset
        // the listener here.
        if (mWebView != null) {
            mWebView.setWebViewClient(null);
            mWebView.setDownloadListener(null);
        }
    }

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        mWebView.stopLoading();
        mSessionData.triggerAuthenticationDidRestart();
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // use the shared menu
        mLayoutHelper.inflateAboutMenu(menu);
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Refresh");
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("Refresh")) {
            Log.d(TAG, "refreshing WebView");
            mWebView.reload();
            return true;
        }

        return mLayoutHelper.handleAboutMenu(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Callback for creating dialogs that are managed.
     */
    protected Dialog onCreateDialog(int id) {
        return mLayoutHelper.onCreateDialog(id);
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

    public void tryToFinishActivity() {
        Log.i(TAG, "[tryToFinishActivity]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            finish();
        }
    }

    private boolean isMobileEndpointUrl(String url) {
        final String thatUrl = mSessionData.getBaseUrl() + "/signin/device";
        return ((!TextUtils.isEmpty(url)) && (url.startsWith(thatUrl)));
    }

    private void loadMobileEndpointUrl(String url) {
        mLayoutHelper.showProgressDialog();

        String urlToLoad = url + "&auth_info=true";
        if (Config.LOGD) {
            Log.d(TAG, "[loadMobileEndpointUrl] loading url: " + urlToLoad);
        }

        JRConnectionManager.createConnection(
                urlToLoad, mMobileEndPointConnectionDelegate, false, RPX_RESULT_TAG);
    }

    DownloadListener mWebViewDownloadListener = new DownloadListener() {
        /**
         * Invoked by WebKit when there is something to be downloaded that it does not
         * typically handle (e.g. result of post, mobile endpoint url results, etc).
         */
        public void onDownloadStart(String url, String userAgent,
                String contentDisposition, String mimetype, long contentLength) {

            if (Config.LOGD) {
                Log.d(TAG, "[onDownloadStart] url: " + url + " | mimetype: " + mimetype
                    + " | length: " + contentLength);
            }

            if (isMobileEndpointUrl(url)) loadMobileEndpointUrl(url);
        }
    };

    /**
     * Handler for webkit events, etc.
     */
    private WebViewClient mWebviewClient = new WebViewClient(){
        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (Config.LOGD) {
                Log.d(TAG, "[onPageStarted] url: " + url);
            }

            /*
             * Check for mobile endpoint URL.
             */
            final String mobileEndpointUrl = mSessionData.getBaseUrl() + "/signin/device";
            if ((!TextUtils.isEmpty(url)) && (url.startsWith(mobileEndpointUrl))) {
                Log.d(TAG, "[onPageStarted] looks like JR mobile endpoint url");
            }

            setProgressBarIndeterminateVisibility(true);

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (Config.LOGD) {
                Log.d(TAG, "[onPageFinished] url: " + url);
            }

            setProgressBarIndeterminateVisibility(false);

            List<String> jsInjects =
                    mProvider.getWebViewOptions().getAsListOfStrings("js_injections", true);
            for (String i : jsInjects) mWebView.loadUrl("javascript:" + i);

            boolean showZoomControl =
                    mProvider.getWebViewOptions().getAsBoolean("show_zoom_control");
            if (showZoomControl) mWebView.invokeZoomPicker();

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            super.onReceivedError(view, errorCode, description, url);
            Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                + " | url: " + url);

            setProgressBarIndeterminateVisibility(false);

            //mIsFinishPending = true;
            showAlertDialog("Log In Failed", "An error occurred while attempting to sign in.");

            JREngageError err = new JREngageError(
                    "Authentication failed: " + description,
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED);
            mSessionData.triggerAuthenticationDidFail(err);
        }
    };

    private JRConnectionManagerDelegate mMobileEndPointConnectionDelegate =
            new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
        public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
            Log.d(TAG, "[connectionDidFinishLoading] userdata: "
                    + userdata + " | payload: " + payload);

            mLayoutHelper.dismissProgressDialog();

            if (userdata.equals(RPX_RESULT_TAG)) {
                JRDictionary payloadDictionary = JRDictionary.fromJSON(payload);
                JRDictionary resultDictionary = payloadDictionary.getAsDictionary("rpx_result");
                final String result = resultDictionary.getAsString("stat");
                if ("ok".equals(result)) {
                    // Back should be disabled at this point because the progress modal dialog is
                    // being displayed.
                    mSessionData.triggerAuthenticationDidCompleteWithPayload(resultDictionary);
                } else {
                    final String error = resultDictionary.getAsString("error");
                    String alertTitle, alertMessage, logMessage;

                    // for OpenID errors we're just firing a dialog and then calling
                    // finish when it's dismissed in order to get back to the landing page
                    // instead of calling triggerAuthenticationDidRestart because that would
                    // return us to the provider selection activity, when the user probably just
                    // wants to correct a minor OpenID typo.
                    // TODO there are inconsistencies in the managed activity stack in the UI
                    // maestro caused by this, but they're benign.  Verify this harmlessness.
                    if ("Discovery failed for the OpenID you entered".equals(error) ||
                            "Your OpenID must be a URL".equals(error)) {
                        alertTitle = "Invalid Input";
                        if (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput()) {
                            String shortText = mSessionData.getCurrentlyAuthenticatingProvider()
                                    .getShortText();
                            alertMessage = "The " + shortText +
                                    " you entered was not valid. Please try again.";
                        } else {
                            alertMessage = "There was a problem authenticating with this provider. Please try again.";
                        }

                        logMessage = "Discovery failed for the OpenID you entered: ";

                        Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                        mIsFinishPending = true;
                        showAlertDialog(alertTitle, alertMessage);
                    } else if ("The URL you entered does not appear to be an OpenID".equals(error)) {
                        alertTitle = "Invalid Input";
                        alertMessage = (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput())
                                ? String.format("The %s you entered was not valid. Please try again.",
                                mSessionData.getCurrentlyAuthenticatingProvider().getShortText())
                                : "There was a problem authenticating with this provider. Please try again.";
                        logMessage = "The URL you entered does not appear to be an OpenID: ";

                        Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                        mIsFinishPending = true;
                        showAlertDialog(alertTitle, alertMessage);
                    } else if ("Please enter your OpenID".equals(error)) {
                        // Caused by entering a ~blank OpenID URL

                        mIsFinishPending = true;
                        showAlertDialog("OpenID Error",
                                "The URL you entered does not appear to be an OpenID");
                    } else {
                        Log.e(TAG, "unrecognized error");
                        JREngageError err = new JREngageError(
                                "Authentication failed: " + payload,
                                JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                                JREngageError.ErrorType.AUTHENTICATION_FAILED);

                        showAlertDialog(
                                "Log In Failed",
                                "An error occurred while attempting to sign in."
                            );

                        mSessionData.triggerAuthenticationDidFail(err);
                    }
                }
            } else if (userdata.equals("request")) {
                mWebView.loadDataWithBaseURL(requestUrl, payload, null, "utf-8", null);
            }
        }

        public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
            Log.i(TAG, "[connectionDidFail] userdata: " + userdata, ex);

            if ((userdata != null) && (userdata instanceof String)) {
                final JREngageError error = new JREngageError(
                        "Authentication failed",
                        JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                        JREngageError.ErrorType.AUTHENTICATION_FAILED,
                        ex);

                final String tag = (String)userdata;
                if (tag.equals(RPX_RESULT_TAG)) {
                    // Back button?
                    mSessionData.triggerAuthenticationDidFail(error);
                } else if (tag.equals("request")) {
                    // Back button?
                    mSessionData.triggerAuthenticationDidFail(error);
                }
            }
        }

        public void connectionWasStopped(Object userdata) {
            Log.i(TAG, "[connectionWasStopped] userdata: " + userdata);
        }
    };
}