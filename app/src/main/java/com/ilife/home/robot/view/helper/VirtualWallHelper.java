package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;

import com.ilife.home.robot.R;
import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class VirtualWallHelper {
    private static final String TAG = "VirtualWallHelper";
    private List<VirtualWallBean> vwBeans;
    private MapView mMapView;
    private VWOT vwot = VWOT.NOON;
    private static final int MIN_WALL_LENGTH = 20;
    private PointF downPoint;
    private RectF curVw;//当前正在操作的虚拟墙
    private Path vwPath;

    public enum VWOT {
        NOON(21),
        ADD(22),
        DELETE(23),
        DRAG(24),
        ROTATE(25);
        final int nativeType;

        VWOT(int type) {
            this.nativeType = type;
        }
    }

    public List<VirtualWallBean> getVwBeans() {
        return vwBeans;
    }

    public RectF getCurVw() {
        return curVw;
    }

    public Path getVwPath() {
        return vwPath;
    }

    public VirtualWallHelper(MapView mapView) {
        vwBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.vwPath = new Path();
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
        switch (vwot) {
            case ADD:
                downPoint.set(mapX, mapY);
                if (getUsefulWallNum() >= 10) {
                    ToastUtils.showToast(UiUtil.getString(R.string.map_aty_max_count));
                }
                break;
            case DELETE:
                break;
            case DRAG:
                break;
            case ROTATE:
                break;
            default:
                break;
        }
    }

    private void doOnActionMove(float mapX, float mapY) {
        switch (vwot) {
            case ADD:
                if (getUsefulWallNum() < 10) {
                    if (distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_WALL_LENGTH) {
                        curVw.set(downPoint.x, downPoint.y, mapX, mapY);
                    }
                }
                break;
            case DELETE:
                break;
            case DRAG:
                break;
            case ROTATE:
                break;
            default:
                break;
        }
    }

    private void doOnActionUp(float mapX, float mapY) {
        switch (vwot) {
            case ADD:
                //clear the cur wall rect ,and make it to a virtual wall bean
                curVw.setEmpty();
                if (getUsefulWallNum() < 10 && distance(downPoint.x, downPoint.y, mapX, mapY) > MIN_WALL_LENGTH) {
                    VirtualWallBean virtualWallBean = new VirtualWallBean(vwBeans.size() + 1,
                            new int[]{(int) mMapView.reMatrixCoordinateX(downPoint.x), (int) mMapView.reMatrixCoordinateY(downPoint.y), (int) mMapView.reMatrixCoordinateX(mapX), (int) mMapView.reMatrixCoordinateY(mapY)}
                            , 2);
                    vwBeans.add(virtualWallBean);
                    drawVirtualWall();
                }
                break;
            case DELETE:
                Iterator<VirtualWallBean> iterator = vwBeans.iterator();
                VirtualWallBean vr;
                while (iterator.hasNext()) {
                    vr = iterator.next();
                    if (vr.getDeleteIcon().contains(mapX, mapY)) {
                        if (vr.getState() == 2) {//新增的电子墙，还未保存到服务器，可以直接移除
                            vwBeans.remove(vr);
                        }
                        if (vr.getState() == 1) {//服务器上的电子墙，可能操作会被取消掉，只需要改变状态
                            vr.setState(3);
                        }
                        drawVirtualWall();
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
     * 查询到服务其电子墙数据后调用绘制电子墙
     * virtual wall encode data,need parse to virtual wall bean
     *
     * @param vwStr 服务器电子墙数据集合
     */
    public void drawVirtualWall(String vwStr) {
        if (!TextUtils.isEmpty(vwStr)) {
            byte[] bytes = Base64.decode(vwStr, Base64.DEFAULT);
            int vwCounts = bytes.length / 12;//一条虚拟墙含12个字节，4个保留字节，加2个坐标（x,y）
            vwBeans.clear();
            int sx, sy, ex, ey;
            VirtualWallBean vwBean;
            for (int i = 0; i < vwCounts; i++) {
                sx = DataUtils.bytesToInt(bytes[12 * i + 4], bytes[12 * i + 5]) + 750;
                sy = 750 - DataUtils.bytesToInt(bytes[12 * i + 6], bytes[12 * i + 7]);
                ex = DataUtils.bytesToInt(bytes[12 * i + 8], bytes[12 * i + 9]) + 750;
                ey = 750 - DataUtils.bytesToInt(bytes[12 * i + 10], bytes[12 * i + 11]);
                vwBean = new VirtualWallBean(i, new int[]{sx, sy, ex, ey}, 1);
                vwBeans.add(vwBean);
            }
        }
        drawVirtualWall();
    }

    /**
     * 绘制电子墙
     */
    public void drawVirtualWall() {
        if (vwBeans == null) {
            return;
        }
        vwPath.reset();
        for (VirtualWallBean vir : vwBeans) {
            if (vir.getState() != 3) {
                vwPath.moveTo(mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[1]));
                vwPath.lineTo(mMapView.matrixCoordinateX(vir.getPointCoordinate()[2]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[3]));
            }
        }
        if (vwot == VWOT.DELETE) {//删除电子墙模式，需要画出减号删除键
            RectF rectF;
            for (VirtualWallBean vir : vwBeans) {
                if (vir.getState() != 3) {
                    float cx = mMapView.matrixCoordinateX((vir.getPointCoordinate()[0] + vir.getPointCoordinate()[2]) / 2f);
                    float cy = mMapView.matrixCoordinateY((vir.getPointCoordinate()[1] + vir.getPointCoordinate()[3]) / 2f);
                    float distance = 60;//偏移坐标中心点的距离
                    if ((mMapView.matrixCoordinateX(vir.getPointCoordinate()[2]) == mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]))) {
                        cx -= distance;
                    } else {
                        float k = (mMapView.matrixCoordinateY(vir.getPointCoordinate()[3]) - mMapView.matrixCoordinateY(vir.getPointCoordinate()[1])) / (mMapView.matrixCoordinateX(vir.getPointCoordinate()[2])
                                - mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]));
                        //
                        MyLogger.d(TAG, "tanx:" + k);

                        float translationY = (float) (distance * (Math.sqrt(1 + k * k) / (1 + k * k)));
                        float translationX = Math.abs(k) * translationY;

                        if (k > 0) {
                            cx += translationX;
                            cy -= translationY;
                        } else {
                            cx -= translationX;
                            cy -= translationY;
                        }
                    }
                    //TODO add delete icon,rotate icon,pull icon
                }
            }

        }
    }


    /**
     * 撤销所有电子墙操作，恢复到与服务器数据一致的状态
     */
    public void undoAllOperation() {
        if (vwBeans != null && vwBeans.size() > 0) {
            Iterator<VirtualWallBean> iterator = vwBeans.iterator();
            while (iterator.hasNext()) {
                VirtualWallBean virtualWallBean = iterator.next();
                if (virtualWallBean.getState() == 2) {
                    iterator.remove();
                } else if (virtualWallBean.getState() == 3) {//被置为待删除的服务器电子墙恢复状态
                    virtualWallBean.setState(1);
                }
            }
        }
        drawVirtualWall();
    }


    /**
     * 获取电子墙列表,包含新增，和删除
     *
     * @return
     */
    public String getVwData() {
        List<Integer> datas = new ArrayList<>();
        for (VirtualWallBean vr : vwBeans) {
            if (vr.getState() != 3) {
                for (int i : vr.getPointCoordinate()) {
                    datas.add(i);
                }
            }
        }
        byte[] bData = new byte[datas.size()];
        for (int i = 0; i < datas.size(); i++) {
            bData[i] = datas.get(i).byteValue();
        }
        String dataStr = Base64.encodeToString(bData, Base64.DEFAULT);
        return dataStr;
    }


    /**
     * the method which will be used to deal with virtual wall data
     */

    private int getUsefulWallNum() {
        int num = 0;
        for (VirtualWallBean vb : vwBeans) {
            if (vb.getState() == 1 || vb.getState() == 2) {
                num++;
            }
        }
        MyLogger.d(TAG, "useful wall number:" + num);
        return num;
    }


    private float distance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }
}
