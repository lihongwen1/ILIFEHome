package com.ilife.home.robot.model.bean;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 电子墙数据结构类
 */
public class VirtualWallBean {
    private int number;
    private int[] pointCoordinate;//virtual wall have four data,forbidden area have eight data
    private RectF deleteIcon;//delete virtual wall icon
    private Rect pullIcon;//pull virtual wall icon,change the wall's end point,may be change the virtual wall's size;
    private Rect rotateWallIcon;//rotate the virtual wall ,won't change the it size;
    private int state;//1-original   2-new added 3-may delete
    private RectF areaRect;//禁区区域矩形
    private float rotateAngle;
    private float translationX, translationY;

    public int[] getPointCoordinate() {
        return pointCoordinate;
    }

    public void setPointCoordinate(int[] pointCoordinate) {
        this.pointCoordinate = pointCoordinate;
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

    public VirtualWallBean(int number, int[] pointCoordinate, int state) {
        this.number = number;
        this.pointCoordinate = pointCoordinate;
        this.state = state;
    }

    public VirtualWallBean(int number, int[] pointCoordinate, RectF areaRect, int state) {
        this.number = number;
        this.pointCoordinate = pointCoordinate;
        this.state = state;
        this.areaRect = areaRect;
    }

    public RectF getDeleteIcon() {
        return deleteIcon;
    }

    public void setDeleteIcon(RectF deleteIcon) {
        this.deleteIcon = deleteIcon;
    }

    public Rect getPullIcon() {
        return pullIcon;
    }

    public void setPullIcon(Rect pullIcon) {
        this.pullIcon = pullIcon;
    }

    public Rect getRotateWallIcon() {
        return rotateWallIcon;
    }

    public RectF getAreaRect() {
        return areaRect;
    }

    public void setAreaRect(RectF areaRect) {
        this.areaRect = areaRect;
    }

    public void setRotateWallIcon(Rect rotateWallIcon) {
        this.rotateWallIcon = rotateWallIcon;
    }

    public float getRotateAngle() {
        return rotateAngle;
    }

    public void updateAngle(float rotateAngle) {
        this.rotateAngle = this.rotateAngle + rotateAngle;
    }

    public float getTranslationX() {
        return translationX;
    }

    public void updateTranslationX(float translationX) {
        this.translationX = this.translationX + translationX;
    }

    public float getTranslationY() {
        return translationY;
    }

    public void updateTranslationY(float translationY) {
        this.translationY = this.translationY + translationY;
    }

    public void updateAreaRect() {
        if (areaRect != null) {
            float newLeft = areaRect.left + translationX;
            float newTop = areaRect.top + translationY;
            float newRight = areaRect.right + translationX;
            float newBottom = areaRect.bottom + translationY;
            areaRect.set(newLeft, newTop, newRight, newBottom);
        }
    }

    public PointF getCenterPoint() {
        return new PointF(areaRect.centerX(), areaRect.centerY());
    }

    /**
     * 对虚拟墙/禁区的修改取消保存时，必须调用
     */
    public void eraseChange() {
        translationX = 0;
        translationY = 0;
        rotateAngle = 0;
    }
}
