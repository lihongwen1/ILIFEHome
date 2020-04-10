package com.ilife.home.robot.view.helper;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;

import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import java.util.Arrays;

/**
 * 清扫区域，只有一条清扫区域
 */
public class CleanAreaHelper {
    private static final String TAG = "ForbiddenAreaHelper";
    private MapView mMapView;
    private PointF downPoint;
    private PointF touchPoint;
    private RectF curRectF;
    private static final int MIN_FBD_LENGTH = 20;
    private VirtualWallBean curCleanAreaBean;//当前操作的禁区对象
    private CAOT caot = CAOT.ADD;
    private final int ICON_RADIUS = 50;
    private Matrix mMatrix;
    private Matrix boundaryMatrix;
    public enum CAOT {
        NOON(51),
        ADD(52),
        DELETE(53),
        DRAG(54),
        ROTATE(55),
        PULL(56);
        final int nativeType;

        CAOT(int type) {
            this.nativeType = type;
        }
    }


    public RectF getCurRectF() {
        return curRectF;
    }

    public VirtualWallBean getCurCleanAreaBean() {
        return curCleanAreaBean;
    }

    public CleanAreaHelper(MapView mapView) {
        this.mMapView = mapView;
        this.downPoint = new PointF();
        this.touchPoint = new PointF();
        this.curRectF = new RectF();
        this.mMatrix = new Matrix();
        this.boundaryMatrix=new Matrix();
    }


    /**
     * 获取划区数据
     * //todo 对坐标数据做四舍五入操作
     */
    public String getCleanAreaData() {
        byte[] bData = new byte[16];
        byte[] intToByte;
        int index = 0;
        int coor;
        if (curCleanAreaBean != null) {
            float[] coordinate = curCleanAreaBean.getPointCoordinate();
            for (int i = 0; i < coordinate.length; i++) {
                if (i % 2 == 0) {
                    coor = Math.round(coordinate[i]) ;
                } else {
                    coor =  - Math.round(coordinate[i]);
                }
                MyLogger.d(TAG, "划区坐标 ：" + coor);
                intToByte = DataUtils.intToBytes(coor);
                bData[index] = intToByte[0];
                index++;
                bData[index] = intToByte[1];
                index++;
            }
        }
        return Base64.encodeToString(bData, Base64.NO_WRAP);
    }


    /**
     * @param fbdStr
     */
        public void setCleanArea(String fbdStr) {
        if (!TextUtils.isEmpty(fbdStr)) {
            byte[] bytes = Base64.decode(fbdStr, Base64.DEFAULT);
            int tlx = DataUtils.bytesToInt(bytes[0], bytes[1]);
            int tly =  - DataUtils.bytesToInt(bytes[2], bytes[3]);
            int trx = DataUtils.bytesToInt(bytes[4], bytes[5]);
            int try_ =  - DataUtils.bytesToInt(bytes[6], bytes[7]);
            int blx = DataUtils.bytesToInt(bytes[8], bytes[9]) ;
            int bly = - DataUtils.bytesToInt(bytes[10], bytes[11]);
            int brx = DataUtils.bytesToInt(bytes[12], bytes[13]);
            int bry =  - DataUtils.bytesToInt(bytes[14], bytes[15]);
            curCleanAreaBean = new VirtualWallBean(1, 3, new float[]{tlx, tly, trx, try_, blx, bly, brx, bry}, 1);
            MyLogger.d(TAG, "清扫区域坐标: " + Arrays.toString(curCleanAreaBean.getPointCoordinate()));
        }
        updatePath();
    }


