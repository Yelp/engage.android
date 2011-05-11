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
package com.janrain.android.engage.session;

import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;

/**
 * @internal
 *
 * @interface JRSessionDelegate
 **/
public interface JRSessionDelegate {

    /**
     * Triggered when
     */
    void authenticationDidRestart();

    /**
     * Triggered when
     */
    //For now this is only triggered by the calling application via JREngage.cancelAuthentication
    void authenticationDidCancel();

    // TODO: Wasn't this deprecated!?
    /**
     * Triggered when
     */
    void authenticationDidComplete(String token, String provider);
    
    /**
     * Triggered when JRWebViewActivity receieves a success message from Engage.
     */
    void authenticationDidComplete(JRDictionary profile, String provider);
    
    /**
     * Triggered when the Engage authentication flow completes with an Engage error, or when there
     * is an error loading a URL during that authentication flow.
     */
    void authenticationDidFail(JREngageError error, String provider);
    
    /**
     * Triggered when
     */
    void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String payload, String provider);
    
    /**
     * triggered when
     */
    void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider);
    
    /**
     * Triggered when
     */
    //nothing calls this?
    //void publishingDidRestart();
    
    /**
     * Triggered when
     */
    //For now this is only triggered by the calling application via JREngage.cancelPublishing
    void publishingDidCancel();
    
    /**
     * Triggered when
     */
    //what's the different between publishDidComplete and publishingJRActivityDidSucceed?
    //nothing is triggering this
    void publishingDidComplete();
    
    /**
     * Triggered when a success response is received from Engage from the activity api
     */
    void publishingJRActivityDidSucceed(JRActivityObject activity, String provider);

    /**
     * Triggered when
     */
    //this is triggered by the connection response handlers for share activity (either by a network
    //error or by an errorful response from RPX
    void publishingJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider);

    /**
     * triggered when the JRPublishActivity fails to load
     */
    void publishingDialogDidFail(JREngageError err);

    /**
     * Triggered when JRSessionData has finished loading the mobile configuration
     */
    void mobileConfigDidFinish();

    public static abstract class SimpleJRSessionDelegate implements JRSessionDelegate {
        public void authenticationDidRestart() {}
        public void authenticationDidCancel() {}
        public void authenticationDidComplete(String token, String provider) {}
        public void authenticationDidComplete(JRDictionary profile, String provider) {}
        public void authenticationDidFail(JREngageError error, String provider) {}
        public void authenticationDidReachTokenUrl(String tokenUrl,
                                                   HttpResponseHeaders response,
                                                   String payload,
                                                   String provider) {}
        public void authenticationCallToTokenUrlDidFail(String tokenUrl,
                                                        JREngageError error,
                                                        String provider) {}
        public void publishingDidRestart() {}
        public void publishingDidCancel() {}
        public void publishingDidComplete() {}
        public void publishingJRActivityDidSucceed(JRActivityObject activity, String provider) {}
        public void publishingDialogDidFail(JREngageError error) {}
        public void publishingJRActivityDidFail(JRActivityObject activity,
                                                JREngageError error,
                                                String provider) {}
        public void mobileConfigDidFinish() {}
    }
}