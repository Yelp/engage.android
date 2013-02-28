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

package com.janrain.android.capture;

import android.util.Pair;
import com.janrain.android.Jump;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.net.JRConnectionManager;
import com.janrain.android.engage.net.JRConnectionManagerDelegate;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.utils.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static android.text.TextUtils.join;
import static com.janrain.android.capture.CaptureStringUtils.readFully;
import static com.janrain.android.engage.utils.AndroidUtils.urlEncode;

public class JRCapture {
    private static JSONObject getEntity(int id) throws IOException, JSONException {
        //URLConnection entityConn = new URL("https://" + CAPTURE_DOMAIN + "/entity?" +
        //        "type_name=" + ENTITY_TYPE_NAME +
        //        "&client_id=" + CLIENT_ID +
        //        "&client_secret=" + CLIENT_SECRET +
        //        "&id=" + id).openConnection();
        URLConnection entityConn = new URL("https://" + Jump.getCaptureDomain() + "/entity?" +
            "access_token=6vxjc6xg88g2q5ht").openConnection();
        entityConn.connect();

        String response = readFully(entityConn.getInputStream());

        JSONObject jo = new JSONObject(new JSONTokener(response));
        if ("ok".equals(jo.optString("stat"))) {
            //CaptureStringUtils.log("response: " + response);
            return jo.getJSONObject("result");
        } else {
            throw new IOException("failed to get entity, bad JSON response: " + jo);
        }
    }

    //public static void main(String[] args) throws IOException, JSONException {
        //JRCaptureEntity.inflate(getEntity(159)); // for entity type "user"
        //JRCaptureRecord record = new JRCaptureRecord(getEntity(14474));
        //JREngage.logd("JRCapture", record.toString(2));

        //CaptureJsonUtils.deeplyRandomizeArrayElementOrder(record);
        //record.put("email", "nathan+androidtest2@janrain.com");
        //((JSONArray) record.opt("pinapinapL1Plural")).put(new JSONObject("{\"string1\":\"poit\"}"));
        //((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string1", "zot");
        //((JSONObject) ((JSONObject) record.opt("oinoL1Object")).opt("oinoL2Object")).put("string2", "narf");

        //record.refreshAccessToken(null);

        //try {
        //    record.synchronize(new RequestCallback() {
        //        public void onSuccess() {
        //            JREngage.logd("JRCapture", "success");
        //        }
        //
        //        public void onFailure(Object e) {
        //            JREngage.logd("JRCapture", ("failure: " + e));
        //        }
        //    });
        //} catch (InvalidApidChangeException e) {
        //    e.printStackTrace();
        //}
    //}

