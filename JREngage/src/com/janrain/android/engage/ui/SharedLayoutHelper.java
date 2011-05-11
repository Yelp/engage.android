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

    private Activity mOwner;
    private ProgressDialog mProgressDialog;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Creates instance of this class, binding it to the owner.
     */
    public SharedLayoutHelper(Activity owner) {
        mOwner = owner;
        mProgressDialog = null;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Changes the header text.
     */
    public void setHeaderText(String text) {
        TextView textView = (TextView) mOwner.findViewById(R.id.header_text);
        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * Inflates the common/shared menu.
     */
    public void inflateAboutMenu(Menu menu) {
        mOwner.getMenuInflater().inflate(R.menu.about_menu, menu);
    }

    /**
     * Common handler for about menu.  If the menu item is the about item, the about dialog
     * is displayed and <code>true</code> is returned.  Otherwise <code>false</code> is
     * returned.
     */
    public boolean handleAboutMenu(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_janrain:
                mOwner.showDialog(DIALOG_ABOUT_ID);
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
                    mOwner,
                    "",
                    mOwner.getString(R.string.progress_loading),
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

        LayoutInflater inflater = (LayoutInflater)mOwner.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.about_dialog, (ViewGroup)mOwner.findViewById(R.id.about_dialog_root));

        builder = new AlertDialog.Builder(mOwner);
        builder.setView(layout);
        builder.setPositiveButton(R.string.about_button_ok, new DialogInterface.OnClickListener() {
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