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

package com.janrain.capture.generator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.stringtemplate.v4.*;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.misc.STMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Generator {
    private static final String ENTITY_TYPE_NAME = "user_dev";
    private static final String CAPTURE_DOMAIN = "mobile.dev.janraincapture.com";
    private static final String CLIENT_ID = "zc7tx83fqy68mper69mxbt5dfvd7c2jh";
    private static final String CLIENT_SECRET = "aqkcrjcf8ceexc5gfvw47fpazjfysyct";
    private static final String GENERATED_OBJECT_PACKAGE = "com.janrain.capture.gen";
    private static final File OUT_DIRECTORY =
            new File("gen/" + join(GENERATED_OBJECT_PACKAGE.split("\\."), "/") + "/");
    private static final String ST_DIRECTORY = "CaptureNativeObjectGenerator/templates/";
    private static final STGroupDir ST_GROUP = new STGroupDir(ST_DIRECTORY);


    public static void main(String[] args) throws JSONException, IOException {
        URLConnection schemaConn = new URL("https://" + CAPTURE_DOMAIN + "/entityType?" +
                "type_name=" + ENTITY_TYPE_NAME +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET
        ).openConnection();
        schemaConn.connect();
        JSONObject jo = new JSONObject(new JSONTokener(schemaConn.getInputStream()));

        OUT_DIRECTORY.mkdirs();
        walkSchema(ENTITY_TYPE_NAME, jo.getJSONObject("schema"));
    }

    private static void walkSchema(String objectName, JSONObject jo)
            throws JSONException, IOException {
        log("Object: " + objectName);

        ST st = ST_GROUP.getInstanceOf("entity");
        st.add("package", GENERATED_OBJECT_PACKAGE);
        st.add("className", objectName);

        JSONArray attrDefs = jo.getJSONArray("attr_defs");

        for (int i = 0; i < attrDefs.length(); i++) {
            JSONObject attr = attrDefs.getJSONObject(i);
            // attr can have
            // description, name, type, case-sensitive, length, features, constraints, attr_defs
            String type = attr.getString("type");
            String name = attr.getString("name");
            String description = attr.optString("description");
            Boolean caseSensitive = attr.has("case-sensitive") ? attr.getBoolean("case-sensitive") : null;
            Integer length = attr.has("length") ?
                    attr.get("length").equals(JSONObject.NULL) ? null : (Integer) attr.get("length")
                    : null;
            Object features = attr.opt("features");
            // query
            Object constraints = attr.opt("constraints");
            // required, unicode-printable, unique, alphabetic, alphanumeric, unicode-letters, email-address
            // locally-unique

            CaptureStAttr stAttr = new CaptureStAttr(null, name, features, constraints, length,
                    caseSensitive, description);
            if (type.equals("boolean")) {
                stAttr.javaType = "Boolean";
            } else if (type.equals("date")) {
                stAttr.javaType = "String";
            } else if (type.equals("dateTime")) {
                stAttr.javaType = "String";
            } else if (type.equals("decimal")) {
                stAttr.javaType = "Number";
            } else if (type.equals("id")) {
                stAttr.javaType = "long";
            } else if (type.equals("integer")) {
                stAttr.javaType = "Integer";
            } else if (type.equals("ipAddress")) {
                stAttr.javaType = "String";
            } else if (type.equals("json")) {
                stAttr.javaType = "String";
            } else if (type.equals("object")) {
                stAttr.javaType = "JRCaptureEntity";
                walkSchema(name, attr);
            } else if (type.equals("password")) {
                stAttr.javaType = "String";
            } else if (type.equals("password")) {
                stAttr.javaType = "Boolean";
            } else if (type.equals("plural")) {
                stAttr.javaType = "JRPlural";
            } else if (type.equals("string")) {
                stAttr.javaType = "String";
            } else if (type.equals("uuid")) {
                stAttr.javaType = "String";
            }
            st.add("attrs", stAttr);
        }

        st.write(new File(OUT_DIRECTORY, objectName + ".java"), new STErrorListener() {
            public void compileTimeError(STMessage stMessage) {
                throw new RuntimeException(stMessage.toString());
            }

            public void runTimeError(STMessage stMessage) {
                throw new RuntimeException(stMessage.toString());
            }

            public void IOError(STMessage stMessage) {
                throw new RuntimeException(stMessage.toString());
            }

            public void internalError(STMessage stMessage) {
                throw new RuntimeException(stMessage.toString());
            }
        });
    }

    public static class CaptureStAttr {
        public String javaType;
        public String propertyName;
        public Object features;
        public Object constraints;
        public Integer length;
        public Boolean caseSensitive;
        public String description;

        CaptureStAttr(String javaType,
                      String propertyName,
                      Object features,
                      Object constraints,
                      Integer length,
                      Boolean caseSensitive,
                      String description) {
            this.javaType = javaType;
            this.propertyName = propertyName;
            this.features = features;
            this.constraints = constraints;
            this.length = length;
            this.caseSensitive = caseSensitive;
            this.description = description;
        }
    }

    private static void fosPrintln(FileOutputStream fos, String line) throws IOException {
        fos.write((line + "\n").getBytes());
    }

    private static void fosPrintln(FileOutputStream fos) throws IOException {
        fosPrintln(fos, "");
    }

    private static void log(Object o) {
        System.out.println(o);
        System.out.flush();
    }

    private static String jsonObjectKeysToString(JSONObject jo) {
        return join(asList(jo.keys()), ",");
    }

    private static List asList(Iterator iterator) {
        List l = new ArrayList();
        while (iterator.hasNext()) l.add(iterator.next());
        return l;
    }

    private static String join(String[] a, String seperator) {
        return join(Arrays.asList(a), seperator);
    }

    private static String join(List l, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object e : l) sb.append(e).append(separator);
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }
}
