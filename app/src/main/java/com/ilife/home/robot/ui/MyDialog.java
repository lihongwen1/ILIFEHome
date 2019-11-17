package com.ilife.home.robot.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chengjiaping on 2017/12/29.
 */

public class MyDialog extends Dialog {
    public MyDialog(int screenWidth, int screenHeight, Context context, int widthOff, int heightOff, View layout, int style) {
        super(context, style);

        setContentView(layout);

        Window window = getWindow();

        WindowManager.LayoutParams params = window.getAttributes();

        params.width = screenWidth - widthOff;

        if (heightOff != 0) {
            params.height = screenHeight - heightOff;
        }

        params.gravity = Gravity.CENTER;

        window.setAttributes(params);
    }
}
