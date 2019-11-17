package com.ilife.home.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.barteksc.pdfviewer.PDFView;

/**
 * this can response to the click event
 */
public class TouchablePDF extends PDFView {
    private OnActionUp onActionUp;
    /**
     * Construct the initial view
     *
     * @param context
     * @param set
     */
    public TouchablePDF(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction()==MotionEvent.ACTION_UP&&onActionUp!=null){
            onActionUp.actionUp();
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN && onActionUp != null) {
            onActionUp.actionDown();
        }
        return super.dispatchTouchEvent(ev);
    }
    public void setOnActionUpLister(OnActionUp onActionUp){
        this.onActionUp=onActionUp;
    }
    public interface OnActionUp{
        void actionUp();
        void actionDown();
    }
}
