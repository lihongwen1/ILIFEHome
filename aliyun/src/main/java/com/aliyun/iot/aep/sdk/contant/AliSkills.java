package com.aliyun.iot.aep.sdk.contant;

import java.util.HashMap;

public class AliSkills {
    private static AliSkills aliSkills;
    HashMap<String, Object> params;

    private AliSkills() {
        params = new HashMap<>();
    }

    public static synchronized AliSkills get() {
        if (aliSkills == null) {
            synchronized (AliSkills.class) {
                if (aliSkills == null) {
                    aliSkills = new AliSkills();
                }
            }
        }
        return aliSkills;
    }

    public HashMap<String, Object> queryVirtual(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, String> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, "");
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, "");
        return params;
    }

    public HashMap<String, Object> enterVirtualEditMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_VIRTUAL_EDIT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_VIRTUAL_EDIT + "");
        return params;
    }

    public HashMap<String, Object> enterCleanAreaEditMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_CLEAN_AREA_EDIT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_CLEAN_AREA_EDIT + "");
        return params;
    }


    public HashMap<String, Object> setVirtualWall(byte[] virtualContentBytes) {
        return params;
    }

    public HashMap<String, Object> enterPointMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_POINT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_POINT + "");
        return params;
    }

    public HashMap<String, Object> enterTemporaryointMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_TEMPORARY_POINT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_TEMPORARY_POINT + "");
        return params;
    }

    public HashMap<String, Object> enterAlongMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_ALONG);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_ALONG + "");
        return params;
    }

    public HashMap<String, Object> enterWaitMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_WAIT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_WAIT + "");
        return params;
    }

    public HashMap<String, Object> enterPauseMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_PAUSE);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_PAUSE + "");
        return params;
    }

    public HashMap<String, Object> enterOTAMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_OTA);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_OTA + "");
        return params;
    }


    public HashMap<String, Object> enterPlanningMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_PLANNING);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_PLANNING + "");
        return params;
    }

    public HashMap<String, Object> enterRandomMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_RANDOM);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_RANDOM + "");
        return params;
    }

    public HashMap<String, Object> enterRechargeMode(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_RECHARGE);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_RECHARGE + "");
        return params;
    }

    public HashMap<String, Object> enterTemporaryPoint(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> workMode = new HashMap<>();
        workMode.put(EnvConfigure.KEY_WORK_MODE, MsgCodeUtils.STATUE_TEMPORARY_POINT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_WORK_MODE);
        params.put(EnvConfigure.KEY_ITEMS, workMode);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.STATUE_TEMPORARY_POINT + "");
        return params;
    }

    public HashMap<String, Object> turnLeft(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> cleanDirection = new HashMap<>();
        cleanDirection.put(EnvConfigure.KEY_CLEAN_DIRECTION, MsgCodeUtils.PROCEED_LEFT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_CLEAN_DIRECTION);
        params.put(EnvConfigure.KEY_ITEMS, cleanDirection);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.PROCEED_LEFT + "");
        return params;
    }

    public HashMap<String, Object> turnRight(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> cleanDirection = new HashMap<>();
        cleanDirection.put(EnvConfigure.KEY_CLEAN_DIRECTION, MsgCodeUtils.PROCEED_RIGHT);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_CLEAN_DIRECTION);
        params.put(EnvConfigure.KEY_ITEMS, cleanDirection);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.PROCEED_RIGHT + "");
        return params;
    }

    public HashMap<String, Object> turnPause(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> cleanDirection = new HashMap<>();
        cleanDirection.put(EnvConfigure.KEY_CLEAN_DIRECTION, MsgCodeUtils.PROCEED_PAUSE);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_CLEAN_DIRECTION);
        params.put(EnvConfigure.KEY_ITEMS, cleanDirection);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.PROCEED_PAUSE + "");
        return params;
    }

    public HashMap<String, Object> turnForward(String iotId) {
        params.clear();
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        HashMap<String, Integer> cleanDirection = new HashMap<>();
        cleanDirection.put(EnvConfigure.KEY_CLEAN_DIRECTION, MsgCodeUtils.PROCEED_FORWARD);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_CLEAN_DIRECTION);
        params.put(EnvConfigure.KEY_ITEMS, cleanDirection);
        params.put(EnvConfigure.KEY_TAG, MsgCodeUtils.PROCEED_FORWARD + "");
        return params;
    }


    public HashMap<String, Object> cleaningNormal(String iotId) {
        params.clear();
        HashMap<String, Integer> cleanMode = new HashMap<>();
        cleanMode.put(EnvConfigure.KEY_MAX_MODE, MsgCodeUtils.CLEANNING_CLEANING_NORMAL);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_MAX_MODE);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, cleanMode);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_NORMAL + "");
        return params;
    }

    public HashMap<String, Object> cleaningMax(String iotId) {
        params.clear();
        HashMap<String, Integer> cleanMode = new HashMap<>();
        cleanMode.put(EnvConfigure.KEY_MAX_MODE, MsgCodeUtils.CLEANNING_CLEANING_MAX);
        params.put(EnvConfigure.KEY_PATH, EnvConfigure.KEY_MAX_MODE);
        params.put(EnvConfigure.KEY_IOT_ID, iotId);
        params.put(EnvConfigure.KEY_ITEMS, cleanMode);
        params.put(EnvConfigure.KEY_TAG, EnvConfigure.VALUE_SET_MAX + "");
        return params;
    }

}
