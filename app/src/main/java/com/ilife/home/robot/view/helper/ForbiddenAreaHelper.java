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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * //todo 旋转，拉伸导致禁区区域不为正矩形的bug
 * contains global forbidden area and mop forbidden area
 */
public class ForbiddenAreaHelper {
    private static final String TAG = "ForbiddenAreaHelper";
    private List<VirtualWallBean> fbdBeans;
    private MapView mMapView;
    private Path mGlobalPath;
    private Path mMopPath;
    private PointF downPoint;
    private PointF touchPoint;
    private RectF curRectF;
    private final int LENGTH = 20;//一条禁区数据占用的字节数
    private static final int MIN_FBD_LENGTH = 20;
    private VirtualWallBean curFbdBean;//当前操作的禁区对象
    public static final int TYPE_GLOBAL = 0;
    public static final int TYPE_MOP = 1;
    private FAOT faot = FAOT.ADD;
    private int mFbdAreaType = TYPE_GLOBAL;
    private final int ICON_RADIUS = 50;
    private int selectVwNum = -1;
    private Matrix mMatrix;
    private Matrix boundaryMatrix;
    public enum FAOT {
        NOON(31),
        ADD(32),
        DELETE(33),
        DRAG(34),
        ROTATE(35),
        PULL(36);
        final int nativeType;

        FAOT(int type) {
            this.nativeType = type;
        }
    }

    public void setmFbdAreaType(int mFbdAreaType) {
        this.mFbdAreaType = mFbdAreaType;
    }

    public List<VirtualWallBean> getFbdBeans() {
        return fbdBeans;
    }

    public RectF getCurRectF() {
        return curRectF;
    }

    public ForbiddenAreaHelper(MapView mapView) {
        fbdBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.mGlobalPath = new Path();
        this.mMopPath = new Path();
        this.downPoint = new PointF();
        this.touchPoint = new PointF();
        this.curRectF = new RectF();
        this.mMatrix = new Matrix();
        this.boundaryMatrix=new Matrix();
    }

    public Path getmGlobalPath() {
        return mGlobalPath;
    }

    public Path getmMopPath() {
        return mMopPath;
    }

