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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nathan
 * Date: 6/7/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class JRCapture {
    private static final List<String> DEPLURALIZATION_LIST =
            new ArrayList<String>(Arrays.asList(new String[]{
                    "accounts", "account", "profiles", "profile", "addresses", "address", "friends", "friend",
                    "photos", "photo", "emails", "email", "games", "game", "opponents", "opponent",
                    "organizations", "organization", "phoneNumbers", "phoneNumber", "securityQuestions",
                    "securityQuestion", "tags", "tag", "urls", "url", "relationships", "relationship",
                    "ims", "im", "mice", "mouse", "mices", "mouse"
            }));

    public static JRCaptureEntity getEntity() throws IOException, JSONException {
        URLConnection entityConn = new URL("https://" + JRCaptureConfiguration.CAPTURE_DOMAIN + "/entity?" +
                "type_name=" + JRCaptureConfiguration.ENTITY_TYPE_NAME +
                "&client_id=" + JRCaptureConfiguration.CLIENT_ID +
                "&client_secret=" + JRCaptureConfiguration.CLIENT_SECRET +
                "&id=159"
        ).openConnection();
        entityConn.connect();
        JSONObject jo = new JSONObject(new JSONTokener(entityConn.getInputStream()));
        if ("ok".equals(jo.optString("stat"))) {
            return JRCaptureEntity.inflate((JSONObject) jo.get("result"));
        } else {
            return null;
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        JRCaptureEntity e = getEntity();
    }

    public static String depluralize(String plural) {
        int i;
        if ((i = DEPLURALIZATION_LIST.indexOf(plural)) >= 0) return DEPLURALIZATION_LIST.get(i + 1);
        Generator.log("Couldn't depluralize: " + plural);
        return plural;
    }

    public static String classNameFor(String name) {
        return "JRCapture" + upcaseFirst(snakeToCamel(name));
    }

    public static String upcaseFirst(String camelName) {
        return camelName.substring(0, 1).toUpperCase() + camelName.substring(1);
    }

    public static String snakeToCamel(String snakeName) {
        String[] namePieces = snakeName.split("_");
        for (int i = 1; i < namePieces.length; i++) {
            namePieces[i] = namePieces[i].substring(0, 1).toUpperCase() + namePieces[i].substring(1);
        }
        String retval = "";
        for (String s : namePieces) retval += s;
        return retval;
    }
}
