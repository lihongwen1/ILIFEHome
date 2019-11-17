package com.aliyun.iot.aep.sdk.bean;

public class PropertyBean {
    private boolean maxMode;
    private int workMode;
    private int battery;
    private int waterLevel;
    private long realTimeMapTimeLine;
    private long historyMapTimeLine;
    private boolean voiceOpen;//0-代表开 1-代表关

    public PropertyBean(){};

    public PropertyBean(boolean maxMode, int workMode, int battery, int waterLevel, long realTimeMapTimeLine, long historyMapTimeLine, boolean voiceOpen) {
        this.maxMode = maxMode;
        this.workMode = workMode;
        this.battery = battery;
        this.waterLevel = waterLevel;
        this.realTimeMapTimeLine = realTimeMapTimeLine;
        this.historyMapTimeLine = historyMapTimeLine;
        this.voiceOpen = voiceOpen;
    }

    public boolean isMaxMode() {
        return maxMode;
    }

    public void setMaxMode(boolean maxMode) {
        this.maxMode = maxMode;
    }

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public long getRealTimeMapTimeLine() {
        return realTimeMapTimeLine;
    }

    public void setRealTimeMapTimeLine(long realTimeMapTimeLine) {
        this.realTimeMapTimeLine = realTimeMapTimeLine;
    }

    public long getHistoryMapTimeLine() {
        return historyMapTimeLine;
    }

    public void setHistoryMapTimeLine(long historyMapTimeLine) {
        this.historyMapTimeLine = historyMapTimeLine;
    }

    public boolean isVoiceOpen() {
        return voiceOpen;
    }

    public void setVoiceOpen(boolean voiceOpen) {
        this.voiceOpen = voiceOpen;
    }
}
