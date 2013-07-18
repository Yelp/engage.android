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

import org.json.JSONObject;

import java.util.Map;

/**
 * http://developers.janrain.com/documentation/capture/restful_api/
 */
public class CaptureApiError {
    /**
     * Indicates a form field validation failure, as a result of form submission.
     * See also getLocalizedValidationErrorMessages()
     */
    public static final int FORM_VALIDATION_ERROR = 390;

    /**
     * Indicates an API response that could not be parsed
     */
    public static final CaptureApiError INVALID_API_RESPONSE = new CaptureApiError();

    /**
     *
     */
    public final int code;

    /**
     *
     */
    public final String error;

    /**
     *
     */
    public final String error_description;

    /**
     *
     */
    JSONObject raw_response;

    private CaptureApiError() {
        error = "INVALID_API_RESPONSE";
        code = -1;
        error_description = null;
    }

    public CaptureApiError(JSONObject response) {
        code = response.optInt("code");
        error = response.optString("error");
        error_description = response.optString("error_description");
        raw_response = response;
    }

    public boolean isInvalidApiResponse() {
        return this == INVALID_API_RESPONSE;
    }

    public String toString() {
        return "<CaptureApiError code: " + code + " error: " + error + " description: " + error_description
                + ">";
    }

    public boolean isInvalidPassword() {
        if (isInvalidApiResponse()) return false;
        if (error.equals("bad username/password combo")) return true; // legacy username/password endpoint
        return false;
    }

    public Map<String, String[]> getLocalizedValidationErrorMessages() {
        return (Map<String, String[]>) raw_response.opt("messages");
    }
}
