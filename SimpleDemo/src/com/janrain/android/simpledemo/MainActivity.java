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
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.types.JRImageMediaObject;
import com.janrain.android.engage.ui.JRLandingActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener, JREngageDelegate {

    private static final String ENGAGE_APP_ID = "appcfamhnpkagijaeinl";
    private static final String ENGAGE_TOKEN_URL = "http://jrengage-for-android.appspot.com/login";

    private JREngage mEngage;
    private Button mBtnTestAuth;
    private Button mBtnTestPub;
    private Button mBtnTestLand;
    String titleText, linkText, descriptionText, imageUrl;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(this);

        mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
        mBtnTestPub.setOnClickListener(this);
        mBtnTestPub.setEnabled(false);

        mBtnTestLand = (Button)findViewById(R.id.btn_test_land);
        mBtnTestLand.setOnClickListener(this);

        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... v) {
                try {
                    String blogurl = "http://www.janrain.com/feed/blogs";
                    InputStream is = (new URL(blogurl)).openStream();
                    Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                    Element rss = (Element) d.getElementsByTagName("rss").item(0);
                    Element channel = (Element) rss.getElementsByTagName("channel").item(0);
                    Element item = (Element) channel.getElementsByTagName("item").item(1);
                    Element title = (Element) item.getElementsByTagName("title").item(0);
                    Element link = (Element) item.getElementsByTagName("link").item(0);
                    Element description = (Element) item.getElementsByTagName("description").item(0);

                    titleText = title.getFirstChild().getNodeValue();
                    linkText = link.getFirstChild().getNodeValue();
                    descriptionText = description.getFirstChild().getNodeValue();

//                    Html.fromHtml(Html.fromHtml(descriptionText).toString(), new Html.ImageGetter() {
//                        public Drawable getDrawable(String s) {
//                            imageUrl = s;
//                            return null;
//                        }
//                    }, null);
                } catch (Exception e) { throw new RuntimeException(e); }
                return null;
            }

            protected void onPostExecute(Void v) {
                mBtnTestPub.setEnabled(true);
            }
        }.execute();
    }

    public void onClick(View view) {
        if (view == mBtnTestAuth) {
            mEngage.showAuthenticationDialog();
        } else if (view == mBtnTestPub) {
            JRActivityObject jra = new JRActivityObject(titleText, linkText);
            jra.setDescription(descriptionText);
            //jra.setMedia(new JRImageMediaObject(imageUrl, ""));
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

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, byte[] tokenUrlPayload, String provider) {
        Toast.makeText(this, "Authentication did reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, byte[] tokenUrlPayload, String provider) {
        Toast.makeText(this, "Authentication did reach token url", Toast.LENGTH_SHORT).show();
    }

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        String message = "Authentication failed to show, error: " +
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

    public void jrSocialPublisingActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
