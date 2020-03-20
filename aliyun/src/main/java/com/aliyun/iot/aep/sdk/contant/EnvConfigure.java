package com.aliyun.iot.aep.sdk.contant;

import android.app.Application;
import android.content.SharedPreferences;

import com.aliyun.iot.aep.sdk.framework.config.AConfigure;
import com.aliyun.iot.aep.sdk.threadpool.ThreadPool;

import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by wuwang on 2017/11/7.
 */
public class EnvConfigure {
    public static final String API_VER = "1.0.2";
    public static final String IOT_AUTH = "iotAuth";

    /**
     * 国内
     */
    static final public String PRODUCT_KEY_X800 = "a1nIeZXrVFg";//CHINA
    static final public String PRODUCT_KEY_X800_W = "a1cdxiwZfP9";//CHINA
    static final public String PRODUCT_KEY_X320 = "a1r76ksgLTV";//CHINA
    static final public String PRODUCT_KEY_X787="a1wBeDmFnJu";//CHINA
    static final public String PRODUCT_KEY_X434="a1JyoDFAJQT";//CHINA

    /**
     * 海外
     */
//    public static final String PRODUCT_KEY_X800 = "a2N5l6U3gnt";//OVERSEA


    /**
     * 区域/AREA
     */

    public static final int AREA_CHINA = 1;
    public static final int AREA_AMERICA = 2;
    public static final int AREA_SOUTH_EAST = 3;
    public static final int AREA_EUROPE = 4;


    /**
     * path
     */
    public static final String PATH_GET_PROPERTIES = "/thing/properties/get";
    public static final String PATH_GET_EVENT = "/thing/events/get";
    public static final String PATH_SET_PROPERTIES = "/thing/properties/set";
    public static final String PATH_SET_DEV_NICK_NAME = "/uc/setDeviceNickName";
    public static final String PATH_MODIFY_ACCOUNT = "/iotx/account/modifyAccount";
    public static final String PATH_GENE_SHARE_CODE = "/uc/generateShareQrCode";
    public static final String PATH_BIND_BY_SHARECODE = "/uc/scanBindByShareQrCode";
    public static final String PATH_UNREGISTER_ACCOUNT = "account/unregister";
    public static final String PATH_LIST_BINDING = "/uc/listBindingByAccount";
    public static final String PAHT_UNBIND_DEV = "/uc/unbindAccountAndDev";
    public static final String PATH_ADD_FEEDBACK = "/feedback/add";
    public static final String PATH_QUERY_ACCOUNT = "/iotx/account/queryIdentityList";
    public static final String PATH_BIND_DEVICE = "/awss/enrollee/user/bind";
    public static final String PATH_GET_PROPERTY_TIMELINE = "/thing/property/timeline/get";
    public static final String TOPIC = "path/of/topic";
    public static final String PATH_QUERY_OTA_VER = "/thing/ota/info/queryByUser";
    public static final String PATH_OTA_UPGRADE = "/thing/ota/batchUpgradeByUser";
    public static final String PATH_OTA_QUERY_UPGRADE_PROGRESS = "/thing/ota/progress/getByUser";
    public static final String PATH_REPORY_OTA_VER = "/thing/ota/version/reportByUser";
    public static final String PATH_BIND_SERVER_FILTER = "/awss/enrollee/product/filter";
    public static final String PATH_RESET_FACTORY = "/living/device/reset";
    /**
     * topic/method
     */
    public static final String METHOD_THING_PROP = "/thing/properties";
    public static final String METHOD_THING_EVENT = "/thing/events";

    /**
     * support country
     */
    public static final String CHINA = "china";
    public static final String SINGAPORE = "singapore";


