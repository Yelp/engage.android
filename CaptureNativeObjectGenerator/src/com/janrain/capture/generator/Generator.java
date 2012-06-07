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
import org.stringtemplate.v4.misc.STMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Generator {
    private static final String ENTITY_TYPE_NAME = "user";
    private static final String CAPTURE_DOMAIN = "mobile.dev.janraincapture.com";
    private static final String CLIENT_ID = "zc7tx83fqy68mper69mxbt5dfvd7c2jh";
    private static final String CLIENT_SECRET = "aqkcrjcf8ceexc5gfvw47fpazjfysyct";
    private static final String GENERATED_OBJECT_PACKAGE = "com.janrain.capture.gen";
    private static final File OUT_DIRECTORY = new File("CaptureNativeObjectGenerator/out/" +
            join(GENERATED_OBJECT_PACKAGE.split("\\."), "/") + "/");
    private static final String ST_DIRECTORY = "CaptureNativeObjectGenerator/templates/";
    private static final STGroupFile ST_GROUP = new STGroupFile(ST_DIRECTORY + "entity.stg");
    private static final List<String> READONLY_ATTRIBUTES = new ArrayList<String>(Arrays.asList(new String[] {
            "id", "uuid", "created", "lastUpdated", "parent_id", "profiles"
    }));
    private static final List<String> DEPLURALIZATION_LIST =
            new ArrayList<String>(Arrays.asList(new String [] {
            "accounts", "account", "profiles", "profile", "addresses", "address", "friends", "friend",
                    "photos", "photo", "emails", "email", "games", "game", "opponents", "opponent",
                    "organizations", "organization", "phoneNumbers", "phoneNumber", "securityQuestions",
                    "securityQuestion", "tags", "tag", "urls", "url", "relationships", "relationship",
                    "ims", "im", "mice", "mouse"
    }));

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

    private static void walkSchema(String entityName, JSONObject jo)
            throws JSONException, IOException {
        log("Object: " + entityName);

        ST st = ST_GROUP.getInstanceOf("entity");
        st.add("package", GENERATED_OBJECT_PACKAGE);
        String className = "plural".equals(jo.optString("type")) ?
                classNameFor(depluralize(entityName))
                : classNameFor(entityName);
        st.add("className", className);
        JSONArray attrDefs = jo.getJSONArray("attr_defs");

        for (int i = 0; i < attrDefs.length(); i++) {
            JSONObject attr = attrDefs.getJSONObject(i);
            // attr can have
            // description, name, type, case-sensitive, length, features, constraints, attr_defs
            String attrType = attr.getString("type");
            String attrName = attr.getString("name");
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

            CaptureStAttr stAttr = new CaptureStAttr(null, attrName, attrType, features, constraints, length,
                    caseSensitive, description, READONLY_ATTRIBUTES.contains(attrName));
            if (attrName.equals("id")) stAttr.inheritedField = true;

            if (attrType.equals("boolean")) {
                stAttr.javaType = "Boolean";
            } else if (attrType.equals("date")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("dateTime")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("decimal")) {
                stAttr.javaType = "Number";
            } else if (attrType.equals("id")) {
                stAttr.javaType = "long";
            } else if (attrType.equals("integer")) {
                stAttr.javaType = "Integer";
            } else if (attrType.equals("ipAddress")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("json")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("object")) {
                stAttr.javaType = classNameFor(attrName);
                walkSchema(attrName, attr);
            } else if (attrType.equals("password")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("password")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("password-crypt-sha256")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("password-md5")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("password-bcrypt")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("plural")) {
                stAttr.javaType = "JRCapturePlural<" + classNameFor(depluralize(attrName)) + ">";
                walkSchema(attrName, attr);
            } else if (attrType.equals("string")) {
                stAttr.javaType = "String";
            } else if (attrType.equals("uuid")) {
                stAttr.javaType = "String";
            } else {
                log("unrecognized type: " + attrType);
                continue;
            }
            st.add("attrs", stAttr);
        }

        st.write(new File(OUT_DIRECTORY, className + ".java"), new STErrorListener() {
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
        public String captureFieldName;
        public String captureType;
        public Object features;
        public Object constraints;
        public Integer length;
        public Boolean caseSensitive;
        public String description;

        public boolean inheritedField = false;
        public boolean readOnly;
        public String javaFieldName;
        public String javaAccessorName; // e.g. javaFieldName with head up-cased

        CaptureStAttr(String javaType,
                      String captureFieldName,
                      String captureType,
                      Object features,
                      Object constraints,
                      Integer length,
                      Boolean caseSensitive,
                      String description,
                      boolean readOnly) {
            this.javaType = javaType;
            this.captureFieldName = captureFieldName;
            this.captureType = captureType;
            this.features = features;
            this.constraints = constraints;
            this.length = length;
            this.caseSensitive = caseSensitive;
            this.description = description;
            this.readOnly = readOnly;

            this.javaFieldName = snakeToCamel(captureFieldName);
            this.javaAccessorName = upcaseFirst(this.javaFieldName);
        }
    }

    private static String depluralize(String plural) {
        int i;
        if ((i = DEPLURALIZATION_LIST.indexOf(plural)) >= 0) return DEPLURALIZATION_LIST.get(i + 1);
        log("Couldn't depluralize: " + plural);
        return plural;
    }

    private static String classNameFor(String name) {
        return upcaseFirst(snakeToCamel(name));
    }

    private static String upcaseFirst(String camelName) {
        return camelName.substring(0, 1).toUpperCase() + camelName.substring(1);
    }

    private static String snakeToCamel(String snakeName) {
        String[] namePieces = snakeName.split("_");
        for (int i = 1; i < namePieces.length; i++) {
            namePieces[i] = namePieces[i].substring(0, 1).toUpperCase() + namePieces[i].substring(1);
        }
        String retval = "";
        for (String s : namePieces) retval += s;
        return retval;
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

    private static String join(String[] a, String separator) {
        return join(Arrays.asList(a), separator);
    }

    private static String join(List l, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object e : l) sb.append(e).append(separator);
        sb.delete(sb.length() - separator.length(), sb.length());
        return sb.toString();
    }
}
