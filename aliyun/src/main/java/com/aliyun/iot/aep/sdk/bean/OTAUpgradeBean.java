package com.aliyun.iot.aep.sdk.bean;

public class OTAUpgradeBean {

    /**
     * iotId : xxxx
     * step : 10
     * desc : xxxxxx
     * success : false
     * needConfirm : true
     * upgradeStatus : 0
     */

    private String iotId;
    private int step;
    private String desc;
    private boolean success;
    private boolean needConfirm;
    private int upgradeStatus;

    public String getIotId() {
        return iotId;
    }

    public void setIotId(String iotId) {
        this.iotId = iotId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(boolean needConfirm) {
        this.needConfirm = needConfirm;
    }

    public int getUpgradeStatus() {
        return upgradeStatus;
    }

    public void setUpgradeStatus(int upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
    }
}
