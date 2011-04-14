package com.janrain.android.quicksignin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.janrain.android.engage.*;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.*;
//import com.janrain.android.engage.ui.JRLandingActivity;


public class MainActivity extends Activity implements View.OnClickListener, JREngageDelegate {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int DIALOG_JRENGAGE_ERROR = 1;

    private static final String ENGAGE_APP_ID = "";
    private static final String ENGAGE_TOKEN_URL = null;//"http://jrengage-for-android.appspot.com/login";

    private JREngage mEngage;
    private Button mBtnTestAuth;
    private String mDialogErrorMessage;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(this);

//        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);


    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_JRENGAGE_ERROR:
                return new AlertDialog.Builder(this)
                    .setPositiveButton("Dismiss", null)
                    .setCancelable(false)
                    .setMessage(mDialogErrorMessage)
                    .create();
        }

        throw new RuntimeException("unknown dialogId");
    }

    public void onClick(View view) {
        Log.d(TAG, "[onClick]");

        this.startActivity(new Intent(this, ProfilesActivity.class));
        

//        if (view == mBtnTestAuth) {
//            mEngage.showAuthenticationDialog();
//        }
    }


    // ------------------------------------------------------------------------
    // JREngage DELEGATE METHODS
    // ------------------------------------------------------------------------
    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        Log.d(TAG, "[jrEngageDialogDidFailToShowWithError]");

        mDialogErrorMessage = "JREngage dialog failed to show, error: " +
                ((error == null) ? "unknown" : error.getMessage());

        showDialog(DIALOG_JRENGAGE_ERROR);
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
        JRDictionary profile = (authInfo == null) ? null : authInfo.getAsDictionary("profile");
        String displayName = (profile == null) ? null : profile.getAsString("displayName");
        String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                ? "" : (" for user: " + displayName));

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, String tokenUrlPayload, String provider) {
        Toast.makeText(this, "Authentication did reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        Toast.makeText(this, "Authentication did reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidNotComplete() {
        Toast.makeText(this, "Authentication did not complete", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        String message = "Authentication failed, error: " +
                ((error == null) ? "unknown" : error.getMessage());

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        Toast.makeText(this, "Authentication failed to reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrSocialDidNotCompletePublishing() {
    }

    public void jrSocialDidCompletePublishing() {
        Toast.makeText(this, "Sharing did complete", Toast.LENGTH_SHORT).show();
    }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        Toast.makeText(this, "Activity shared", Toast.LENGTH_SHORT).show();
    }

    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
    }
}
