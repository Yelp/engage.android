package com.janrain.android.quickshare;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogDetailedViewActivity extends Activity implements View.OnClickListener {

    private static final String TAG = BlogSummaryListActivity.class.getSimpleName();

    private BlogData mBlogData;
    private BlogArticle mBlogArticle;

    private Button mShareBlog;
//    private TextView mTitle;
//    private TextView mDate;
//    private TextView mText;

    private WebView mWebview;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_detailed_view_layout);

        mShareBlog = (Button)findViewById(R.id.share_button);
        mShareBlog.setOnClickListener(this);

        mWebview = (WebView)findViewById(R.id.webview);

//        mTitle = (TextView)findViewById(R.id.title);
//        mDate = (TextView)findViewById(R.id.date);
//        mText = (TextView)findViewById(R.id.text);

        mBlogData = BlogData.getInstance(this);

        loadCurrentBlog();
    }

    public void loadCurrentBlog() {
        mBlogArticle = mBlogData.getCurrentBlogArticle();

        mWebview.loadDataWithBaseURL("http://www.janrain.com/blogs/",
                                            "<html><body>" +
                                            "<h1>" + mBlogArticle.getTitle() + "</h1><br />" +
                                            "<h2>" + mBlogArticle.getDate() + "</h2><br />" +
                                            "<div class='body'>" + mBlogArticle.getDescription() + "</div>" +
                                            "</body></html>",
                                            "text/html", "UTF-8", "");

//        mTitle.setText(mBlogArticle.getTitle());
//        mDate.setText(mBlogArticle.getDate());
//        mText.setText(mBlogArticle.getDescription());
    }

    public void onClick(View view) {
        mBlogData.shareCurrentBlogArticle();
    }
}