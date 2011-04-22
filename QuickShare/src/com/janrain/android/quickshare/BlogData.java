package com.janrain.android.quickshare;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Config;
import android.util.Log;

import android.widget.ArrayAdapter;
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

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogData {
    private static final String TAG = BlogData.class.getSimpleName();

    private final Uri BLOGURL = Uri.parse("http://www.janrain.com/feed/blogs");

    private static BlogData sInstance;
    private ArrayList<BlogArticle> mBlogs;

    public static BlogData getInstance() {

        if (sInstance != null) {
            if (Config.LOGD) {
                Log.d(TAG, "[getInstance] returning existing instance.");
            }
            return sInstance;
        }

        sInstance = new BlogData();
        if (Config.LOGD) {
            Log.d(TAG, "[getInstance] returning new instance.");
        }

        return sInstance;
    }

    private BlogData() {
        if (Config.LOGD) {
            Log.d(TAG, "[ctor] creating instance.");
        }

        mBlogs = new ArrayList<BlogArticle>();
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

                    for (int i = 0; i < numItems; i++) {
                        Element item = (Element)items.item(0);

                        Element title = (Element) item.getElementsByTagName("title").item(0);
                        Element link = (Element) item.getElementsByTagName("link").item(0);
                        Element description = (Element) item.getElementsByTagName("description").item(0);

                        String titleText = title.getFirstChild().getNodeValue();
                        String linkText = link.getFirstChild().getNodeValue();

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

                        BlogArticle article = new BlogArticle(
                                titleText, descriptionText, plainText, linkText, imageUrl);

                        addBlog(article);
                    }
                    logd("asyncLoadBlog", "blog feed walked");

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
                Log.d(TAG, "blog loader onPostExecute");
                if (loadSuccess)
                    mListener.AsyncBlogLoadSucceeded();
                else
                    mListener.AsyncBlogLoadFailed();
            }
        }.execute();
    }

    private void addBlog(BlogArticle blogArticle) {
        mBlogs.add(blogArticle);
    }

    public ArrayList<BlogArticle> getBlogList() {
        return mBlogs;
    }
}
