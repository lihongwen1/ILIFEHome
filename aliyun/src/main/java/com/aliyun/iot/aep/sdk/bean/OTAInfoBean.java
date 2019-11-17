package com.aliyun.iot.aep.sdk.bean;

public class OTAInfoBean {
    private int UpdateState;
    private int UpdateProgess;
    private int CurrentVer;
    private int TargetVer;

    public int getUpdateState() {
        return UpdateState;
    }

    public void setUpdateState(int updateState) {
        UpdateState = updateState;
    }

    public int getUpdateProgess() {
        return UpdateProgess;
    }

    public void setUpdateProgess(int updateProgess) {
        UpdateProgess = updateProgess;
    }

    public int getCurrentVer() {
        return CurrentVer;
    }

    public void setCurrentVer(int currentVer) {
        CurrentVer = currentVer;
    }

    public int getTargetVer() {
        return TargetVer;
    }

    public void setTargetVer(int targetVer) {
        TargetVer = targetVer;
    }
}
