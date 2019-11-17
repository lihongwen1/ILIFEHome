package com.aliyun.iot.aep.sdk.bean;

public class ScheduleBean {

    /**
     * ScheduleHour : 0
     * ScheduleType : 0
     * ScheduleEnd : 300
     * ScheduleEnable : 0
     * ScheduleMode : 6
     * ScheduleWeek : 1
     * ScheduleArea : AAAAAAAAAAAAAAAA
     * ScheduleMinutes : 0
     */

    private int ScheduleHour;
    private int ScheduleType;
    private int ScheduleEnd;
    private int ScheduleEnable;
    private int ScheduleMode;
    private int ScheduleWeek;
    private String ScheduleArea;
    private int ScheduleMinutes;

    public int getScheduleHour() {
        return ScheduleHour;
    }

    public void setScheduleHour(int ScheduleHour) {
        this.ScheduleHour = ScheduleHour;
    }

    public int getScheduleType() {
        return ScheduleType;
    }

    public void setScheduleType(int ScheduleType) {
        this.ScheduleType = ScheduleType;
    }

    public int getScheduleEnd() {
        return ScheduleEnd;
    }

    public void setScheduleEnd(int ScheduleEnd) {
        this.ScheduleEnd = ScheduleEnd;
    }

    public int getScheduleEnable() {
        return ScheduleEnable;
    }

    public void setScheduleEnable(int ScheduleEnable) {
        this.ScheduleEnable = ScheduleEnable;
    }

    public int getScheduleMode() {
        return ScheduleMode;
    }

    public void setScheduleMode(int ScheduleMode) {
        this.ScheduleMode = ScheduleMode;
    }

    public int getScheduleWeek() {
        return ScheduleWeek;
    }

    public void setScheduleWeek(int ScheduleWeek) {
        this.ScheduleWeek = ScheduleWeek;
    }

    public String getScheduleArea() {
        return ScheduleArea;
    }

    public void setScheduleArea(String ScheduleArea) {
        this.ScheduleArea = ScheduleArea;
    }

    public int getScheduleMinutes() {
        return ScheduleMinutes;
    }

    public void setScheduleMinutes(int ScheduleMinutes) {
        this.ScheduleMinutes = ScheduleMinutes;
    }
}
