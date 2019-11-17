package com.ilife.home.robot.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.ilife.home.robot.activity.SelectActivity_x;
import com.ilife.home.robot.utils.MyLogger;

public class MyRelativeLayout extends RelativeLayout {
    //    private final String TAG = MyRelativeLayout.class.getSimpleName();
    private final String TAG = "MyView";
    Rect mRect;
    Context mContext;
    int downX, downY;

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int downX = (int) ev.getX();
                int downY = (int) ev.getY();
                MyLogger.e(TAG, "onInterceptTouchEvent downX = " + downX + " downY = " + downY);
                if (mRect != null && mRect.contains(downX, downY)) {
                    MyLogger.e(TAG, "消费了事件");
                    return true;
                } else {
                    MyLogger.e(TAG, "没有消费事件");
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
                MyLogger.e(TAG, "onInterceptTouchEvent ACTION_CANCEL");
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                MyLogger.e(TAG, "ACTION_DOWN downX = " + downX + " downY = " + downY);
                break;
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                MyLogger.e(TAG, "ACTION_UP upX = " + upX + " upY = " + upY);
                if (mRect.contains(downX, downY) &&
                        mRect.contains(upX, upY)) {
                    Intent i = new Intent(mContext, SelectActivity_x.class);
                    mContext.startActivity(i);
                }
//                else {
//                    return super.onTouchEvent(event);
//                }
                break;
            case MotionEvent.ACTION_MOVE:
                return false;
        }
        return true;
    }

    public void setmRect(Rect rect) {
        mRect = rect;
    }
}
