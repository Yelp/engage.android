/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or
 *    other materials provided with the distribution.
 *  * Neither the name of the Janrain, Inc. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
package com.janrain.android.engage.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @brief An email a user can send to share an activity.
 *
 * JREmailObject is a simple container object for a pre-composed email to be attached to a
 * {@link com.janrain.android.engage.types.JRActivityObject JRActivityObject}.
 *
 * Instantiate a new JREmailObject with a subject and a body.  Optionally, add a list of URLs
 * from the body and Engage will automatically shorten them, track click-throughs and provide
 * analytics on your dashboard.
 *
 * See {@link com.janrain.android.engage.types.JRActivityObject JRActivityObject},
 * {@link com.janrain.android.engage.types.JRSmsObject JRSmsObject}
 */

public class JREmailObject implements Serializable {
    private String mSubject, mBody;
    private List<String> mUrls;
    private List<String> mShortUrls;

    /**
     * Create an empty email object.
     */
    public JREmailObject() {
        this("", "");
    }

    /**
     * Create an email object with a given subject and body.
     *
     * @param subject The email's subject line, passed to the device's email application
     * @param body The email's body, which will be appended to the user's comment and passed to the
     *  devices email application.
     */
    public JREmailObject(String subject, String body) {
        if (subject == null) subject = "";
        if (body == null) body = "";

        mSubject = subject;
        mBody = body;
        //mHtml = false;
        mUrls = new ArrayList<String>();
    }

    /**
     * @internal
     *
     * @param shortUrls
     */
    /* package*/ void setShortUrls(List<String> shortUrls) {
        mShortUrls = shortUrls;

        for (String longUrl : mUrls) {
            String shortUrl = mShortUrls.get(mUrls.indexOf(longUrl));
            mBody = mBody.replace(longUrl, shortUrl);
        }
    }

    /**
     * Return the email's subject.
     *
     * @return The email's subject.
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Set the email's subject.
     *
     * @param subject The email's subject.
     */
    public void setSubject(String subject) {
        mSubject = subject;
    }

    /**
     * Return the email's body.
     *
     * @return The email's body.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Set the email's body.
     *
     * @param body The email's body.
     */
    public void setBody(String body) {
        mBody = body;
    }

    //public void setHtml(boolean html) {
    //    mHtml = html;
    //}
    //
    //public boolean getHtml() {
    //    return mHtml;
    //}

    /**
     * Return the email's list of URLs to be shortened.
     *
     * @return An immutable list of URLs to be shortened.
     * @see JREmailObject#setUrls(java.util.List)
     */
    public List<String> getUrls() {
        return Collections.unmodifiableList(mUrls);
    }

    /**
     * Set the email's list of URLs to be shortened.  Each of these URLs will be shortened to an
     * rpx.me URL which tracks click-throughs and provides analytics.  Once shortened, the Engage
     * for Android library will substitute the shortened version for the original long version for
     * each URL found in the body of the email.
     *
     * @param urls The email's list of URLs to be shortened, not more than five URLs.
     */
    public void setUrls(List<String> urls) {
        if (urls == null) urls = new ArrayList<String>();
        if (urls.size() >= 5) throw
                new IllegalArgumentException("JREmailObject supports a maximum of five URLs");

        mUrls = urls;
    }

    /**
     * Add a single URL to the list of URLs to shorten.
     *
     * @param url The URL to add to the list of URLs to shorten.
     *
     * @see JREmailObject#setUrls(java.util.List)
     */
    public void addUrl(String url) {
        if (mUrls.size() >= 5) throw
                new IllegalArgumentException("JREmailObject supports a maximum of five URLs");

        mUrls.add(url);
    }
}
