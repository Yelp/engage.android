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
package com.janrain.android.engage.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JRSmsObject {
    private String mBody;
    private List<String> mUrls;
    private List<String> mShortUrls;

    public JRSmsObject() {
        this("");
    }

    public JRSmsObject(String body) {
        if (body == null) body = "";

        mBody = body;
        mUrls = new ArrayList<String>();
    }

    void setShortUrls(List<String> shortUrls) {
        mShortUrls = shortUrls;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public List<String> getUrls() {
        return Collections.unmodifiableList(mUrls);
    }

    public void setUrls(List<String> urls) {
        if (urls == null) urls = new ArrayList<String>();
        if (urls.size() >= 5) throw
                new IllegalArgumentException("JRSmsObject supports a max of five URLs");

        mUrls = urls;
    }

    public void addUrl(String url) {
        if (mUrls.size() >= 5) throw
                new IllegalArgumentException("JRSmsObject supports a max of five URLs");

        mUrls.add(url);
    }
}
