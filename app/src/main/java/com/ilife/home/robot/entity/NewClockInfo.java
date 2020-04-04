package com.ilife.home.robot.entity;

import com.aliyun.iot.aep.sdk.bean.ScheduleBean;

/**
 * Created by chengjiaping on 2018/9/11.
 */

public class NewClockInfo {
    private int week;
    private int hour;
    private int minute;
    private int open;
    private int mode;//2-待机 3-随机 4-延边 6-规划 7-回冲
    private int type;//0-默认 1-分区 2-分房
    private int times;//执行次数
    private String area;//划区坐标
    private int room;//房间id

    public NewClockInfo(ScheduleBean bean) {
        this.week = bean.getScheduleWeek();
        this.hour = bean.getScheduleHour();
        this.minute = bean.getScheduleMinutes();
        this.open = bean.getScheduleEnable();
        this.mode = bean.getScheduleMode();
        this.type = bean.getScheduleType();
        this.area = bean.getScheduleArea();
        this.times=bean.getScheduleLoop();
        this.area=bean.getScheduleArea();
        this.room=bean.getScheduleRoom();
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }
}
