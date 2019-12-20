package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ScrollView;

import com.ilife.home.robot.utils.MyLogger;

public class CustomScrollView extends ScrollView {
    private static final String TAG="CustomScrollView";
    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MyLogger.d(TAG,"dispatchTouchEvent 操作码："+ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        MyLogger.d(TAG,"onTouchEvent 操作码："+ev.getAction());
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MyLogger.d(TAG,"onInterceptTouchEvent 操作码："+ev.getAction());
        return super.onInterceptTouchEvent(ev);
    }
}
