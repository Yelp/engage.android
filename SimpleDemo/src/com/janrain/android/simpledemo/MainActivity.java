package com.janrain.android.simpledemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

public class MainActivity extends Activity implements View.OnClickListener, JREngageDelegate {

    private static final String ENGAGE_APP_ID = "pjdhmmpbeiimnpfmndkm";
    private static final String ENGAGE_TOKEN_URL = "http://jrengage-for-android.appspot.com/login";

    private JREngage mEngage;
    private Button mBtnTestAuth;
    private Button mBtnTestPub;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(this);

        mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
        mBtnTestPub.setOnClickListener(this);

        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);
    }

    public void onClick(View view) {
        if (view == mBtnTestAuth) {
            mEngage.showAuthenticationDialog();
        } else if (view == mBtnTestPub) {
//            mEngage.showSocialPublishingDialogWithActivity(
//                    new JRActivityObject()
//            );
        }
    }

    // ------------------------------------------------------------------------
    // JREngage DELEGATE METHODS
    // ------------------------------------------------------------------------

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidNotComplete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, byte[] tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, byte[] tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidNotCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidPublishActivity(JRActivityObject activity, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialPublisingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
