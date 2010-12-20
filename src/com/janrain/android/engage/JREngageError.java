/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 Copyright (c) 2010, Janrain, Inc.
 
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution. 
 * Neither the name of the Janrain, Inc. nor the names of its
 contributors may be used to endorse or promote products derived from this
 software without specific prior written permission.
 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
*/
package com.janrain.android.engage;

/**
 * Unified error reporting class based on java.lang.Exception.
 */
public class JREngageError extends Exception {

    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    public static final class ErrorType {
        public static final String NO_NETWORK_CONNECTION = "noNetwork";
        public static final String MINOR = "minor";
        public static final String MAJOR = "major";
        public static final String CONFIGURATION_FAILED = "configurationFailed";
        public static final String CONFIGURATION_INFORMATION_MISSING = "missingInformation";
        public static final String AUTHENTICATION_FAILED = "authenticationFailed";
        public static final String PUBLISH_FAILED = "publishFailed";
        public static final String PUBLISH_NEEDS_REAUTHENTICATION = "publishNeedsReauthentication";
        public static final String PUBLISH_INVALID_ACTIVITY = "publishInvalidActivity";
    }

    public static final class ConfigurationError {
        private static final int START = 100;

        public static final int URL_ERROR = START;
        public static final int DATA_PARSING_ERROR = START + 1;
        public static final int JSON_ERROR = START + 2;
        public static final int CONFIGURATION_INFORMATION_ERROR = START + 3;
        public static final int SESSION_DATA_FINISH_GET_PROVIDERS_ERROR = START + 4;
    }

    public static final class AuthenticationError {
        private static final int START = 200;

        public static final int CODE_AuthenticationFailedError = START;
    }

    public static final class SocialPublishingError {
        private static final int START = 300;

        public static final int Publish_Failed_Error = START;
        public static final int PUBLISH_ERROR_ACTIVITY_NIL = START + 1;
        public static final int PUBLISH_ERROR_MISSING_API_KEY = START + 2;
        public static final int PUBLISH_ERROR_INVALID_OAUTH_TOKEN = START + 3;
        public static final int PUBLISH_ERROR_DUPLICATE_TWITTER = START + 4;
        public static final int PUBLISH_ERROR_LINKEDIN_CHARACTER_EXCEEDED = START + 5;
    }

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    /* Unspecified/unknown error code. */
    public static final int CODE_UNKNOWN = 0;


	/* Serial version UID, added to keep compiler from complaining about not having one. */
	private static final long serialVersionUID = 1328615314195240659L;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private int mCode;
    private String mType;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public JREngageError(String message, int code, String type) {
        super(message);
        mCode = code;
        mType = type;
    }

    public JREngageError(String message, int code, String type, Throwable cause) {
        super(message, cause);
        mCode = code;
        mType = type;
    }

    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }




}