    /**
     * @param event 触摸事件
     * @param mapX  屏幕坐标转化后的地图坐标X
     * @param mapY  屏幕坐标转化后的地图坐标Y
     */
    public void onTouch(MotionEvent event, int mapX, int mapY) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        touchPoint.set(mapX, mapY);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                doOnActionDown(mapX, mapY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (caot != CAOT.NOON) {
                    doOnActionMove(mapX, mapY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (caot != CAOT.NOON) {
                    doOnActionMove(mapX, mapY);
                }
                doOnActionUp(mapX, mapY);
                break;
        }
        mMapView.invalidateUI();
    }

    private void doOnActionDown(float mapX, float mapY) {
        downPoint.set(mapX, mapY);
        mMatrix.reset();
        if (curCleanAreaBean == null) {
            caot = CAOT.ADD;
        } else if (curCleanAreaBean.getPullIcon() != null && curCleanAreaBean.getPullIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
            caot = CAOT.PULL;
        } else if (curCleanAreaBean.getDeleteIcon() != null && curCleanAreaBean.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
            caot = CAOT.DELETE;
        } else if (curCleanAreaBean.getRotateIcon() != null && curCleanAreaBean.getRotateIcon().contains(Math.round(mapX), Math.round(mapY))) {//drag
            caot = CAOT.ROTATE;
        } else if (curCleanAreaBean.getBoundaryRegion() != null && curCleanAreaBean.getBoundaryRegion().contains(Math.round(mapX), Math.round(mapY))) {//drag
            caot = CAOT.DRAG;
        } else {
            caot = CAOT.NOON;//此时忽略触摸事件
        }
    }

    private void doOnActionMove(float mapX, float mapY) {
        switch (caot) {
            case ADD:
                curRectF.set(downPoint.x, downPoint.y, mapX, mapY);
                updatePath();
                break;
            case DELETE:
                //删除up处理
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离
                if (curCleanAreaBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    updatePath();
                }
                break;
            case ROTATE:
                PointF centerP = curCleanAreaBean.getCenterPoint();
                float x = mMapView.matrixCoordinateX(centerP.x);
                float y = mMapView.matrixCoordinateY(centerP.y);
                centerP.set(x, y);
                float angle = DataUtils.getAngle(centerP, downPoint, new PointF(mapX, mapY));
                mMatrix.reset();
                mMatrix.postRotate(angle, centerP.x, centerP.y);
                updatePath();
                break;
            case PULL:
                float[] matrixCoordinate = toMapCoordinate(curCleanAreaBean.getPointCoordinate());
                float k = (matrixCoordinate[3] - matrixCoordinate[1]) / (matrixCoordinate[2] - matrixCoordinate[0]);
                float degree = (float) (Math.atan(k) * 180 / Math.PI);
                mMatrix.reset();
                mMatrix.setTranslate(-matrixCoordinate[0], -matrixCoordinate[1]);
                mMatrix.postRotate(-degree, 0, 0);
                updatePath();
                break;
            default:
                break;
        }
    }


    private void doOnActionUp(float mapX, float mapY) {
        switch (caot) {
            case ADD:
                if (Math.abs(mapX - downPoint.x) > MIN_FBD_LENGTH && Math.abs(mapY - downPoint.y) > MIN_FBD_LENGTH) {
                    float x1, y1, x2, y2;
                    /**
                     * 转化为云端坐标系坐标
                     */
                    x1 = mMapView.reMatrixCoordinateX(downPoint.x < mapX ? downPoint.x : mapX);//x1
                    y1 = mMapView.reMatrixCoordinateY(downPoint.y < mapY ? downPoint.y : mapY);//y1
                    x2 = mMapView.reMatrixCoordinateX(downPoint.x > mapX ? downPoint.x : mapX);//x2
                    y2 = mMapView.reMatrixCoordinateY(downPoint.y > mapY ? downPoint.y : mapY);//y2
                    float[] coordinate = new float[]{x1, y1, x2, y1, x2, y2, x1, y2};
                    curCleanAreaBean = new VirtualWallBean(1, 3, coordinate, 2);
                    updatePath();
                }
                break;
            case DELETE:
                if ((mapX == downPoint.x && mapY == downPoint.y) || DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) < 10) {//点击事件
                    if (curCleanAreaBean != null) {
                        if (curCleanAreaBean.getDeleteIcon() != null && curCleanAreaBean.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了删除键
                            curCleanAreaBean = null;
                            updatePath();
                        }
                    }
                }
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离 预览地图偏移量
                if (curCleanAreaBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curCleanAreaBean.getPointCoordinate()));
                    updatePath();
                }
                float ctx = mMapView.reMatrixCoordinateX(mapX) - mMapView.reMatrixCoordinateX(downPoint.x);//x轴平移距离
                float cty = mMapView.reMatrixCoordinateY(mapY) - mMapView.reMatrixCoordinateY(downPoint.y);//y轴平移距离 主机坐标偏移量
                mMatrix.reset();
                mMatrix.postTranslate(ctx, cty);
                curCleanAreaBean.updateCoordinateWithMatrix(mMatrix);
                break;
            case ROTATE:
                PointF centerP = curCleanAreaBean.getCenterPoint();
                float x = mMapView.matrixCoordinateX(centerP.x);
                float y = mMapView.matrixCoordinateY(centerP.y);
                float angle = DataUtils.getAngle(new PointF(x, y), downPoint, new PointF(mapX, mapY));
                /**
                 * 更新坐标
                 */
                mMatrix.reset();
                mMatrix.postRotate(angle, centerP.x, centerP.y);
                curCleanAreaBean.updateCoordinateWithMatrix(mMatrix);
                mMatrix.reset();
                updatePath();
                break;
            case PULL:
                float[] matrixCoordinate = toMapCoordinate(curCleanAreaBean.getPointCoordinate());
                float k = (matrixCoordinate[3] - matrixCoordinate[1]) / (matrixCoordinate[2] - matrixCoordinate[0]);
                float degree = (float) (Math.atan(k) * 180 / Math.PI);
                mMatrix.reset();
                mMatrix.setTranslate(-matrixCoordinate[0], -matrixCoordinate[1]);
                mMatrix.postRotate(-degree, 0, 0);

