package com.ilife.home.robot.bean;

import java.util.List;

public class RobotConfigBean {

    /**
     * brand : ILIFE
     * area : china
     * robot : [{"productKey":"a1nIeZXrVFg","factoryType":"X800-黑色","robotType":"X800 \u2022 黑","settingRobot":"X800","faceImg":"n_x800","openPowerImg":"pic_open_power","readyWifiImg":"pic_start","openPowerTip":"","prepareWifiTip":"ap_guide_sty_typ2_x800","readyForWifiTip":"ap_guide_already_open_wifi","rechargeStand":"pic_charging_base","isHaveMap":true,"isHaveMapData":true,"isOnlyRandomMode":false,"isLongPressControl":false,"isSupportPause":true,"pointToAlong":false,"alongToPoint":true,"planningToAlong":false,"pointAlongToRecharge":false,"settingMode":false,"settingUpdate":true,"settingRecord":true,"settingVoice":true},{"productKey":"a1cdxiwZfP9","factoryType":"X800-白色","robotType":"X800 \u2022 白","settingRobot":"X800","faceImg":"n_x800_w","openPowerImg":"pic_open_power_w","readyWifiImg":"pic_start","openPowerTip":"","prepareWifiTip":"ap_guide_sty_typ2_x800","readyForWifiTip":"ap_guide_already_open_wifi","rechargeStand":"pic_charging_base","isHaveMap":true,"isHaveMapData":true,"isOnlyRandomMode":false,"isLongPressControl":false,"isSupportPause":true,"pointToAlong":false,"alongToPoint":true,"planningToAlong":false,"pointAlongToRecharge":false,"settingMode":false,"settingUpdate":true,"settingRecord":true,"settingVoice":true},{"productKey":"a1wBeDmFnJu","factoryType":"X787","robotType":"X787","settingRobot":"X787","faceImg":"n_x787","openPowerImg":"pic_open_power_787","readyWifiImg":"pic_start","openPowerTip":"","prepareWifiTip":"ap_guide_sty_typ2_x320","readyForWifiTip":"ap_guide_already_heared_didi","rechargeStand":"pic_charging_base","isHaveMap":true,"isHaveMapData":true,"isOnlyRandomMode":false,"isLongPressControl":true,"isSupportPause":false,"pointToAlong":true,"alongToPoint":true,"planningToAlong":true,"pointAlongToRecharge":true,"settingMode":false,"settingUpdate":false,"settingRecord":true,"settingVoice":false},{"productKey":"a1r76ksgLTV","factoryType":"X320","robotType":"V3x","settingRobot":"V3x","faceImg":"n_v3x","openPowerImg":"pic_open_power_v3x","readyWifiImg":"pic_start","openPowerTip":"","prepareWifiTip":"ap_guide_sty_typ2_x320","readyForWifiTip":"ap_guide_already_heared_didi","rechargeStand":"recharge_stand_v3x","isHaveMap":false,"isHaveMapData":false,"isOnlyRandomMode":true,"isLongPressControl":false,"isSupportPause":false,"pointToAlong":true,"alongToPoint":true,"planningToAlong":false,"pointAlongToRecharge":false,"settingMode":false,"settingUpdate":false,"settingRecord":false,"settingVoice":false},{"productKey":"a1JyoDFAJQT","factoryType":"X434","robotType":"X451","settingRobot":"X451","faceImg":"","openPowerImg":"pic_open_power","readyWifiImg":"pic_start","openPowerTip":"","prepareWifiTip":"ap_guide_sty_typ2_x320","readyForWifiTip":"ap_guide_already_heared_didi","rechargeStand":"recharge_stand_v3x","isHaveMap":true,"isHaveMapData":true,"isOnlyRandomMode":false,"isLongPressControl":true,"isSupportPause":false,"pointToAlong":true,"alongToPoint":true,"planningToAlong":true,"pointAlongToRecharge":true,"settingMode":false,"settingUpdate":false,"settingRecord":true,"settingVoice":false}]
     */

    private String brand;
    private String area;
    private List<RobotBean> robot;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public List<RobotBean> getRobot() {
        return robot;
    }

    public void setRobot(List<RobotBean> robot) {
        this.robot = robot;
    }

