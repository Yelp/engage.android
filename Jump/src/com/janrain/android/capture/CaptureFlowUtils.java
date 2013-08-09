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

import android.util.Pair;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.janrain.android.utils.CollectionUtils.Function;
import static com.janrain.android.utils.CollectionUtils.filter;
import static com.janrain.android.utils.LogUtils.throwDebugException;

public class CaptureFlowUtils {
    public static Set<Pair<String, String>> getFormFields(JSONObject newUser,
                                                          String formName,
                                                          Map<String, Object> captureFlow) {
        HashSet<Pair<String, String>> retval = new HashSet<Pair<String, String>>();
        if (formName == null || captureFlow == null) return null;
        Map form = (Map) ((Map) captureFlow.get("fields")).get(formName);
        final List fieldNames = (List) form.get("fields");
        Map fieldEntries = filter(((Map) captureFlow.get("fields")), new Function<Boolean, Map.Entry>() {
            public Boolean operate(Map.Entry arg) {
                return fieldNames.contains(arg.getKey());
            }
        });

        for (Object fieldEntry_ : fieldEntries.entrySet()) {
            Map.Entry fieldEntry = (Map.Entry) fieldEntry_;
            if (!(fieldEntry.getValue() instanceof Map)) {
                throwDebugException(new RuntimeException("unrecognized field defn: " +
                        fieldEntry.getValue()));
                continue;
            }

            String attrDothPath = (String) ((Map) fieldEntry.getValue()).get("schemaId");
            String formFieldValue = CaptureJsonUtils.valueForAttrByDotPath(newUser, attrDothPath);
            if (formFieldValue != null) {
                retval.add(new Pair<String, String>((String) fieldEntry.getKey(), formFieldValue));
            }
        }

        return retval;
    }

    public static String getFlowVersion(Map<String, Object> captureFlow) {
        Object version = captureFlow.get("version");
        if (version instanceof String) return (String) version;
        throwDebugException(new RuntimeException("Error parsing flow version: " + version));
        return null;
    }

    public static Map<String, Object> getFieldDefinition(Map<String, Object> flow, String fieldName) {
        return (Map<String, Object>) ((Map<String, Object>) flow.get("fields")).get(fieldName);
    }
}
