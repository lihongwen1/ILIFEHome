package com.ilife.home.robot.ui;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.MotionEvent;

import com.ilife.home.robot.utils.MyLogger;

public class MyRecyclerView extends RecyclerView {
    //    private final String TAG = MyRecyclerView.class.getSimpleName();
    private final String TAG = "MyView";

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int downX = (int) e.getX();
                int downY = (int) e.getY();
                MyLogger.e(TAG, "onInterceptTouchEvent ACTION_DOWN  downX = " + downX + " downY = " + downY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
//        return super.onInterceptTouchEvent(e);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int downX = (int) e.getX();
                int downY = (int) e.getY();
                MyLogger.e(TAG, "onTouchEvent ACTION_DOWN  downX = " + downX + " downY = " + downY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return false;
    }
}
