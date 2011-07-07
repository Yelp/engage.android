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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Config;
import android.util.Log;
import android.widget.FrameLayout;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.session.JRSessionData;
import java.util.Stack;

/**
 * @internal
 *
 * @class JRUserInterfaceMaestro
 * Helper class for UI display/state.
 */
public class JRUserInterfaceMaestro {
    public static final String ACTION_FINISH_FRAGMENT = "com.janrain.android.engage.ACTION_FINISH_FRAGMENT";
    public static final String EXTRA_FINISH_FRAGMENT_TARGET =
            "com.janrain.android.engage.EXTRA_FINISH_FRAGMENT_TARGET";
    public static final IntentFilter FINISH_INTENT_FILTER =
            new IntentFilter(ACTION_FINISH_FRAGMENT);
    private static final String TAG = JRUserInterfaceMaestro.class.getSimpleName();
    private static JRUserInterfaceMaestro sInstance = null;

    //private Stack<Class<? extends Activity>> mActivityStack;
    private Stack<Class<? extends Fragment>> mFragmentStack;
    private JRSessionData mSessionData;
    private FrameLayout mFragmentContainer;
    private FragmentManager mFragmentManager;

    private JRUserInterfaceMaestro() {
        //mActivityStack = new Stack<Class<? extends Activity>>();
        mFragmentStack = new Stack<Class<? extends Fragment>>();
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
        }

