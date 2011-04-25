package com.janrain.android.quickshare;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.janrain.android.engage.JREngage;
import sun.misc.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogSummaryListActivity extends ListActivity implements View.OnClickListener, BlogLoadListener {
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    private static final String TAG = BlogSummaryListActivity.class.getSimpleName();

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    private ArrayList<BlogArticle> mBlogList;
    private BlogListAdapter mAdapter;
    private BlogData mBlogData;

    private Button mReloadBlog;
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    public BlogSummaryListActivity() {
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------

    private String readAsset(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState
     *      If the activity is being re-initialized after previously being shut down then this
     *      Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *      Note: Otherwise it is null.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_summary_listview);

        mReloadBlog = (Button)findViewById(R.id.btn_refresh_blog);
        mReloadBlog.setOnClickListener(this);

        mBlogData = BlogData.getInstance(this);
        mBlogList = mBlogData.getBlogList();

        if (mBlogList == null) {
            mBlogList = new ArrayList<BlogArticle>();
        }

        mAdapter = new BlogListAdapter(this, R.layout.blog_summary_listview_row, mBlogList);
        setListAdapter(mAdapter);
    }

    public void onResume () {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * This method will be called when an item in the list is selected.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        BlogArticle article = mAdapter.getItem(pos);
        mBlogData.setCurrentBlogArticle(article);
        this.startActivity(new Intent(this, BlogDetailedViewActivity.class));
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.profile_managing_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (!mEditing)
//            menu.findItem(R.id.edit_profiles).setTitle("Edit Profiles");
//        else
//            menu.findItem(R.id.edit_profiles).setTitle("Done Editing");
//
        return true;
    }

    /**
    * This hook is called whenever an item in your options menu is selected.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.edit_profiles:
//                if (!mEditing) {
//                    mEditing = true;
//                    mAddProfile.setText("Done Editing");
//                    mAdapter.notifyDataSetChanged();
//                    return true;
//                }
//                else {
//                    mEditing = false;
//                    mAddProfile.setText("Add Another Profile");
//                    mAdapter.notifyDataSetChanged();
//                    return true;
//                }
//            case R.id.delete_all_profiles:
//                mEditing = false;
//                mAddProfile.setText("Add Another Profile");
//                mProfileData.deleteAllProfiles();
//                mAdapter.notifyDataSetChanged();
//                return true;
//            default:
                return super.onOptionsItemSelected(item);
//        }
    }

    public void onClick(View view) {
        mReloadBlog.setText("Loading new articles...");
        mBlogData.asyncLoadJanrainBlog(this);
    }

    public void AsyncBlogLoadSucceeded() {
        mReloadBlog.setText("Refresh");
        mAdapter.notifyDataSetChanged();
    }

    public void AsyncBlogLoadFailed() {
        mReloadBlog.setText("Refresh");
        mAdapter.notifyDataSetChanged();
    }


    /**
     * Array adapter used to render individual providers in list view.
     */
    private class BlogListAdapter extends ArrayAdapter<BlogArticle> {
        private int mResourceId;

        public BlogListAdapter(Context context, int resId, ArrayList<BlogArticle> items) {
            super(context, -1, items);

            mResourceId = resId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(mResourceId, null);
                Log.i(TAG, "[getView] with null converView");
            } else Log.i(TAG, "[getView] with non null convertView");

            ImageView icon = (ImageView)v.findViewById(R.id.row_profile_provider_icon);
            TextView title = (TextView)v.findViewById(R.id.row_profile_preferred_username_label);
            TextView date = (TextView)v.findViewById(R.id.row_profile_timestamp_label);

            BlogArticle article = getItem(position);

            Log.d(TAG, "[getView] for row " + ((Integer) position).toString() + ": " + article.getTitle());

            //icon.setImageDrawable(getProviderIconDrawable(getContext(), article.getProvider()));
            title.setText(article.getTitle());
            date.setText(article.getDate());

            return v;
        }
    }
}