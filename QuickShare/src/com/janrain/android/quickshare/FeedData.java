/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 Copyright (c) 2011, Janrain, Inc.

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

 File:   FeedData.java
 Author: Lilli Szafranski - lilli@janrain.com
 Date:   April 22, 2011
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.janrain.android.quickshare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Config;
import android.util.Log;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.Archiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import static com.janrain.android.quickshare.QuickShareEnvironment.*;

public class FeedData implements JREngageDelegate {
    private static final String TAG = FeedData.class.getSimpleName();

    private final Uri FEED_URL = Uri.parse("http://www.janrain.com/feed/blogs");

    private static final String ARCHIVE_STORIES_ARRAY = "storiesArray";
    private static final String ARCHIVE_STORY_LINKS_HASH = "storyLinksHash";

    private static String ENGAGE_APP_ID = getAppId();
    private static String ENGAGE_TOKEN_URL = getTokenUrl();

    private static FeedData sInstance;

    private final HashSet<String> mStoryLinks;
    private final ArrayList<Story> mStories;

    private Story mCurrentStory;

    private JREngage mEngage;
    private Context mContext;

    private String mUrlToBeLoaded;


    public static FeedData getInstance(Context context) {
        if (sInstance != null) {
            if (Config.LOGD)
                Log.d(TAG, "[getInstance] returning existing instance");

            return sInstance;
        }

        /* testing git stuff */
        sInstance = new FeedData(context);

        if (Config.LOGD)
            Log.d(TAG, "[getInstance] returning new instance.");

        return sInstance;
    }

