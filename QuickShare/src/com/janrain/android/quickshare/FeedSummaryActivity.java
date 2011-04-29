package com.janrain.android.quickshare;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: lillialexis
 * Date: 4/22/11
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class FeedSummaryActivity extends ListActivity implements View.OnClickListener, FeedReaderListener {
    private static final String TAG = FeedSummaryActivity.class.getSimpleName();

    private ArrayList<Story> mStories;
    private StoryAdapter mAdapter;
    private FeedData mFeedData;

    private Button mRefreshBlog;

    public FeedSummaryActivity() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_summary_listview);

        mRefreshBlog = (Button)findViewById(R.id.refresh_blog);
        mRefreshBlog.setOnClickListener(this);
        mRefreshBlog.setVisibility(View.GONE);


        mFeedData = FeedData.getInstance(this);

        mStories = new ArrayList<Story>();
        getUpdatedStoriesList();

        mAdapter = new StoryAdapter(this, R.layout.feed_summary_listview_row, mStories);
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

    @Override
    protected void onListItemClick(ListView l, View v, int pos, long id) {
        if (pos == 0) {
            ((TextView)v.findViewById(R.id.row_story_title)).setText("Loading new articles...");
            mFeedData.asyncLoadJanrainBlog(this);
        }
        else {
            Story story = mAdapter.getItem(pos);
            mFeedData.setCurrentStory(story);
            this.startActivity(new Intent(this, StoryDetailActivity.class));
        }
    }

    public void onClick(View view) {
        mRefreshBlog.setText("Loading new articles...");
        mFeedData.asyncLoadJanrainBlog(this);
    }

    public void AsyncFeedReadSucceeded() {
        mRefreshBlog.setText("Refresh");

        getUpdatedStoriesList();
        mAdapter.notifyDataSetChanged();
    }

    public void AsyncFeedReadFailed() {
        mRefreshBlog.setText("Refresh");

        getUpdatedStoriesList();
        mAdapter.notifyDataSetChanged();
    }

    private void getUpdatedStoriesList() {

        mStories.clear();
        mStories.addAll(mFeedData.getFeed());

        if (Config.LOGD)
            Log.d(TAG, "[getUpdatedStoriesList] " + ((Integer)mStories.size()).toString() + " stories remain");

        mStories.add(0, Story.dummyStory());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.story_managing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "[onOptionsItemSelected] here");
        switch (item.getItemId()) {
            case R.id.delete_all_stories:
                Log.d(TAG, "[onOptionsItemSelected] delete all stories option selected");
                mFeedData.deleteAllStories();
                getUpdatedStoriesList();

//                mAdapter = new StoryAdapter(this, R.layout.feed_summary_listview_row, mStories);
//                setListAdapter(mAdapter);
//                mAdapter.notifyDataSetInvalidated();

                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class StoryAdapter extends ArrayAdapter<Story> {
        private int mResourceId;

        public StoryAdapter(Context context, int resId, ArrayList<Story> items) {
            super(context, -1, items);

            mResourceId = resId;
        }

        private View getInflatedView() {
            Log.i(TAG, "[getView] with null or dummy convertView");
            LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return li.inflate(mResourceId, null);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getInflatedView();
            } else if (v.getTag().equals("DUMMY_ROW")) {
                v = getInflatedView();
            } else Log.i(TAG, "[getView] with non null convertView");

            TextView title = (TextView)v.findViewById(R.id.row_story_title);
            ImageView icon = (ImageView)v.findViewById(R.id.row_story_icon);
            TextView text = (TextView)v.findViewById(R.id.row_story_text);
            TextView date = (TextView)v.findViewById(R.id.row_story_date);

            Story story = getItem(position);

            /* This is the row that contains dummy (empty) story, really to be used as a "Refresh"
                button that is clickable/focusable like other listview rows, and looks nicer than
                a real button. */
            if (position == 0) {
                v.setTag("DUMMY_ROW");

                icon.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                date.setVisibility(View.GONE);

                title.setText("Refresh");
                title.setGravity(Gravity.CENTER_HORIZONTAL);
            }
            else {
                v.setTag("STORY_ROW");
                Log.d(TAG, "[getView] for row " + ((Integer) position).toString() + ": " + story.getTitle());

                title.setGravity(Gravity.LEFT);

//                icon.setVisibility(View.VISIBLE);
//                text.setVisibility(View.VISIBLE);
//                date.setVisibility(View.VISIBLE);

                title.setText(story.getTitle());
                icon.setImageBitmap(story.getImage());
                text.setText(story.getPlainText());
                date.setText(story.getDate());
            }

            return v;
        }
    }
}