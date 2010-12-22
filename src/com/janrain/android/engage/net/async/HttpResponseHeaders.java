package com.janrain.android.engage.net.async;

import android.util.Config;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * TODO: javadoc
 */
public class HttpResponseHeaders {

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // Tag used for logging
    private static final String TAG = HttpResponseHeaders.class.getSimpleName();

    // HTTP response header for last modified (UTC) value
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    // HTTP response header for ETag value
    public static final String HEADER_ETAG = "ETag";

    // Invalid response code (initial state of object)
    public static final int RESPONSE_CODE_INVALID = -1;

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    public static HttpResponseHeaders fromConnection(HttpURLConnection connection)
            throws IOException {

        if (Config.LOGD) {
            Log.d(TAG, "[fromConnection] BEGIN");
        }
        
        HttpResponseHeaders headers = new HttpResponseHeaders();
        
        headers.setResponseCode(connection.getResponseCode());
        headers.setContentEncoding(connection.getContentEncoding());
        headers.setContentLength(connection.getContentLength());
        headers.setContentType(connection.getContentType());
        headers.setDate(connection.getDate());
        headers.setLastModified(connection.getLastModified());
        headers.setLastModifiedUtc(connection.getHeaderField(HEADER_LAST_MODIFIED));
        headers.setETag(connection.getHeaderField(HEADER_ETAG));
        
        return headers;
    }

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private int mResponseCode;
    private String mContentEncoding;
    private int mContentLength;
    private String mContentType;
    private long mDate;
    private long mLastModified;
    private String mLastModifiedUtc;
    private String mETag;

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public HttpResponseHeaders() {
        mResponseCode = RESPONSE_CODE_INVALID;
    }
    
    // ------------------------------------------------------------------------
    // GETTERS/SETTERS
    // ------------------------------------------------------------------------

    public int getResponseCode() {
        return mResponseCode;
    }

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public String getContentEncoding() {
        return mContentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        mContentEncoding = contentEncoding;
    }

    public int getContentLength() {
        return mContentLength;
    }

    public void setContentLength(int contentLength) {
        mContentLength = contentLength;
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public long getLastModified() {
        return mLastModified;
    }

    public void setLastModified(long lastModified) {
        mLastModified = lastModified;
    }

    public String getLastModifiedUtc() {
        return mLastModifiedUtc;
    }

    public void setLastModifiedUtc(String lastModifiedUtc) {
        mLastModifiedUtc = lastModifiedUtc;
    }

    public String getETag() {
        return mETag;
    }

    public void setETag(String eTag) {
        mETag = eTag;
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

}