                float[] touchCoordinate = new float[]{mapX, mapY};
                mMatrix.mapPoints(touchCoordinate);
                mMatrix.mapPoints(matrixCoordinate);
                matrixCoordinate[2] = touchCoordinate[0];
                matrixCoordinate[3] = matrixCoordinate[1];

                matrixCoordinate[4] = touchCoordinate[0];
                matrixCoordinate[5] = touchCoordinate[1];

                matrixCoordinate[6] = matrixCoordinate[0];
                matrixCoordinate[7] = touchCoordinate[1];
                mMatrix.invert(mMatrix);
                mMatrix.mapPoints(matrixCoordinate);
                curCleanAreaBean.setPointCoordinate(toRobotCoordinate(matrixCoordinate));
                mMatrix.reset();
                updatePath();
                break;
        }
        mMatrix.reset();
        curRectF.setEmpty();
        caot = CAOT.ADD;

    }


    /**
     * draw history forbidden area,draw the adding forbidden area
     */
    private void updatePath() {
        //TODO draw forbidden area
        float[] matrixCoordinate;
        float[] boundaryCoordinate;
        Region boundaryRegion;
        if (curCleanAreaBean == null) {
            mMapView.invalidateUI();
            return;
        }
        matrixCoordinate = toMapCoordinate(curCleanAreaBean.getPointCoordinate());
        if (mMatrix != null && !mMatrix.isIdentity()) {
            if (caot == CAOT.PULL) {
                float[] touchCoordinate = new float[]{touchPoint.x, touchPoint.y};
                mMatrix.mapPoints(touchCoordinate);
                mMatrix.mapPoints(matrixCoordinate);
                matrixCoordinate[2] = touchCoordinate[0];
                matrixCoordinate[3] = matrixCoordinate[1];

                matrixCoordinate[4] = touchCoordinate[0];
                matrixCoordinate[5] = touchCoordinate[1];

                matrixCoordinate[6] = matrixCoordinate[0];
                matrixCoordinate[7] = touchCoordinate[1];
                mMatrix.invert(mMatrix);
                mMatrix.mapPoints(matrixCoordinate);
            } else {
                mMatrix.mapPoints(matrixCoordinate);
            }
        }
        boundaryCoordinate = new float[matrixCoordinate.length];
        System.arraycopy(matrixCoordinate, 0, boundaryCoordinate, 0, matrixCoordinate.length);
        boundaryMatrix.setScale(1.2f, 1.2f,
                (boundaryCoordinate[0] + boundaryCoordinate[4]) / 2, (boundaryCoordinate[1] + boundaryCoordinate[5]) / 2);
        boundaryMatrix.mapPoints(boundaryCoordinate);
        float minx = boundaryCoordinate[0], miny = boundaryCoordinate[1], maxx = boundaryCoordinate[0], maxy = boundaryCoordinate[1];
        for (int i = 0; i < boundaryCoordinate.length; i++) {
            float value = boundaryCoordinate[i];
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

        if (curCleanAreaBean.getPath() == null) {
            curCleanAreaBean.setPath(new Path());
        }
        /**
         * 划去
         */
        Path realPath = curCleanAreaBean.getPath();
        realPath.reset();
        realPath.moveTo(matrixCoordinate[0], matrixCoordinate[1]);
        realPath.lineTo(matrixCoordinate[2], matrixCoordinate[3]);
        realPath.lineTo(matrixCoordinate[4], matrixCoordinate[5]);
        realPath.lineTo(matrixCoordinate[6], matrixCoordinate[7]);
        realPath.close();
        /**
         * 边界
         */
        if (curCleanAreaBean.getBoundaryPath()==null){
            curCleanAreaBean.setBoundaryPath(new Path());
        }
        Path boundaryPath=curCleanAreaBean.getBoundaryPath();
        boundaryPath.reset();
        boundaryPath.moveTo(boundaryCoordinate[0], boundaryCoordinate[1]);
        boundaryPath.lineTo(boundaryCoordinate[2], boundaryCoordinate[3]);
        boundaryPath.lineTo(boundaryCoordinate[4], boundaryCoordinate[5]);
        boundaryPath.lineTo(boundaryCoordinate[6], boundaryCoordinate[7]);
        boundaryPath.close();
        boundaryRegion = new Region((int) minx, (int) miny, (int) maxx, (int) maxy);
        boundaryRegion.setPath(boundaryPath, boundaryRegion);
        curCleanAreaBean.setBoundaryRegion(boundaryRegion);
        curCleanAreaBean.setDeleteIcon(new RectF(boundaryCoordinate[0] - ICON_RADIUS, boundaryCoordinate[1] - ICON_RADIUS, boundaryCoordinate[0] + ICON_RADIUS, matrixCoordinate[1] + ICON_RADIUS));
        curCleanAreaBean.setRotateIcon(new RectF(boundaryCoordinate[2] - ICON_RADIUS, boundaryCoordinate[3] - ICON_RADIUS, boundaryCoordinate[2] + ICON_RADIUS, boundaryCoordinate[3] + ICON_RADIUS));
        curCleanAreaBean.setPullIcon(new RectF(boundaryCoordinate[4] - ICON_RADIUS, boundaryCoordinate[5] - ICON_RADIUS, boundaryCoordinate[4] + ICON_RADIUS, boundaryCoordinate[5] + ICON_RADIUS));
        mMapView.invalidateUI();
    }

    /**
     * 根据地图坐标计算主机坐标
     *
     * @param coordinate 地图坐标
     */
    private float[] toRobotCoordinate(float[] coordinate) {
        float[] robotCoor = new float[coordinate.length];
        int index = 0;
        for (float coo : coordinate) {
            if (index % 2 == 0) {
                robotCoor[index] = mMapView.reMatrixCoordinateY(coo);
            } else {
                robotCoor[index] = mMapView.reMatrixCoordinateY(coo);
            }
            index++;
        }
        return robotCoor;
    }

    /**
     * 根据主机坐标计算地图坐标
     *
     * @param coordinate
     */
    private float[] toMapCoordinate(float[] coordinate) {
        float[] mapCoor = new float[coordinate.length];
        int index = 0;
        for (float coo : coordinate) {
            if (index % 2 == 0) {
                mapCoor[index] = mMapView.matrixCoordinateX(coo);
            } else {
                mapCoor[index] = mMapView.matrixCoordinateY(coo);
            }
            index++;
        }
        return mapCoor;
    }
}
