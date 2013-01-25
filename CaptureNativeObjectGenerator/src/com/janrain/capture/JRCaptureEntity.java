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
    protected Long id;

    /**
     * Synchronize changes on this Capture entity or sub-entity with the Capture API daemon.
     * This both updates the server with local changes and updates the local data with the latest server
     * data.
     * @param listener A listener for success / failure callbacks. May be null.
     */
    public final void synchronize(SyncListener listener) {
    }

    public static interface SyncListener {
        public void onSuccess();

        public void onFailure();
    }

    /*package*/ static JRCaptureEntity inflate(JSONObject jo) {
        return inflate(JRCaptureConfiguration.ENTITY_TYPE_NAME, jo);
    }

    /*package*/ static JRCaptureEntity inflate(String entityTypeName, JSONObject jo) {
        try {
            Class c = Class.forName(Generator.GENERATED_OBJECT_PACKAGE + "." +
                    CaptureStringUtils.classNameFor(entityTypeName));
            JRCaptureEntity retval = (JRCaptureEntity) c.newInstance();
            Iterator keys = jo.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object val = jo.get(key);

                Field f = recursiveGetDeclaredField(c, key);
                f.setAccessible(true);
                if (JRCapturePassword.class.isAssignableFrom(f.getType())) {
                    // passwords are an edge case, they can either be raw strings or json objects
                    f.set(retval, f.getType().newInstance());
                    Field f_ = f.getType().getField("password");
                    f_.set(f.get(retval), val.toString());
                } else if (JSONObject.NULL.equals(val)) {
                    f.set(retval, null);
                } else if (val instanceof JSONObject) {
                    f.set(retval, inflate(key, (JSONObject) val));
                } else if (val instanceof JSONArray) {
                    JRCapturePlural plural = (JRCapturePlural) f.getType().newInstance();
                    f.set(retval, plural);
                    JSONArray ja = (JSONArray) val;
                    for (int i = 0; i < ja.length(); i++) {
                        plural.add(inflate(CaptureStringUtils.depluralize(key), (JSONObject) ja.get(i)));
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
            CaptureStringUtils.log("Class not found for: " + entityTypeName + " " + e.getLocalizedMessage());
        } catch (JSONException e) {
            CaptureStringUtils.log("Unexpected value not found for key: " + e);
        } catch (InstantiationException e) {
            CaptureStringUtils.log("Unexpected instantiation exception: " + e);
        } catch (IllegalAccessException e) {
            CaptureStringUtils.log("Unexpected illegal access exception: " + e);
        } catch (NoSuchFieldException e) {
            CaptureStringUtils.log("Unexpected no such field exception: " + e);
        }

        return null;
    }

    private static Field recursiveGetDeclaredField(Class c, String key) throws NoSuchFieldException {
        try {
            return c.getDeclaredField(CaptureStringUtils.snakeToCamel(key));
        } catch (NoSuchFieldException e) {
            Class c_ = c.getSuperclass();
            if (c_ != null) return recursiveGetDeclaredField(c_, key);
            throw e;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Field[] fields = getClass().getDeclaredFields();
        try {
            boolean firstField = true;
            for (Field f : fields) {
                if (!firstField) sb.append(",");
                firstField = false;

                f.setAccessible(true);
                sb.append("\"");
                sb.append(f.getName());
                sb.append("\":");
                Object value = f.get(this);
                if (value == null) {
                    sb.append("null");
                } else if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else if (JRCapturePlural.class.isAssignableFrom(f.getType())) {
                    JRCapturePlural p = (JRCapturePlural) value;
                    sb.append("[");
                    boolean firstElement = true;
                    for (Object e : p) {
                        if (!firstElement) sb.append(",");
                        sb.append(e.toString());
                        if (firstElement) firstElement = false;
                    }
                    sb.append("]");
                } else if (JRCaptureEntity.class.isAssignableFrom(f.getType())) {
                    sb.append(value.toString());
                } else {
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
