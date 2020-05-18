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
    private String chargePort;
    private String cleanRoomData;//选房清扫数据
    private boolean initStatus;
    private int voiceVolume;//音量
    private int suctionNumber;//吸力
    private int brushSpeed;//边刷速度
    private int languageCode;
    private int carpetControl;//地毯增压
    private int virtualWallEn;//虚拟墙使能
    private int appRemind;//1-提示主机已被移动
    private int saveMapDataInfoMapId1;
    private int saveMapDataInfoMapId2;
    private int saveMapDataInfoMapId3;
    private int saveMapDataMapId1;
    private int saveMapDataMapId2;
    private int saveMapDataMapId3;
    private String mapRoomInfo1;//地图房间信息
    private String mapRoomInfo2;
    private String mapRoomInfo3;
    public PropertyBean() {
    }

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

    public String getChargePort() {
        return chargePort;
    }

    public void setChargePort(String chargePort) {
        this.chargePort = chargePort;
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

    public int getVoiceVolume() {
        return voiceVolume;
    }

    public void setVoiceVolume(int voiceVolume) {
        this.voiceVolume = voiceVolume;
    }

    public int getSuctionNumber() {
        return suctionNumber;
    }

    public void setSuctionNumber(int suctionNumber) {
        this.suctionNumber = suctionNumber;
    }

    public int getBrushSpeed() {
        return brushSpeed;
    }

    public void setBrushSpeed(int brushSpeed) {
        this.brushSpeed = brushSpeed;
    }

    public int getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }

    public int getCarpetControl() {
        return carpetControl;
    }

    public void setCarpetControl(int carpetControl) {
        this.carpetControl = carpetControl;
    }

    public int getVirtualWallEn() {
        return virtualWallEn;
    }

    public void setVirtualWallEn(int virtualWallEn) {
        this.virtualWallEn = virtualWallEn;
    }

    public int getAppRemind() {
        return appRemind;
    }

    public void setAppRemind(int appRemind) {
        this.appRemind = appRemind;
    }

    public int getSaveMapDataInfoMapId1() {
        return saveMapDataInfoMapId1;
    }

    public void setSaveMapDataInfoMapId1(int saveMapDataInfoMapId1) {
        this.saveMapDataInfoMapId1 = saveMapDataInfoMapId1;
    }

    public int getSaveMapDataInfoMapId2() {
        return saveMapDataInfoMapId2;
    }

    public void setSaveMapDataInfoMapId2(int saveMapDataInfoMapId2) {
        this.saveMapDataInfoMapId2 = saveMapDataInfoMapId2;
    }

    public int getSaveMapDataInfoMapId3() {
        return saveMapDataInfoMapId3;
    }

    public void setSaveMapDataInfoMapId3(int saveMapDataInfoMapId3) {
        this.saveMapDataInfoMapId3 = saveMapDataInfoMapId3;
    }

    public int getSaveMapDataMapId1() {
        return saveMapDataMapId1;
    }

    public void setSaveMapDataMapId1(int saveMapDataMapId1) {
        this.saveMapDataMapId1 = saveMapDataMapId1;
    }

    public int getSaveMapDataMapId2() {
        return saveMapDataMapId2;
    }

    public void setSaveMapDataMapId2(int saveMapDataMapId2) {
        this.saveMapDataMapId2 = saveMapDataMapId2;
    }

    public int getSaveMapDataMapId3() {
        return saveMapDataMapId3;
    }

    public void setSaveMapDataMapId3(int saveMapDataMapId3) {
        this.saveMapDataMapId3 = saveMapDataMapId3;
    }

    public String getMapRoomInfo1() {
        return mapRoomInfo1;
    }

    public void setMapRoomInfo1(String mapRoomInfo1) {
        this.mapRoomInfo1 = mapRoomInfo1;
    }

    public String getMapRoomInfo2() {
        return mapRoomInfo2;
    }

    public void setMapRoomInfo2(String mapRoomInfo2) {
        this.mapRoomInfo2 = mapRoomInfo2;
    }

    public String getMapRoomInfo3() {
        return mapRoomInfo3;
    }

    public void setMapRoomInfo3(String mapRoomInfo3) {
        this.mapRoomInfo3 = mapRoomInfo3;
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
