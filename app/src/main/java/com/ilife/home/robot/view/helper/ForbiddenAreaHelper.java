package com.ilife.home.robot.view.helper;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;

import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * contains global forbidden area and mop forbidden area
 */
public class ForbiddenAreaHelper {
    private static final String TAG = "ForbiddenAreaHelper";
    private List<VirtualWallBean> fbdBeans;
    private MapView mMapView;
    private Path mGlobalPath;
    private Path mMoplPath;
    private PointF downPoint;
    private RectF curRectF;
    private final int LENGTH = 20;//一条禁区数据占用的字节数
    private static final int MIN_FBD_LENGTH = 20;
    private VirtualWallBean curFbdBean;//当前操作的禁区对象
    public static final int TYPE_GLOBAL = 0;
    public static final int TYPE_MOP = 1;
    private FAOT faot = FAOT.ADD;
    private int mFbdAreaType = TYPE_GLOBAL;
    private int leftX, leftY;

    public enum FAOT {
        NOON(31),
        ADD(32),
        DELETE(33),
        DRAG(34),
        ROTATE(35);
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
        this.mMoplPath = new Path();
        this.downPoint = new PointF();
        this.curRectF = new RectF();
    }

    public Path getmGlobalPath() {
        return mGlobalPath;
    }

    public Path getmMoplPath() {
        return mMoplPath;
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
                    coor =Math.round (coordinate[i] + leftX);
                } else {
                    coor = Math.round(leftY - coordinate[i]);
                }
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
    public void setForbiddenArea(int leftX, int leftY, String fbdStr) {
        this.leftX = leftX;
        this.leftY = leftY;
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
                tlx = DataUtils.bytesToInt(bytes[LENGTH * i + 4], bytes[LENGTH * i + 5]) - leftX;
                tly = leftY - DataUtils.bytesToInt(bytes[LENGTH * i + 6], bytes[LENGTH * i + 7]);
                trx = DataUtils.bytesToInt(bytes[LENGTH * i + 8], bytes[LENGTH * i + 9]) - leftX;
                try_ = leftY - DataUtils.bytesToInt(bytes[LENGTH * i + 10], bytes[LENGTH * i + 11]);
                blx = DataUtils.bytesToInt(bytes[LENGTH * i + 12], bytes[LENGTH * i + 13]) - leftX;
                bly = leftY - DataUtils.bytesToInt(bytes[LENGTH * i + 14], bytes[LENGTH * i + 15]);
                brx = DataUtils.bytesToInt(bytes[LENGTH * i + 16], bytes[LENGTH * i + 17]) - leftX;
                bry = leftY - DataUtils.bytesToInt(bytes[LENGTH * i + 18], bytes[LENGTH * i + 19]);
                vwBean = new VirtualWallBean(i, type, new float[]{tlx, tly, trx, try_, blx, bly, brx, bry}, 1);
                fbdBeans.add(vwBean);
            }
        }
        updatePath();
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
                    Matrix matrix = curFbdBean.getMatrix();
                    matrix.postTranslate(tx, ty);
                    curFbdBean.updateAreaRect();
                    updatePath();
                }
                break;
            case ROTATE:
                PointF centerP = curFbdBean.getCenterPoint();
                float angle = DataUtils.getAngle(centerP, downPoint, new PointF(mapX, mapY));
                curFbdBean.getMatrix().postRotate(angle, centerP.x, centerP.y);
                curFbdBean.updateAreaRect();
                updatePath();
                break;
            default:
                break;
        }
    }


    private void doOnActionUp(float mapX, float mapY) {
        switch (faot) {
            case ADD:
                curRectF.setEmpty();
                if (getUsefulFbdArea() < 10 && DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_FBD_LENGTH) {
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
                    fbdBeans.add(fbd);
                    updatePath();
                } else {
                    ToastUtils.showToast("最多添加10条禁区！");
                }
                updatePath();
                break;
            case DELETE:
                for (VirtualWallBean fbd : fbdBeans) {
                    if (fbd.getDeleteIcon().contains(mapX, mapY)) {
                        fbd.setState(3);
                        break;
                    }
                }
                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离
                if (curFbdBean != null) {//理论上不为空，为空时应该是在添加禁区
                    Matrix matrix = curFbdBean.getMatrix();
                    matrix.postTranslate(tx, ty);
                    curFbdBean.updateAreaRect();
                    updatePath();
                }
                break;
            case ROTATE:
                PointF centerP = curFbdBean.getCenterPoint();
                float angle = DataUtils.getAngle(centerP, downPoint, new PointF(mapX, mapY));
                curFbdBean.getMatrix().postRotate(angle, centerP.x, centerP.y);
                curFbdBean.updateAreaRect();
                updatePath();
                break;
            default:
                break;
        }
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
        mMoplPath.reset();
        Path realPath = null;
        float[] coordinate;
        for (VirtualWallBean fbd : fbdBeans) {
            coordinate = fbd.getPointCoordinate();
            realPath = fbd.getType() == TYPE_GLOBAL ? mGlobalPath : mMoplPath;
            if (realPath != null) {
                realPath.moveTo(mMapView.matrixCoordinateX(coordinate[0]), mMapView.matrixCoordinateY(coordinate[1]));
                realPath.lineTo(mMapView.matrixCoordinateX(coordinate[2]), mMapView.matrixCoordinateY(coordinate[3]));
                realPath.lineTo(mMapView.matrixCoordinateX(coordinate[4]), mMapView.matrixCoordinateY(coordinate[5]));
                realPath.lineTo(mMapView.matrixCoordinateX(coordinate[6]), mMapView.matrixCoordinateY(coordinate[7]));
            }

        }
        mMapView.invalidateUI();
    }
}
