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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSessionData;

import java.util.Stack;

/**
 * @internal
 *
 * @class JRUserInterfaceMaestro
 * Helper class for UI display/state.
 */
public class JRUserInterfaceMaestro {
    public static final String ACTION_FINISH_ACTIVITY =
            "com.janrain.android.engage.ACTION_FINISH_ACTIVITY";
    public static final String EXTRA_FINISH_ACTIVITY_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_ACTIVITY_TARGET";
    public static final IntentFilter FINISH_INTENT_FILTER =
            new IntentFilter(ACTION_FINISH_ACTIVITY);
    private static final String TAG = JRUserInterfaceMaestro.class.getSimpleName();
    private static JRUserInterfaceMaestro sInstance = null;

    private Stack<Class<? extends Activity>> mActivityStack;
    private JRSessionData mSessionData;
    private FrameLayout mFragmentContainer;
    private FragmentManager mFragmentManager;

    private JRUserInterfaceMaestro() {
        mActivityStack = new Stack<Class<? extends Activity>>();
        mSessionData = JRSessionData.getInstance();
    }

    public static JRUserInterfaceMaestro getInstance() {
        if (sInstance == null) {
            Log.w(TAG, "[getInstance()] creating singleton instance");
            sInstance = new JRUserInterfaceMaestro();
        }
        return sInstance;
    }

    /**
     * Displays the provider list for authentication.
     */
    public void showProviderSelectionDialog() {
        showProviderSelectionDialog(null);
    }

    /**
     * Displays the provider list for authentication.
     *
     * @param fragmentContainer
     *  A FrameLayout in which to display library UI fragments.  If \c null the library operates in modal
     *  dialog mode.
     */
    public void showProviderSelectionDialog(FrameLayout fragmentContainer) {
        mFragmentContainer = fragmentContainer;
        mSessionData.setDialogIsShowing(true);
        mSessionData.setSocialSharingMode(false);

        if (fragmentContainer != null) {
            mFragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
            showFragmentProviderSelection();
        } else {
            showModalProviderSelection();
        }
    }

    private void showFragmentProviderSelection() {
        Fragment plf = new JRProviderListFragment();
        mFragmentManager.beginTransaction().add(mFragmentContainer.getId(), plf).commit();
    }

    private void showModalProviderSelection() {
        int screenConfig = JREngage.getContext().getResources().getConfiguration().screenLayout;
        screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;
        // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
        // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
        // Nexus S reports SCREENLAYOUT_SIZE_NORMAL
        
        boolean smallOrNormal = screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL;

        if (smallOrNormal) {
            /* Phone sized device, use the whole screen, start a new Activity */

            /* See JRProviderListFragment.onCreate for an explanation of the flow control when there's a
             * "returning provider." */

            startActivity(JRProviderListActivity.class);
        } else {
            /* Tablet sized? Use the dialog box overlay mode */
            Context c = JREngage.getContext();
            DialogFragment f = new JRProviderListFragment();
            f.onCreate(null);

            Dialog d = new Dialog(c, R.style.jr_dialog_no_title);
            LayoutInflater li = d.getLayoutInflater();
            d.setContentView(f.onCreateView(li, null, null));
            d.setOwnerActivity((Activity) c);
            d.show();
        }
    }

    /**
     * Shows the user landing page.
     */
    public void showUserLanding() {
        startActivity(JRLandingActivity.class);
    }

    /**
     * Shows the web view for authentication.
     */
    public void showWebView() {
        startActivity(JRWebViewActivity.class);
    }

    /**
     * Shows the social publishing activity.
     */
    public void showPublishingDialogWithActivity() {
        setUpSocialPublishing();
        mSessionData.setDialogIsShowing(true);
        startActivity(JRPublishActivity.class);
    }

    public boolean isModal() {
        return mFragmentContainer == null;
    }

    public boolean isEmbedded() {
        return !isModal();
    }

    private Context getContext() {
        return JREngage.getContext();
    }

