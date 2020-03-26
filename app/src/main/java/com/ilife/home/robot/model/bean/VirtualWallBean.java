package com.ilife.home.robot.model.bean;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * 电子墙数据结构类
 */
public class VirtualWallBean {
    private int number;
    private float[] pointCoordinate;//virtual wall have 4 data,forbidden area have 8 data
    private RectF deleteIcon;//delete virtual wall icon
    private Rect pullIcon;//pull virtual wall icon,change the wall's end point,may be change the virtual wall's size;
    private Rect rotateWallIcon;//rotate the virtual wall ,won't change the it size;
    private int state;//1-original   2-new added 3-may delete
    private Matrix matrix;
    private int type;//-1-virtual wall  0-global area 1-mop area 2-sweep area
    private Path boundaryPath;//虚拟墙边界框框path;
    private Region boundaryRegion;
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

    public VirtualWallBean(int number,int type, float[] pointCoordinate, int state) {
        this.number = number;
        this.type=type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Path getBoundaryPath() {
        return boundaryPath;
    }

    public void setBoundaryPath(Path boundaryPath) {
        if (this.boundaryPath==null){
            this.boundaryPath=new Path();
        }
        this.boundaryPath.set(boundaryPath);
    }

    public Region getBoundaryRegion() {
        return boundaryRegion;
    }

    public void setBoundaryRegion(Region boundaryRegion) {
        this.boundaryRegion = boundaryRegion;
    }
}
