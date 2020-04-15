package com.ilife.home.robot.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.aliyun.iot.aep.sdk.bean.ScheduleBean;

/**
 * Created by chengjiaping on 2018/9/11.
 */

public class NewClockInfo implements Parcelable {
    private int week;
    private int hour;
    private int minute;
    private int open;
    private int mode;//2-待机 3-随机 4-延边 6-规划 7-回冲
    private int type;//0-默认 1-分区 2-分房
    private int times;//执行次数
    private String area;//划区坐标
    private int room;//
    private String scheduleKey;
    public NewClockInfo() {
    }

    public NewClockInfo(ScheduleBean bean) {
        this.week = bean.getWeek();
        this.hour = bean.getHour();
        this.minute = bean.getMinutes();
        this.open = bean.getEnable();
        this.mode = bean.getMode();
        this.type = bean.getType();
        this.times = bean.getLoop();
        this.area = bean.getArea();
        this.room = bean.getRoom();
    }

    public ScheduleBean toScheduleBean() {
        ScheduleBean scheduleBean = new ScheduleBean();
        scheduleBean.setWeek(this.week);
        scheduleBean.setHour(this.hour);
        scheduleBean.setMinutes(this.minute);
        scheduleBean.setEnable(this.open);
        scheduleBean.setMode(this.mode);
        scheduleBean.setType(this.type);
        scheduleBean.setLoop(this.times);
        scheduleBean.setArea(this.area);
        scheduleBean.setRoom(this.room);
        scheduleBean.setEnd(300);
       return scheduleBean;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.week);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
        dest.writeInt(this.open);
        dest.writeInt(this.mode);
        dest.writeInt(this.type);
        dest.writeInt(this.times);
        dest.writeString(this.area);
        dest.writeInt(this.room);
        dest.writeString(this.scheduleKey);
    }

    protected NewClockInfo(Parcel in) {
        this.week = in.readInt();
        this.hour = in.readInt();
        this.minute = in.readInt();
        this.open = in.readInt();
        this.mode = in.readInt();
        this.type = in.readInt();
        this.times = in.readInt();
        this.area = in.readString();
        this.room = in.readInt();
        this.scheduleKey=in.readString();
    }

    public String getScheduleKey() {
        return scheduleKey;
    }

    public void setScheduleKey(String scheduleKey) {
        this.scheduleKey = scheduleKey;
    }

    public static final Parcelable.Creator<NewClockInfo> CREATOR = new Parcelable.Creator<NewClockInfo>() {
        @Override
        public NewClockInfo createFromParcel(Parcel source) {
            return new NewClockInfo(source);
        }

        @Override
        public NewClockInfo[] newArray(int size) {
            return new NewClockInfo[size];
        }
    };
}
