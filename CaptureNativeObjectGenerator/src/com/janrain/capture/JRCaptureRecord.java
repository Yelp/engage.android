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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class JRCaptureRecord extends JSONObject {
    JSONObject original;

    public JRCaptureRecord(JSONObject jo) {
        super();

        try {
            original = new JSONObject(jo.toString());
            CaptureJsonUtils.deepArraySort(original);
            CaptureJsonUtils.deepCopy(original, this);
        } catch (JSONException e) {
            throw new RuntimeException("Unexpected JSONException", e);
        }
    }

    public final void synchronize(JRCapture.SyncListener listener)
            throws JRCapture.InvalidApidChangeException {
        Set<JRCapture.ApidChange> changeSet = getApidChangeSet();
        CaptureStringUtils.log(changeSet);
        Set<JRCapture.ApidChange> changeSet_ = collapseApidChanges(changeSet);
        CaptureStringUtils.log(changeSet_);
    }

    private Set<JRCapture.ApidChange> collapseApidChanges(Set<JRCapture.ApidChange> changeSet) {
        HashMap<String, Set<JRCapture.ApidUpdate>> subentityUpdateBuckets =
                new HashMap<String, Set<JRCapture.ApidUpdate>>();

        Set<JRCapture.ApidChange> changeSet_ = new HashSet<JRCapture.ApidChange>();
        for (JRCapture.ApidChange change : changeSet) {
            if (change instanceof JRCapture.ApidUpdate) {
                String parent = findClosestParentSubentity(change);
                JRCapture.ApidUpdate rewritten =
                        rewriteUpdateForParent((JRCapture.ApidUpdate) change, parent);
                Set<JRCapture.ApidUpdate> bucket = subentityUpdateBuckets.get(parent);
                if (bucket == null) {
                    bucket = new HashSet<JRCapture.ApidUpdate>();
                    subentityUpdateBuckets.put(parent, bucket);
                }
                bucket.add(rewritten);
            } else if (change instanceof JRCapture.ApidReplace) {
                changeSet_.add(change);
            } else if (change instanceof JRCapture.ApidDelete) {
                changeSet_.add(change);
            }
        }

        changeSet_.addAll(collapseApidUpdateBuckets(subentityUpdateBuckets));

        return changeSet_;
    }

    private Set<? extends JRCapture.ApidChange> collapseApidUpdateBuckets(
            Map<String, Set<JRCapture.ApidUpdate>> subentityUpdateBuckets) {
        Set<JRCapture.ApidChange> changeSet = new HashSet<JRCapture.ApidChange>();
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
                changeSet.add(collapsedUpdate);
            } else {
                throw new RuntimeException("Unexpected null collapsed update");
            }
        }

        return changeSet;
    }

    private JRCapture.ApidUpdate rewriteUpdateForParent(JRCapture.ApidUpdate update, String parent) {
        String subObjectPath = update.attrPath.replaceFirst(parent, "");
        String[] flattenedObjectPaths = subObjectPath.split("/");
        Object newVal = update.newVal;
        for (String s : flattenedObjectPaths) {
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

    private String findClosestParentSubentity(JRCapture.ApidChange change) {
        int n = change.attrPath.lastIndexOf("#");
        if (n == -1) return "/";
        String number = Pattern.compile("#([0-9])*").matcher(change.attrPath.substring(n)).group();
        return change.attrPath.substring(0, n) + number;
    }

    public Set<JRCapture.ApidChange> getApidChangeSet() throws JRCapture.InvalidApidChangeException {
        CaptureJsonUtils.deepArraySort(this);
        return CaptureJsonUtils.deepDiff(original, this);
    }
}
