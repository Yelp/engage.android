package com.janrain.android.engage.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: nathan
 * Date: 8/30/11
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoBlankingRelativeLayout extends RelativeLayout {
    public AutoBlankingRelativeLayout(Context context) {
        super(context);
    }

    public AutoBlankingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoBlankingRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //@Override
    //protected void onDraw(Canvas canvas) {
    //    int h = getMeasuredHeight();
    //    int maxChildH = h;
    //
    //    for (int x = 0, y = getChildCount(); x < y; x++) {
    //        maxChildH = Math.max(maxChildH, getChildAt(x).getMeasuredHeight());
    //    }
    //
    //    if (h >= maxChildH) {
    //        //super.onDraw(canvas);
    //    } else {
    //        //draw();
    //        getBackground().draw(canvas);
    //    }
    //}

    @Override
    public void draw(Canvas canvas) {
        int h = getMeasuredHeight();
        int maxChildH = h;

        // Measure the biggest child's height
        for (int x = 0, y = getChildCount(); x < y; x++) {
            View v = getChildAt(x);
            v.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                    MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            maxChildH = Math.max(maxChildH, v.getMeasuredHeight());
        }

        // If this layout is big enough for the biggest child's height, then draw, otherwise no draw.
        if (h >= maxChildH) {
            super.draw(canvas);
        }
    }

    //@Override
    //protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //    int oldW = getMeasuredWidth();
    //    int oldH = getMeasuredHeight();
    //
    //    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //
    //    if ((oldW < getMeasuredWidth()) || (oldH < getMeasuredHeight())) {
    //        //getParent().
    //        invalidate();
    //    }
    //}

    //@Override
    //protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //    int width = MeasureSpec.getSize(widthMeasureSpec);
    //    int height = MeasureSpec.getSize(heightMeasureSpec);
    //
    //    int wMode = MeasureSpec.getMode(widthMeasureSpec);
    //    int hMode = MeasureSpec.getMode(heightMeasureSpec);
    //
    //    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //
    //    List<View> views = new ArrayList<View>();
    //    views.add(this);
    //    // What's happening is that this object is being measured a bunch, the last two times
    //    // with height of ~14, but it's children measure what they want to be, not 14
    //
    //    while (!views.isEmpty()) {
    //        View v = views.remove(0);
    //        if (v instanceof ViewGroup) {
    //            ViewGroup v_ = (ViewGroup) v;
    //            for (int x = 0, y = v_.getChildCount(); x < y; x++) views.add(v_.getChildAt(x));
    //        }
    //
    //        int w_ = v.getMeasuredWidthAndState();
    //        int h_ = v.getMeasuredHeightAndState();
    //
    //        if ((h_ & View.MEASURED_STATE_TOO_SMALL) > 0) {
    //            new Object();
    //        }
    //    }
    //
    //    //super.onMeasure(MeasureSpec.makeMeasureSpec(width, 0), MeasureSpec.makeMeasureSpec(height, 0));
    //
    //    Log.d("AutoBlankingRelativeLayout", "" + width + " " + height + " " + wMode + " " + hMode + " " +
    //            getMeasuredWidth() + " " + getMeasuredHeight());
    //}
}