    /**
     * 通过robot type 获取配置
     * @param robotType
     * @return
     */
    public RobotBean getRobotBeanByRt(String robotType){
        RobotBean bean=null;
        for (RobotBean rBean:robot) {
            if (rBean.getRobotType().equals(robotType)){
                bean=rBean;
                break;
            }
        }
        return bean;
    }
    /**
     * 通过product key 获取配置
     * @param productKey
     * @return
     */
    public RobotBean getRobotBeanByPk(String productKey){
        RobotBean bean=null;
        for (RobotBean rBean:robot) {
            if (rBean.getProductKey().equals(productKey)){
                bean=rBean;
                break;
            }
        }
        return bean;
    }

    public static class RobotBean {
        /**
         * productKey : a1nIeZXrVFg
         * factoryType : X800-黑色
         * robotType : X800 • 黑
         * settingRobot : X800
         * faceImg : n_x800
         * openPowerImg : pic_open_power
         * readyWifiImg : pic_start
         * openPowerTip :
         * prepareWifiTip : ap_guide_sty_typ2_x800
         * readyForWifiTip : ap_guide_already_open_wifi
         * rechargeStand : pic_charging_base
         * isHaveMap : true
         * isHaveMapData : true
         * isOnlyRandomMode : false
         * isLongPressControl : false
         * isSupportPause : true
         * pointToAlong : false
         * alongToPoint : true
         * planningToAlong : false
         * pointAlongToRecharge : false
         * settingMode : false
         * settingUpdate : true
         * settingRecord : true
         * settingVoice : true
         * historyMapType: 2
         * scheduleInDark: true 是否可以直接在黑暗环境下预约，不弹提示框
         * waitForOpenPower:false 按配网键前是否需要提升等待机器启动完成
         * waterLevelType: 1  1-普通水量顺序（0-1-2），2-x787水量顺序（1-2-3）
         * suctionType: false false-不是吸口型 true-吸口型  （吸口型的机器没有滚刷）
         * newScheduleVersion:true X800系列是新协议版本 X787 V3x等是旧协议版本
          */

        private String productKey;
        private String factoryType;
        private String robotType;
        private String settingRobot;
        private String faceImg;
        private String openPowerImg;
        private String readyWifiImg;
        private String openPowerTip;
        private String prepareWifiTip;
        private String readyForWifiTip;
        private String rechargeStand;
        private boolean isHaveMap;
        private boolean isHaveMapData;
        private boolean isOnlyRandomMode;
        private boolean isLongPressControl;
        private boolean isSupportPause;
        private boolean pointToAlong;
        private boolean alongToPoint;
        private boolean planningToAlong;
        private boolean pointAlongToRecharge;
        private boolean settingMode;
        private boolean settingUpdate;
        private boolean settingRecord;
        private boolean settingVoice;
        private int historyMapType;
        private boolean scheduleInDark;
        private boolean waitForOpenPower;
        private int waterLevelType;
        private boolean suctionType;
        private boolean newScheduleVersion;
        private boolean needDivide100;
        public String getProductKey() {
            return productKey;
        }

        public void setProductKey(String productKey) {
            this.productKey = productKey;
        }

        public String getFactoryType() {
            return factoryType;
        }

        public void setFactoryType(String factoryType) {
            this.factoryType = factoryType;
        }

        public String getRobotType() {
            return robotType;
        }

        public void setRobotType(String robotType) {
            this.robotType = robotType;
        }

        public String getSettingRobot() {
            return settingRobot;
        }

        public void setSettingRobot(String settingRobot) {
            this.settingRobot = settingRobot;
        }

        public String getFaceImg() {
            return faceImg;
        }

        public void setFaceImg(String faceImg) {
            this.faceImg = faceImg;
        }

        public String getOpenPowerImg() {
            return openPowerImg;
        }

        public void setOpenPowerImg(String openPowerImg) {
            this.openPowerImg = openPowerImg;
        }

        public String getReadyWifiImg() {
            return readyWifiImg;
        }

        public void setReadyWifiImg(String readyWifiImg) {
            this.readyWifiImg = readyWifiImg;
        }

        public String getOpenPowerTip() {
            return openPowerTip;
        }

        public void setOpenPowerTip(String openPowerTip) {
            this.openPowerTip = openPowerTip;
        }

        public String getPrepareWifiTip() {
            return prepareWifiTip;
        }

        public void setPrepareWifiTip(String prepareWifiTip) {
            this.prepareWifiTip = prepareWifiTip;
        }

