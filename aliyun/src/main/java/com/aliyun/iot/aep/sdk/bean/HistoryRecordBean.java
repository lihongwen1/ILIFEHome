package com.aliyun.iot.aep.sdk.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryRecordBean implements Serializable {
    /**
     * StopCleanReason : 1
     * CleanTotalTime : 1341
     * PackNum : 15
     * MapResolution : 0
     * StartCleanReason : 1
     * StartTime : 1573609668
     * CleanTotalArea : 33
     * PackId : 15
     * CleanMapData : Av++AAL/vwAC/78AAv/AAAL/wAAC/8EAAv/CAAL/wgAC/8MAAv/DAAL/xAAC/8UAAv/FAAL/xgAC/8YAAv/HAAL/yAAC/8cAAv/GAAL/xgAC/8UAAv/FAAP/xQAE/8UABP/EAAT/wwAE/8MABP/CAAT/wgAE/8EABP/AAAT/wAAE/78ABP+/AAT/vgAE/70ABP+9AAT/vAAE/7wABP+7AAT/ugAE/7oABP+5AAT/uQAE/7gABP+3AAT/twAE/7YABP+2AAT/tQAE/7QABP+0AAT/tAAE/7MABP+zAAT/sgAE/7EABP+xAAT/sAAE/7AABP+vAAT/rgAE/64ABP+tAAT/rQAE/6wABP+rAAT/qwAE/6sABP+sAAT/rAAF/60ABf+tAAX/rQAF/64ABf+uAAX/rwAF/7AABf+wAAX/sQAF/7EABf+yAAX/swAF/7MABf+0AAX/tAAF/7UABf+2AAX/tgAF/7cABf+3AAX/uAAF/7kABf+5AAX/ugAF/7oABf+7AAX/vAAF/7wABf+9AAX/vQAF/74ABf+/AAX/vwAF/8AABf/AAAX/wQAF/8IABf/CAAX/wwAF/8MABf/EAAX/xQAF/8UABf/GAAX/xQAF/8UABf/EAAX/wwAF/8MABf/CAAX/wgAF/8EABf/AAAX/wAAF/78ABf+/AAX/vgAF/70ABf+9AAX/vAAF/7wABf+7AAX/ugAF/7oABf+5AAX/uQAF/7gABf+3AAX/twAF/7YABf+2AAX/tQAF/7QABf+0AAX/swAF/7MABf+yAAX/sQAF/7EABf+wAAX/sAAF/68ABf+uAAX/rgAF/60ABf+tAAX/rQAE/60ABP+tAAT/rQAD/6wAA/+sAAL/qwAC/6sAAv+qAAL/qgAC/6kAAv+oAAL/qAAC/6gAAv+oAAH/qAAB/6gAAf+pAAH/qgAB/6oAAf+rAAH/qwAA/6v///+r////q////6z///+s//7/q//+/6v//v+q//7/qv/+/6n//v+pAAA=
     * RoadResolution : 0
     */

    private int StopCleanReason;
    private int CleanTotalTime;
    private int PackNum;
    private int MapResolution;
    private int StartCleanReason;
    private int StartTime;
    private int CleanTotalArea;
    private int PackId;
    private String CleanMapData;
    private int RoadResolution;
    /**
     * 该记录所有清扫数据集合/建议使用数组实现，可以避免数据包乱序导致地图异常
     */
    private List<String> mapDataList;

    public int getStopCleanReason() {
        return StopCleanReason;
    }

    public void setStopCleanReason(int StopCleanReason) {
        this.StopCleanReason = StopCleanReason;
    }

    public int getCleanTotalTime() {
        return CleanTotalTime;
    }

    public void setCleanTotalTime(int CleanTotalTime) {
        this.CleanTotalTime = CleanTotalTime;
    }

    public int getPackNum() {
        return PackNum;
    }

    public void setPackNum(int PackNum) {
        this.PackNum = PackNum;
    }

    public int getMapResolution() {
        return MapResolution;
    }

    public void setMapResolution(int MapResolution) {
        this.MapResolution = MapResolution;
    }

    public int getStartCleanReason() {
        return StartCleanReason;
    }

    public void setStartCleanReason(int StartCleanReason) {
        this.StartCleanReason = StartCleanReason;
    }

    public int getStartTime() {
        return StartTime;
    }

    public void setStartTime(int StartTime) {
        this.StartTime = StartTime;
    }

    public int getCleanTotalArea() {
        return CleanTotalArea;
    }

    public void setCleanTotalArea(int CleanTotalArea) {
        this.CleanTotalArea = CleanTotalArea;
    }

    public int getPackId() {
        return PackId;
    }

    public void setPackId(int PackId) {
        this.PackId = PackId;
    }

    public String getCleanMapData() {
        return CleanMapData;
    }

    public void setCleanMapData(String CleanMapData) {
        this.CleanMapData = CleanMapData;
    }

    public int getRoadResolution() {
        return RoadResolution;
    }

    public void setRoadResolution(int RoadResolution) {
        this.RoadResolution = RoadResolution;
    }


    public List<String> getMapDataList() {
        return mapDataList;
    }

    public boolean isCleanDataExit(String cleanMapData) {
        if (mapDataList == null) {
            return false;
        }
        return mapDataList.contains(cleanMapData);
    }

    public void addCleanData(String cleanData) {
        if (mapDataList == null) {
            mapDataList = new ArrayList<>();
        }
        mapDataList.add(0, cleanData);
    }
}
