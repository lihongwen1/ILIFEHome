package com.ilife.home.robot.view.helper;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;

import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class SegmentationRoomHelper {
    private static final String TAG = "VirtualWallHelper";
    private MapView mMapView;
    private SROT smOT = SROT.NOON;
    private PointF downPoint;
    private Region boundaryRegion;//分割线外框
    private Path sgPath, mBoundaryPath;//房间分割线路径
    private final int ICON_RADIUS = 100;
    private Matrix mMatrix, mBoundaryMatix;
    private int BOUNDARY_LENGTH = 100;
    private Path gatePath;
    private float[] mCoordinates = new float[4];
    private float[] gateCoordinates = new float[4];
    private float[] originalCoordinates = new float[4];
    private boolean isHaveLine = false;
    private boolean isNeedCalculateGate;
    private RectF startCircle, endCircle;
    private int radius;
    public enum SROT {
        NOON(61),
        DRAG(62),
        PULL_START(63),
        PULL_END(64),
        ADD(65);
        final int nativeType;

        SROT(int type) {
            this.nativeType = type;
        }
    }

    public Path getSgPath() {
        return sgPath;
    }


    public SegmentationRoomHelper(MapView mapView) {
        this.mMapView = mapView;
        this.sgPath = new Path();
        this.gatePath = new Path();
        this.mBoundaryPath = new Path();
        this.downPoint = new PointF();
        this.mMatrix = new Matrix();
        this.startCircle = new RectF();
        this.endCircle = new RectF();
        this.mBoundaryMatix = new Matrix();
        radius = MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.dp_20);
    }

    /**
     * @param event 触摸事件
     * @param mapX  屏幕坐标转化后的地图坐标X
     * @param mapY  屏幕坐标转化后的地图坐标Y
     */
    public void onTouch(MotionEvent event, float mapX, float mapY) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isNeedCalculateGate = false;
                doOnActionDown(mapX, mapY);
                break;
            case MotionEvent.ACTION_MOVE:
                doOnActionMove(mapX, mapY);
                break;
            case MotionEvent.ACTION_UP:
                isNeedCalculateGate = true;
                doOnActionUp(mapX, mapY);
                break;
        }
        mMapView.invalidateUI();
    }

    private void doOnActionDown(float mapX, float mapY) {
        downPoint.set(mapX, mapY);
        mMatrix.reset();
        if (!isHaveLine) {
            smOT = SROT.ADD;
            isHaveLine = true;
        } else {
            if (startCircle.contains(mapX, mapY)) {//点击了拉长图标
                smOT = SROT.PULL_START;
                Log.d(TAG, "拉伸分割线");
            } else if (endCircle.contains(mapX, mapY)) {//点击了拉长图标
                smOT = SROT.PULL_END;
                Log.d(TAG, "拉伸分割线");
            } else if (boundaryRegion.contains(Math.round(mapX), Math.round(mapY))) {//drag
                Log.d(TAG, "推拽分割线");
                smOT = SROT.DRAG;
                System.arraycopy(mCoordinates, 0, originalCoordinates, 0, mCoordinates.length);
            }
        }
    }

    private void doOnActionMove(float mapX, float mapY) {
        switch (smOT) {
            case ADD:
                mCoordinates[0] = downPoint.x;
                mCoordinates[1] = downPoint.y;
                mCoordinates[2] = mapX;
                mCoordinates[3] = mapY;
                updateVirtualWall();
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离
                mMatrix.reset();
                mMatrix.postTranslate(tx, ty);
                System.arraycopy(originalCoordinates, 0, mCoordinates, 0, originalCoordinates.length);
                mMatrix.mapPoints(mCoordinates);
                updateVirtualWall();
                break;
            case PULL_END:
                mCoordinates[2] = mapX;
                mCoordinates[3] = mapY;
                updateVirtualWall();
                break;
            case PULL_START:
                mCoordinates[0] = mapX;
                mCoordinates[1] = mapY;
                updateVirtualWall();
                break;
            default:
                break;
        }

    }

    /**
     * @param mapX
     * @param mapY
     */
    private void doOnActionUp(float mapX, float mapY) {
        isNeedCalculateGate = true;
        updateVirtualWall();
        smOT = SROT.NOON;
    }


    /**
     * 绘制电子墙
     * 根据最新的缩放比例刷新虚拟墙
     */
    public void updateVirtualWall() {
        sgPath.reset();
        gatePath.reset();
        sgPath.moveTo(mCoordinates[0], mCoordinates[1]);
        sgPath.lineTo(mCoordinates[2], mCoordinates[3]);


        /**
         * 绘制虚拟墙边界
         */
        mBoundaryMatix.reset();
        mBoundaryPath.reset();
        float[] cooBoundary = new float[8];
        float[] cooStrength=new float[4];
        float k = (mCoordinates[3] - mCoordinates[1]) / (mCoordinates[2]
                - mCoordinates[0]);
        float degree = (float) (Math.atan(k) * 180 / Math.PI);
        mBoundaryMatix.setTranslate(-mCoordinates[0], -mCoordinates[1]);
        mBoundaryMatix.postRotate(-degree, 0, 0);
        mBoundaryMatix.mapPoints(mCoordinates);
        int x_direction = mCoordinates[0] < mCoordinates[2] ? 1 : -1;
        cooBoundary[0] = mCoordinates[0] - x_direction * BOUNDARY_LENGTH;
        cooBoundary[1] = mCoordinates[1] - x_direction * BOUNDARY_LENGTH;
        cooBoundary[2] = mCoordinates[0] - x_direction * BOUNDARY_LENGTH;
        cooBoundary[3] = mCoordinates[1] + x_direction * BOUNDARY_LENGTH;
        cooBoundary[4] = mCoordinates[2] + x_direction * BOUNDARY_LENGTH;
        cooBoundary[5] = mCoordinates[3] + x_direction * BOUNDARY_LENGTH;
        cooBoundary[6] = mCoordinates[2] + x_direction * BOUNDARY_LENGTH;
        cooBoundary[7] = mCoordinates[3] - x_direction * BOUNDARY_LENGTH;

        cooStrength[0]=mCoordinates[0] - x_direction * BOUNDARY_LENGTH;
        cooStrength[1]=mCoordinates[1];
        cooStrength[2]=mCoordinates[2] + x_direction * BOUNDARY_LENGTH;
        cooStrength[3]=mCoordinates[3];

        mBoundaryMatix.invert(mBoundaryMatix);
        mBoundaryMatix.mapPoints(cooBoundary);
        mBoundaryMatix.mapPoints(mCoordinates);
        mBoundaryMatix.mapPoints(cooStrength);

        startCircle = new RectF(mCoordinates[0] - ICON_RADIUS, mCoordinates[1] - ICON_RADIUS, mCoordinates[0] + ICON_RADIUS, mCoordinates[1] + ICON_RADIUS);
        endCircle = new RectF(mCoordinates[2] - ICON_RADIUS, mCoordinates[3] - ICON_RADIUS, mCoordinates[2] + ICON_RADIUS, mCoordinates[3] + ICON_RADIUS);
        float minx = cooBoundary[0], miny = cooBoundary[1], maxx = cooBoundary[0], maxy = cooBoundary[1];
        for (int i = 0; i < cooBoundary.length; i++) {
            float value = cooBoundary[i];
            if (i % 2 == 0) {//x
                if (value < minx) {
                    minx = value;
                }
                if (value > maxx) {
                    maxx = value;
                }
            } else {//y
                if (value < miny) {
                    miny = value;
                }
                if (value > maxy) {
                    maxy = value;
                }
            }
        }
        boundaryRegion = new Region((int) minx, (int) miny, (int) maxx, (int) maxy);
        mBoundaryPath.moveTo(cooBoundary[0], cooBoundary[1]);
        mBoundaryPath.lineTo(cooBoundary[2], cooBoundary[3]);
        mBoundaryPath.lineTo(cooBoundary[4], cooBoundary[5]);
        mBoundaryPath.lineTo(cooBoundary[6], cooBoundary[7]);
        mBoundaryPath.close();
        boundaryRegion.setPath(mBoundaryPath, boundaryRegion);

        if (!isNeedCalculateGate) {
            return;
        }

        /**
         * 计算分割线
         */
        List<Coordinate> containsCoo = new ArrayList<>();
        for (Coordinate coo : mMapView.getPointList()) {
            if (coo.getType() == 2) {
                if (boundaryRegion.contains((int) mMapView.matrixCoordinateX(coo.getX()), (int) mMapView.matrixCoordinateY(coo.getY()))) {
                    double space = DataUtils.lineToPointSpace(mCoordinates[0], mCoordinates[1], mCoordinates[2], mCoordinates[3],
                            mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
                    MyLogger.d(TAG, "点线距离：    " + space);
                    if (space < 10) {
                        containsCoo.add(coo);
                    }

                }
            }

        }
        float mind1 = -1, mind2 = -1;
        for (Coordinate coo : containsCoo) {
            float dis1 = DataUtils.distance(mCoordinates[0], mCoordinates[1], mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
            float dis2 = DataUtils.distance(mCoordinates[2], mCoordinates[3], mMapView.matrixCoordinateX(coo.getX()), mMapView.matrixCoordinateY(coo.getY()));
            if (mind1 == -1 && mind2 == -1) {
                mind1 = dis1;
                mind2 = dis2;
            } else {
                if (dis1 < mind1) {
                    mind1 = dis1;
                }
                if (dis2 < mind2) {
                    mind2 = dis2;
                }
            }
        }
        gatePath.reset();
        PathMeasure pathMeasure = new PathMeasure();
        pathMeasure.setPath(sgPath, false);
        float[] pos = new float[2];
        float[] tan = new float[2];
        pathMeasure.getPosTan(mind1, pos, tan);
        gateCoordinates[0] = pos[0];
        gateCoordinates[1] = pos[1];
        pathMeasure.getPosTan(pathMeasure.getLength() - mind2, pos, tan);
        gateCoordinates[2] = pos[0];
        gateCoordinates[3] = pos[1];
        pathMeasure.getSegment(mind1, pathMeasure.getLength() - mind2, gatePath, true);

    }



    public RectF getStartCircle() {
        return startCircle;
    }

    public RectF getEndCircle() {
        return endCircle;
    }

    public boolean isHaveLine() {
        return isHaveLine;
    }

    public float[] getmCoordinates() {
        return mCoordinates;
    }

    public float[] getGateCoordinates() {
        return gateCoordinates;
    }

    public boolean isNeedCalculateGate() {
        return isNeedCalculateGate;
    }

    public int getRadius() {
        return radius;
    }
}
