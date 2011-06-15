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
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.janrain.android.engage.types;


import android.graphics.Bitmap;
import org.codehaus.jackson.annotate.JsonIgnore;


/**
 * @brief Image object to be included in a post to a user's stream.
 *
 * Create an image media object, fill in the object's fields, and add the object to the
 * JRActivityObject#mMedia array in your JRActivityObject.  How the images get presented
 * and whether or not they are used, depend on the provider.
 *
 * Each image must contain a \e src URL, which maps to the photo's URL, and an
 * \e href URL, which maps to the URL where a user should be taken if he or she clicks the photo.
 *
 * @sa Format and rules are identical to those described on the
 * <a href="http://developers.facebook.com/docs/guides/attachments">
 * Facebook Developer page on Attachments</a>.
 *
 * @nosubgrouping
 **/
public class JRImageMediaObject extends JRMediaObject {
/**
 * @name Private Attributes
 * The various properties of the image media object that you can access and configure through the
 * object's constructor, getters, and setters
 **/
/*@{*/
    /**
     * The photo's URL.
     *
     * @par Getter:
     *      #getSrc()
     **/
    private String mSrc;

    /**
     * The URL where a user should be taken if he or she clicks the photo.
     *
     * @par Getter:
     *      #getHref()
     **/
    private String mHref;

    /**
     * @internal
     * Contains the downloaded preview of the image for display in the publish activity dialog.
     **/
    private Bitmap mPreview;
/*@}*/

/**
 * @name Constructors
 * Constructor for JRImageMediaObject
 **/
/*@{*/
    /**
     * Returns an image media object initialized with the given src and href.
     *
     * @param src
     *   The photo's URL. This value cannot be null
     *
     * @param href
     *   The URL where a user should be taken if he or she clicks the photo. This value cannot be null
     *
     * @throws IllegalArgumentException
     *   if src or href is null
     **/
    public JRImageMediaObject(String src, String href) {
        // TODO: if null/empty should we throw?
        if (src == null || href == null) throw new IllegalArgumentException("illegal null src or href");
        mSrc = src;
        mHref = href;
    }
/*@}*/

/**
 * @name Getters/Setters
 * Getters and setters for the image media object's private properties
 **/
/*@{*/
    /**
     * Getter for the image object's #mSrc property.
     *
     * @return
     *      The photo's URL
     **/
    public String getSrc() {  /* (readonly) */
        return mSrc;
    }

    /**
     * Getter for the image object's #mHref property.
     *
     * @return
     *      The URL where a user should be taken if he or she clicks the photo
     **/
    public String getHref() {  /* (readonly) */
        return mHref;
    }
/*@}*/

    @JsonIgnore
    public Bitmap getPreview() {
        return mPreview;
    }

    public void setPreview(Bitmap preview) {
        mPreview = preview;
    }

    public boolean hasThumbnail() {
        return true;
    }

    public String getThumbnail() {
        return mSrc;
    }

    public String getType() {
        return "image";
    }

}
