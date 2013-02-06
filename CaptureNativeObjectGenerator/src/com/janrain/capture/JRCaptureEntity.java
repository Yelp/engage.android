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

import com.janrain.capture.generator.Generator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;

public abstract class JRCaptureEntity {
    /**
     * Synchronize changes on this Capture entity or sub-entity with the Capture API daemon.
     * This both updates the server with local changes and updates the local data with the latest server
     * data.
     * @param listener A listener for success / failure callbacks. May be null.
     */
    public final void synchronize(JRCapture.SyncListener listener) {
    }

    /*package*/ static JRCaptureEntity inflate(JSONObject jo) {
        String entityTypeName = JRCaptureConfiguration.ENTITY_TYPE_NAME;
        String javaEntityTypeName = CaptureStringUtils.javaEntityTypeNameForCaptureAttrName(entityTypeName);
        return inflate(javaEntityTypeName, jo);
    }

    /*package*/ static JRCaptureEntity inflate(String javaTypeName, JSONObject jo) {
        try {
            Class c = Class.forName(Generator.GENERATED_OBJECT_PACKAGE + "." + javaTypeName);
            JRCaptureEntity retval = (JRCaptureEntity) c.newInstance();
            Iterator keys = jo.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object val = jo.get(key);

                Field f = c.getDeclaredField(key);
                f.setAccessible(true);
                if (JRCapturePassword.class.isAssignableFrom(f.getType())) {
                    // passwords are an edge case, they can either be raw strings or json objects
                    f.set(retval, f.getType().newInstance());
                    Field f_ = f.getType().getField("password");
                    f_.set(f.get(retval), val.toString());
                } else if (JSONObject.NULL.equals(val)) {
                    f.set(retval, null);
                } else if (val instanceof JSONObject) {
                    String typeName = CaptureStringUtils.javaEntityTypeNameForCaptureAttrName(key);
                    f.set(retval, inflate(typeName, (JSONObject) val));
                } else if (val instanceof JSONArray) {
                    JRCapturePlural plural = new JRCapturePlural();
                    f.set(retval, plural);
                    String depluralize = CaptureStringUtils.depluralize(key);
                    String typeName = CaptureStringUtils.javaEntityTypeNameForCaptureAttrName(depluralize);
                    JSONArray ja = (JSONArray) val;
                    for (int i = 0; i < ja.length(); i++) {
                        plural.add(inflate(typeName, (JSONObject) ja.get(i)));
                    }
                } else if (String.class.isAssignableFrom(f.getType())) {
                    f.set(retval, jo.getString(key));
                } else if (Boolean.class.isAssignableFrom(f.getType())) {
                    f.set(retval, jo.getBoolean(key));
                } else if (Double.class.isAssignableFrom(f.getType())) {
                    f.set(retval, jo.getDouble(key));
                } else if (Integer.class.isAssignableFrom(f.getType())) {
                    f.set(retval, jo.getInt(key));
                } else if (Long.class.isAssignableFrom(f.getType())) {
                    f.set(retval, jo.getLong(key));
                }
            }

            return retval;
        } catch (ClassNotFoundException e) {
            CaptureStringUtils.log("Class not found for: " + javaTypeName + " " + e.getLocalizedMessage());
        } catch (JSONException e) {
            CaptureStringUtils.log("Unexpected value not found for key: " + e.getLocalizedMessage());
        } catch (InstantiationException e) {
            CaptureStringUtils.log("Unexpected instantiation exception: " + e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            CaptureStringUtils.log("Unexpected illegal access exception: " + e.getLocalizedMessage());
        } catch (NoSuchFieldException e) {
            CaptureStringUtils.log("Unexpected no such field exception: " + e.getLocalizedMessage());
        }

        return null;
    }

    public String toString() {
        return toJsonString();
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Field[] fields = getClass().getDeclaredFields();
        try {
            boolean firstField = true;
            for (Field f : fields) {
                if (!firstField) sb.append(",");
                firstField = false;

                f.setAccessible(true);
                sb.append("\"").append(f.getName()).append("\":");
                Object value = f.get(this);
                if (value == null) {
                    sb.append("null");
                } else if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else if (value instanceof JRCapturePlural) {
                    JRCapturePlural p = (JRCapturePlural) value;
                    sb.append("[");
                    boolean firstElement = true;
                    for (Object e : p) {
                        if (!firstElement) sb.append(",");
                        firstElement = false;
                        sb.append(e.toString());
                    }
                    sb.append("]");
                } else if (value instanceof JRCaptureEntity) {
                    sb.append(((JRCaptureEntity) value).toJsonString());
                } else {
                    // TODO not sure if all "primitives" will toString properly
                    // likely broken for Doubles which are NaN or infinities
                    sb.append(value.toString());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        sb.append("}");
        return sb.toString();
    }
}
