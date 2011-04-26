package com.janrain.android.quickshare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import com.janrain.android.engage.session.JRSessionData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Story implements Serializable {
    private static final String TAG = Story.class.getSimpleName();

    private String mTitle;
    private String mDate;
    private String mDescription;
    private String mPlainText;
    private String mLink;
    private ArrayList<String> mImageUrls;
    private transient BitmapDrawable mImage;

    private boolean mCurrentlyDownloading;

    public Story(String title, String date, String description,
                 String plainText, String link, ArrayList<String> imageUrls) {
        this.mTitle = title;
        this.mDate = date;
        this.mDescription = description;
        this.mPlainText = plainText;
        this.mLink = link;
        this.mImageUrls = imageUrls;

        if (mImageUrls != null)
            if (!mImageUrls.isEmpty())
                startDownloadImage(mImageUrls.get(0));
    }

    private void startDownloadImage(final String imageUrl) {

        synchronized (this) {
            if (mCurrentlyDownloading) return;
            else mCurrentlyDownloading = true;
        }

        new AsyncTask<Void, Void, Void>(){
            public Void doInBackground(Void... s) {
                Log.d(TAG, "[doInBackground] downloading image");
                
                try {
                    URL url = new URL(imageUrl);
                    InputStream is = url.openStream();

                    mImage = new BitmapDrawable(BitmapFactory.decodeStream(is));

                } catch (MalformedURLException e) {
                    Log.d(TAG, e.toString());
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                } catch (RuntimeException e) {
                    Log.d(TAG, e.toString());
                }

                mCurrentlyDownloading = false;
                return null;
            }

            protected void onPostExecute(Boolean loadSuccess) {
                Log.d(TAG, "[onPostExecute] image download onPostExecute, result: " +
                        (loadSuccess ? "succeeded" : "failed"));

//                if (loadSuccess)
//                    mListener.AsyncFeedReadSucceeded();
//                else
//                    mListener.AsyncFeedReadFailed();
            }
        }.execute();
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

    public ArrayList<String> getImageUrls() {
        return mImageUrls;
    }

    public BitmapDrawable getImage() {
        return mImage;
    }

}
