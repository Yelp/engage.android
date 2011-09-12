package com.janrain.android.engage.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.janrain.android.engage.utils.AndroidUtils;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 9/2/11
 * Time: 9:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomMeasuringFrameLayout extends FrameLayout {
    private int mTargetHeight;
    private int mTargetWidth;

    public CustomMeasuringFrameLayout(Context context) {
        super(context);

        init(context);
    }

    public CustomMeasuringFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CustomMeasuringFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context c) {
        setTargetHeight(c.getResources().getConfiguration());
    }

    private void setTargetHeight(Configuration c) {
        mTargetHeight = (int) (AndroidUtils.scaleDipToPixels(c.screenHeightDp) * 0.71);
        mTargetWidth = (int) (AndroidUtils.scaleDipToPixels(c.screenWidthDp) * 0.71);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wM = MeasureSpec.getMode(widthMeasureSpec);
        int hM = MeasureSpec.getMode(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        switch (hM) {
            case MeasureSpec.AT_MOST:
                h = Math.min(mTargetHeight, h);
                hM = MeasureSpec.EXACTLY;
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                h = mTargetHeight;
                hM = MeasureSpec.EXACTLY;
                break;
        }

        switch (wM) {
            case MeasureSpec.AT_MOST:
                w = Math.min(mTargetWidth, h);
                wM = MeasureSpec.EXACTLY;
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                w = mTargetWidth;
                wM = MeasureSpec.EXACTLY;
                break;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, hM);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, wM);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setTargetHeight(newConfig);
    }
}
