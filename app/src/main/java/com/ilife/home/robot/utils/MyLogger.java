package com.ilife.home.robot.utils;

import com.ilife.home.robot.BuildConfig;
import com.orhanobut.logger.Logger;


public class MyLogger {

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Logger.i(tag+":"+msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Logger.e(tag+":"+msg);
        }
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Logger.v(tag+":"+ msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Logger.d(tag+":"+msg);
        }
    }
    public static void w(String tag,  Object... args) {
        if (BuildConfig.DEBUG) {
            Logger.w(tag+":"+ args);
        }
    }
}
