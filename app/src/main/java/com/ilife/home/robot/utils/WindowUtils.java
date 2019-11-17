package com.ilife.home.robot.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by chengjiaping on 2017/10/20.
 */

public class WindowUtils {
    public static void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}