    /**
     * 设备功能定义标识符 key
     */
    public static final String KEY_IOT_ID = "iotId";
    public static final String KEY_IOT_IDS = "iotIds";
    public static final String KEY_ITEMS = "items";
    public static final String KEY_ACCOUNT_META = "accountMetaV2";
    public static final String KEY_TAG = "tag";
    public static final String KEY_PATH = "key";
    public static final String KEY_POWER_SWITCH = "PowerSwitch";
    public static final String KEY_WORK_MODE = "WorkMode";
    public static final String KEY_MODE = "mode";
    public static final String KEY_BATTERY_STATE = "BatteryState";
    public static final String KEY_CLEAN_DIRECTION = "CleanDirection";
    public static final String KEY_MAX_MODE = "MaxMode";
    public static final String KEY_VOICE_OPNE = "VOICE_OPEN";
    public static final String KEY_WATER_CONTROL = "WaterTankContrl";
    public static final String KEY_SCHEDULE = "Schedule";
    public static final String KEY_SCHEDULE_ENABLE = "ScheduleEnable";
    public static final String KEY_SCHEDULE_HOUR = "ScheduleHour";
    public static final String KEY_SCHEDULE_MINUTES = "ScheduleMinutes";
    public static final String KEY_SCHEDULE_WEEK = "ScheduleWeek";
    public static final String KEY_SCHEDULE_AREA = "ScheduleArea";
    public static final String KEY_SCHEDULE_TYPE = "ScheduleType";
    public static final String KEY_VALUE = "value";
    public static final String KEY_DEV_NICKNAME = "nickName";
    public static final String KEY_IDENTITY_ID = "identityId";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_NICK_NAME = "nickName";
    public static final String KEY_APP_KEY = "appKey";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_IOT_ID_LIST = "iotIdList";
    public static final String KEY_QR_KEY = "qrKey";
    public static final String KEY_POSITION = "position";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_EXTRA = "extra";
    public static final String KEY_PARTS_STATUS = "PartsStatus";
    public static final String KEY_FILTER_LIFE = "FilterLife";
    public static final String KEY_SIDE_BRUSH_LIFE = "SideBrushLife";
    public static final String KEY_MAIN_BRUSH_LIFE = "MainBrushLife";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_ERRORCODE = "ErrorCode";
    public static final String KEY_EVENTBODY = "eventBody";
    public static final String KEY_USER_NICK = "_userNick";
    public static final String KEY_DEVICE_STR_LIST = "deviceStrList";
    public static final String KEY_IS_FAC_RESET = "isFacReset";
    public static final String KEY_TOKEN_INVALID = "onIoTTokenInvalid";
    public static final String KEY_REAL_TIME_MAP_START = "RealTimeMapStart";
    public static final String KEY_HISTORY_START_TIME = "CleanHistoryStartTime";
    public static final String KEY_IDENTIFIER = "identifier";
    public static final String KEY_REALTIMEMAP = "RealTimeMap";
    public static final String KEY_CLEAN_HISTORY = "CleanHistory";
    public static final String KEY_OTA_INFO = "OTAInfo";
    public static final String KEY_TIME = "time";
    public static String KEY_CLEAN_AREA = "CleanArea";
    public static String KEY_CLEAN_TIME = "CleanTime";
    public static final String KEY_BEEP_NO_DISTURB = "BeepNoDisturb";
    public static final String KEY_SAVE_MAP="SaveMap";
    public static final String KEY_SELECT_MAP_ID="SelectedMapId";
    public static final String KEY_FORBIDDEN_AREA="ForbiddenAreaData";
    public static final String VirtualWallData="VirtualWallData";
    public static final String KEY_SWITCH = "Switch";
    public static final String KEY_DATA = "data";
    public static final String KEY_START_TIME = "StartTime";
    public static final String KEY_TOTAL_TIME = "CleanTotalTime";
    public static final String KEY_TOTAL_AREA = "CleanTotalArea";
    public static final String KEY_CLEAN_MAP_DATA = "CleanMapData";
    public static final String KEY_VERSION = "version";
    /**
     * value
     */

    public static final int VALUE_GET_PROPERTY = 0;
    public static final int VALUE_WORK_MODE = 1;
    public static final int VALUE_SET_WATER = 2;
    public static final int VALUE_SET_MAX = 3;
    public static final int VALUE_SET_NORMAL = 4;

    public static final int VALUE_FIND_ROBOT = 5;
    public static final int VALUE_FAC_RESET = 6;
    public static final int VALUE_SET_PARTSTIME = 7;
    public static final int VALUE_GET_EVENT = 8;
    public static final int VALUE_SET_VOICE_SWITCH = 9;


    /**
     * ali
     */

    static final private String SHARED_PREFERENCES_NAME = "ENV_CONFIGURE";
    static private Application app = null;
    static final private HashSet<Listener> listeners = new HashSet<>();

    static public void init(Application app, HashSet<String> configuresWithinDB) {
        EnvConfigure.app = app;
        EnvConfigure.initConfiguresByConfigureDB(configuresWithinDB);
    }

