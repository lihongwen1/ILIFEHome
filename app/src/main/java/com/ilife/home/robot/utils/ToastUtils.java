package com.ilife.home.robot.utils;

import android.content.Context;

import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.toast.Toasty;

/**
 * Created by chenjiaping on 2017/7/4.
 */

public class ToastUtils {
    public static void showToast(Context context, String msg) {
        Toasty.normal(context, msg).show();
    }

    public static void showToast(String msg) {
        Toasty.normal(MyApplication.getInstance(), msg).show();
    }

    public static void showErrorToast(Context context, int code) {
        //默认显示连接超时
        String msg = context.getString(R.string.error_toast_timeout);
        switch (code) {
            case 1993:
                msg = context.getString(R.string.error_toast_timeout);
                break;
            case 1986:
                msg = context.getString(R.string.add_aty_no_wifi);
                break;
            case 3807:
                msg = context.getString(R.string.clock_aty_dev_offline);
                break;
        }
        showToast(msg);
    }
}
