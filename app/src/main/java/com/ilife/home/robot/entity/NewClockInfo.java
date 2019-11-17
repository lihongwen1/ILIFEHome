package com.ilife.home.robot.entity;

/**
 * Created by chengjiaping on 2018/9/11.
 */

public class NewClockInfo {
    private String week;
    private byte hour;
    private byte minute;
    private byte open;

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public byte getOpen() {
        return open;
    }

    public void setOpen(byte open) {
        this.open = open;
    }

    public byte getMinute() {
        return minute;
    }

    public void setMinute(byte minute) {
        this.minute = minute;
    }

    public byte getHour() {
        return hour;
    }

    public void setHour(byte hour) {
        this.hour = hour;
    }
}
