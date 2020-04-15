package com.aliyun.iot.aep.sdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;

public class ScheduleBean implements Parcelable {


    /**
     * hour : 0
     * type : 0
     * end : 300
     * enable : 0
     * mode : 6
     * loop : 0
     * week : 1
     * area : AAAAAAAAAAAAAAAAAAAAAA==
     * room : 0
     * minutes : 0
     */
    @JSONField(name = "ScheduleHour")
    private int hour;
    @JSONField(name = "ScheduleType")
    private int type;
    @JSONField(name = "ScheduleEnd")
    private int end;
    @JSONField(name = "ScheduleEnable")
    private int enable;
    @JSONField(name = "ScheduleMode")
    private int mode;
    @JSONField(name = "ScheduleLoop")
    private int loop;
    @JSONField(name = "ScheduleWeek")
    private int week;
    @JSONField(name = "ScheduleArea")
    private String area;
    @JSONField(name = "ScheduleRoom")
    private int room;
    @JSONField(name = "ScheduleMinutes")
    private int minutes;
    private transient int keyIndex;

    public int getHour() {
        return hour;
    }

    public void setHour(int ScheduleHour) {
        this.hour = ScheduleHour;
    }

    public int getType() {
        return type;
    }

    public void setType(int ScheduleType) {
        this.type = ScheduleType;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int ScheduleEnd) {
        this.end = ScheduleEnd;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int ScheduleEnable) {
        this.enable = ScheduleEnable;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int ScheduleMode) {
        this.mode = ScheduleMode;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int ScheduleLoop) {
        this.loop = ScheduleLoop;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int ScheduleWeek) {
        this.week = ScheduleWeek;
    }

    public String getArea() {
        if (TextUtils.isEmpty(area)) {
            area = "AAAAAAAAAAAAAAAAAAAAAA==";
        }
        return area;
    }

    public void setArea(String ScheduleArea) {
        this.area = ScheduleArea;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int ScheduleRoom) {
        this.room = ScheduleRoom;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int ScheduleMinutes) {
        this.minutes = ScheduleMinutes;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

   public void copy(ScheduleBean that){
        this.room=that.room;
        this.loop=that.loop;
        this.hour=that.hour;
        this.minutes=that.minutes;
        this.area=that.area;
        this.enable=that.enable;
        this.week=that.week;
        this.end=that.end;
        this.mode=that.mode;
        this.type=this.type;
   }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hour);
        dest.writeInt(this.type);
        dest.writeInt(this.end);
        dest.writeInt(this.enable);
        dest.writeInt(this.mode);
        dest.writeInt(this.loop);
        dest.writeInt(this.week);
        dest.writeString(this.area);
        dest.writeInt(this.room);
        dest.writeInt(this.minutes);
        dest.writeInt(this.keyIndex);
    }

    public void reset() {
        week = 0;
        enable = 0;
        area = "AAAAAAAAAAAAAAAAAAAAAA==";
        minutes = 0;
        hour = 0;
        loop = 0;
        room=0;
    }

    public ScheduleBean() {
    }

    protected ScheduleBean(Parcel in) {
        this.hour = in.readInt();
        this.type = in.readInt();
        this.end = in.readInt();
        this.enable = in.readInt();
        this.mode = in.readInt();
        this.loop = in.readInt();
        this.week = in.readInt();
        this.area = in.readString();
        this.room = in.readInt();
        this.minutes = in.readInt();
        this.keyIndex = in.readInt();
    }

    public static final Parcelable.Creator<ScheduleBean> CREATOR = new Parcelable.Creator<ScheduleBean>() {
        @Override
        public ScheduleBean createFromParcel(Parcel source) {
            return new ScheduleBean(source);
        }

        @Override
        public ScheduleBean[] newArray(int size) {
            return new ScheduleBean[size];
        }
    };
}
