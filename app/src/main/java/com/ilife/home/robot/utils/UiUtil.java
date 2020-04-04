package com.ilife.home.robot.utils;

import android.graphics.Color;

import com.ilife.home.robot.app.MyApplication;

public class UiUtil {
    public static int getDrawable(String srcName) {
        return MyApplication.getInstance().getResources().getIdentifier(srcName, "drawable", MyApplication.getInstance().getPackageName());
    }

    public static int getString(String srcName) {
        return MyApplication.getInstance().getResources().getIdentifier(srcName, "string", MyApplication.getInstance().getPackageName());
    }

    public static String getString(int srcId) {
        return MyApplication.getInstance().getResources().getString(srcId);
    }

    public static int getColor(int srcId) {
        return MyApplication.getInstance().getResources().getColor(srcId);
    }
    public static int getColorByARGB(String argb) {
        return Color.parseColor(argb);
    }

    public static int getColor(String srcName) {
        return MyApplication.getInstance().getResources().getIdentifier(srcName, "color", MyApplication.getInstance().getPackageName());
    }
}
