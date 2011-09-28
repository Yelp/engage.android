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

package com.janrain.android.engage.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

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
