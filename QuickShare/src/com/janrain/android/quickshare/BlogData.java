package com.janrain.android.quickshare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Config;
import android.util.Log;

import android.widget.ArrayAdapter;
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
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogData implements JREngageDelegate {
    private static final String TAG = BlogData.class.getSimpleName();

    private final Uri BLOGURL = Uri.parse("http://www.janrain.com/feed/blogs");

    private static final String ARCHIVE_BLOG_LIST_ARRAY = "blogListArray";
    private static final String ARCHIVE_BLOG_LINKS_HASH = "blogLinksHash";

    private static final String ENGAGE_APP_ID = "appcfamhnpkagijaeinl";
    private static final String ENGAGE_TOKEN_URL = "";


    private static BlogData sInstance;
    private HashSet<String> mBlogsLinks;
    private ArrayList<BlogArticle> mBlogs;
    private BlogArticle mCurrentBlog;

    private JREngage mEngage;
    private Context mContext;


    public static BlogData getInstance(Context context) {

        if (sInstance != null) {
            if (Config.LOGD) {
                Log.d(TAG, "[getInstance] returning existing instance.");
            }
            return sInstance;
        }

        sInstance = new BlogData(context);
        if (Config.LOGD) {
            Log.d(TAG, "[getInstance] returning new instance.");
        }

        return sInstance;
    }

    private BlogData(Context context) {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

        mContext = context;
        mEngage = JREngage.initInstance(context, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);

        //mBlogs = (ArrayList<BlogArticle>)Archiver.load(ARCHIVE_BLOG_LIST_ARRAY);
        //if (mBlogs == null)
            mBlogs = new ArrayList<BlogArticle>();

        //mBlogsLinks = (HashSet<String>) Archiver.load(ARCHIVE_BLOG_LINKS_HASH);
        //if (mBlogsLinks == null)
            mBlogsLinks = new HashSet<String>();

//        mBlogs = new ArrayList<BlogArticle>();
    }

    private void logd(String function, String message) {
        if (Config.LOGD && function != null && message != null)
            Log.d(TAG, "[" + function + "] " + message);
    }

    BlogLoadListener mListener;

    public void asyncLoadJanrainBlog(BlogLoadListener listener) {
        mListener = listener;
        
        new AsyncTask<Void, Void, Boolean>() {
            private String imageUrl;

            protected Boolean doInBackground(Void... v) {
                logd("asyncLoadBlog", "loading blog");

                try {
                    URL u = (new URL(BLOGURL.toString()));
                    URLConnection uc = u.openConnection();
                    InputStream is = uc.getInputStream();
                    logd("asyncLoadBlog", "blog stream open");

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setCoalescing(true);
                    dbf.setValidating(false);
                    dbf.setNamespaceAware(false);
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    logd("asyncLoadBlog", "blog factory instantiated");

                    //the following parse call takes ten seconds on a fast phone.
                    //XMLPullParser is said to be a faster way to go.
                    //sample code here: http://groups.google.com/group/android-developers/msg/ddc6a8e83963a6b5
                    //another thread: http://stackoverflow.com/questions/4958973/3rd-party-android-xml-parser
                    Document d = db.parse(is);
                    logd("asyncLoadBlog", "blog feed parsed");

                    Element rss = (Element) d.getElementsByTagName("rss").item(0);
                    Element channel = (Element) rss.getElementsByTagName("channel").item(0);

                    NodeList items = channel.getElementsByTagName("item");
                    int numItems = items.getLength();

                    logd("asyncLoadBlog", "walking " + ((Integer)numItems).toString() + " stories");

                    for (int i = 0; i < numItems; i++) {
                        Element item = (Element)items.item(i);

                        Element title = (Element) item.getElementsByTagName("title").item(0);
                        Element link = (Element) item.getElementsByTagName("link").item(0);
                        Element description = (Element) item.getElementsByTagName("description").item(0);
                        Element date = (Element) item.getElementsByTagName("pubDate").item(0);

                        String titleText = title.getFirstChild().getNodeValue();
                        String linkText = link.getFirstChild().getNodeValue();
                        String dateText = date.getFirstChild().getNodeValue();

                        logd("asyncLoadBlog", "adding story: " + titleText);

                        //need to concatenate all the children of the description element (which has
                        // ~100s of TextElement children) in order to come up with the complete
                        //description text
                        String descriptionText = "";
                        NodeList nl = description.getChildNodes();
                        for (int x=0; x<nl.getLength(); x++) {
                            descriptionText += nl.item(x).getNodeValue();
                        }

                        //the description is in html, so we decode it to display it as plain text
                        //and while decoding it we yoink out a link to an image if there is one.
                        String plainText = Html.fromHtml(descriptionText, new Html.ImageGetter() {
                            public Drawable getDrawable(String s) {
                                imageUrl = BLOGURL.getScheme() + "://" + BLOGURL.getHost() + s;
                                return null;
                            }
                        }, null).toString();

                        BlogArticle article = new BlogArticle(titleText, dateText, descriptionText,
                                                              plainText, linkText, imageUrl);

                        if (!addBlogOnlyIfNew(article))
                            break;
                    }
                    logd("asyncLoadBlog", "blog feed walked");

                    //Archiver.save(ARCHIVE_BLOG_LIST_ARRAY, mBlogs);
                    //Archiver.save(ARCHIVE_BLOG_LINKS_HASH, mBlogsLinks);

                    logd("asyncLoadBlog", "blogs saved");
                    //no exceptions -> success
                    return true;
                }
                catch (MalformedURLException e) { logd("asyncLoadBlog", "MalformedURLException" + e.getLocalizedMessage()); }
                catch (IOException e) { logd("asyncLoadBlog", "IOException" + e.getLocalizedMessage()); }
                catch (ParserConfigurationException e) { logd("asyncLoadBlog", "ParserConfigurationException" + e.getLocalizedMessage()); }
                catch (SAXException e) { logd("asyncLoadBlog", "SAXException" + e.getLocalizedMessage()); }
//                catch (NullPointerException e) { logd("asyncLoadBlog", "NullPointerException" + e.getLocalizedMessage()); }

                //exceptions -> failure
                return false;
            }

            protected void onPostExecute(Boolean loadSuccess) {
                Log.d(TAG, "blog loader onPostExecute, result: " + (loadSuccess ? "succeeded" : "failed"));
                if (loadSuccess)
                    mListener.AsyncBlogLoadSucceeded();
                else
                    mListener.AsyncBlogLoadFailed();
            }
        }.execute();
    }

    private boolean addBlogOnlyIfNew(BlogArticle blogArticle) {
        if (mBlogsLinks.contains(blogArticle.getLink()))
            return false;

        mBlogs.add(blogArticle);
        return true;
    }

    public ArrayList<BlogArticle> getBlogList() {
        return new ArrayList<BlogArticle>(mBlogs);
    }

    public void setCurrentBlogArticle(BlogArticle article) {
        mCurrentBlog = article;
    }

    public BlogArticle getCurrentBlogArticle() {
        return mCurrentBlog;
    }

    public void shareCurrentBlogArticle() {
        JRActivityObject activityObject =
                new JRActivityObject("shared an article from the Janrain Blog", mCurrentBlog.getLink());

        activityObject.setTitle(mCurrentBlog.getTitle());
        activityObject.setDescription(mCurrentBlog.getPlainText());

        mEngage.showSocialPublishingDialogWithActivity(activityObject);
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
}
