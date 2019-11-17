package com.aliyun.iot.aep.sdk.bean;

public class RealTimeMapBean {


    /**
     * MapResolution : 0
     * CurrentPiont : -1507326
     * CleanArea : 49
     * MapData : //oAAAH/+QAAAf/4AAAB//cAAAH/9gAAAf/1AAAB//QAAAH/8wAAAf/yAAAB//EAAAH/8AAAAf/vAAAB/+4AAAH/6wAAAv/r//8C/+4AAQH/7QABAf/sAAEB/+wAAgH/6v//Av/s//8C/+sAAgH/6gACAf/p//8C/+kAAgH/6P//Ag==
     * CleanDirection : 0
     * MapDataType : 0
     * CleanTime : 21
     * RoadResolution : 0
     */

    private int MapResolution;
    private int CurrentPiont;
    private int CleanArea;
    private String MapData;
    private int CleanDirection;
    private int MapDataType;
    private int CleanTime;
    private int RoadResolution;

    public int getMapResolution() {
        return MapResolution;
    }

    public void setMapResolution(int MapResolution) {
        this.MapResolution = MapResolution;
    }

    public int getCurrentPiont() {
        return CurrentPiont;
    }

    public void setCurrentPiont(int CurrentPiont) {
        this.CurrentPiont = CurrentPiont;
    }

    public int getCleanArea() {
        return CleanArea;
    }

    public void setCleanArea(int CleanArea) {
        this.CleanArea = CleanArea;
    }

    public String getMapData() {
        return MapData;
    }

    public void setMapData(String MapData) {
        this.MapData = MapData;
    }

    public int getCleanDirection() {
        return CleanDirection;
    }

    public void setCleanDirection(int CleanDirection) {
        this.CleanDirection = CleanDirection;
    }

    public int getMapDataType() {
        return MapDataType;
    }

    public void setMapDataType(int MapDataType) {
        this.MapDataType = MapDataType;
    }

    public int getCleanTime() {
        return CleanTime;
    }

    public void setCleanTime(int CleanTime) {
        this.CleanTime = CleanTime;
    }

    public int getRoadResolution() {
        return RoadResolution;
    }

    public void setRoadResolution(int RoadResolution) {
        this.RoadResolution = RoadResolution;
    }

    @Override
    public String toString() {
        return "RealTimeMapBean{" +
                "MapResolution=" + MapResolution +
                ", CurrentPiont=" + CurrentPiont +
                ", CleanArea=" + CleanArea +
                ", MapData='" + MapData + '\'' +
                ", CleanDirection=" + CleanDirection +
                ", MapDataType=" + MapDataType +
                ", CleanTime=" + CleanTime +
                ", RoadResolution=" + RoadResolution +
                '}';
    }
}