        showUiPiece(JRProviderListFragment.class, JRFragmentHostActivity.JR_PROVIDER_LIST);
    }

    private void showUiPiece(Class<? extends Fragment> f, int fragId) {
        if (isEmbeddedMode()) {
            try {
                Object o = f.newInstance();
                mFragmentManager.beginTransaction().add(mFragmentContainer.getId(), (Fragment) o).commit();
            } catch (InstantiationException e) {
                throw new RuntimeException("Error instantiating fragment: ", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error instantiating fragment: ", e);
            }
        } else {
            Intent i = new Intent(JREngage.getContext(), JRFragmentHostActivity.class);
            i.putExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, fragId);
            startActivity(i);
        }
    }

    private void showModalUiPiece(Class<? extends Fragment> f, Class<? extends Activity> a) {
        if (isSmallOrNormalScreen()) {
            /* Phone sized device, use the whole screen, start a new Activity */

            /* See JRProviderListFragment.onCreate for an explanation of the flow control when there's a
             * "returning provider." */

            startActivity(a);
        } else {
            /* Tablet sized? Use the dialog box overlay mode */
            //try {
            //    Fragment newF = f.newInstance();
            //    newF.onCreate(null);
            //
            //    Dialog d = new Dialog(c, R.style.jr_dialog_no_title);
            //    LayoutInflater li = d.getLayoutInflater();
            //    d.setContentView(newF.onCreateView(li, null, null));
            //    d.setOwnerActivity((Activity) c);
            //    d.show();
            //} catch (InstantiationException e) {
            //    throw new RuntimeException("Error instantiating fragment: ", e);
            //} catch (IllegalAccessException e) {
            //    throw new RuntimeException("Error instantiating fragment: ", e);
            //}
            startActivity(a);
        }
    }

    /**
     * Shows the user landing page.
     */
    public void showUserLanding() {
        showUiPiece(JRLandingFragment.class, JRFragmentHostActivity.JR_LANDING);
    }

    /**
     * Shows the web view for authentication.
     */
    public void showWebView() {
        showUiPiece(JRWebViewFragment.class, JRFragmentHostActivity.JR_WEBVIEW);
    }

    /**
     * Shows the social publishing activity.
     */
    public void showPublishingDialogWithActivity() {
        showPublishingDialogWithActivity(null);
    }

    public void showPublishingDialogWithActivity(FrameLayout fragmentContainer) {
        mFragmentContainer = fragmentContainer;
        setUpSocialPublishing();
        mSessionData.setDialogIsShowing(true);

        if (fragmentContainer != null) {
            mFragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
        }

        showUiPiece(JRPublishFragment.class, JRFragmentHostActivity.JR_PUBLISH);
    }

    public boolean isSmallOrNormalScreen() {
        int screenConfig = JREngage.getContext().getResources().getConfiguration().screenLayout;
        screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;

        // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
        // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
        // Nexus S reports SCREENLAYOUT_SIZE_NORMAL

        return screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public boolean isDialogMode() {
        return !isEmbeddedMode() && !isSmallOrNormalScreen();

    }

    public boolean isEmbeddedMode() {
        return mFragmentContainer != null;
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
     * @param intent
     *      The Intent for the managed activity to be started/displayed.
     */
    private void startActivity(Intent intent) {
        Context context = JREngage.getContext();
        context.startActivity(intent);
        //try {
            /* We run time type check the cast immediately below in an assertion */
            //@SuppressWarnings("unchecked")
            //Class<? extends Activity> managedActivity;
            //managedActivity = (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            //assert (Activity.class.isAssignableFrom(managedActivity));

            int fragId = intent.getIntExtra(JRFragmentHostActivity.JR_FRAGMENT_ID, 0);
            Class<? extends Fragment> f = JRFragmentHostActivity.getFragmentClassForId(fragId);
            mFragmentStack.push(f);
            //Log.i(TAG, "[startActivity] pushed and started: " + managedActivity);
        //} catch (ClassNotFoundException e) {
        //    throw new RuntimeException(e);
        //}
    }

    private void startActivity(Class<? extends Activity> managedActivity) {
        startActivity(new Intent(JREngage.getContext(), managedActivity));
    }

    //protected Stack<Class<? extends Activity>> getManagedActivityStack() {
    //    return mActivityStack;
    //}
    protected Stack<Class<? extends Fragment>> getManagedFragmentStack() {
        return mFragmentStack;
    }

    private void popAll() {
        if (Config.LOGD) {
            Log.d(TAG, "[popAll]");
        }

        //while (!mActivityStack.isEmpty()) {
        //    doFinishActivity(mActivityStack.pop());
        //}
        while (!mFragmentStack.isEmpty()) {
            doFinishFragment(mFragmentStack.pop());
        }
    }

    private void popToOriginal() {
        if (Config.LOGD) {
            Log.d(TAG, "[popToOriginal]");
        }

        //Class<? extends Activity> originalRootActivity = (mSessionData.getSocialSharingMode())
        //        ? JRPublishActivity.class : JRProviderListActivity.class;
        Class<? extends Fragment> originalRootFragment = (mSessionData.getSocialSharingMode())
                ? JRPublishFragment.class : JRProviderListFragment.class;

        //popAndFinishActivitiesUntil(originalRootActivity);
        popAndFinishFragmentsUntil(originalRootFragment);

    }

    //private void popAndFinishActivitiesUntil(Class<? extends Activity> untilManagedActivity) {
    private void popAndFinishFragmentsUntil(Class<? extends Fragment> untilManagedFragment) {
        Log.i(TAG, "[popAndFinishActivitiesUntil] until: " + untilManagedFragment);

        if (mFragmentStack.size() == 0) {
            // Crash horribly
            throw new IllegalStateException("JRUserInterfaceMaestro activity stack illegal state");
        }

        // If we're done, we're done.
        if (mFragmentStack.peek().equals(untilManagedFragment)) return;

        // Else pop and recurse.
        doFinishFragment(mFragmentStack.pop());
        popAndFinishFragmentsUntil(untilManagedFragment);
    }

    //private void doFinishActivity(Class<? extends Activity> managedActivity) {
    private void doFinishFragment(Class<? extends Fragment> managedFragment) {
        Log.d(TAG, "[doFinishActivity] sending broadcast to: " + managedFragment);
        Context context = JREngage.getContext();
        Intent intent = new Intent(ACTION_FINISH_FRAGMENT);
        intent.putExtra(EXTRA_FINISH_FRAGMENT_TARGET, managedFragment.toString());
        context.sendBroadcast(intent);
    }
}