package com.ilife.home.robot.model.bean;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 电子墙数据结构类
 */
public class VirtualWallBean {
    private int number;
    private float[] pointCoordinate;//virtual wall have four data,forbidden area have eight data
    private RectF deleteIcon;//delete virtual wall icon
    private Rect pullIcon;//pull virtual wall icon,change the wall's end point,may be change the virtual wall's size;
    private Rect rotateWallIcon;//rotate the virtual wall ,won't change the it size;
    private int state;//1-original   2-new added 3-may delete
    private Matrix matrix;

    public float[] getPointCoordinate() {
        return pointCoordinate;
    }

    public void setPointCoordinate(float[] pointCoordinate) {
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

    public VirtualWallBean(int number, float[] pointCoordinate, int state) {
        this.number = number;
        this.pointCoordinate = pointCoordinate;
        this.state = state;
        this.matrix=new Matrix();
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


    public void setRotateWallIcon(Rect rotateWallIcon) {
        this.rotateWallIcon = rotateWallIcon;
    }

    public Matrix getMatrix() {
        return matrix;
    }
    /**
     * 基于矩阵变换更新禁区区域矩阵
     */
    public void updateAreaRect() {
        matrix.mapPoints(pointCoordinate);
    }

    /**
     * 根据位移和旋转，更新坐标
     */
    public void updateCoordinate() {
        matrix.mapPoints(pointCoordinate);
    }

    public PointF getCenterPoint() {
        RectF rectF = new RectF(pointCoordinate[0], pointCoordinate[1], pointCoordinate[4], pointCoordinate[5]);
        return new PointF(rectF.centerX(), rectF.centerY());
    }


}
