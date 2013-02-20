/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2013, Janrain, Inc.
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

import android.util.Base64;
import android.util.Pair;
import com.janrain.android.engage.JREngage;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static com.janrain.android.engage.utils.AndroidUtils.urlEncode;

public class JRCaptureRecord extends JSONObject {
    private static final SimpleDateFormat CAPTURE_SIGNATURE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        CAPTURE_SIGNATURE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private JSONObject original;
    private String accessToken;
    private String refreshSecret;

    public JRCaptureRecord(JSONObject jo) {
        super();

        try {
            original = new JSONObject(jo.toString());
            CaptureJsonUtils.deepCopy(original, this);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }
    }

    public final void refreshAccessToken(JRCapture.RequestCallback callback) {
        accessToken = "6bunfwu42h2rwgbq";
        refreshSecret = "a";
        String domain = "test-multi.janraincapture.com";

        try {
            URL url = new URL("https://" + domain + "/access/getAccessToken");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            String date = CAPTURE_SIGNATURE_DATE_FORMAT.format(new Date());
            Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
            params.add(new Pair<String, String>("application_id", "fvbamf9kkkad3gnd9qyb4ggw6w"));
            params.add(new Pair<String, String>("access_token", accessToken));
            params.add(new Pair<String, String>("Signature", urlEncode(makeHash(date))));
            params.add(new Pair<String, String>("Date", urlEncode(date)));
            JRCapture.writePostParams(connection, params);
            connection.getOutputStream().close();
            Object response = connection.getContent();
            if (response instanceof JSONObject && "ok".equals(((JSONObject) response).opt("stat"))) {
                accessToken = (String) ((JSONObject) response).opt("access_token");
            } else {
                JREngage.logd("JRCapture", response.toString());
                callback.onFailure(response);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    private String makeHash(String date) {
        String stringToSign = date + "\n" + accessToken + "\n";

        byte[] hash;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(refreshSecret.getBytes("UTF-8"), mac.getAlgorithm());
            mac.init(secret);
            hash = mac.doFinal(stringToSign.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Unexpected", e);
        }

        return Base64.encodeToString(hash, Base64.DEFAULT);
    }

    public final void synchronize(final JRCapture.RequestCallback callback)
            throws JRCapture.InvalidApidChangeException {
        Set<JRCapture.ApidChange> changeSet = getApidChangeSet();
        List<JRCapture.ApidChange> changeList = new ArrayList<JRCapture.ApidChange>();
        changeList.addAll(changeSet);
        URLConnection.setContentHandlerFactory(CaptureJsonUtils.JSON_CONTENT_HANDLER_FACTORY);

        fireNextChange(changeList, callback);

        // add params to initinstance or something to init capture settings
        // plumb tokenURL to capture
        // plumb handler to construct this
        // add method for trad sign-in
    }

    private void fireNextChange(List<JRCapture.ApidChange> changeList, JRCapture.RequestCallback callback) {
        if (changeList.size() == 0) {
            callback.onSuccess();
            return;
        }

        JRCapture.ApidChange change = changeList.get(0);
        try {
            URLConnection urlConnection = change.getUrlFor().openConnection();
            urlConnection.setDoOutput(true);
            change.writeConnectionBody(urlConnection, accessToken);
            urlConnection.getOutputStream().close();
            Object content = urlConnection.getContent();
            if (content instanceof JSONObject && ((JSONObject) content).opt("stat").equals("ok")) {
                JREngage.logd("JRCapture", change.toString());
                JREngage.logd("JRCapture", ((JSONObject) content).toString(2));
                List<JRCapture.ApidChange> tail = changeList.subList(1, changeList.size());
                fireNextChange(tail, callback);
            } else {
                callback.onFailure(content);
            }
        } catch (IOException e) {
            callback.onFailure(e);
        } catch (JSONException e) {
            throw new RuntimeException("unexpected");
        }
    }

    private Set<JRCapture.ApidChange> collapseApidChanges(Set<JRCapture.ApidChange> changeSet) {
        HashMap<String, Set<JRCapture.ApidUpdate>> subentityUpdateBuckets =
                new HashMap<String, Set<JRCapture.ApidUpdate>>();

        Set<JRCapture.ApidChange> collapsedChangeSet = new HashSet<JRCapture.ApidChange>();
        for (JRCapture.ApidChange change : changeSet) {
            if (change instanceof JRCapture.ApidUpdate) {
                String parent = change.findClosestParentSubentity();
                JRCapture.ApidUpdate rewritten =
                        rewriteUpdateForParent((JRCapture.ApidUpdate) change, parent);
                Set<JRCapture.ApidUpdate> bucket = subentityUpdateBuckets.get(parent);
                if (bucket == null) {
                    subentityUpdateBuckets.put(parent, bucket = new HashSet<JRCapture.ApidUpdate>());
                }
                bucket.add(rewritten);
            } else if (change instanceof JRCapture.ApidReplace) {
                collapsedChangeSet.add(change);
            } else if (change instanceof JRCapture.ApidDelete) {
                collapsedChangeSet.add(change);
            }
        }

        collapsedChangeSet.addAll(collapseApidUpdateBuckets(subentityUpdateBuckets));

        return collapsedChangeSet;
    }

    private static Set<? extends JRCapture.ApidChange> collapseApidUpdateBuckets(
            Map<String, Set<JRCapture.ApidUpdate>> subentityUpdateBuckets) {
        Set<JRCapture.ApidChange> collapsedApidUpdates = new HashSet<JRCapture.ApidChange>();
        for (String subentity : subentityUpdateBuckets.keySet()) {
            JRCapture.ApidUpdate collapsedUpdate = null;
            for (JRCapture.ApidUpdate update : subentityUpdateBuckets.get(subentity)) {
                if (collapsedUpdate == null) {
                    collapsedUpdate = new JRCapture.ApidUpdate(update.newVal, update.attrPath);
                } else {
                    collapsedUpdate = collapsedUpdate.collapseWith(update);
                }
            }

            if (collapsedUpdate != null) {
                collapsedApidUpdates.add(collapsedUpdate);
            } else {
                throw new RuntimeException("Unexpected null collapsed update");
            }
        }

        return collapsedApidUpdates;
    }

    private static JRCapture.ApidUpdate rewriteUpdateForParent(JRCapture.ApidUpdate update, String parent) {
        String subObjectPath = update.attrPath.replaceFirst(parent, "");
        String[] flattenedObjectPaths = subObjectPath.split("/");
        Object newVal = update.newVal;
        for (int i = flattenedObjectPaths.length - 1; i >= 0; i--) {
            String s = flattenedObjectPaths[i];
            if (s.equals("")) continue; // ignore bad~ path components from .split
            JSONObject wrapper = new JSONObject();
            try {
                wrapper.put(s, newVal);
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected");
            }
            newVal = wrapper;
        }
        return new JRCapture.ApidUpdate(newVal, parent);
    }

    private Set<JRCapture.ApidChange> getApidChangeSet() throws JRCapture.InvalidApidChangeException {
        //CaptureJsonUtils.deepArraySort(this);
        return collapseApidChanges(CaptureJsonUtils.compileChangeSet(original, this));
    }
}
