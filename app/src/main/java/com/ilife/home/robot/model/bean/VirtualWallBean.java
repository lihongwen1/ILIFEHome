package com.ilife.home.robot.model.bean;

import android.graphics.RectF;

/**
 * 电子墙数据结构类
 */
public class VirtualWallBean {
    private  int number;
    private int[] pointfs;
    private RectF deleteIcon;
    private int state;//1-original   2-new added 3-may delete

    public int[] getPointfs() {
        return pointfs;
    }

    public void setPointfs(int[] pointfs) {
        this.pointfs = pointfs;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public VirtualWallBean(int number, int[] pointfs, int state) {
        this.number = number;
        this.pointfs = pointfs;
        this.state = state;
    }

    public RectF getDeleteIcon() {
        return deleteIcon;
    }

    public void setDeleteIcon(RectF deleteIcon) {
        this.deleteIcon = deleteIcon;
    }
}
