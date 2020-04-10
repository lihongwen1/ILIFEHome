package com.aliyun.iot.aep.sdk.bean;

public class PropertyBean {
    private boolean maxMode;
    private int workMode;
    private int battery;
    private int waterLevel;
    private long realTimeMapTimeLine;
    private long historyMapTimeLine;
    private boolean voiceOpen;//0-代表开 1-代表关
    private long selectedMapId;
    private String saveMapId;
    private String forbiddenArea;//禁区
    private String virtualWall;
    private String partition;//分区（分房）
    private String cleanArea;//划区数据
    private String chagePort;
    private String cleanRoomData;//选房清扫数据
    private boolean initStatus;
    public PropertyBean(){}

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

    public String getCleanArea() {
        return cleanArea;
    }

    public void setCleanArea(String cleanArea) {
        this.cleanArea = cleanArea;
    }

    public long getHistoryMapTimeLine() {
        return historyMapTimeLine;
    }

    public void setHistoryMapTimeLine(long historyMapTimeLine) {
        this.historyMapTimeLine = historyMapTimeLine;
    }

    public long getSelectedMapId() {
        return selectedMapId;
    }

    public void setSelectedMapId(long selectedMapId) {
        this.selectedMapId = selectedMapId;
    }

    public String getSaveMapId() {
        return saveMapId;
    }

    public void setSaveMapId(String saveMapId) {
        this.saveMapId = saveMapId;
    }

    public String getForbiddenArea() {
        return forbiddenArea;
    }

    public void setForbiddenArea(String forbiddenArea) {
        this.forbiddenArea = forbiddenArea;
    }

    public String getVirtualWall() {
        return virtualWall;
    }

    public void setVirtualWall(String virtualWall) {
        this.virtualWall = virtualWall;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getChagePort() {
        return chagePort;
    }

    public void setChagePort(String chagePort) {
        this.chagePort = chagePort;
    }

    public boolean isVoiceOpen() {
        return voiceOpen;
    }

    public void setVoiceOpen(boolean voiceOpen) {
        this.voiceOpen = voiceOpen;
    }

    public boolean isInitStatus() {
        return initStatus;
    }

    public void setInitStatus(boolean initStatus) {
        this.initStatus = initStatus;
    }

    public String getCleanRoomData() {
        return cleanRoomData;
    }

    public void setCleanRoomData(String cleanRoomData) {
        this.cleanRoomData = cleanRoomData;
    }

    @Override
    public String toString() {
        return "PropertyBean{" +
                "maxMode=" + maxMode +
                ", workMode=" + workMode +
                ", battery=" + battery +
                ", waterLevel=" + waterLevel +
                ", realTimeMapTimeLine=" + realTimeMapTimeLine +
                ", historyMapTimeLine=" + historyMapTimeLine +
                ", voiceOpen=" + voiceOpen +
                '}';
    }
}
