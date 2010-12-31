package com.janrain.android.engage.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.janrain.android.engage.JREngage;

/**
 * TODO: javadoc
 */
public class JRUserInterfaceMaestro {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    private JRUserInterfaceMaestro() {
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    public void showAuthenticationDialog() {
        startActivity(JRProvidersActivity.class);
    }

    private void startActivity(Class activityClass) {
        Context context = JREngage.getContext();
        context.startActivity(new Intent(context, activityClass));
    }

}
