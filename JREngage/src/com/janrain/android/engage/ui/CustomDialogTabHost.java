package com.janrain.android.engage.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 9/2/11
 * Time: 9:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomDialogTabHost extends TabHost {
    public CustomDialogTabHost(Context context) {
        super(context);
    }

    public CustomDialogTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wM = MeasureSpec.getMode(widthMeasureSpec);
        int hM = MeasureSpec.getMode(heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        switch (hM) {
            case MeasureSpec.AT_MOST:
                h = Math.min(540, h);
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                h = 540;
                hM = MeasureSpec.AT_MOST;
                break;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, hM);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
