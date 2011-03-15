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
package com.janrain.android.simpledemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.janrain.android.engage.*;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.*;
import com.janrain.android.engage.ui.JRLandingActivity;

import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ResponseCache;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener, JREngageDelegate {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ENGAGE_APP_ID = "aehecdnjkodopeijgjgo";//"appcfamhnpkagijaeinl";
    private static final String ENGAGE_TOKEN_URL = null;//"http://jrengage-for-android.appspot.com/login";

    private JREngage mEngage;
    private Button mBtnTestAuth;
    private Button mBtnTestPub;
    private Button mBtnTestLand;

    //blog fetching variables
    String mTitleText = "title text",
           mActionLink = "http://www.janrain.com/feed/blogs",
           mDescriptionText = "description text",
           mImageUrl = "http://www.janrain.com/sites/default/themes/janrain/logo.png";
    final Uri BLOGURL = Uri.parse("http://www.janrain.com/feed/blogs");

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(this);

        mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
        mBtnTestPub.setOnClickListener(this);

        mBtnTestLand = (Button)findViewById(R.id.btn_test_land);
        mBtnTestLand.setOnClickListener(this);

        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

        if (savedInstanceState != null) {
            mTitleText = savedInstanceState.getString("a");
            mDescriptionText = savedInstanceState.getString("b");
            mImageUrl = savedInstanceState.getString("c");
            mActionLink = savedInstanceState.getString("d");
            mBtnTestPub.setText("Test Publishing");
        } else {
            mBtnTestPub.setText("loading blog");
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... v) {
                    try {
                        Log.d(TAG, "blogload");
                        InputStream is = (new URL(BLOGURL.toString())).openStream();
                        Log.d(TAG, "blogload steam open");
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setCoalescing(true);
                        dbf.setValidating(false);
                        dbf.setNamespaceAware(false);
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Log.d(TAG, "blogload factory instantiated");
                        //the following parse call takes an astonishing ten seconds on a nexus s.
                        //XMLPullParser is said to be a faster way to go.
                        //thread with sample code here: http://groups.google.com/group/android-developers/msg/ddc6a8e83963a6b5
                        //another thread: http://stackoverflow.com/questions/4958973/3rd-party-android-xml-parser
                        Document d = db.parse(is);
                        Log.d(TAG, "blogload parsed");
                        Element rss = (Element) d.getElementsByTagName("rss").item(0);
                        Element channel = (Element) rss.getElementsByTagName("channel").item(0);
                        Element item = (Element) channel.getElementsByTagName("item").item(0);
                        Element title = (Element) item.getElementsByTagName("title").item(0);
                        Element link = (Element) item.getElementsByTagName("link").item(0);
                        Element description = (Element) item.getElementsByTagName("description").item(0);
                        Log.d(TAG, "blogload walked");

                        mTitleText = title.getFirstChild().getNodeValue();
                        mActionLink = link.getFirstChild().getNodeValue();

                        NodeList nl = description.getChildNodes();
                        mDescriptionText = new String();
                        for (int x=0; x<nl.getLength(); x++) { mDescriptionText += nl.item(x).getNodeValue(); }

                        //need to concatenate all the children of mDescriptionText (which has ~100s of TextElement children)
                        //in order to come up with the complete text body of the description tag.

                        mDescriptionText = Html.fromHtml(mDescriptionText, new Html.ImageGetter() {
                            public Drawable getDrawable(String s) {
                                mImageUrl = BLOGURL.getScheme() + "://" + BLOGURL.getHost() + s;
                                return null;
                            }
                        }, null).toString();
                    } catch (Exception e) { throw new RuntimeException(e); }
                    return null;
                }

                protected void onPostExecute(Void v) {
                    //mBtnTestPub.setEnabled(true);
                    Log.d(TAG, "blog loader onPostExecute");
                    mBtnTestPub.setText("Test Publishing");
                }
            }.execute();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!mBtnTestPub.getText().equals("loading blog")) {
            outState.putString("a", mTitleText);
            outState.putString("b", mDescriptionText);
            outState.putString("c", mImageUrl);
            outState.putString("d", mActionLink);
        }
    }

    public void onClick(View view) {
        if (view == mBtnTestAuth) {
            mEngage.showAuthenticationDialog();
        } else if (view == mBtnTestPub) {
            JRActivityObject jra = new JRActivityObject("shared an article from the Janrain Blog!", mActionLink);
            jra.setTitle(mTitleText);
            jra.setDescription(mDescriptionText);
            jra.setMedia(new JRImageMediaObject(mImageUrl, mImageUrl));
            mEngage.showSocialPublishingDialogWithActivity(jra);
        } else if (view == mBtnTestLand) {
            Intent intent = new Intent(this, JRLandingActivity.class);
            startActivity(intent);
        }
    }

    // ------------------------------------------------------------------------
    // JREngage DELEGATE METHODS
    // ------------------------------------------------------------------------

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

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        String message = "JREngage dialog failed to show, error: " +
                ((error == null) ? "unknown" : error.getMessage());

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidPublishActivity(JRActivityObject activity, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialPublishingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
