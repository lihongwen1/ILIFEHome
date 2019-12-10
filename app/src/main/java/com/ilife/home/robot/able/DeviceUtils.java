package com.ilife.home.robot.able;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.R;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class DeviceUtils {


    public static String getServiceName(String subdomain) {
        String serviceName = "";
        return serviceName;
    }


    public static String getProductKeyByRobotType(String robotType) {
        String procutKey="";
        switch (robotType) {
            case Constants.X800:
                procutKey=EnvConfigure.PRODUCT_KEY_X800;
                break;
            case Constants.X800W:
                procutKey=EnvConfigure.PRODUCT_KEY_X800_W;
                break;
            case Constants.V3x:
                procutKey=EnvConfigure.PRODUCT_KEY_X320;
                break;
        }
        return procutKey;
    }

    public static String getRobotType(String productKey) {
        String robotType = "";
        switch (productKey) {
            case EnvConfigure.PRODUCT_KEY_X800:
                robotType = Constants.X800;
                break;
            case EnvConfigure.PRODUCT_KEY_X800_W:
                robotType = Constants.X800W;
                break;
            case EnvConfigure.PRODUCT_KEY_X320:
                robotType=Constants.V3x;
                break;
        }
        MyLogger.i("ROBOT_TYPE", "-------" + robotType + "------------");
        return robotType;
    }




    /**
     * get the image resource id of recharging
     *
     * @param robotType
     * @return
     */
    public static int getRobotPic(String robotType) {
        int src;
        switch (robotType) {
            case Constants.X800:
                src = R.drawable.n_x800;
                break;
            case Constants.X800W:
                src = R.drawable.n_x800_w;
                break;
            case Constants.V3x:
                src=R.drawable.n_v3x;
                break;
            default:
                src = R.drawable.n_x800;

        }
        return src;
    }


    public static String getErrorText(Context context, int code, String robotType) {
        String strError = "";
        switch (code) {
            case 0:
                strError = context.getString(R.string.adapter_error_no_error);
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
                strError = context.getString(R.string.adapter_error_gl);
                break;
            case 24:
                strError = "";
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
            } else if (b == MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
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
            }
        }
        return str;
    }

    public static String[] getSupportDevices() {
        return MyApplication.getInstance().getResources().getStringArray(R.array.array_device_type);
    }


}
