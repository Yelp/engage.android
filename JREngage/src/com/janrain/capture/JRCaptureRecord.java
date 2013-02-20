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

import com.janrain.android.engage.JREngage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.join;

public class JRCaptureRecord extends JSONObject {
    private JSONObject original;
    String accessToken;
    private String refreshSecret;

    public JRCaptureRecord(JSONObject jo) {
        super();

        try {
            original = new JSONObject(jo.toString());
            //CaptureJsonUtils.deepArraySort(original);
            CaptureJsonUtils.deepCopy(original, this);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }
    }

    public final void synchronize(final JRCapture.SyncListener listener)
            throws JRCapture.InvalidApidChangeException {
        Set<JRCapture.ApidChange> changeSet = getApidChangeSet();
        List<JRCapture.ApidChange> changeList = new ArrayList<JRCapture.ApidChange>();
        changeList.addAll(changeSet);
        URLConnection.setContentHandlerFactory(new CaptureJsonUtils.JsonContentHandlerFactory());

        fireNextChange(changeList, listener);

        // add params to initinstance or something to init capture settings
        // plumb tokenURL to capture
        // plumb handler to construct this
        // add method for trad sign-in
    }

    private void fireNextChange(List<JRCapture.ApidChange> changeList, JRCapture.SyncListener listener) {
        if (changeList.size() == 0) {
            listener.onSuccess();
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
                fireNextChange(tail, listener);
            } else {
                listener.onFailure(content);
            }
        } catch (IOException e) {
            listener.onFailure(e);
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

    public Set<JRCapture.ApidChange> getApidChangeSet() throws JRCapture.InvalidApidChangeException {
        //CaptureJsonUtils.deepArraySort(this);
        return collapseApidChanges(CaptureJsonUtils.compileChangeSet(original, this));
    }
}
