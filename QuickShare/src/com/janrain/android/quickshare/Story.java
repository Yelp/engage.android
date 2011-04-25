package com.janrain.android.quickshare;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Story implements Serializable {
    private String mTitle;
    private String mDate;
    private String mDescription;
    private String mPlainText;
    private String mLink;
    private String mImageUrl;

    public Story(String title, String date, String description,
                 String plainText, String link, String imageUrl) {
        this.mTitle = title;
        this.mDate = date;
        this.mDescription = description;
        this.mPlainText = plainText;
        this.mLink = link;
        this.mImageUrl = imageUrl;
    }

    public String getTitle() {
            return mTitle;
    }

    public String getDate() {
        return mDate;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getPlainText() {
        return mPlainText;
    }

    public String getLink() {
        return mLink;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
