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

/**
 * @brief Flash object to be included in a post to a user's stream.
 *
 * Create an flash media object, fill in the object's fields, and add the object to the
 * JRActivityObject#media array in your JRActivityObject.  How the flash videos get presented
 * and whether or not they are used, depend on the provider.
 *
 * Each video must contain a \e swfSrc url, which is the URL of the Flash object to be rendered,
 * and an \e imgSrc, which is the URL of an photo that should be displayed in place of the
 * flash object until the user clicks to prompt the flash object to play.  Flash object
 * has two optional fields, \e width and \e height, which can be used to override the
 * default choices when displaying the video in the provider's stream (e.g., Facebook's stream).
 * It also has two optional fields, \e expanded_width and \e expanded_height, to specify
 * the width and height of flash object will resize to, on the provider's stream,
 * once the user clicks on it.
 *
 * @note
 * You can only include one JRFlashMediaObject in the media array.  Any others
 * will be ignored.
 *
 * @sa Format and rules are identical to those described on the
 * <a href="http://developers.facebook.com/docs/guides/attachments">
 * Facebook Developer page on Attachments</a>.
 **/
public class JRFlashMediaObject extends JRMediaObject {

    /**
     * The URL of the Flash object to be rendered
     *
     * @par Getter/Setter:
     *      #getSwfSrc(), #setSwfSrc()
     **/
    private String mSwfSrc;

    /**
     * The URL of an photo that should be displayed in place of the flash object
     *
     * @par Getter/Setter:
     *      #getImgSrc(), #setImgSrc()
     **/
    private String mImgSrc;

    /**
     * Used to override the default width
     *
     * @par Getter/Setter:
     *      #getWidth(), #setWidth()
     **/
    private int mWidth;

    /**
     * Used to override the default height
     *
     * @par Getter/Setter:
     *      #getHeight(), #setHeight()
     **/
    private int mHeight;

    /**
     * Width the video will resize to once the user clicks it
     *
     * @par Getter/Setter:
     *      #getExpandedWidth(), #setExpandedWidth()
     **/
    private int mExpandedWidth;

    /**
     * Height the video will resize to once the user clicks it
     *
     * @par Getter/Setter:
     *      #getExpandedHeight(), #setExpandedHeight()
     **/
    private int mExpandedHeight;

    /**
     * @internal
     * Contains the downloaded preview of the image for display in the publish
     * activity dialog
     **/
    private Bitmap mPreview;

/**
 * @name Constructors
 **/
/*@{*/
    /**
     * Returns a JRFlashMediaObject initialized with the given swfSrc and imgSrc.
     *
     * @param swfSrc
     *   The URL of the Flash object to be rendered.  This value cannot be \c nil.
     *
     * @param imgSrc
     *   The URL of an photo that should be displayed in place of the flash object.  This value cannot be \c nil.
     *
     * @return
     *   A JRFlashMediaObject initialized with the given swfSrc and imgSrc.  If either
     *   \c swfSrc or \c imgSrc are \e nil, returns \c nil.
     *
     * @throws IllegalArgumentException
     *   if swfSrc or imgSrc is null
     **/
    public JRFlashMediaObject(String swfSrc, String imgSrc) {
        // TODO: if null/empty should we throw?
        if (swfSrc == null || imgSrc == null) throw new IllegalArgumentException("illegal null parameter");
        mSwfSrc = swfSrc;
        mImgSrc = imgSrc;
    }
/*@}*/

/**
 * @name Getters/Setters
 **/
/*@{*/
    /**
     * Getter for the flash object's #mSwfSrc property
     *
     * @return
     *      The URL of the Flash object to be rendered
     **/
    public String getSwfSrc() {  /* (readonly) */
        return mSwfSrc;
    }

    /**
     * Getter for the flash object's #mImgSrc property
     *
     * @return
     *      The URL of an photo that should be displayed in place of the flash object
     **/
    public String getImgSrc() {  /* (readonly) */
        return mImgSrc;
    }

    /**
     * Getter for the flash object's #mWidth property
     *
     * @return
     *      Used to override the default width
     **/
    public int getWidth() {
        return mWidth;
    }

    /**
     * Setter for the flash object's #mWidth property
     *
     * @param width
     *      Used to override the default width
     **/
    public void setWidth(int width) {
        mWidth = width;
    }

    /**
     * Getter for the flash object's #mHeight property
     *
     * @return
     *      Used to override the default height
     **/
    public int getHeight() {
        return mHeight;
    }

    /**
     * Setter for the flash object's #mHeight property
     *
     * @param height
     *      Used to override the default height
     **/
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * Getter for the flash object's #mExpandedWidth property
     *
     * @return
     *      Width the video will resize to once the user clicks it
     **/
    public int getExpandedWidth() {
        return mExpandedWidth;
    }

    /**
     * Setter for the flash object's #mExpandedWidth property
     *
     * @param expandedWidth
     *      Width the video will resize to once the user clicks it
     **/
    public void setExpandedWidth(int expandedWidth) {
        mExpandedWidth = expandedWidth;
    }

    /**
     * Getter for the flash object's #mExpandedHeight property
     *
     * @return
     *      Height the video will resize to once the user clicks it
     **/
    public int getExpandedHeight() {
        return mExpandedHeight;
    }

    /**
     * Setter for the flash object's #mExpandedHeight property
     *
     * @param expandedHeight
     *      Height the video will resize to once the user clicks it
     **/
    public void setExpandedHeight(int expandedHeight) {
        mExpandedHeight = expandedHeight;
    }
/*@}*/

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
        return mImgSrc;
    }

    public String getType() {
        return "flash";
    }
}
