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

package com.janrain.android.engage.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSession;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @internal
 */
public abstract class JRUiFragment extends Fragment {
    private static final String KEY_MANAGED_DIALOGS = "jr_managed_dialogs";
    private static final String KEY_DIALOG_ID = "jr_dialog_id";
    private static final String KEY_MANAGED_DIALOG_OPTIONS = "jr_dialog_options";
    private static final String KEY_DIALOG_PROGRESS_TEXT = "jr_progress_dialog_text";

    public static final int REQUEST_LANDING = 1;
    public static final int REQUEST_WEBVIEW = 2;
    public static final int DIALOG_ABOUT = 1000;
    public static final int DIALOG_PROGRESS = 1001;

    private FinishReceiver mFinishReceiver;
    private HashMap<Integer, ManagedDialog> mManagedDialogs = new HashMap<Integer, ManagedDialog>();

    /*package*/ JRSession mSession;
    /*package*/ final String TAG = getLogTag();
    /*package*/ String getLogTag() { return this.getClass().getSimpleName(); }

    /**
     * @internal
     *
     * @class FinishReceiver
     * Used to listen to "Finish" broadcast messages sent by JREngage.cancel*
     **/
    private class FinishReceiver extends BroadcastReceiver {
        private String TAG = JRUiFragment.this.TAG + "-" + FinishReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            String target = intent.getStringExtra(JRFragmentHostActivity.EXTRA_FINISH_FRAGMENT_TARGET);