        public String getReadyForWifiTip() {
            return readyForWifiTip;
        }

        public void setReadyForWifiTip(String readyForWifiTip) {
            this.readyForWifiTip = readyForWifiTip;
        }

        public String getRechargeStand() {
            return rechargeStand;
        }

        public void setRechargeStand(String rechargeStand) {
            this.rechargeStand = rechargeStand;
        }

        public boolean isIsHaveMap() {
            return isHaveMap;
        }

        public void setIsHaveMap(boolean isHaveMap) {
            this.isHaveMap = isHaveMap;
        }

        public boolean isIsHaveMapData() {
            return isHaveMapData;
        }

        public void setIsHaveMapData(boolean isHaveMapData) {
            this.isHaveMapData = isHaveMapData;
        }

        public boolean isIsOnlyRandomMode() {
            return isOnlyRandomMode;
        }

        public void setIsOnlyRandomMode(boolean isOnlyRandomMode) {
            this.isOnlyRandomMode = isOnlyRandomMode;
        }

        public boolean isIsLongPressControl() {
            return isLongPressControl;
        }

        public void setIsLongPressControl(boolean isLongPressControl) {
            this.isLongPressControl = isLongPressControl;
        }

        public boolean isIsSupportPause() {
            return isSupportPause;
        }

        public void setIsSupportPause(boolean isSupportPause) {
            this.isSupportPause = isSupportPause;
        }

        public boolean isPointToAlong() {
            return pointToAlong;
        }

        public void setPointToAlong(boolean pointToAlong) {
            this.pointToAlong = pointToAlong;
        }

        public boolean isAlongToPoint() {
            return alongToPoint;
        }

        public void setAlongToPoint(boolean alongToPoint) {
            this.alongToPoint = alongToPoint;
        }

        public boolean isPlanningToAlong() {
            return planningToAlong;
        }

        public void setPlanningToAlong(boolean planningToAlong) {
            this.planningToAlong = planningToAlong;
        }

        public boolean isPointAlongToRecharge() {
            return pointAlongToRecharge;
        }

        public void setPointAlongToRecharge(boolean pointAlongToRecharge) {
            this.pointAlongToRecharge = pointAlongToRecharge;
        }

        public boolean isSettingMode() {
            return settingMode;
        }

        public void setSettingMode(boolean settingMode) {
            this.settingMode = settingMode;
        }

        public boolean isSettingUpdate() {
            return settingUpdate;
        }

        public void setSettingUpdate(boolean settingUpdate) {
            this.settingUpdate = settingUpdate;
        }

        public boolean isSettingRecord() {
            return settingRecord;
        }

        public void setSettingRecord(boolean settingRecord) {
            this.settingRecord = settingRecord;
        }

        public boolean isSettingVoice() {
            return settingVoice;
        }

        public void setSettingVoice(boolean settingVoice) {
            this.settingVoice = settingVoice;
        }

        public int getHistoryMapType() {
            return historyMapType;
        }

        /**
         * 是否可以直接预约黑暗环境清扫
         * @return
         */
        public boolean isScheduleInDark() {
            return scheduleInDark;
        }

        public void setScheduleInDark(boolean scheduleInDark) {
            this.scheduleInDark = scheduleInDark;
        }

        public void setHistoryMapType(int historyMapType) {
            this.historyMapType = historyMapType;
        }

        public boolean isWaitForOpenPower() {
            return waitForOpenPower;
        }

        public void setWaitForOpenPower(boolean waitForOpenPower) {
            this.waitForOpenPower = waitForOpenPower;
        }

        public int getWaterLevelType() {
            return waterLevelType;
        }

        public void setWaterLevelType(int waterLevelType) {
            this.waterLevelType = waterLevelType;
        }

        public boolean isSuctionType() {
            return suctionType;
        }

        public void setSuctionType(boolean suctionType) {
            this.suctionType = suctionType;
        }

        public boolean isNewScheduleVersion() {
            return newScheduleVersion;
        }

        public void setNewScheduleVersion(boolean newScheduleVersion) {
            this.newScheduleVersion = newScheduleVersion;
        }

        public boolean isNeedDivide100() {
            return needDivide100;
        }

        public void setNeedDivide100(boolean needDivide100) {
            this.needDivide100 = needDivide100;
        }
    }
}
