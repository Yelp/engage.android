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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSessionData;

import java.lang.ref.WeakReference;


/**
 * @internal
 *
 * @class SharedLayoutHelper
 * Encapsulated common functionality for UI Activities that use the shared header/footer
 * XML layout.
 */
public class SharedLayoutHelper {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    /** ID for common about dialog. */
    public static final int DIALOG_ABOUT_ID = 1000;

    /* Tag used for logging. */
    private static final String TAG = SharedLayoutHelper.class.getSimpleName();

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private WeakReference<Activity> mOwner;
    //private Activity mOwner;
    private ProgressDialog mProgressDialog;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Creates instance of this class, binding it to the owner.
     */
    public SharedLayoutHelper(Activity owner) {
        mOwner = new WeakReference<Activity>(owner);
        mProgressDialog = null;
        
        boolean hideTagline = JRSessionData.getInstance().getHidePoweredBy();
        int visibility = hideTagline ? View.GONE : View.VISIBLE;
        owner.findViewById(R.id.jr_tagline).setVisibility(visibility);
        try {
            // This View is only found in JRPublishActivity
            owner.findViewById(R.id.jr_email_sms_powered_by_text).setVisibility(visibility);
        } catch (NullPointerException e) {
            // Do nothing, we're not displaying JRPublishActivity
        }
    }

    // Declare the default constructor private so this class is only instantiated with an owner
    // Activity
    private SharedLayoutHelper() {
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Changes the header text.
     */
    public void setHeaderText(String text) {
        TextView textView = (TextView) mOwner.get().findViewById(R.id.jr_header_text);
        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * Inflates the common/shared menu.
     */
    public void inflateAboutMenu(Menu menu) {
        mOwner.get().getMenuInflater().inflate(R.menu.jr_about_menu, menu);
    }

    /**
     * Common handler for about menu.  If the menu item is the about item, the about dialog
     * is displayed and <code>true</code> is returned.  Otherwise <code>false</code> is
     * returned.
     */
    public boolean handleAboutMenu(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.jr_about_janrain:
                mOwner.get().showDialog(DIALOG_ABOUT_ID);
                return true;
            default:
                return false;
        }
    }

    /**
     * Shows the progress dialog associated with the owner.
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            // Progress dialog has not been displayed yet, need to create & show.
            mProgressDialog = ProgressDialog.show(
                    mOwner.get(),
                    "",
                    mOwner.get().getString(R.string.jr_progress_loading),
                    true);
            Log.d(TAG, "[showProgressDialog] create/show");
        } else if (!mProgressDialog.isShowing()) {
            // Progress dialog already exists, but not visible...just show.
            mProgressDialog.show();
            Log.d(TAG, "[showProgressDialog] show");
        } else {
            // Progress dialog already exists and is already visible...do nothing.
            Log.d(TAG, "[showProgressDialog] ignore");
        }
    }

    /**
     * Dismisses the progress dialog, if valid and visible.
     */
    public void dismissProgressDialog() {
        if ((mProgressDialog != null) && (mProgressDialog.isShowing())) {
            mProgressDialog.dismiss();
            Log.d(TAG, "[dismissProgressDialog] dismissed");
            //mProgressDialog = null;
        }
    }

    /**
     * Common/shared onCreateDialog method.
     */
    public Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch (id) {
            case DIALOG_ABOUT_ID:
                dialog = getAboutDialog();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }

    /**
     * Builds the common/shared about dialog.
     */
    private AlertDialog getAboutDialog() {
        AlertDialog.Builder builder;
        AlertDialog dialog;

        LayoutInflater inflater =
                (LayoutInflater)mOwner.get().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.jr_about_dialog,
                (ViewGroup)mOwner.get().findViewById(R.id.jr_about_dialog_root));

        builder = new AlertDialog.Builder(mOwner.get());
        builder.setView(layout);
        builder.setPositiveButton(R.string.jr_about_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();

        return dialog;
    }

    public boolean getProgressDialogShowing() {
        return (mProgressDialog != null) && mProgressDialog.isShowing();
    }
}
