package com.ilife.home.robot.bean;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;

public class SaveMapBean {
    private String[] mapData;
    private int mapId;

    public SaveMapBean(String[] mapData, int mapId) {
        this.mapData = mapData;
        this.mapId = mapId;
    }

    public String[] getMapData() {
        return mapData;
    }

    public int getMapId() {
        return mapId;
    }
}