    /**
     * 获取禁区数据
     * //todo 对坐标数据做四舍五入操作
     */
    public String getFbdaData() {
        List<VirtualWallBean> usefulVr = new ArrayList<>();
        for (VirtualWallBean vr : fbdBeans) {
            if (vr.getState() != 3) {
                usefulVr.add(vr);
            }
        }
        byte[] bData = new byte[usefulVr.size() * 20];
        byte[] intToByte;
        int index = 0;
        float[] coordinate;
        int coor;
        for (VirtualWallBean vr : usefulVr) {
            coordinate = vr.getPointCoordinate();
            byte[] b_type = DataUtils.intToBytes4(vr.getType());
            bData[index] = b_type[0];
            index++;
            bData[index] = b_type[1];
            index++;
            bData[index] = b_type[2];
            index++;
            bData[index] = b_type[3];
            index++;
            for (int i = 0; i < coordinate.length; i++) {
                if (i % 2 == 0) {
                    coor = Math.round(coordinate[i]) ;
                } else {
                    coor =  - Math.round(coordinate[i]);
                }
                MyLogger.d(TAG, "禁区坐标 ：" + coor);
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
    public void setForbiddenArea(String fbdStr) {
        if (!TextUtils.isEmpty(fbdStr)) {
            byte[] bytes = Base64.decode(fbdStr, Base64.DEFAULT);
            int vwCounts = bytes.length / LENGTH;//一条虚拟墙含12个字节，4个保留字节，加4个坐标（x,y）
            fbdBeans.clear();
            int tlx, tly, trx, try_, blx, bly, brx, bry, type;
            VirtualWallBean vwBean;
            byte[] bType;
            for (int i = 0; i < vwCounts; i++) {
                bType = new byte[]{bytes[LENGTH * i], bytes[LENGTH * i + 1], bytes[LENGTH * i + 2], bytes[LENGTH * i + 3]};
                type = DataUtils.bytesToInt(bType);
                tlx = DataUtils.bytesToInt(bytes[LENGTH * i + 4], bytes[LENGTH * i + 5]) ;
                tly =  - DataUtils.bytesToInt(bytes[LENGTH * i + 6], bytes[LENGTH * i + 7]);
                trx = DataUtils.bytesToInt(bytes[LENGTH * i + 8], bytes[LENGTH * i + 9]);
                try_ =  - DataUtils.bytesToInt(bytes[LENGTH * i + 10], bytes[LENGTH * i + 11]);
                blx = DataUtils.bytesToInt(bytes[LENGTH * i + 12], bytes[LENGTH * i + 13]);
                bly =  - DataUtils.bytesToInt(bytes[LENGTH * i + 14], bytes[LENGTH * i + 15]);
                brx = DataUtils.bytesToInt(bytes[LENGTH * i + 16], bytes[LENGTH * i + 17]);
                bry =  - DataUtils.bytesToInt(bytes[LENGTH * i + 18], bytes[LENGTH * i + 19]);
                vwBean = new VirtualWallBean(i + 1, type, new float[]{tlx, tly, trx, try_, blx, bly, brx, bry}, 1);
                fbdBeans.add(vwBean);
                MyLogger.d(TAG, "禁区坐标: " + Arrays.toString(vwBean.getPointCoordinate()));
            }
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
                doOnActionMove(mapX, mapY);
                break;
            case MotionEvent.ACTION_UP:
                doOnActionUp(mapX, mapY);
                break;
        }
        mMapView.invalidateUI();
    }

    private void doOnActionDown(float mapX, float mapY) {
        downPoint.set(mapX, mapY);
        mMatrix.reset();
        for (VirtualWallBean vw : fbdBeans) {
            if (vw.getPullIcon() != null && vw.getPullIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                faot = FAOT.PULL;
                Log.d(TAG, "拉伸禁区");
                curFbdBean = vw;
                break;
            } else if (vw.getDeleteIcon() != null && vw.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了拉长图标
                faot = FAOT.DELETE;
                Log.d(TAG, "删除禁区");
                curFbdBean = vw;
                break;
            } else if (vw.getRotateIcon() != null && vw.getRotateIcon().contains(Math.round(mapX), Math.round(mapY))) {//drag
                Log.d(TAG, "旋转禁区");
                selectVwNum = vw.getNumber();
                faot = FAOT.ROTATE;
                curFbdBean = vw;
                break;
            } else if (vw.getBoundaryRegion() != null && vw.getBoundaryRegion().contains(Math.round(mapX), Math.round(mapY))) {//drag
                Log.d(TAG, "选择禁区");
                selectVwNum = vw.getNumber();
                faot = FAOT.DRAG;
                curFbdBean = vw;
                break;
            }
        }
        if (curFbdBean != null) {
            fbdBeans.remove(curFbdBean);
            fbdBeans.add(0, curFbdBean);
        }
    }

    private void doOnActionMove(float mapX, float mapY) {
        switch (faot) {
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
                if (curFbdBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    updatePath();
                }
                break;
            case ROTATE:
                PointF centerP = curFbdBean.getCenterPoint();
                float x = mMapView.matrixCoordinateX(centerP.x);
                float y = mMapView.matrixCoordinateY(centerP.y);
                centerP.set(x, y);
                float angle = DataUtils.getAngle(centerP, downPoint, new PointF(mapX, mapY));
                mMatrix.reset();
                mMatrix.postRotate(angle, centerP.x, centerP.y);
                updatePath();
                break;
            case PULL:
                float[] matrixCoordinate = toMapCoordinate(curFbdBean.getPointCoordinate());
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
        switch (faot) {
            case ADD:
                if (getUsefulFbdArea() < 10 &&Math.abs( mapX-downPoint.x )> MIN_FBD_LENGTH&&Math.abs(mapY-downPoint.y)>MIN_FBD_LENGTH) {
                    float x1, y1, x2, y2;
                    /**
                     * 转化为云端坐标系坐标
                     */
                    x1 = mMapView.reMatrixCoordinateX(downPoint.x < mapX ? downPoint.x : mapX);//x1
                    y1 = mMapView.reMatrixCoordinateY(downPoint.y < mapY ? downPoint.y : mapY);//y1
                    x2 = mMapView.reMatrixCoordinateX(downPoint.x > mapX ? downPoint.x : mapX);//x2
                    y2 = mMapView.reMatrixCoordinateY(downPoint.y > mapY ? downPoint.y : mapY);//y2
                    float[] coordinate = new float[]{x1, y1, x2, y1, x2, y2, x1, y2};
                    VirtualWallBean fbd = new VirtualWallBean(fbdBeans.size() + 1, mFbdAreaType, coordinate, 2);
                    fbdBeans.add(0,fbd);
                    selectVwNum=fbd.getNumber();
                    updatePath();
                }
                break;
            case DELETE:
                if ((mapX == downPoint.x && mapY == downPoint.y) || DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) < 10) {//点击事件
                    if (curFbdBean != null) {
                        if (curFbdBean.getDeleteIcon() != null && curFbdBean.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了删除键
                            if (curFbdBean.getState() == 2) {//新增的电子墙，还未保存到服务器，可以直接移除
                                fbdBeans.remove(curFbdBean);
                            }
                            if (curFbdBean.getState() == 1) {//服务器上的电子墙，可能操作会被取消掉，只需要改变状态
                                curFbdBean.setState(3);
                                curFbdBean.clear();
                            }
                            selectVwNum = -1;
                            updatePath();
                        }
                    }
                }
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离 预览地图偏移量
                if (curFbdBean != null) {//理论上不为空，为空时应该是在添加禁区
                    mMatrix.reset();
                    mMatrix.postTranslate(tx, ty);
                    MyLogger.d(TAG, "变换后的坐标:" + Arrays.toString(curFbdBean.getPointCoordinate()));
                    updatePath();
                }
                float ctx = mMapView.reMatrixCoordinateX(mapX) - mMapView.reMatrixCoordinateX(downPoint.x);//x轴平移距离
                float cty = mMapView.reMatrixCoordinateY(mapY) - mMapView.reMatrixCoordinateY(downPoint.y);//y轴平移距离 主机坐标偏移量
                mMatrix.reset();
                mMatrix.postTranslate(ctx, cty);
                curFbdBean.updateCoordinateWithMatrix(mMatrix);
                break;
            case ROTATE:
                PointF centerP = curFbdBean.getCenterPoint();
                float x = mMapView.matrixCoordinateX(centerP.x);
                float y = mMapView.matrixCoordinateY(centerP.y);
                float angle = DataUtils.getAngle(new PointF(x, y), downPoint, new PointF(mapX, mapY));
                /**
                 * 更新坐标
                 */
                mMatrix.reset();
                mMatrix.postRotate(angle, centerP.x, centerP.y);
                curFbdBean.updateCoordinateWithMatrix(mMatrix);
                mMatrix.reset();
                updatePath();
                break;
            case PULL:
                float[] matrixCoordinate = toMapCoordinate(curFbdBean.getPointCoordinate());
                float k = (matrixCoordinate[3] - matrixCoordinate[1]) / (matrixCoordinate[2] - matrixCoordinate[0]);
                float degree = (float) (Math.atan(k) * 180 / Math.PI);
                mMatrix.reset();
                mMatrix.setTranslate(-matrixCoordinate[0], -matrixCoordinate[1]);
                mMatrix.postRotate(-degree, 0, 0);

                float[] touchCoordinate =  new float[]{touchPoint.x, touchPoint.y};
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
                curFbdBean.setPointCoordinate(toRobotCoordinate(matrixCoordinate));
                mMatrix.reset();
                updatePath();
                break;
        }
        curFbdBean = null;
        mMatrix.reset();
        curRectF.setEmpty();
        faot = FAOT.ADD;

    }


    /**
     * the method which will be used to deal with virtual wall data
     */

    private int getUsefulFbdArea() {
        int num = 0;
        for (VirtualWallBean vb : fbdBeans) {
            if (vb.getState() == 1 || vb.getState() == 2) {
                num++;
            }
        }
        MyLogger.d(TAG, "useful wall number:" + num);
        return num;
    }

    /**
     * draw history forbidden area,draw the adding forbidden area
     */
    private void updatePath() {
        //TODO draw forbidden area
        mGlobalPath.reset();
        mMopPath.reset();
        Path realPath;
        float[] matrixCoordinate;
        float[] boundaryCoordinate;
        Region boundaryRegion;
        for (VirtualWallBean fbd : fbdBeans) {
            if (fbd.getState() == 3) {
                continue;
            }
            matrixCoordinate = toMapCoordinate(fbd.getPointCoordinate());
            if (selectVwNum == fbd.getNumber()&&mMatrix!=null&&!mMatrix.isIdentity()) {
                if (faot == FAOT.PULL&&!mMatrix.isIdentity()) {
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

            if (fbd.getPath() == null) {
                fbd.setPath(new Path());
            }
            realPath = fbd.getPath();
            realPath.reset();
            realPath.moveTo(matrixCoordinate[0], matrixCoordinate[1]);
            realPath.lineTo(matrixCoordinate[2], matrixCoordinate[3]);
            realPath.lineTo(matrixCoordinate[4], matrixCoordinate[5]);
            realPath.lineTo(matrixCoordinate[6], matrixCoordinate[7]);
            realPath.close();
            /**
             * 边界
             */
            if (fbd.getBoundaryPath()==null){
                fbd.setBoundaryPath(new Path());
            }
            Path boundaryPath=fbd.getBoundaryPath();
            boundaryPath.reset();
            boundaryPath.moveTo(boundaryCoordinate[0], boundaryCoordinate[1]);
            boundaryPath.lineTo(boundaryCoordinate[2], boundaryCoordinate[3]);
            boundaryPath.lineTo(boundaryCoordinate[4], boundaryCoordinate[5]);
            boundaryPath.lineTo(boundaryCoordinate[6], boundaryCoordinate[7]);
            boundaryPath.close();
            boundaryRegion = new Region((int) minx, (int) miny, (int) maxx, (int) maxy);
            boundaryRegion.setPath(boundaryPath, boundaryRegion);
            fbd.setBoundaryRegion(boundaryRegion);
            fbd.setDeleteIcon(new RectF(boundaryCoordinate[0] - ICON_RADIUS, boundaryCoordinate[1] - ICON_RADIUS, boundaryCoordinate[0] + ICON_RADIUS, matrixCoordinate[1] + ICON_RADIUS));
            fbd.setRotateIcon(new RectF(boundaryCoordinate[2] - ICON_RADIUS, boundaryCoordinate[3] - ICON_RADIUS, boundaryCoordinate[2] + ICON_RADIUS, boundaryCoordinate[3] + ICON_RADIUS));
            fbd.setPullIcon(new RectF(boundaryCoordinate[4] - ICON_RADIUS, boundaryCoordinate[5] - ICON_RADIUS, boundaryCoordinate[4] + ICON_RADIUS, boundaryCoordinate[5] + ICON_RADIUS));
        }
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
                robotCoor[index] = mMapView.reMatrixCoordinateX(coo);
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

    public int getSelectVwNum() {
        return selectVwNum;
    }
    public void reserSelectNum(){
        selectVwNum=-1;
    }
}