            if (JRUiFragment.this.getClass().toString().equals(target) ||
                    target.equals(JRFragmentHostActivity.FINISH_TARGET_ALL)) {
                if (!isEmbeddedMode()) tryToFinishActivity();
                JREngage.logd(TAG, "[onReceive] handled");
            } else if (Config.LOGD) {
                JREngage.logd(TAG, "[onReceive] ignored");
            }
        }
    }

    private static class ManagedDialog implements Serializable {
        int mId;
        transient Dialog mDialog;
        transient Bundle mOptions;
        boolean mShowing;
    }

    /* These overrides are declared in the exact order they're called */

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);

        if (JRSession.getInstance() == null) {
            throw new IllegalStateException("You must call JREngage.initInstance before inflating " +
                    "JREngage fragments.");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mFinishReceiver == null) mFinishReceiver = new FinishReceiver();
        getActivity().registerReceiver(mFinishReceiver, JRFragmentHostActivity.FINISH_INTENT_FILTER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSession = JRSession.getInstance();
        if (mSession != null) mSession.setUiIsShowing(true);
        /* Embedded mode isn't compatible with setRetainInstance */
        if (!isEmbeddedMode()) setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public abstract View onCreateView(LayoutInflater inflater,
                                      ViewGroup container,
                                      Bundle savedInstanceState);

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSession = JRSession.getInstance();

        if (savedInstanceState != null) {
            mManagedDialogs = (HashMap) savedInstanceState.get(KEY_MANAGED_DIALOGS);
            Parcelable[] p = savedInstanceState.getParcelableArray(KEY_MANAGED_DIALOG_OPTIONS);
            if (mManagedDialogs != null && p != null) {
                for (Parcelable p_ : p) {
                    Bundle b = (Bundle) p_;
                    mManagedDialogs.get(b.getInt(KEY_DIALOG_ID)).mOptions = b;
                }
            } else {
                mManagedDialogs = new HashMap<Integer, ManagedDialog>();
            }
        }

        for (ManagedDialog d : mManagedDialogs.values()) {
            d.mDialog = onCreateDialog(d.mId, d.mOptions);
            if (d.mShowing) d.mDialog.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasView()) showHideTaglines();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        for (ManagedDialog d : mManagedDialogs.values()) d.mDialog.dismiss();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        JREngage.logd(TAG, "[onDestroy]");
        if (mSession != null) mSession.setUiIsShowing(false);

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mFinishReceiver != null) getActivity().unregisterReceiver(mFinishReceiver);

        super.onDetach();
    }

    /* May be called at any time before onDestroy() */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle[] dialogOptions = new Bundle[mManagedDialogs.size()];
        int x = 0;
        for (ManagedDialog d : mManagedDialogs.values()) {
            d.mShowing = d.mDialog.isShowing();
            dialogOptions[x++] = d.mOptions;
            d.mOptions.putInt(KEY_DIALOG_ID, d.mId);
        }
        outState.putSerializable(KEY_MANAGED_DIALOGS, mManagedDialogs);
        outState.putParcelableArray(KEY_MANAGED_DIALOG_OPTIONS, dialogOptions);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
    //--

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //menu.add("test");

        if (mSession == null || mSession.getHidePoweredBy()) {
            Log.e(TAG, "Bailing out of onCreateOptionsMenu");
            return;
        } else {
            inflater.inflate(R.menu.jr_about_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.jr_menu_about) {
            showDialog(DIALOG_ABOUT);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*package*/ void onFragmentHostActivityCreate(JRFragmentHostActivity jrfh, JRSession session) {}

    /*package*/ void showHideTaglines() {
        if (mSession == null) {
            Log.e(TAG, "Bailing out of showHideTaglines");
            return;
        }

        boolean hideTagline = mSession.getHidePoweredBy();
        int visibility = hideTagline ? View.GONE : View.VISIBLE;

        View tagline = getView().findViewById(R.id.jr_tagline);
        if (tagline != null) tagline.setVisibility(visibility);

        View bonusTagline = getView().findViewById(R.id.jr_email_sms_powered_by_text);
        if (bonusTagline != null) bonusTagline.setVisibility(visibility);
    }

    /*package*/ ProgressDialog getProgressDialog(Bundle options) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(options.getString(KEY_DIALOG_PROGRESS_TEXT));
        return progressDialog;
    }

    /*package*/ void showProgressDialog(String displayText) {
        Bundle opts = new Bundle();
        opts.putString(KEY_DIALOG_PROGRESS_TEXT, displayText);
        showDialog(DIALOG_PROGRESS, opts);
    }

    /*package*/ void showProgressDialog() {
        showProgressDialog(getString(R.string.jr_progress_loading));
    }

    /*package*/ void dismissProgressDialog() {
        dismissDialog(DIALOG_PROGRESS);
    }

    private AlertDialog getAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.jr_about_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog retval = builder.create();
        View l = retval.getWindow().getLayoutInflater().inflate(R.layout.jr_about_dialog, null);
        retval.setView(l);

        return retval;
    }

    /*package*/ final boolean isEmbeddedMode() {
        FragmentActivity a = getActivity();
        return a != null && !(a instanceof JRFragmentHostActivity);
    }

    /*package*/ Dialog onCreateDialog(int id, Bundle options) {
        Dialog dialog;
        switch (id) {
            case DIALOG_ABOUT:
                dialog = getAboutDialog();
                break;
            case DIALOG_PROGRESS:
                dialog = getProgressDialog(options);
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    /*package*/ void onPrepareDialog(int id, Dialog d, Bundle options) {}

    /*package*/ void showDialog(int dialogId) {
        showDialog(dialogId, new Bundle());
    }

    /*package*/ void showDialog(int dialogId, Bundle options) {
        ManagedDialog d = mManagedDialogs.get(dialogId);
        if (d == null) {
            d = new ManagedDialog();
            d.mDialog = onCreateDialog(dialogId, options);
            d.mId = dialogId;
            mManagedDialogs.put(dialogId, d);
        }

        d.mOptions = options;
        onPrepareDialog(dialogId, d.mDialog, options);
        d.mDialog.show();
        //d.mShowing = true; // See also dismissDialog comment
    }

    /*package*/ void dismissDialog(int dialogId) {
        ManagedDialog d = mManagedDialogs.get(dialogId);
        if (d != null) d.mDialog.dismiss();
    }

    private void startActivityForFragId(int fragId, int requestCode) {
        startActivityForFragId(fragId, requestCode, null);
    }

    /*package*/ int getColor(int colorId) {
        return getResources().getColor(colorId);
    }

    private void startActivityForFragId(int fragId, int requestCode, Bundle opts) {
        boolean showTitle;
        switch (fragId) {
            case JRFragmentHostActivity.JR_LANDING:
                showTitle = true;
                break;
            case JRFragmentHostActivity.JR_WEBVIEW:
                showTitle = false;
                break;
            default: throw new JRFragmentHostActivity.IllegalFragmentIdException(fragId);
        }

        Intent i = JRFragmentHostActivity.createIntentForCurrentScreen(getActivity(), showTitle);
        i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, fragId);
        if (!isEmbeddedMode()) {
            i.putExtra(JRFragmentHostActivity.JR_AUTH_FLOW,
                    ((JRFragmentHostActivity) getActivity()).isAuthFlow());
        }
        if (opts != null) i.putExtras(opts);
        startActivityForResult(i, requestCode);
    }

    /*package*/ void showUserLanding() {
        startActivityForFragId(JRFragmentHostActivity.JR_LANDING, REQUEST_LANDING);
    }

    /*package*/ void showWebView(boolean socialSharingSignIn) {
        Bundle opts = new Bundle();
        opts.putBoolean(JRWebViewFragment.SOCIAL_SHARING_MODE, socialSharingSignIn);
        startActivityForFragId(JRFragmentHostActivity.JR_WEBVIEW, REQUEST_WEBVIEW, opts);
    }

    /*package*/ void showWebView() {
        showWebView(false);
    }

    /*package*/ void tryToFinishActivity() {
        JREngage.logd(TAG, "[tryToFinishActivity]");
        getActivity().finish();
    }

    /*package*/ boolean hasView() {
        return getView() != null;
    }

    /*package*/ boolean isSpecificProviderFlow() {
        return getArguments().getString(JRFragmentHostActivity.JR_PROVIDER) != null;
    }

    /*package*/ String getSpecificProvider() {
        return getArguments().getString(JRFragmentHostActivity.JR_PROVIDER);
    }

    /**
     * @internal
     * Delegated to from JRFragmentHostActivity
     */
    /*package*/ void onBackPressed() {}
}
