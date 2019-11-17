package com.ilife.home.robot.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ilife.home.robot.utils.MyLogger;

public class KeyboardLinearLayout extends LinearLayout {
    private static final String TAG = KeyboardLinearLayout.class.getSimpleName();
    public static final byte KEYBOARD_STATE_SHOW = -3;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_INIT = -1;
    private boolean mHasInit;
    private boolean mHasKeybord;
    private int mHeight;
    private onKeyboaddsChangeListener mListener;

    public KeyboardLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardLinearLayout(Context context) {
        super(context);
    }

    /**
     * set keyboard state listener
     */
    public void setOnkbdStateListener(onKeyboaddsChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
            mHeight = b;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_INIT);
            }
        } else {
            mHeight = mHeight < b ? b : mHeight;
        }
        if (mHasInit && mHeight > b) {
            mHasKeybord = true;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_SHOW);
            }
            MyLogger.w(TAG, "show keyboard.......");
        }
        if (mHasInit && mHasKeybord && mHeight == b) {
            mHasKeybord = false;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_HIDE);
            }
            MyLogger.w(TAG, "hide keyboard.......");
        }
    }

    public interface onKeyboaddsChangeListener {
        public void onKeyBoardStateChange(int state);
    }
}

