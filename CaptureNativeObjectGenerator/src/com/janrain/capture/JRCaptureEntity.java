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
    public void synchronize(SyncListener listener) {

    }

    public static interface SyncListener {
        public void onSuccess();

        public void onFailure();
    }

    protected static JRCaptureEntity inflate(JSONObject jo) {
        return inflate(JRCapture.classNameFor(JRCaptureConfiguration.ENTITY_TYPE_NAME), jo);
    }

    protected static JRCaptureEntity inflate(String name, JSONObject jo) {
        try {
            Class c = Class.forName(Generator.GENERATED_OBJECT_PACKAGE + "." + JRCapture.classNameFor(name));
            JRCaptureEntity retval = (JRCaptureEntity) c.newInstance();
            Iterator keys = jo.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object val = jo.get(key);

                Field f = c.getDeclaredField(JRCapture.snakeToCamel(key));
                f.setAccessible(true);
                if (JSONObject.NULL.equals(val)) {
                    f.set(retval, null);
                } else if (val instanceof JSONObject) {
                    f.set(retval, inflate(key, (JSONObject) val));
                } else if (val instanceof JSONArray) {
                    JRCapturePlural plural = (JRCapturePlural) f.getType().newInstance();
                    f.set(retval, plural);
                    JSONArray ja = (JSONArray) val;
                    //for (int i = 0; i < ja.length(); i++) plural.add()
                } else {
                    f.set(retval, val);
                }
            }

            return retval;
        } catch (ClassNotFoundException e) {
            Generator.log("Class not found for: " + name);
        } catch (JSONException e) {
            Generator.log("Unexpected value not found for key: " + e);
        } catch (InstantiationException e) {
            Generator.log("Unexpected instantiation exception: " + e);
        } catch (IllegalAccessException e) {
            Generator.log("Unexpected illegal access exception: " + e);
        } catch (NoSuchFieldException e) {
            Generator.log("Unexpected no such field exception: " + e);
        }

        return null;
    }
}