    /* */

    public void authenticationRestarted() {
        popToOriginal();
    }

    public void authenticationCompleted() {
        if (!mSessionData.getSocialSharingMode()) {
            popAll();
            mSessionData.setDialogIsShowing(false);
        } else {
            popToOriginal();
        }
    }

    public void authenticationFailed() {
        //todo what's this doing, what should it be doing?
//        if (mSessionData.getSocialSharingMode()) popToOriginal();
//        else popAll();
        popToOriginal();
    }

    public void authenticationCanceled() {
        popAll();
        mSessionData.setDialogIsShowing(false);
    }

    public void setUpSocialPublishing() {
        mSessionData.setSocialSharingMode(true);

        // TODO:
        // if (myPublishActivityController)
        //      [sessionData addDelegate:myPublishActivityController];
    }

    public void tearDownSocialPublishing() {
        mSessionData.setSocialSharingMode(false);
        mSessionData.setJRActivity(null);

        // TODO:
        // if (myPublishActivityController)
        //      [sessionData removeDelegate:myPublishActivityController];
    }

    public void publishingRestarted() {
        popToOriginal();
    }

    public void publishingCompleted() {
        popAll();
        mSessionData.setDialogIsShowing(false);
    }

    public void publishingJRActivityFailed() {
        Log.d(TAG, "publishingJRActivityFailed");
        popToOriginal();
    }

    public void publishingDialogFailed() {
        Log.d(TAG, "publishingDialogFailed");
        popAll();
        mSessionData.setDialogIsShowing(false);
    }

    public void publishingCanceled() {
        Log.d(TAG, "publishingCanceled");
        popAll();
        mSessionData.setDialogIsShowing(false);
    }

    /**
     * Helper method used to launch a new display managedActivity.
     *
     * @param managedActivity
     *      The ManagedActivity to be started/displayed.
     */
    private void startActivity(Class<? extends Activity> managedActivity) {
        Context context = JREngage.getContext();
        Intent intent = new Intent(context, managedActivity);
        // I was thinking about setting theme here but that can't be done.
        context.startActivity(intent);
        mActivityStack.push(managedActivity);
        Log.i(TAG, "[startActivity] pushed and started: " + managedActivity);
    }

    protected Stack<Class<? extends Activity>> getManagedActivityStack() {
        return mActivityStack;
    }

    private void popAll() {
        if (Config.LOGD) {
            Log.d(TAG, "[popAll]");
        }

        while (!mActivityStack.isEmpty()) {
            doFinishActivity(mActivityStack.pop());
        }
    }

    private void popToOriginal() {
        if (Config.LOGD) {
            Log.d(TAG, "[popToOriginal]");
        }

        Class<? extends Activity> originalRootActivity = (mSessionData.getSocialSharingMode())
                ? JRPublishActivity.class : JRProviderListActivity.class;

        popAndFinishActivitiesUntil(originalRootActivity);
    }

    private void popAndFinishActivitiesUntil(Class<? extends Activity> untilManagedActivity) {
        Log.i(TAG, "[popAndFinishActivitiesUntil] until: " + untilManagedActivity);

        if (mActivityStack.size() == 0) {
            // Crash horribly
            throw new IllegalStateException("JRUserInterfaceMaestro activity stack illegal state");
        }

        // If we're done, we're done.
        if (mActivityStack.peek().equals(untilManagedActivity)) return;

        // Else pop and recurse.
        doFinishActivity(mActivityStack.pop());
        popAndFinishActivitiesUntil(untilManagedActivity);
    }

    private void doFinishActivity(Class<? extends Activity> managedActivity) {
        Log.d(TAG, "[doFinishActivity] sending broadcast to: " + managedActivity);
        Context context = JREngage.getContext();
        Intent intent = new Intent(ACTION_FINISH_ACTIVITY);
        intent.putExtra(EXTRA_FINISH_ACTIVITY_TARGET, managedActivity.toString());
        context.sendBroadcast(intent);
    }
}