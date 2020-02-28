package com.ilife.home.robot.utils;

import com.ilife.home.robot.app.MyApplication;

public class UiUtil {
    public static int getDrawable(String srcName){
       return MyApplication.getInstance().getResources().getIdentifier(srcName,"drawable",MyApplication.getInstance().getPackageName());
    }

    public static int getString(String srcName) {
        return MyApplication.getInstance().getResources().getIdentifier(srcName, "string", MyApplication.getInstance().getPackageName());
    }
}