    private FeedData(Context context) {
        if (Config.LOGD)
            Log.d(TAG, "[ctor] creating instance");

        mContext = context;
        mEngage = JREngage.initInstance(context, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

        ArrayList<Story> stories = (ArrayList<Story>) Archiver.load(ARCHIVE_STORIES_ARRAY);
        if (stories == null)
            stories = new ArrayList<Story>();

        mStories = new ArrayList<Story>(stories);

        if (Config.LOGD)
            Log.d(TAG, "[ctor] loaded " + ((Integer)mStories.size()).toString() + " stories from disk");

        for (Story story : mStories)
            story.downloadImage();

        /* If the Story class changes, then the Archiver can't load the new stories, which is fine,
            They'll just get redownloaded/added, but we also have to clear the links hash, so that
            the new stories get added. */
        HashSet<String> links = (HashSet<String>) Archiver.load(ARCHIVE_STORY_LINKS_HASH);
        if (links == null || mStories.isEmpty())
            links = new HashSet<String>();

        mStoryLinks = links;
    }

    private void LOGD(String function, String message) {
        if (Config.LOGD && function != null && message != null)
            Log.d(TAG, "[" + function + "] " + message);
    }

    FeedReaderListener mListener;

    public void asyncLoadJanrainBlog(FeedReaderListener listener) {
        mListener = listener;
        
        new AsyncTask<Void, Void, Boolean>() {
            private ArrayList<String> imageUrls;

            protected Boolean doInBackground(Void... v) {
                LOGD("asyncLoadJanrainBlog", "loading blog");

                try {
                    LOGD("asyncLoadJanrainBlog", "opening blog stream");
                    URL u = (new URL(FEED_URL.toString()));
                    URLConnection uc = u.openConnection();
                    InputStream is = uc.getInputStream();
                    LOGD("asyncLoadJanrainBlog", "blog stream open");

                    LOGD("asyncLoadJanrainBlog", "instantiating blog factory");
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setCoalescing(true);
                    dbf.setValidating(false);
                    dbf.setNamespaceAware(false);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    LOGD("asyncLoadJanrainBlog", "blog factory instantiated");

                    /* The following parse call takes ten seconds on a fast phone.
                        XMLPullParser is said to be a faster way to go.
                        Sample code here: http://groups.google.com/group/android-developers/msg/ddc6a8e83963a6b5
                        Another thread: http://stackoverflow.com/questions/4958973/3rd-party-android-xml-parser */
                    LOGD("asyncLoadJanrainBlog", "parsing feed");
                    Document d = db.parse(is);
                    LOGD("asyncLoadJanrainBlog", "feed parsed");

                    Element rss = (Element) d.getElementsByTagName("rss").item(0);
                    Element channel = (Element) rss.getElementsByTagName("channel").item(0);

                    NodeList items = channel.getElementsByTagName("item");
                    int numItems = items.getLength();

                    LOGD("asyncLoadJanrainBlog", "walking " + ((Integer) numItems).toString() + " stories");

                    for (int i = 0; i < numItems; i++) {
                        Element item = (Element)items.item(i);

                        Element title = (Element) item.getElementsByTagName("title").item(0);
                        Element link = (Element) item.getElementsByTagName("link").item(0);
                        Element description = (Element) item.getElementsByTagName("description").item(0);
                        Element date = (Element) item.getElementsByTagName("pubDate").item(0);

                        String titleText = title.getFirstChild().getNodeValue();
                        String linkText = link.getFirstChild().getNodeValue();
                        String dateText = date.getFirstChild().getNodeValue();

                        LOGD("asyncLoadJanrainBlog", "adding story: " + titleText);

                        /* We need to concatenate all the children of the description element (which has
                            ~100s of TextElement children) in order to come up with the complete
                            description text */
                        String descriptionText = "";
                        NodeList nl = description.getChildNodes();
                        for (int x=0; x<nl.getLength(); x++) {
                            String nodeValue = nl.item(x).getNodeValue();
                            descriptionText += nodeValue;
                        }

                        imageUrls = new ArrayList<String>();

                        /* The description is in html, so we decode it to display it as plain text,
                            and while decoding it we yoink out a link to an image if there is one. */
                        String plainText = Html.fromHtml(descriptionText, new Html.ImageGetter() {
                            public Drawable getDrawable(String s) {
                                imageUrls.add(FEED_URL.getScheme() + "://" + FEED_URL.getHost() + s);
                                return null;
                            }
                        }, null).toString();

                        // Parse the blog dates, and then reformat them to be more readable.
                        // Example: 2011-04-25PDT04:30:00-:00
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddzzzhh:mm:ss'-:00'");
                        SimpleDateFormat moreReadableDate =
                                new SimpleDateFormat("EEE, MMMM d, yyyy h:mm aa");
                        try {
                            Date parsedDate = sdf.parse(dateText);
                            dateText = moreReadableDate.format(parsedDate);
                        } catch (ParseException e) {
                            //do nothing, leave the date as it was
                        }

                        Story story = new Story(titleText, dateText, descriptionText,
                                                plainText, linkText, imageUrls);

                        if (!addStoryOnlyIfNew(story))
                            break;
                    }
                    LOGD("asyncLoadJanrainBlog", "feed walked");

                    LOGD("asyncLoadJanrainBlog", "saving stories");
                    Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
                    Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
                    LOGD("asyncLoadJanrainBlog", "stories saved");

                    /* If there are no exceptions, then it was a success */
                    return true;
                }
                catch (MalformedURLException e) { LOGD("asyncLoadJanrainBlog", "MalformedURLException" + e.getLocalizedMessage()); }
                catch (IOException e) { LOGD("asyncLoadJanrainBlog", "IOException" + e.getLocalizedMessage()); }
                catch (ParserConfigurationException e) { LOGD("asyncLoadJanrainBlog", "ParserConfigurationException" + e.getLocalizedMessage()); }
                catch (SAXException e) { LOGD("asyncLoadJanrainBlog", "SAXException" + e.getLocalizedMessage()); }
                catch (NullPointerException e) { LOGD("asyncLoadJanrainBlog", "NullPointerException" + e.getLocalizedMessage()); }

                /* If there were exceptions, then it was a failure. */
                return false;
            }

            private String resizeImage(String nodeValue) {
                Log.d(TAG, "[resizeImage] :" + nodeValue);
                return nodeValue;
            }

            protected void onPostExecute(Boolean loadSuccess) {
                LOGD("onPostExecute", "blog loader onPostExecute, result: " +
                        (loadSuccess ? "succeeded" : "failed"));

                if (loadSuccess)
                    mListener.AsyncFeedReadSucceeded();
                else
                    mListener.AsyncFeedReadFailed();
            }
        }.execute();
    }

    private boolean addStoryOnlyIfNew(Story story) {
        if (mStoryLinks.contains(story.getLink()))
            return false;

        if (Config.LOGD)
            Log.d(TAG, "[addStoryOnlyIfNew] story hasn't been added");

        synchronized (mStories) {
            mStories.add(story);
            mStoryLinks.add(story.getLink());
        }

        return true;
    }

    public ArrayList<Story> getFeed() {
        /* Returning a copy of the mStories array list so that adding stories while the list
            is being used as the list adapter for the Feed Summary activity doesn't crash the app. */
        synchronized (mStories) {
            return new ArrayList<Story>(mStories);
        }
    }

    public void setCurrentStory(Story story) {
        mCurrentStory = story;
    }

    public Story getCurrentStory() {
        return mCurrentStory;
    }

    public void shareCurrentStory() {
        LOGD("shareCurrentStory", "sharing story");

        JRActivityObject activityObject =
                new JRActivityObject("shared an article from the Janrain Blog", mCurrentStory.getLink());

        activityObject.setTitle(mCurrentStory.getTitle());
        activityObject.setDescription(mCurrentStory.getPlainText());

        mEngage.showSocialPublishingDialogWithActivity(activityObject);
    }

    public void deleteAllStories() {
        synchronized (mStories) {
            mStories.clear();
            mStoryLinks.clear();
        }

        if (Config.LOGD)
            Log.d(TAG, "[deleteAllStories] " + ((Integer)mStories.size()).toString() + " stories remain");

        Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
        Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
    }

    public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidNotComplete() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, String tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidNotCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidCompletePublishing() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrlToBeLoaded() {
        return mUrlToBeLoaded;
    }

    public void setUrlToBeLoaded(String mUrlToBeLoaded) {
        this.mUrlToBeLoaded = mUrlToBeLoaded;
    }
}
