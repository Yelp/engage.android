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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class JRCapture {
    public static JSONObject getEntity(int id) throws IOException, JSONException {
        URLConnection entityConn = new URL("https://" + JRCaptureConfiguration.CAPTURE_DOMAIN + "/entity?" +
                "type_name=" + JRCaptureConfiguration.ENTITY_TYPE_NAME +
                "&client_id=" + JRCaptureConfiguration.CLIENT_ID +
                "&client_secret=" + JRCaptureConfiguration.CLIENT_SECRET +
                "&id=" + id).openConnection();
        entityConn.connect();

        String response = CaptureStringUtils.readFully(entityConn.getInputStream());

        JSONObject jo = new JSONObject(new JSONTokener(response));
        if ("ok".equals(jo.optString("stat"))) {
            //CaptureStringUtils.log("response: " + response);
            return jo.getJSONObject("result");
        } else {
            throw new IOException("failed to get entity, bad JSON response: " + jo);
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        //JRCaptureEntity.inflate(getEntity(159)); // for entity type "user"
        JRCaptureRecord record = new JRCaptureRecord(getEntity(680));
        CaptureStringUtils.log(record.toString(2));

        record.put("email", "nathan+androidtest@janrain.com");

        //CaptureJsonUtils.deepArrayOrderRandomizer(record);

        record.shallowDiff();
    }

    public static interface SyncListener {
        public void onSuccess();

        public void onFailure();
    }
}
