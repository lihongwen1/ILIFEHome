package com.aliyun.iot.aep.sdk.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class ScheduleBean {


    /**
     * ScheduleHour : 0
     * ScheduleType : 0
     * ScheduleEnd : 300
     * ScheduleEnable : 0
     * ScheduleMode : 6
     * ScheduleLoop : 0
     * ScheduleWeek : 1
     * ScheduleArea : AAAAAAAAAAAAAAAAAAAAAA==
     * ScheduleRoom : 0
     * ScheduleMinutes : 0
     */
    private int ScheduleHour;
    private int ScheduleType;
    private int ScheduleEnd;
    private int ScheduleEnable;
    private int ScheduleMode;
    private int ScheduleLoop;
    private int ScheduleWeek;
    private String ScheduleArea;
    private int ScheduleRoom;
    private int ScheduleMinutes;

    @JSONField(name = "ScheduleHour")
    public int getScheduleHour() {
        return ScheduleHour;
    }

    public void setScheduleHour(int ScheduleHour) {
        this.ScheduleHour = ScheduleHour;
    }

    @JSONField(name = "ScheduleType")
    public int getScheduleType() {
        return ScheduleType;
    }

    public void setScheduleType(int ScheduleType) {
        this.ScheduleType = ScheduleType;
    }

    @JSONField(name = "ScheduleEnd")
    public int getScheduleEnd() {
        return ScheduleEnd;
    }

    public void setScheduleEnd(int ScheduleEnd) {
        this.ScheduleEnd = ScheduleEnd;
    }

    @JSONField(name = "ScheduleEnable")
    public int getScheduleEnable() {
        return ScheduleEnable;
    }

    public void setScheduleEnable(int ScheduleEnable) {
        this.ScheduleEnable = ScheduleEnable;
    }

    @JSONField(name = "ScheduleMode")
    public int getScheduleMode() {
        return ScheduleMode;
    }

    public void setScheduleMode(int ScheduleMode) {
        this.ScheduleMode = ScheduleMode;
    }

    @JSONField(name = "ScheduleLoop")
    public int getScheduleLoop() {
        return ScheduleLoop;
    }

    public void setScheduleLoop(int ScheduleLoop) {
        this.ScheduleLoop = ScheduleLoop;
    }

    @JSONField(name = "ScheduleWeek")
    public int getScheduleWeek() {
        return ScheduleWeek;
    }

    public void setScheduleWeek(int ScheduleWeek) {
        this.ScheduleWeek = ScheduleWeek;
    }

    @JSONField(name = "ScheduleArea")
    public String getScheduleArea() {
        return ScheduleArea;
    }

    public void setScheduleArea(String ScheduleArea) {
        this.ScheduleArea = ScheduleArea;
    }

    @JSONField(name = "ScheduleRoom")
    public int getScheduleRoom() {
        return ScheduleRoom;
    }

    public void setScheduleRoom(int ScheduleRoom) {
        this.ScheduleRoom = ScheduleRoom;
    }

    @JSONField(name = "ScheduleMinutes")
    public int getScheduleMinutes() {
        return ScheduleMinutes;
    }

    public void setScheduleMinutes(int ScheduleMinutes) {
        this.ScheduleMinutes = ScheduleMinutes;
    }
}
