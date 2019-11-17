package com.ilife.home.robot.able;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.R;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class DeviceUtils {
    public static String getPhysicalDeviceId(Activity activity) {
        Intent intent = activity.getIntent();
        String physicalDeviceId = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                physicalDeviceId = bundle.getString("physicalDeviceId");
            }
        }
        return physicalDeviceId;
    }

    public static String getServiceName(String subdomain) {
        String serviceName = "";
        return serviceName;
    }

    public static String getRobotType(String subdomain) {
        String robotType = "X800";
        MyLogger.i("ROBOT_TYPE", "-------" + robotType + "------------");
        return robotType;
    }

    public static long getOwner(Activity activity) {
        Intent intent = activity.getIntent();
        long owner = 0;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                owner = bundle.getLong("owner");
            }
        }
        return owner;
    }

    public static long getDeviceId(Activity activity) {
        Intent intent = activity.getIntent();
        long deviceId = 0;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                deviceId = bundle.getLong("deviceId");
            }
        }
        return deviceId;
    }

    public static boolean getCanChange(Activity activity) {
        Intent intent = activity.getIntent();
        boolean canChange = false;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                canChange = bundle.getBoolean("canChange");
            }
        }
        return canChange;
    }


    public static String getDevName(Activity activity) {
        Intent intent = activity.getIntent();
        String devName = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                devName = bundle.getString("devName");
            }
        }
        return devName;
    }

    public static String getSubdomain(Activity activity) {
        Intent intent = activity.getIntent();
        String devName = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                devName = bundle.getString("subdomain");
            }
        }
        return devName;
    }


    /**
     * get the image resource id of recharging
     *
     * @param robotType
     * @return
     */
    public static int getRechargeImageSrc(String robotType, boolean isWhite) {
        int src;
        switch (robotType) {
            case Constants.X910:
                src = R.drawable.rechage_device_x910;
                break;
            case Constants.X900:
                src = R.drawable.rechage_device_x900;
                break;
            case Constants.A9:
            case Constants.A9s:
            case Constants.X800:
                if (isWhite) {
                    src = R.drawable.rechage_device_x800w;
                } else {
                    src = R.drawable.rechage_device_x800;
                }
                break;
            case Constants.A7:
            case Constants.X787:
                src = R.drawable.rechage_device_x787;
                break;
            case Constants.X785:
                src = R.drawable.rechage_device_x785;
                break;
            case Constants.A8s:
                src = R.drawable.rechage_device_a8s;
                break;
            case Constants.V85:
                src = R.drawable.rechage_device_v85;
                break;
            case Constants.V3x:
            case Constants.V5x:
                src = R.drawable.rechage_device_v5x;
                break;
            default:
                src = R.drawable.rechage_device_x800;
                break;

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
                strError = context.getString(R.string.adapter_error_xj);
                break;
            case 24:
                strError="光流组件异常";
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
                str = context.getString(R.string.map_aty_along);
            } else if (b == MsgCodeUtils.STATUE_POINT) {
                str = context.getString(R.string.map_aty_point);
            } else if (b == MsgCodeUtils.STATUE_PLANNING) {
                str = context.getString(R.string.map_aty_plan_mode);
            } else if (b == MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
                str = context.getString(R.string.map_aty_edit_mode);
            } else if (b == MsgCodeUtils.STATUE_RECHARGE) {
                str = context.getString(R.string.map_aty_recharge);
            } else if (b == MsgCodeUtils.STATUE_CHARGING) {
                str = context.getString(R.string.map_aty_charge);
            } else if (b == MsgCodeUtils.STATUE_REMOTE_CONTROL) {
                str = context.getString(R.string.map_aty_remote);
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
        return new String[]{"X800"};
    }


}
