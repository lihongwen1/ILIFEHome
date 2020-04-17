package com.ilife.home.robot.utils;

import android.content.Context;

import com.huawei.android.hms.agent.common.UIUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
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

    /**
     * 显示虚拟墙，禁区，地毯区等距离充电座，主机位置过近的提示
     *
     * @param thisType   虚拟墙，禁区 -1-virtual wall  0-global area 1-carpet area 2-sweep area 3-clean area
     * @param targetType 机器位置，充电座位置 0-robot position 1-charging port
     */
    public static void showVirFbdCloseTip(int thisType, int targetType) {
        String value = "";
        switch (thisType) {
            case -1:
                switch (targetType) {
                    case 0:
                        value = UiUtil.getString(R.string.toast_vir_too_close_to_robot);
                        break;
                    case 1:
                        value =UiUtil.getString(R.string.toast_vir_too_close_to_charging);
                        break;
                }
                break;
            case 0:
                switch (targetType) {
                    case 0:
                        value = UiUtil.getString(R.string.toast_fbd_too_close_to_robot);
                        break;
                    case 1:
                        value =UiUtil.getString(R.string.toast_fbd_too_close_to_charging);
                        break;
                }
                break;
            case 1:

                switch (targetType) {
                    case 0:
                        value = UiUtil.getString(R.string.toast_carpet_too_close_to_robot);
                        break;
                    case 1:
                        value =UiUtil.getString(R.string.toast_carpet_too_close_to_charging);
                        break;
                }
                break;

        }
        if (!value.isEmpty()){
            showToast(value);
        }
    }
}