    /*package*/ public static byte[] paramsGetBytes(Set<Pair<String, String>> bodyParams) {
        try {
            return paramsToString(bodyParams).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    /*package*/ static String paramsToString(Set<Pair<String, String>> bodyParams) {
        Collection<String> paramPairs = CollectionUtils.map(bodyParams,
                new CollectionUtils.Function<String, Pair<String, String>>() {
                    public String operate(Pair<String, String> val) {
                        return val.first.concat("=").concat(urlEncode(val.second));
                    }
                });

        return join("&", paramPairs);
    }

    public static JRConnectionManagerDelegate performTraditionalSignIn(String username,
                                                                       String password,
                                                                       Jump.TraditionalSignInType type,
                                                                       FetchJsonCallback handler) {
        /**
         * client_id
         * locale
         * response_type
         * redirect_uri
         * an email address param
         * a password param
         * form
         * attributeUpdates
         */

        String url = "https://" + Jump.getCaptureDomain() + "/oauth/auth_native_traditional";
        Connection connection = new Connection(url);
        connection.addAllToParams("client_id", Jump.getCaptureClientId(),
                "locale", "en_US",
                "response_type", "token",
                "redirect_uri", "http://android.library",
                "email", username,
                "password", password,
                "form", "signin");
        return connection.fetchResponseAsJson(handler);
    }

    public static JRConnectionManagerDelegate performLegacyTraditionalSignIn(String username,
                                                                             String password,
                                                                             Jump.TraditionalSignInType type,
                                                                             FetchJsonCallback handler) {
        String url = "https://" + Jump.getCaptureDomain() + "/oauth/mobile_signin_username_password";
        Connection connection = new Connection(url);
        connection.addAllToParams("client_id", Jump.getCaptureClientId(),
                "redirect_uri", "http://android.library",
                "email", username,
                "password", password);
        return connection.fetchResponseAsJson(handler);
    }

    //public static void writePostParams(URLConnection connection, Set<Pair<String, String>> params)
    //        throws IOException {
    //    connection.getOutputStream().write(paramsGetBytes(params));
    //}

    public static class InvalidApidChangeException extends Exception {
        public InvalidApidChangeException(String description) {
            super(description);
        }
    }

    public static interface RequestCallback {
        public void onSuccess();

        public void onFailure(Object e);
    }

    public static void performSocialSignIn(String authInfoToken, final FetchJsonCallback handler) {
        /***
         * client_id
         * locale
         * response_type
         * redirect_uri
         * token
         * attributeUpdates
         * thin_registration
         * flow_name
         */

        Connection c = new Connection("https://" + Jump.getCaptureDomain() + "/oauth/auth_native");
        c.addAllToParams("client_id", Jump.getCaptureClientId(),
                "locale", "en_US",
                "response_type", "token",
                "redirect_uri", "http://android-library",
                "token", authInfoToken,
                "thin_registration", "true");
        c.fetchResponseAsJson(handler);
    }

    public static void performLegacySocialSignIn(String authInfoToken, final FetchJsonCallback handler) {
        Connection c = new Connection("https://" + Jump.getCaptureDomain() + "/oauth/mobile_signin");
        c.addAllToParams("client_id", Jump.getCaptureClientId());
        c.addAllToParams("redirect_uri", "http://android-library");
        c.addAllToParams("token", authInfoToken);
        c.fetchResponseAsJson(handler);
    }

    public interface FetchJsonCallback {
        void run(JSONObject jsonObject);
    }

    public interface FetchCallback {
        void run(Object response);
    }
}

/*package*/ class Connection {
    /*package*/ String url;
    /*package*/ Set<Pair<String,String>> params = new HashSet<Pair<String, String>>();
    /*package*/ Method method = Method.POST;

    enum Method {POST, GET}

    /*package*/ Connection(String url) {
        this.url = url;
    }

    /*package*/ void addAllToParams(String... params) {
        for (int i = 0; i < params.length - 1; i += 2) {
            this.params.add(new Pair<String, String>(params[i], params[i + 1]));
        }

        if (params.length % 2 == 1) JREngage.loge("error: odd number of param strings");
    }

    /*package*/ void addAllToParams(Set<Pair<String, String>> params) {
        this.params.addAll(params);
    }

    /*package*/ JRConnectionManagerDelegate fetchResponseMaybeJson(final JRCapture.FetchCallback callback) {
        JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate connectionCallback =
                new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                    @Override
                    public void connectionDidFinishLoading(HttpResponseHeaders headers,
                                                           byte[] payload,
                                                           String requestUrl,
                                                           Object tag) {
                        Object response = CaptureJsonUtils.connectionManagerGetJsonContent(headers, payload);
                        callback.run(response);
                    }

                    @Override
                    public void connectionDidFail(Exception ex, String requestUrl, Object tag) {
                        JREngage.loge("failed request: " + requestUrl, ex);
                        callback.run(null);
                    }
                };

        if (method == Method.POST) {
            byte[] postData = JRCapture.paramsGetBytes(params);
            JRConnectionManager.createConnection(url, connectionCallback, null, null, postData);
        } else {
            String urlWithParms = url + "?" + JRCapture.paramsToString(params);
            JRConnectionManager.createConnection(urlWithParms, connectionCallback, null, null, null);
        }
        return connectionCallback;
    }

    /*package*/ JRConnectionManagerDelegate fetchResponseAsJson(final JRCapture.FetchJsonCallback callback) {
        return fetchResponseMaybeJson(new JRCapture.FetchCallback() {
            public void run(Object response) {
                if (response instanceof JSONObject) {
                    callback.run(((JSONObject) response));
                } else {
                    JREngage.loge("bad response: " + response);
                    callback.run(null);
                }
            }
        });
    }
}