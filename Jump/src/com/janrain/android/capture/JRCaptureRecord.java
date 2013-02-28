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

package com.janrain.android.capture;

import android.content.Context;
import android.util.Base64;
import android.util.Pair;
import com.janrain.android.engage.JREngage;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class JRCaptureRecord extends JSONObject {
    private static final SimpleDateFormat CAPTURE_SIGNATURE_DATE_FORMAT;
    public static final String JR_CAPTURE_SIGNED_IN_USER_FILENAME = "jr_capture_signed_in_user";

    static {
        CAPTURE_SIGNATURE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CAPTURE_SIGNATURE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private JSONObject original;
    private String accessToken;
    private String refreshSecret;

    private JRCaptureRecord(){}

    /**
     *
     * @param jo
     */
    public JRCaptureRecord(JSONObject jo) {
        super();

        try {
            original = new JSONObject(jo.toString());
            CaptureJsonUtils.deepCopy(original, this);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }
    }

    /**
     *
     * @param applicationContext
     * @return
     */
    public static JRCaptureRecord loadFromDisk(Context applicationContext) {
        String fileContents = null;
        try {
            FileInputStream fis = applicationContext.openFileInput(JR_CAPTURE_SIGNED_IN_USER_FILENAME);
            fileContents = CaptureStringUtils.readFully(fis);
            JSONObject serializedVersion = new JSONObject(fileContents);
            JRCaptureRecord loadedRecord = new JRCaptureRecord();
            loadedRecord.original = serializedVersion.getJSONObject("original");
            loadedRecord.accessToken = serializedVersion.getString("accessToken");
            loadedRecord.refreshSecret = serializedVersion.getString("refreshSecret");
            CaptureJsonUtils.deepCopy(serializedVersion.getJSONObject("this"), loadedRecord);
            fis.close();
        } catch (FileNotFoundException ignore) {
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (JSONException ignore) {
            JREngage.logd("Bad JRCaptureRecord file contents:\n" + fileContents, ignore);
            // Will happen on badly formed file
        }
        return null;
    }

    /**
     *
     * @param applicationContext
     */
    public void saveToDisk(Context applicationContext) {
        try {
            FileOutputStream fos = applicationContext.openFileOutput(JR_CAPTURE_SIGNED_IN_USER_FILENAME, 0);
            JSONObject serializedVersion = new JSONObject();
            serializedVersion.put("original", original);
            serializedVersion.put("accessToken", accessToken);
            serializedVersion.put("refreshSecret", refreshSecret);
            serializedVersion.put("this", this);
            fos.write(serializedVersion.toString().getBytes("UTF-8"));
            fos.close();
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unexpected", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected", e);
        }
    }

    //public void refreshAccessToken(JRCapture.RequestCallback callback) {
    //    accessToken = "6bunfwu42h2rwgbq";
    //    refreshSecret = "a";
    //    String domain = "test-multi.janraincapture.com";
    //
    //    try {
    //        URL url = new URL("https://" + domain + "/access/getAccessToken");
    //        URLConnection connection = url.openConnection();
    //        connection.setDoOutput(true);
    //        String date = CAPTURE_SIGNATURE_DATE_FORMAT.format(new Date());
    //        Set<Pair<String, String>> params = new HashSet<Pair<String, String>>();
    //        params.add(new Pair<String, String>("application_id", "fvbamf9kkkad3gnd9qyb4ggw6w"));
    //        params.add(new Pair<String, String>("access_token", accessToken));
    //        params.add(new Pair<String, String>("Signature", urlEncode(getRefreshSignature(date))));
    //        params.add(new Pair<String, String>("Date", urlEncode(date)));
    //        JRCapture.writePostParams(connection, params);
    //        connection.getOutputStream().close();
    //        Object response = CaptureJsonUtils.urlConnectionGetJsonContent(connection);
    //        if (response instanceof JSONObject && "ok".equals(((JSONObject) response).opt("stat"))) {
    //            accessToken = (String) ((JSONObject) response).opt("access_token");
    //            if (callback != null) callback.onSuccess();
    //        } else {
    //            JREngage.logd("JRCapture", response.toString());
    //            if (callback != null) callback.onFailure(response);
    //        }
    //    } catch (MalformedURLException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    } catch (IOException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    }
    //}

    //private String getRefreshSignature(String date) {
    //    String stringToSign = date + "\n" + accessToken + "\n";
    //
    //    byte[] hash;
    //    try {
    //        Mac mac = Mac.getInstance("HmacSHA1");
    //        SecretKeySpec secret = new SecretKeySpec(refreshSecret.getBytes("UTF-8"), mac.getAlgorithm());
    //        mac.init(secret);
    //        hash = mac.doFinal(stringToSign.getBytes("UTF-8"));
    //    } catch (NoSuchAlgorithmException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    } catch (UnsupportedEncodingException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    } catch (InvalidKeyException e) {
    //        throw new RuntimeException("Unexpected", e);
    //    }
    //
    //    return Base64.encodeToString(hash, Base64.DEFAULT);
    //}

    /**
     *
     * @param callback
     * @throws JRCapture.InvalidApidChangeException
     */
    public void synchronize(final JRCapture.RequestCallback callback)
            throws JRCapture.InvalidApidChangeException {
        Set<ApidChange> changeSet = getApidChangeSet();
        List<ApidChange> changeList = new ArrayList<ApidChange>();
        changeList.addAll(changeSet);

        fireNextChange(changeList, callback);

        // add params to initinstance or something to init capture settings
        // plumb tokenURL to capture
        // plumb handler to construct this
        // add method for trad sign-in
    }

    private void fireNextChange(final List<ApidChange> changeList, final JRCapture.RequestCallback callback) {
        if (changeList.size() == 0) {
            if (callback != null) callback.onSuccess();
            return;
        }

        final ApidChange change = changeList.get(0);
        Set<Pair<String, String>> params = new HashSet<Pair<String, String>>(change.getBodyParams());
        params.add(new Pair<String, String>("access_token", accessToken));

        JRCapture.FetchCallback jsonCallback = new JRCapture.FetchCallback() {
            public void run(Object content) {
                if (content instanceof JSONObject && ((JSONObject) content).opt("stat").equals("ok")) {
                    JREngage.logd("JRCapture", change.toString());
                    try {
                        JREngage.logd("JRCapture", ((JSONObject) content).toString(2));
                    } catch (JSONException e) {
                        throw new RuntimeException("Unexpected", e);
                    }
                    List<ApidChange> tail = changeList.subList(1, changeList.size());
                    fireNextChange(tail, callback);
                } else {
                    if (callback != null) callback.onFailure(content);
                }
            }
        };

        Connection connection = new Connection(change.getUrlFor().toString());
        connection.addAllToParams(params);
        connection.fetchResponseMaybeJson(jsonCallback);
    }

    private Set<ApidChange> collapseApidChanges(Set<ApidChange> changeSet) {
        HashMap<String, Set<ApidUpdate>> subentityUpdateBuckets =
                new HashMap<String, Set<ApidUpdate>>();

        Set<ApidChange> collapsedChangeSet = new HashSet<ApidChange>();
        for (ApidChange change : changeSet) {
            if (change instanceof ApidUpdate) {
                String parent = change.findClosestParentSubentity();
                ApidUpdate rewritten =
                        rewriteUpdateForParent((ApidUpdate) change, parent);
                Set<ApidUpdate> bucket = subentityUpdateBuckets.get(parent);
                if (bucket == null) {
                    subentityUpdateBuckets.put(parent, bucket = new HashSet<ApidUpdate>());
                }
                bucket.add(rewritten);
            } else if (change instanceof ApidReplace) {
                collapsedChangeSet.add(change);
            } else if (change instanceof ApidDelete) {
                collapsedChangeSet.add(change);
            }
        }

        collapsedChangeSet.addAll(collapseApidUpdateBuckets(subentityUpdateBuckets));

        return collapsedChangeSet;
    }

    private static Set<? extends ApidChange> collapseApidUpdateBuckets(
            Map<String, Set<ApidUpdate>> subentityUpdateBuckets) {
        Set<ApidChange> collapsedApidUpdates = new HashSet<ApidChange>();
        for (String subentity : subentityUpdateBuckets.keySet()) {
            ApidUpdate collapsedUpdate = null;
            for (ApidUpdate update : subentityUpdateBuckets.get(subentity)) {
                if (collapsedUpdate == null) {
                    collapsedUpdate = new ApidUpdate(update.newVal, update.attrPath);
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

    private static ApidUpdate rewriteUpdateForParent(ApidUpdate update, String parent) {
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
        return new ApidUpdate(newVal, parent);
    }

    private Set<ApidChange> getApidChangeSet() throws JRCapture.InvalidApidChangeException {
        //CaptureJsonUtils.deepArraySort(this);
        return collapseApidChanges(CaptureJsonUtils.compileChangeSet(original, this));
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