    static public boolean hasEnvArg(String key) {
        return AConfigure.getInstance().getConfig().containsKey(key);
    }

    static public String getEnvArg(String key) {
        return (String) AConfigure.getInstance().getConfig().get(key);
    }

    static public String getEnvArg(String key, boolean fromDB) {
        String ret = null;

        do {
            if (false == fromDB) {
                ret = (String) AConfigure.getInstance().getConfig().get(key);
                break;
            }

            SharedPreferences preferences = app.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            if (null == preferences)
                break;

            if (preferences.contains(key))
                ret = preferences.getString(key, null);

            AConfigure.getInstance().getConfig().put(key, ret);
        } while (false);

        return ret;
    }

    static public void putEnvArg(String key, String value) {
        putEnvArg(key, value, false);
    }

    static public void putEnvArg(String key, String value, boolean saveToDB) {
        if (null == key || 0 >= key.length())
            return;

        String oldValue = null;
        synchronized (EnvConfigure.class) {
            oldValue = (String) AConfigure.getInstance().getConfig().get(key);
            AConfigure.getInstance().getConfig().put(key, value);
        }

        /* is there changed ? */
        boolean changed = false;
        if (null != oldValue && null != value) {
            changed = !oldValue.equals(value);
        } else if (null != oldValue && null == value) {
            changed = true;
        } else if (null == oldValue && null != value) {
            changed = true;
        } else if (null == oldValue && null == value) {
            changed = false;
        }

        if (false == changed) {
            return;
        }

        /* OK, let's save it into DB */
        if (saveToDB) {
            saveToDB(key, value);
        }

        /* OK, let's invoke the listeners */
        if (null == EnvConfigure.listeners || EnvConfigure.listeners.isEmpty())
            return;
        ThreadPool.DefaultThreadPool.getInstance().submit(new InvokeListenerTask(key, oldValue, value));
    }

    static public void registerListener(Listener listener) {
        synchronized (EnvConfigure.class) {
            if (false == listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    static public void unRegisterListener(Listener listener) {
        synchronized (EnvConfigure.class) {
            if (listeners.contains(listener)) {
                listeners.remove(listener);
            }
        }
    }

    /* helper */

    static private void initConfiguresByConfigureDB(HashSet<String> configuresWithinDB) {
        if (null == configuresWithinDB || configuresWithinDB.isEmpty())
            return;

        SharedPreferences preferences = app.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        if (null == preferences)
            return;

        for (String key : configuresWithinDB) {
            if (preferences.contains(key))
                AConfigure.getInstance().getConfig().put(key, preferences.getString(key, ""));
        }
    }

    static private void saveToDB(String key, String value) {
        if (null == key || key.isEmpty())
            return;

        SharedPreferences preferences = app.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        if (null == preferences)
            return;

        SharedPreferences.Editor editor = preferences.edit();
        if (null == editor)
            return;

        editor.putString(key, value);
        editor.commit();
    }

    /* inner type */

    public interface Listener {
        boolean needUIThread();

        boolean needInvoked(String key);

        void onConfigureChanged(String key, String oldValue, String newValue);
    }

    static private class InvokeListenerTask implements Runnable {

        final private String key;
        final private String oldValue;
        final private String newValue;

        public InvokeListenerTask(String key, String oldValue, String newValue) {
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public void run() {
            synchronized (EnvConfigure.class) {

                /* check the parameters */
                if (null == this.key || this.key.isEmpty())
                    return;

                /* is there listener ? */
                if (null == EnvConfigure.listeners || EnvConfigure.listeners.isEmpty())
                    return;

                for (Listener l : EnvConfigure.listeners) {
                    invoke(l);
                }
            }

        }

        private void invoke(final Listener listener) {
            if (null == listener)
                return;

            try {
                if (listener.needInvoked(this.key)) {
                    if (listener.needUIThread())
                        listener.onConfigureChanged(this.key, this.oldValue, this.newValue);
                    else
                        ThreadPool.MainThreadHandler.getInstance().post(new Runnable() {

                            @Override
                            public void run() {
                                listener.onConfigureChanged(InvokeListenerTask.this.key, InvokeListenerTask.this.oldValue, InvokeListenerTask.this.newValue);
                            }

                        });
                }
            } catch (Exception ex) {
            }

        }

    }

}
