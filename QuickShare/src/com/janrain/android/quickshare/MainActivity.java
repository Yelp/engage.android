package com.janrain.android.quickshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener, BlogLoadListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mBtnTestAuth;
    private boolean mBlogLoaded;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
        mBtnTestAuth.setOnClickListener(this);

        BlogData.getInstance().asyncLoadJanrainBlog(this);
    }

    public void onClick(View view) {
        if (mBlogLoaded)
            this.startActivity(new Intent(this, BlogListActivity.class));
        else
            BlogData.getInstance().asyncLoadJanrainBlog(this);
    }

    public void AsyncBlogLoadSucceeded() {
        mBlogLoaded = true;
        mBtnTestAuth.setText("Browse Janrain Blog");
    }

    public void AsyncBlogLoadFailed() {
        mBlogLoaded = false;
        mBtnTestAuth.setText("Reload Blog");
    }
}
