package com.ilife.home.robot.able;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.R;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class DeviceUtils {
    public static String getErrorText(Context context, int code, String robotType) {
        String strError = "";
        switch (code) {
            case 0:
                strError = "";
                break;
            case 1:
                strError = context.getString(R.string.adapter_error_bxg);
                break;
            case 2:
                strError = context.getString(R.string.adapter_error_obs);
                break;
            case 3:
                strError = context.getString(R.string.adapter_error_yq);
                break;
            case 4:
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_td_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_td);
                }
                break;
            case 5:
                strError = context.getString(R.string.dev_error_xuankong);
                break;
            case 6:
                strError = context.getString(R.string.adapter_error_wxl);
                break;
            case 7:
                strError = context.getString(R.string.adapter_error_zbs);
                break;
            case 8:
                strError = context.getString(R.string.adapter_error_ybs);
                break;
            case 9:
                strError = context.getString(R.string.adapter_error_bs);
                break;
            case 10:
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_zbl_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_zbl);
                }
                break;
            case 11:
                if (robotType.equals(Constants.A9)) {
                    strError = context.getString(R.string.adapter_error_ybl_a9);
                } else {
                    strError = context.getString(R.string.adapter_error_ybl);
                }
                break;
            case 12:
//                if (robotType.equals(Constants.A9)) {
//                    strError = context.getString(R.string.adapter_error_gs_a9);
//                } else {
//                    strError = context.getString(R.string.adapter_error_gs);
//                }
                strError = context.getString(R.string.adapter_error_gs_a9);
                break;
            case 13:
                strError = context.getString(R.string.adapter_error_fs);
                break;
            case 14:
                strError = context.getString(R.string.adapter_error_sb);
                break;
            case 15:
                strError = context.getString(R.string.adapter_error_qb);
                break;
            case 16:
                strError = context.getString(R.string.adapter_error_ljx);
                break;
            case 17:
                strError = context.getString(R.string.adapter_error_sx);
                break;
            case 18:
                strError = context.getString(R.string.adapter_error_lw);
                break;
            case 19:
                strError = context.getString(R.string.adapter_error_dc);
                break;
            case 20:
                strError = context.getString(R.string.adapter_error_tly);
                break;
            case 21:
                strError = context.getString(R.string.adapter_error_ld);
                break;
            case 22:
                strError = context.getString(R.string.adapter_error_sxt);
                break;
            case 23:
                strError = context.getString(R.string.adapter_error_robot_sk);
                break;
            case 24:
                strError = context.getString(R.string.adapter_error_gl);
                break;
            case 0x25:
                strError = context.getString(R.string.adapter_error_qt);
                break;
            default:
                strError = context.getString(R.string.adapter_error_qt);
        }
        return strError;
    }


    public static String getStatusStr(Context context, int b, int errCode) {
        String str = "";

        if (errCode != 0) {
            str = context.getString(R.string.map_aty_exception);
        } else {
            if (b == MsgCodeUtils.STATUE_OFF_LINE) {
                str = context.getString(R.string.device_adapter_device_offline);
            } else if (b == MsgCodeUtils.STATUE_SLEEPING) {
                str = context.getString(R.string.map_aty_sleep);
            } else if (b == MsgCodeUtils.STATUE_WAIT) {
                str = context.getString(R.string.map_aty_standby_mode);
            } else if (b == MsgCodeUtils.STATUE_RANDOM) {
                str = context.getString(R.string.map_aty_random);
            } else if (b == MsgCodeUtils.STATUE_ALONG) {
                str = context.getString(R.string.map_aty_along_ing);
            } else if (b == MsgCodeUtils.STATUE_POINT) {
                str = context.getString(R.string.map_aty_point_ing);
            } else if (b == MsgCodeUtils.STATUE_PLANNING) {
                str = context.getString(R.string.map_aty_plan_mode);
            } else if (b == MsgCodeUtils.STATUE_VIRTUAL_EDIT || b == MsgCodeUtils.STATUE_CLEAN_AREA_EDIT) {
                str = context.getString(R.string.map_aty_edit_mode);
            } else if (b == MsgCodeUtils.STATUE_RECHARGE) {
                str = context.getString(R.string.map_aty_recharging);
            } else if (b == MsgCodeUtils.STATUE_CHARGING) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
                str = context.getString(R.string.map_aty_remote_ing);
            } else if (b == MsgCodeUtils.STATUE_CHARGING_) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == MsgCodeUtils.STATUE_PAUSE) {
                str = context.getString(R.string.map_aty_pause);
            } else if (b == MsgCodeUtils.STATUE_TEMPORARY_POINT) {
                str = context.getString(R.string.map_aty_temp_keypoint);
            } else if (b == MsgCodeUtils.STATUE_CLEAN_AREA) {//区域清扫
                str = context.getString(R.string.map_status_clean_area);
            }
            if (b == MsgCodeUtils.STATUE_CLEAN_ROOM) {//选房清扫
                str = context.getString(R.string.map_status_select_room);
            }
            if (b == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP) {//充电座休眠
                str = context.getString(R.string.map_status_select_room);
            }
            if (b == MsgCodeUtils.STATUE_CHARGING_ADAPTER_SLEEP) {//适配器充电休眠
                str = context.getString(R.string.map_status_select_room);
            }
        }
        return str;
    }

    public static String[] getSupportDevices() {
        String[] supportDevice;
        switch (BuildConfig.BRAND) {
            case Constants.BRAND_ILIFE:
                supportDevice = MyApplication.getInstance().getResources().getStringArray(R.array.array_device_type);
                break;
            case Constants.BRAND_ZACO:
                supportDevice = MyApplication.getInstance().getResources().getStringArray(R.array.array_zaco_device_type);
                break;
            default:
                supportDevice = MyApplication.getInstance().getResources().getStringArray(R.array.array_oversea_device_type);
                break;
        }
        return supportDevice;
    }


}
