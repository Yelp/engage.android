/*
 *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Copyright (c) 2011, Janrain, Inc.
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

package com.janrain.android.quickshare;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Html;
import android.util.Config;
import android.util.Log;
import android.widget.Toast;
import com.google.android.filecache.FileResponseCache;
import com.google.android.imageloader.BitmapContentHandler;
import com.google.android.imageloader.ImageLoader;
import com.janrain.android.engage.JREngage;
import com.janrain.android.engage.JREngageDelegate;
import com.janrain.android.engage.JREngageError;
import com.janrain.android.engage.net.async.HttpResponseHeaders;
import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JRDictionary;
import com.janrain.android.engage.utils.AndroidUtils;
import com.janrain.android.engage.utils.Archiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.janrain.android.quickshare.QuickShareEnvironment.getAppId;
import static com.janrain.android.quickshare.QuickShareEnvironment.getTokenUrl;

public class FeedData {
    private static final String TAG = FeedData.class.getSimpleName();
    static {
        System.setProperty("http.keepAlive", "false");
    }

    private final Uri FEED_URL = Uri.parse("http://www.janrain.com/feed/blogs");

    private static final String ARCHIVE_STORIES_ARRAY = "storiesArray";
    private static final String ARCHIVE_STORY_LINKS_HASH = "storyLinksHash";

    private static String ENGAGE_APP_ID = getAppId();
    private static String ENGAGE_TOKEN_URL = getTokenUrl();

    private static FeedData sInstance;

    private HashSet<String> mStoryLinks;
    final private ArrayList<Story> mStories = new ArrayList<Story>();

    private JREngage mEngage;

    private ImageLoader mImageLoader;


    public static FeedData getInstance(Activity activity) {
        if (sInstance != null) {
            if (Config.LOGD) Log.d(TAG, "[getInstance] returning existing instance");

            return sInstance;
        }

        sInstance = new FeedData(activity);

        if (Config.LOGD) Log.d(TAG, "[getInstance] returning new instance.");

        return sInstance;
    }

    public static FeedData getInstance() {
        return sInstance;
    }

    public JREngage getJREngage() {
        return mEngage;
    }

    private static class JRFileResponseCache extends FileResponseCache {
        private Context mContext;
        public JRFileResponseCache(Context c) {
            mContext = c;
        }

        @Override
        protected File getFile(URI uri, String s, Map<String, List<String>> stringListMap, Object o) {
            try {
                File parent = new File(mContext.getCacheDir(), "filecache");
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(String.valueOf(uri).getBytes("UTF-8"));
                byte[] output = digest.digest();
                StringBuilder builder = new StringBuilder("jr_cache_");
                for (byte anOutput : output) builder.append(Integer.toHexString(0xFF & anOutput));
                builder.append(AndroidUtils.urlEncode(uri.toString()));
                return new File(parent, builder.toString());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private FeedData(final Activity activity) {
        if (Config.LOGD) Log.d(TAG, "[ctor] creating instance");

        FileResponseCache frc = new JRFileResponseCache(activity);
        JRFileResponseCache.setDefault(frc);
        ContentHandler bmch = JRFileResponseCache.capture(new BitmapContentHandler(), null);
        ContentHandler pfch = JRFileResponseCache.capture(JRFileResponseCache.sink(), null);
        mImageLoader = new ImageLoader(ImageLoader.DEFAULT_TASK_LIMIT, null, bmch, pfch,
                ImageLoader.DEFAULT_CACHE_SIZE, null);

        mEngage = JREngage.initInstance(activity, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, mJrEngageDelegate);

        /* If the Story class changes, then the Archiver can't load the new stories, which is fine,
            They'll just get re-downloaded/added, but we also have to clear the links hash, so that
            the new stories get added. */
        try {
            ArrayList<Story> loadedStories = Archiver.load(ARCHIVE_STORIES_ARRAY);
            mStories.clear();
            mStories.addAll(loadedStories);
            mStoryLinks = Archiver.load(ARCHIVE_STORY_LINKS_HASH);
            logd(TAG, "[ctor] loaded " + ((Integer) mStories.size()).toString() + " stories from disk");
        } catch (Archiver.LoadException e) {
            mStories.clear();
            mStoryLinks = new HashSet<String>();
            logd(TAG, "[ctor] stories reset");
        }
    }

    private void logd(String function, String message) {
        if (Config.LOGD && function != null && message != null) Log.d(TAG, "[" + function + "] " + message);
    }

    private void logd(String function, String message, Exception e) {
        if (Config.LOGD && function != null && message != null) {
            Log.d(TAG, "[" + function + "] " + message, e);
        }
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public interface FeedReaderListener {
        void asyncFeedReadSucceeded();

        void asyncFeedReadFailed();
    }

    FeedReaderListener mListener;

    @SuppressWarnings("unchecked")
    public void asyncLoadJanrainBlog(FeedReaderListener listener) {
        mListener = listener;

        new AsyncTask<Void, Void, Boolean>() {
            private ArrayList<String> imageUrls;

            protected Boolean doInBackground(Void... v) {
                logd("asyncLoadJanrainBlog", "loading blog");

                try {
                    logd("asyncLoadJanrainBlog", "opening blog stream");
                    URL u = (new URL(FEED_URL.toString()));
                    URLConnection uc = u.openConnection();
                    InputStream is = uc.getInputStream();
                    logd("asyncLoadJanrainBlog", "blog stream open");

                    logd("asyncLoadJanrainBlog", "instantiating blog factory");
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setCoalescing(true);
                    dbf.setValidating(false);
                    dbf.setNamespaceAware(false);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    logd("asyncLoadJanrainBlog", "blog factory instantiated");

                    /* The following parse call takes ten seconds on a fast phone.
                        XMLPullParser is said to be a faster way to go.
                        Sample code here: http://groups.google.com/group/android-developers/msg/ddc6a8e83963a6b5
                        Another thread: http://stackoverflow.com/questions/4958973/3rd-party-android-xml-parser */
                    logd("asyncLoadJanrainBlog", "parsing feed");

                    // Due to some weird ~half read connection problems this code
                    // is paranoid about input streams. It reads everything into a BIS to ensure that it's
                    // all available for db.parse
                    //BufferedInputStream bis = new BufferedInputStream(is);
                    //bis.mark(uc.getContentLength());
                    //byte[] buffer = new byte[1000];
                    // XXX this for loop just reads all the data into bis, it has no loop body
                    //for (int r=0; r != -1; r = bis.read(buffer));
                    //bis.reset();

                    Document d = db.parse(is);
                    logd("asyncLoadJanrainBlog", "feed parsed");

                    Element rss = (Element) d.getElementsByTagName("rss").item(0);
                    Element channel = (Element) rss.getElementsByTagName("channel").item(0);

                    NodeList items = channel.getElementsByTagName("item");
                    int numItems = items.getLength();

                    logd("asyncLoadJanrainBlog", "walking " + numItems + " stories");

                    for (int i = 0; i < numItems; i++) {
                        Element item = (Element) items.item(i);

                        Element title = (Element) item.getElementsByTagName("title").item(0);
                        Element link = (Element) item.getElementsByTagName("link").item(0);
                        Element description = (Element) item.getElementsByTagName("description").item(0);
                        Element date = (Element) item.getElementsByTagName("pubDate").item(0);
                        Element postedBy = (Element) item.getElementsByTagName("dc:creator").item(0);

                        String titleText = title.getFirstChild().getNodeValue();
                        String linkText = link.getFirstChild().getNodeValue();
                        String dateText = date.getFirstChild().getNodeValue();
                        String postedByText = postedBy.getFirstChild().getNodeValue();

                        logd("asyncLoadJanrainBlog", "adding story: " + titleText);

                        /* We need to concatenate all the children of the description element (which has
                            ~100s of TextElement children) in order to come up with the complete
                            description text */
                        String descriptionText = "";
                        NodeList nl = description.getChildNodes();
                        for (int x = 0; x < nl.getLength(); x++) {
                            String nodeValue = nl.item(x).getNodeValue();
                            descriptionText += nodeValue;
                        }

                        imageUrls = new ArrayList<String>();

                        /* The description is in HTML, so we decode it to display it as plain text,
                            and while decoding it we yoink out a link to an image if there is one. */
                        String plainText = Html.fromHtml(descriptionText, new Html.ImageGetter() {
                            public Drawable getDrawable(String s) {
                                imageUrls.add(FEED_URL.getScheme() + "://" + FEED_URL.getHost() + s);
                                return null;
                            }
                        }, new Html.TagHandler() {
                            public void handleTag(boolean opening, String tag, Editable output,
                                                  XMLReader xmlReader) {
                                if (tag.equalsIgnoreCase("style")) output.clear();
                            }
                        }).toString();

                        /* Remove Unicode object characters */
                        plainText = plainText.replaceAll("\ufffc", "");

                        // Parse the blog dates, and then reformat them to be more readable.
                        // Example: 2011-04-25PDT04:30:00-:00
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddzzzhh:mm:ss'-:00'");
                        SimpleDateFormat moreReadableDate =
                                new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm aa");
                        try {
                            Date parsedDate = sdf.parse(dateText);
                            dateText = moreReadableDate.format(parsedDate);
                        } catch (ParseException e) {
                            //do nothing, leaving the date as it was
                        }

                        Story story = new Story(titleText, dateText, postedByText, descriptionText,
                                plainText, linkText, imageUrls);

                        if (!addStoryOnlyIfNew(story, i)) break;
                    }
                    logd("asyncLoadJanrainBlog", "feed walked");

                    logd("asyncLoadJanrainBlog", "saving stories");
                    Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
                    Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
                    logd("asyncLoadJanrainBlog", "stories saved");

                    /* If there are no exceptions, then it was a success */
                    return true;
                } catch (MalformedURLException e) {
                    logd("asyncLoadJanrainBlog", "MalformedURLException", e);
                } catch (IOException e) {
                    logd("asyncLoadJanrainBlog", "IOException", e);
                } catch (ParserConfigurationException e) {
                    logd("asyncLoadJanrainBlog", "ParserConfigurationException", e);
                } catch (SAXException e) {
                    logd("asyncLoadJanrainBlog", "SAXException", e);
                } catch (NullPointerException e) {
                    logd("asyncLoadJanrainBlog", "NullPointerException", e);
                }

                /* If there were exceptions, then it was a failure. */
                return false;
            }

            protected void onPostExecute(Boolean loadSuccess) {
                logd("onPostExecute", "blog loader onPostExecute, result: " +
                        (loadSuccess ? "succeeded" : "failed"));

                if (loadSuccess) {
                    mListener.asyncFeedReadSucceeded();
                } else {
                    mListener.asyncFeedReadFailed();
                }
            }
        }.execute();
    }

    private boolean addStoryOnlyIfNew(Story story, int index) {
        if (mStoryLinks.contains(story.getLink()))
            return false;

        if (Config.LOGD)
            Log.d(TAG, "[addStoryOnlyIfNew] story hasn't been added");

        synchronized (mStories) {
            if (index <= mStories.size())
                mStories.add(index, story);
            else
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

    public void deleteAllStories() {
        synchronized (mStories) {
            mStories.clear();
            mStoryLinks.clear();
        }

        if (Config.LOGD) {
            Log.d(TAG, "[deleteAllStories] " + ((Integer) mStories.size()).toString() + " stories remain");
        }

        Archiver.save(ARCHIVE_STORIES_ARRAY, mStories);
        Archiver.save(ARCHIVE_STORY_LINKS_HASH, mStoryLinks);
    }

    JREngageDelegate mJrEngageDelegate = new JREngageDelegate() {
        public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
            String toastText;
            if (error.hasException()) {
                //noinspection ThrowableResultOfMethodCallIgnored
                toastText = error.getException().toString();
            } else {
                toastText = error.getMessage();
            }

            Toast.makeText(JREngage.getActivity(), toastText, Toast.LENGTH_LONG).show();
        }

        public void jrAuthenticationDidNotComplete() {
        }

        public void jrAuthenticationDidSucceedForUser(JRDictionary auth_info, String provider) {
        }

        public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
        }

        public void jrAuthenticationDidReachTokenUrl(String tokenUrl, HttpResponseHeaders response, String tokenUrlPayload, String provider) {
        }

        public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl, JREngageError error, String provider) {
        }

        public void jrSocialDidNotCompletePublishing() {
        }

        public void jrSocialDidCompletePublishing() {
        }

        public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
        }

        public void jrSocialPublishJRActivityDidFail(JRActivityObject activity, JREngageError error, String provider) {
        }
    };
}
