package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;

import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * contains global forbidden area and mop forbidden area
 */
public class ForbiddenAreaHelper {
    private static final String TAG = "ForbiddenAreaHelper";
    private List<VirtualWallBean> fbdBeans;
    private MapView mMapView;
    private FAOT faot = FAOT.NOON;
    private Path mPath;
    private PointF downPoint;
    private RectF curRectF;
    private final int LENGTH = 20;//一条禁区数据占用的字节数
    private static final int MIN_FBD_LENGTH = 20;
    private VirtualWallBean curFbdBean;//当前操作的禁区对象

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

    public List<VirtualWallBean> getFbdBeans() {
        return fbdBeans;
    }

    public RectF getCurRectF() {
        return curRectF;
    }

    public ForbiddenAreaHelper(MapView mapView) {
        fbdBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.mPath = new Path();
        this.downPoint = new PointF();
        this.curRectF = new RectF();
    }

    public Path getmPath() {
        return mPath;
    }

    /**
     * 获取禁区数据
     */
    public String getFbdaData() {
        List<Integer> data = new ArrayList<>();
        for (VirtualWallBean fbd : fbdBeans) {
            if (fbd.getState() != 3) {
                for (int i : fbd.getPointCoordinate()) {
                    data.add(i);
                }
            }
        }
        byte[] bData = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            bData[i] = data.get(i).byteValue();
        }
        String dataStr = Base64.encodeToString(bData, Base64.DEFAULT);
        return dataStr;
    }


    public void setForbiddenArea(String fbdStr) {
        if (!TextUtils.isEmpty(fbdStr)) {
            byte[] bytes = Base64.decode(fbdStr, Base64.DEFAULT);
            int vwCounts = bytes.length / LENGTH;//一条虚拟墙含12个字节，4个保留字节，加4个坐标（x,y）
            fbdBeans.clear();
            int tlx, tly, trx, try_, blx, bly, brx, bry;
            VirtualWallBean vwBean;
            for (int i = 0; i < vwCounts; i++) {
                tlx = DataUtils.bytesToInt(bytes[LENGTH * i + 4], bytes[LENGTH * i + 5]) + 750;
                tly = 750 - DataUtils.bytesToInt(bytes[LENGTH * i + 6], bytes[LENGTH * i + 7]);
                trx = DataUtils.bytesToInt(bytes[LENGTH * i + 8], bytes[LENGTH * i + 9]) + 750;
                try_ = 750 - DataUtils.bytesToInt(bytes[LENGTH * i + 10], bytes[LENGTH * i + 11]);
                blx = DataUtils.bytesToInt(bytes[LENGTH * i + 12], bytes[LENGTH * i + 13]) + 750;
                bly = 750 - DataUtils.bytesToInt(bytes[LENGTH * i + 14], bytes[LENGTH * i + 15]);
                brx = DataUtils.bytesToInt(bytes[LENGTH * i + 16], bytes[LENGTH * i + 17]) + 750;
                bry = 750 - DataUtils.bytesToInt(bytes[LENGTH * i + 18], bytes[LENGTH * i + 19]);
                vwBean = new VirtualWallBean(i, new int[]{tlx, tly, trx, try_, blx, bly, brx, bry}, 1);
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

                break;
            case DRAG:
                float tx = mapX - downPoint.x;//x轴平移距离
                float ty = mapY - downPoint.y;//y轴平移距离
                if (curFbdBean != null) {//理论上不为空，为空时应该是在添加禁区
                curFbdBean.updateTranslationX(tx);
                curFbdBean.updateTranslationY(ty);
                curFbdBean.updateAreaRect();
                updatePath();
            }
                break;
            case ROTATE:
                float angle=DataUtils.getAngle(curFbdBean.getCenterPoint(),downPoint,new PointF(mapX,mapY));
                curFbdBean.updateAngle(angle);
                break;
            default:
                break;
        }
    }


    private void doOnActionUp(float mapX, float mapY) {
        switch (faot) {
            case ADD:
                curRectF.setEmpty();
                if (getUsefulWallNum() < 10 && DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_FBD_LENGTH) {
                    int[] coordinate = new int[]{(int) mMapView.reMatrixCoordinateX(downPoint.x), (int) mMapView.reMatrixCoordinateY(downPoint.y), (int) mMapView.reMatrixCoordinateX(mapX), (int) mMapView.reMatrixCoordinateY(mapY)};
                    RectF area = new RectF(mMapView.reMatrixCoordinateX(downPoint.x), mMapView.reMatrixCoordinateY(downPoint.y), mMapView.reMatrixCoordinateX(mapX), mMapView.reMatrixCoordinateY(mapY));
                    VirtualWallBean virtualWallBean = new VirtualWallBean(fbdBeans.size() + 1, coordinate, area, 2);
                    fbdBeans.add(virtualWallBean);
                    updatePath();
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

                break;
            case ROTATE:
                break;
            default:
                break;
        }
    }


    /**
     * the method which will be used to deal with virtual wall data
     */

    private int getUsefulWallNum() {
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
        for (VirtualWallBean fbd : fbdBeans) {
            mPath.addRect(fbd.getAreaRect(), Path.Direction.CW);
        }
        mPath.addRect(curRectF, Path.Direction.CW);
        mMapView.invalidateUI();
    }
}
