package com.ilife.home.robot.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;

public class SaveMapBean implements Parcelable {
    private String[] mapData;
    private String[] mapDataInfo;
    private int mapId;
    public SaveMapBean(){};
    public SaveMapBean(String[] mapData, String[] mapDataInfo, int mapId) {
        this.mapData = mapData;
        this.mapDataInfo = mapDataInfo;
        this.mapId = mapId;
    }

    public String[] getMapData() {
        return mapData;
    }

    public void setMapData(String[] mapData) {
        this.mapData = mapData;
    }

    public String[] getMapDataInfo() {
        return mapDataInfo;
    }

    public void setMapDataInfo(String[] mapDataInfo) {
        this.mapDataInfo = mapDataInfo;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(this.mapData);
        dest.writeStringArray(this.mapDataInfo);
        dest.writeInt(this.mapId);
    }

    protected SaveMapBean(Parcel in) {
        this.mapData = in.createStringArray();
        this.mapDataInfo = in.createStringArray();
        this.mapId = in.readInt();
    }

    public static final Parcelable.Creator<SaveMapBean> CREATOR = new Parcelable.Creator<SaveMapBean>() {
        @Override
        public SaveMapBean createFromParcel(Parcel source) {
            return new SaveMapBean(source);
        }

        @Override
        public SaveMapBean[] newArray(int size) {
            return new SaveMapBean[size];
        }
    };
}
