package com.ilife.home.robot.bean;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;

public class SaveMapBean {
    private HistoryRecordBean recordBean;
    private long mapId;

    public SaveMapBean(HistoryRecordBean recordBean, long mapId) {
        this.recordBean = recordBean;
        this.mapId = mapId;
    }

    public HistoryRecordBean getRecordBean() {
        return recordBean;
    }

    public void setRecordBean(HistoryRecordBean recordBean) {
        this.recordBean = recordBean;
    }

    public long getMapId() {
        return mapId;
    }

    public void setMapId(long mapId) {
        this.mapId = mapId;
    }
}
