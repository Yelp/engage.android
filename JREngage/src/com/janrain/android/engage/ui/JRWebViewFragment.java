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
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.ProgressBar;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.R;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.session.JRProvider;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.AndroidUtils;

import java.net.URL;
import java.util.List;

/**
 * @internal
 *
 * @class JRWebViewActivity
 * Container for authentication web view.
 */
public class JRWebViewFragment extends JRUiFragment {
    public static final int RESULT_RESTART = 1;
    public static final int RESULT_FAIL = 2;
    public static final int RESULT_BAD_OPENID_URL = 3;
    public static final String SOCIAL_SHARING_MODE = "com.janrain.android.engage.SOCIAL_SHARING_MODE";

    {
        TAG = JRWebViewFragment.class.getSimpleName();
    }

    private WebView mWebView;
    private boolean mIsSocialSharingSignIn = false;
    private boolean mIsAlertShowing = false;
    private boolean mIsFinishPending = false;
    private boolean mIsLoadingMobileEndpoint = false;
    private JRProvider mProvider;
    private WebSettings mWebViewSettings;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jr_provider_webview, container, false);

        mWebView = (WebView)view.findViewById(R.id.jr_webview);
        mProgressBar = (ProgressBar)view.findViewById(R.id.jr_webview_progress);

        mWebViewSettings = mWebView.getSettings();

        // Shim some information about the OS version into the WebView for use by hax ala Yahoo!:
        mWebView.addJavascriptInterface(new Object() {
        			// These functions may be invoked via the javascript binding, but they are
        			// never invoked from this Java code, so they will always generate compiler
        			// warnings, so we suppress those warnings safely.
                    @SuppressWarnings("unused")
					String getAndroidIncremental() {
                        return Build.VERSION.INCREMENTAL;
                    }

                    @SuppressWarnings("unused")
					String getAndroidRelease() {
                        return Build.VERSION.RELEASE;
                    }

                    @SuppressWarnings("unused")
					int getAndroidSdkInt() {
                        return AndroidUtils.getAndroidSdkInt();
                    }
                }, "jrengage_mobile");

        mWebViewSettings.setBuiltInZoomControls(true);
        mWebViewSettings.setLoadsImagesAutomatically(true);
        mWebViewSettings.setJavaScriptEnabled(true);
        mWebViewSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        mWebViewSettings.setSupportZoom(true);

        mWebView.setWebViewClient(mWebviewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setDownloadListener(mWebViewDownloadListener);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mIsSocialSharingSignIn = getActivity().getIntent().getExtras().getBoolean(SOCIAL_SHARING_MODE);
        mProvider = mSessionData.getCurrentlyAuthenticatingProvider();

        String customUa = mProvider.getWebViewOptions().getAsString("user_agent");
        if (customUa != null) mWebViewSettings.setUserAgentString(customUa);

        URL startUrl = mSessionData.startUrlForCurrentlyAuthenticatingProvider();
        mWebView.loadUrl(startUrl.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        dismissProgressDialog();

        // onDestroy may be called even if onCreateView never is, guard against NPEs
        if (mWebView != null) {
            mWebView.stopLoading();

            // This listener's callback assumes the activity is running, but if the user presses
            // the back button while the WebView is transitioning between pages the activity may
            // not be shown when this listener is fired, which would cause a crash, so we unset
            // the listener here.
            mWebView.setWebViewClient(null);
            mWebView.setDownloadListener(null);
        }
    }

    private void showAlertDialog(String title, String message) {
        mIsAlertShowing = true;
        new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.jr_dialog_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mIsAlertShowing = false;
                    if (mIsFinishPending) {
                        mIsFinishPending = false;
                        getActivity().finish();
                    }
                }
            }).show();
    }

    @Override
    protected void tryToFinishActivity() {
        if (Config.LOGD) Log.d(TAG, "[tryToFinishActivity]");
        if (mIsAlertShowing) {
            mIsFinishPending = true;
        } else {
            getActivity().finish();
        }
    }

    private boolean isMobileEndpointUrl(String url) {
        final String thatUrl = mSessionData.getBaseUrl() + "/signin/device";
        return ((!TextUtils.isEmpty(url)) && (url.startsWith(thatUrl)));
    }

    private void showProgressSpinner() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressSpinner() {
        if (!mIsLoadingMobileEndpoint) mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.jr_menu_item_refresh));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals(getString(R.string.jr_menu_item_refresh))) {
            if (Config.LOGD) Log.d(TAG, "refreshing WebView");
            mWebView.reload();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void loadMobileEndpointUrl(String url) {
        //if (!AndroidUtils.isSmallOrNormalScreen()) {
            mIsLoadingMobileEndpoint = true;
            showProgressSpinner();
        //} else { // full screen mode
        //    mLayoutHelper.showProgressDialog();
        //}

        String urlToLoad = url + "&auth_info=true";
        if (Config.LOGD) Log.d(TAG, "[loadMobileEndpointUrl] loading url: " + urlToLoad);


        JRConnectionManager.createConnection(urlToLoad, mMobileEndPointConnectionDelegate, false, null);
    }

    DownloadListener mWebViewDownloadListener = new DownloadListener() {
        /**
         * Invoked by WebKit when there is something to be downloaded that it does not
         * typically handle (e.g. result of post, mobile endpoint url results, etc).
         */
        public void onDownloadStart(String url,
                                    String userAgent,
                                    String contentDisposition,
                                    String mimetype,
                                    long contentLength) {

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
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Seems to be broken according to this:
            // http://code.google.com/p/android/issues/detail?id=2887
            // This method is getting called only once in the Engage flow, when there are at least two
            // redirects involved.
            // Another bug documents that this method isn't called on a form submission via POST
            // http://code.google.com/p/android/issues/detail?id=9122
            if (Config.LOGD) Log.d(TAG, "[shouldOverrideUrlLoading]: " + view + ", " + url);

            if (isMobileEndpointUrl(url)) {
                loadMobileEndpointUrl(url);
                return true;
            } else {
                view.loadUrl(url);
            }

            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (Config.LOGD) Log.d(TAG, "[onPageStarted] url: " + url);

            /* Check for mobile endpoint URL. */
            if (isMobileEndpointUrl(url)) {
                Log.d(TAG, "[onPageStarted] looks like JR mobile endpoint url");
                loadMobileEndpointUrl(url);
                mWebView.stopLoading();
            }

            showProgressSpinner();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (Config.LOGD) Log.d(TAG, "[onPageFinished] url: " + url);

            hideProgressSpinner();

            List<String> jsInjects =
                    mProvider.getWebViewOptions().getAsListOfStrings("js_injections", true);
            for (String i : jsInjects) mWebView.loadUrl("javascript:" + i);

            boolean showZoomControl = mProvider.getWebViewOptions().getAsBoolean("show_zoom_control");
            if (showZoomControl) mWebView.invokeZoomPicker();

            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            super.onReceivedError(view, errorCode, description, url);
            Log.e(TAG, "[onReceivedError] code: " + errorCode + " | description: " + description
                + " | url: " + url);

            hideProgressSpinner();

            mIsFinishPending = true;
            getActivity().setResult(RESULT_FAIL);
            showAlertDialog("Sign-in failed", "An error occurred while attempting to sign in.");

            JREngageError err = new JREngageError(
                    "Authentication failed: " + description,
                    JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                    JREngageError.ErrorType.AUTHENTICATION_FAILED);
            mSessionData.triggerAuthenticationDidFail(err);
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress > 50) {
                hideProgressSpinner();
            }
        }
    };

    private JRConnectionManagerDelegate mMobileEndPointConnectionDelegate =
            new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
        @Override
        public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
            Log.d(TAG, "[connectionDidFinishLoading] userdata: "
                    + userdata + " | payload: " + payload);

            dismissProgressDialog();

            JRDictionary payloadDictionary = JRDictionary.fromJSON(payload);
            JRDictionary resultDictionary = payloadDictionary.getAsDictionary("rpx_result");
            final String result = resultDictionary.getAsString("stat");
            if ("ok".equals(result)) {
                // Back should be disabled at this point because the progress modal dialog is
                // being displayed.
                mSessionData.triggerAuthenticationDidCompleteWithPayload(resultDictionary);
                if (!mIsSocialSharingSignIn) mSessionData.saveLastUsedBasicProvider();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            } else {
                final String error = resultDictionary.getAsString("error");
                String alertTitle, alertMessage, logMessage;

                if ("Discovery failed for the OpenID you entered".equals(error) ||
                        "Your OpenID must be a URL".equals(error)) {
                    alertTitle = "Invalid Input";
                    if (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput()) {
                        String shortText = mSessionData.getCurrentlyAuthenticatingProvider()
                                .getShortText();
                        alertMessage = "The " + shortText +
                                " you entered was not valid. Please try again.";
                    } else {
                        alertMessage = "There was a problem authenticating. Please try again.";
                    }

                    logMessage = "Discovery failed for the OpenID you entered: ";

                    Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                    mIsFinishPending = true;
                    getActivity().setResult(RESULT_BAD_OPENID_URL);
                    showAlertDialog(alertTitle, alertMessage);
                } else if ("The URL you entered does not appear to be an OpenID".equals(error)) {
                    alertTitle = "Invalid Input";
                    alertMessage = (mSessionData.getCurrentlyAuthenticatingProvider().requiresInput())
                            ? String.format("The %s you entered was not valid. Please try again.",
                            mSessionData.getCurrentlyAuthenticatingProvider().getShortText())
                            : "There was a problem authenticating. Please try again.";
                    logMessage = "The URL you entered does not appear to be an OpenID: ";

                    Log.w(TAG, "[connectionDidFinishLoading] " + logMessage + alertMessage);

                    mIsFinishPending = true;
                    getActivity().setResult(RESULT_BAD_OPENID_URL);
                    showAlertDialog(alertTitle, alertMessage);
                } else if ("Please enter your OpenID".equals(error)) {
                    // Caused by entering a ~blank OpenID URL

                    mIsFinishPending = true;
                    getActivity().setResult(RESULT_BAD_OPENID_URL);
                    showAlertDialog("OpenID Error", "The URL you entered does not appear to be an OpenID");
                } else if ("canceled".equals(error)) {
                    mSessionData.getCurrentlyAuthenticatingProvider().setForceReauth(true);
                    mSessionData.triggerAuthenticationDidRestart();
                    getActivity().setResult(RESULT_RESTART);
                    getActivity().finish();
                } else {
                    Log.e(TAG, "unrecognized error");
                    JREngageError err = new JREngageError(
                            "Authentication failed: " + payload,
                            JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                            JREngageError.ErrorType.AUTHENTICATION_FAILED);

                    mSessionData.triggerAuthenticationDidFail(err);
                    getActivity().setResult(RESULT_FAIL);
                    mIsFinishPending = true;
                    showAlertDialog(getString(R.string.jr_dialog_sign_in_failed),
                            "An error occurred while attempting to sign in.");
                }
            }
            //} else if (userdata.equals("request")) {
            //    mWebView.loadDataWithBaseURL(requestUrl, payload, null, "utf-8", null);
            //}
        }

        @Override
        public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
            Log.i(TAG, "[connectionDidFail] userdata: " + userdata, ex);

            if ((userdata != null) && (userdata instanceof String)) {
                final JREngageError error = new JREngageError(
                        "Authentication failed",
                        JREngageError.AuthenticationError.AUTHENTICATION_FAILED,
                        JREngageError.ErrorType.AUTHENTICATION_FAILED,
                        ex);

                // Back button? race condition
                mSessionData.triggerAuthenticationDidFail(error);
                mIsFinishPending = true;
                getActivity().setResult(RESULT_FAIL);
                showAlertDialog(getString(R.string.jr_dialog_sign_in_failed),
                        getString(R.string.jr_dialog_network_error));

                //} else if (userdata.equals("request")) {
                //    // Back button?
                //    mSessionData.triggerAuthenticationDidFail(error);
                //}
            }
        }
    };
}