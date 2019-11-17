package com.ilife.home.robot.utils;

import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Created by chengjiaping on 2018/3/2.
 */

public class LanguageUtils {


    public static String getDefaultLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage();
    }

    public static String getPreferedLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else locale = Locale.getDefault();

        return  locale.getLanguage() + "-" + locale.getCountry();
    }
}
