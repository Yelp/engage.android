package com.janrain.android.engage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.janrain.android.engage.R;
import com.janrain.android.engage.session.JRSessionData;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 6/22/11
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class JRProviderListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (JRUserInterfaceMaestro.getInstance().isModal()) {
            setTheme(R.style.jr_dialog_no_title);
        }

        /* What's happening is that we need to start the LandingActivity from this onCreate method
         * (as opposed to the LandingActivity being created from the UI maestro class) so that this
         * activity's FinishHandler is registered (which happens in this onCreate).  Otherwise this activity
         * isn't created until it's popped back into view (from the LandingActivity), at which point the
         * FinishHandler is registered, but by which time this activity has already missed it's finish
         * message. The getStack call allows us to add the LandingActivity to the managed activity stack
         * after we start it with startActivityForResult. startActivityForResult has a special case for
         * requestCodes >= 0 that makes this activity's window not draw.  That can only be called from within
         * an Activity, however, so the UI maestro can't use it (because it uses a generic application
         * Context.)
         *
         * If there is a returning basic provider, we open to the LandingActivity, otherwise, we stay here.
         * If skipReturningUserLandingPage was set to "true" getReturningBasicProvider will return null
         * (and we'll stay here). */

        JRSessionData sessionData = JRSessionData.getInstance();
        if (!TextUtils.isEmpty(sessionData.getReturningBasicProvider())) {
           sessionData.setCurrentlyAuthenticatingProvider(
                    sessionData.getReturningBasicProvider());
            startActivityForResult(new Intent(this, JRLandingActivity.class), 0);
            JRUserInterfaceMaestro.getInstance()
                    .getManagedActivityStack().push(JRLandingActivity.class);
        }

        if (savedInstanceState == null) {
            // todo verify this flow control path -- when will savedInstanceState != null?
            JRProviderListFragment providerList = new JRProviderListFragment();

            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, providerList).commit();
        }
    }
}