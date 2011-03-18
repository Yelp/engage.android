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
 * TODO:DOC
 */
public interface JRSessionDelegate {

    /**
     * triggered when
     */
    void authenticationDidRestart();

    /**
     * triggered when
     */
    //For now this is only triggered by the calling application via JREngage.cancelAuthentication
    void authenticationDidCancel();

    // TODO: Wasn't this deprecated!?
    /**
     * triggered when
     */
    void authenticationDidComplete(String token, String provider);
    
    /**
     * triggered when
     */
    void authenticationDidComplete(JRDictionary profile, String provider);
    
    /**
     * triggered when
     */
    //For now this is only triggered by a network error loading the mobile endpoint URL from within the JRWebViewActivity
    //maybe it should also be triggered by http errors from inside the JRWebView
    void authenticationDidFail(JREngageError error, String provider);
    
    /**
     * triggered when
     */
    void authenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String payload, String provider);
    
    /**
     * triggered when
     */
    void authenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider);
    
    /**
     * triggered when
     */
    void publishingDidRestart();
    
    /**
     * triggered when
     */
    //For now this is only triggered by the calling application via JREngage.cancelPublishing
    void publishingDidCancel();
    
    /**
     * triggered when
     */
    //what's the different between publishDidComplete and publishingJRActivityDidSucceed?
    //nothing is triggering this
    void publishingDidComplete();
    
    /**
     * triggered when
     */
    //this is triggered by the connection started by JRSessionData.shareActivity completing
    //successfully, in the JRSessionData.processShareActivityResponse
    void publishingJRActivityDidSucceed(JRActivityObject activity, String provider);

    /**
     * triggered when
     */
    //this is triggered by the connection response handlers for share activity (either by a network
    //error or by an errorful response from RPX
    void publishingJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider);

    /**
     * triggered when the JRPublishActivity fails to load
     */
    void publishingDialogDidFail(JREngageError err);

    /**
     * triggered when JRSessionData has finished loading the mobile configuration
     */
    void mobileConfigDidFinish();
}

