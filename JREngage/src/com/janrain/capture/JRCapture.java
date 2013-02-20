/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2012, Janrain, Inc.
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

package com.janrain.capture;

import android.util.Pair;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.utils.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static android.text.TextUtils.join;
import static com.janrain.android.engage.utils.AndroidUtils.urlEncode;

public class JRCapture {
    public static final String ENTITY_TYPE_NAME = "test_user1";
    public static final String CAPTURE_DOMAIN = "mobile.dev.janraincapture.com";
    public static final String CLIENT_ID = "zc7tx83fqy68mper69mxbt5dfvd7c2jh";
    public static final String CLIENT_SECRET = "aqkcrjcf8ceexc5gfvw47fpazjfysyct";

    public static JSONObject getEntity(int id) throws IOException, JSONException {
        //URLConnection entityConn = new URL("https://" + CAPTURE_DOMAIN + "/entity?" +
        //        "type_name=" + ENTITY_TYPE_NAME +
        //        "&client_id=" + CLIENT_ID +
        //        "&client_secret=" + CLIENT_SECRET +
        //        "&id=" + id).openConnection();
        URLConnection entityConn = new URL("https://" + CAPTURE_DOMAIN + "/entity?" +
            "access_token=6vxjc6xg88g2q5ht").openConnection();
        entityConn.connect();

        String response = CaptureStringUtils.readFully(entityConn.getInputStream());

        JSONObject jo = new JSONObject(new JSONTokener(response));
        if ("ok".equals(jo.optString("stat"))) {
            //CaptureStringUtils.log("response: " + response);
            return jo.getJSONObject("result");
        } else {
            throw new IOException("failed to get entity, bad JSON response: " + jo);
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        //JRCaptureEntity.inflate(getEntity(159)); // for entity type "user"
        JRCaptureRecord record = new JRCaptureRecord(getEntity(14474));
        JREngage.logd("JRCapture", record.toString(2));

        //CaptureJsonUtils.deeplyRandomizeArrayElementOrder(record);
        record.put("email", "nathan+androidtest2@janrain.com");
        ((JSONArray) record.opt("pinapinapL1Plural")).put(new JSONObject("{\"string1\":\"poit\"}"));
        ((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string1", "zot");
        ((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string2", "narf");

        try {
            record.accessToken = "6vxjc6xg88g2q5ht";
            record.synchronize(new SyncListener() {
                public void onSuccess() {
                    JREngage.logd("JRCapture", "success");
                }

                public void onFailure(Object e) {
                    JREngage.logd("JRCapture", ("failure: " + e));
                }
            });
        } catch (InvalidApidChangeException e) {
            e.printStackTrace();
        }
    }

    public static class InvalidApidChangeException extends Exception {
        public InvalidApidChangeException(String description) {
            super(description);
        }
    }

    /*package*/ static abstract class ApidChange {
        /*package*/ String attrPath;
        /*package*/ Object newVal;

        /*package*/ String findClosestParentSubentity() {
            int n = attrPath.lastIndexOf("#");
            if (n == -1) return "/";
            String number = Pattern.compile("#([0-9])*").matcher(attrPath.substring(n)).group();
            return attrPath.substring(0, n) + number;
        }

        @Override
        public String toString() {
            return "<" + getClass().getSimpleName() + " attrPath: " + attrPath + " newVal: " + newVal + ">";
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this instanceof ApidUpdate) return super.equals(obj);
            return toString().equals(obj.toString()) && obj instanceof ApidChange;
        }

        public abstract URL getUrlFor();

        public void writeConnectionBody(URLConnection urlConnection, String accessToken) throws IOException {
            OutputStream outputStream = urlConnection.getOutputStream();

            Set<Pair<String, String>> bodyParams = getBodyParams();
            bodyParams.add(new Pair<String, String>("access_token", accessToken));
            Collection<String> paramPairs = CollectionUtils.map(bodyParams,
                    new CollectionUtils.Function<String, Pair<String, String>>() {
                        public String operate(Pair<String, String> val) {
                            return val.first.concat("=").concat(urlEncode(val.second));
                        }
                    });

            String body = join("&", paramPairs);
            outputStream.write(body.getBytes("UTF-8"));
        }

        /*package*/ abstract Set<Pair<String, String>> getBodyParams();
    }

    /*package*/ static class ApidUpdate extends ApidChange {
        /*package*/ ApidUpdate(Object newVal, String attrPath) {
            this.newVal = newVal;
            this.attrPath = attrPath;
        }

        public ApidUpdate collapseWith(ApidUpdate update) {
            assert update.attrPath.equals(attrPath);
            return new ApidUpdate(CaptureJsonUtils.collapseJsonObjects((JSONObject) newVal,
                    (JSONObject) update.newVal), attrPath);
        }

        @Override
        public URL getUrlFor() {
            try {
                return new URL("https://" + CAPTURE_DOMAIN + "/entity.update");
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        @Override
        Set<Pair<String, String>> getBodyParams() {
            Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
            params.add(new Pair<String, String>("value", newVal.toString()));
            if (!attrPath.equals("/")) params.add(new Pair<String, String>("attribute_name", attrPath));
            return params;
        }
    }

    /*package*/ static class ApidReplace extends ApidChange {
        ApidReplace(Object newVal, String attrPath) {
            this.newVal = newVal;
            this.attrPath = attrPath;
        }

        public URL getUrlFor() {
            try {
                return new URL("https://" + CAPTURE_DOMAIN + "/entity.replace");
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        @Override
        Set<Pair<String, String>> getBodyParams() {
            Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
            params.add(new Pair<String, String>("value", newVal.toString()));
            if (attrPath.equals("/")) throw new RuntimeException("Unexpected root attrPath in: " + this);
            params.add(new Pair<String, String>("attribute_name", attrPath));
            return params;
        }
    }

    /*package*/ static class ApidDelete extends ApidChange {
        ApidDelete(String attrPath) {
            this.attrPath = attrPath;
        }

        public URL getUrlFor() {
            try {
                return new URL("https://" + CAPTURE_DOMAIN + "/entity.delete");
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unexpected", e);
            }
        }

        @Override
        Set<Pair<String, String>> getBodyParams() {
            Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
            if (attrPath.equals("/")) throw new RuntimeException("Unexpected root attrPath in: " + this);
            params.add(new Pair<String, String>("attribute_name", attrPath));
            return params;
        }
    }

    public static interface SyncListener {
        public void onSuccess();

        public void onFailure(Object e);
    }
}
