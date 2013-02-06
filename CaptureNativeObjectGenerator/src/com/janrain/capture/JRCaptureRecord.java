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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.janrain.capture.CaptureJsonUtils.makeSortedSetFromIterator;

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

    void shallowDiff() {
        CaptureJsonUtils.deepArraySort(this);

        SortedSet<String> origKeys = makeSortedSetFromIterator((Iterator<String>) original.keys());
        SortedSet<String> currentKeys = makeSortedSetFromIterator((Iterator<String>) keys());

        TreeSet<String> temp = new TreeSet<String>(currentKeys);
        temp.removeAll(origKeys);
        SortedSet<String> newKeys = temp;

        temp = new TreeSet<String>(origKeys);
        temp.removeAll(currentKeys);
        SortedSet<String> removedKeys = temp;

        temp = new TreeSet<String>(origKeys);
        temp.removeAll(removedKeys);

        SortedSet<String> intersection = temp;

        SortedSet<String> changedKeys = new TreeSet<String>();
        for (String k : intersection) {
            try {
                // todo, what if types mismatch between curVal and corresponding val from original?
                Object curVal = get(k);
                Object oldVal = original.get(k);

                if (curVal instanceof JSONObject) {
                    if (CaptureJsonUtils.jsonObjectCompareTo((JSONObject) curVal, oldVal) != 0) {
                        changedKeys.add(k);
                    }
                } else if (curVal instanceof JSONArray) {
                    if (CaptureJsonUtils.jsonArrayCompareTo((JSONArray) curVal, oldVal) != 0) {
                        changedKeys.add(k);
                    }
                } else if (!curVal.equals(oldVal)) {
                    // catches String, Boolean, Integer, Long, Double
                    changedKeys.add(k);
                }
            } catch (JSONException e) {
                throw new RuntimeException("Unexpected JSONException", e);
            }
        }

        CaptureStringUtils.log("newKeys: " + newKeys.toString() + " removedKeys: " + removedKeys.toString() +
                " changedKeys: " + changedKeys.toString());
    }

    public final void synchronize(JRCapture.SyncListener listener) {
    }
}
