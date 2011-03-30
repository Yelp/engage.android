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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.session.JRSessionData;

import java.util.Stack;

/**
 * Helper class for UI display/state.
 */
public class JRUserInterfaceMaestro {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    public static final String ACTION_FINISH_ACTIVITY =
            "com.janrain.android.engage.ACTION_FINISH_ACTIVITY";

    public static final String EXTRA_FINISH_ACTIVITY_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_ACTIVITY_TARGET";

    public static final IntentFilter FINISH_INTENT_FILTER =
            new IntentFilter(ACTION_FINISH_ACTIVITY);

    private static final String TAG = JRUserInterfaceMaestro.class.getSimpleName();

    private static JRUserInterfaceMaestro sInstance = null;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    public static JRUserInterfaceMaestro getInstance() {
        if (sInstance == null) {
            Log.w(TAG, "[getInstance()] creating singleton instance");
            sInstance = new JRUserInterfaceMaestro();
        }
        return sInstance;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private Stack<Class> mActivityStack;
    private JRSessionData mSessionData;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private JRUserInterfaceMaestro() {
        mActivityStack = new Stack<Class>();
        mSessionData = JRSessionData.getInstance();
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    /**
     * Displays the provider list for authentication.
     */
    public void showProviderSelectionDialog() {
        mSessionData.setDialogIsShowing(true);
        startActivity(JRProvidersActivity.class);

//        if (!TextUtils.isEmpty(mSessionData.getReturningBasicProvider())) {
//            mSessionData.setCurrentlyAuthenticatingProvider(mSessionData.getReturningBasicProvider());
//            showUserLanding();
//        }
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

    public void authenticationRestarted() {
        popToOriginal();
    }

    public void authenticationCompleted() {
        if (!mSessionData.getSocial()) {
            popAll();
            mSessionData.setDialogIsShowing(false);
        } else {
            popToOriginal();
        }
    }

    public void authenticationFailed() {
        //todo what's this doing, what should it be doing?
        popToOriginal();
    }

    public void authenticationCanceled() {
        //todo what's this doing, what should it be doing?
        //popAll();
    }

    public void setUpSocialPublishing() {
        mSessionData.setSocial(true);

        // TODO:
        // if (myPublishActivityController)
        //      [sessionData addDelegate:myPublishActivityController];
    }

    public void tearDownSocialPublishing() {
        mSessionData.setSocial(false);
        mSessionData.setActivity(null);

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
        //popAll(); //why does this popAll(), isn't that a bug?
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
    private void startActivity(Class managedActivity) {
        Context context = JREngage.getContext();
        context.startActivity(new Intent(context, managedActivity));
        mActivityStack.push(managedActivity);
        Log.i(TAG, "[startActivity] pushed and started: " + managedActivity);
    }

    protected Stack<Class> getManagedActivityStack() {
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

        Class originalRootActivity = (mSessionData.getSocial())
                ? JRPublishActivity.class : JRProvidersActivity.class;

        popAndFinishActivitiesUntil(originalRootActivity);
    }

    private void popAndFinishActivitiesUntil(Class untilManagedActivity) {
        Log.i(TAG, "[popAndFinishActivitiesUntil] until: " + untilManagedActivity);

        Class top;
        do {
            if (mActivityStack.size() < 1) {
                if (Config.LOGD) {
                    Log.d(TAG, "[popAndFinishActivitiesUntil] stack empty");
                }
                break;
            }
            top = mActivityStack.peek();
            if (top.equals(untilManagedActivity)) {
                if (Config.LOGD) {
                    Log.d(TAG, "[popAndFinishActivitiesUntil] found until");
                }
                break;
            }
            top = mActivityStack.pop();
            doFinishActivity(top);
            Log.i(TAG, "[popAndFinishActivitiesUntil] popped: " + top);
        } while (top != null);

        while (mActivityStack.peek() != untilManagedActivity) {
            Class managedActivity = mActivityStack.pop();
            doFinishActivity(managedActivity);
            Log.i(TAG, "[popAndFinishActivitiesUntil] popped and finished: " + managedActivity);
        }
    }

    private void doFinishActivity(Class managedActivity) {
        Log.d(TAG, "[doFinishActivity] sending broadcast to: " + managedActivity);
        Context context = JREngage.getContext();
        Intent intent = new Intent(ACTION_FINISH_ACTIVITY);
        intent.putExtra(EXTRA_FINISH_ACTIVITY_TARGET, managedActivity.toString());
        context.sendBroadcast(intent);
    }
}
