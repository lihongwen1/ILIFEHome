package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Base64;
import android.view.MotionEvent;

import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class GateHelper {
    private static final String TAG = "VirtualWallHelper";
    private List<VirtualWallBean> gtBeans;
    private MapView mMapView;
    private PointF downPoint;
    private Path gtPath;
    private final int ICON_RADIUS = 50;


    public List<VirtualWallBean> getGtBeans() {
        return gtBeans;
    }

    public Path getGtPath() {
        return gtPath;
    }


    public GateHelper(MapView mapView) {
        gtBeans = new ArrayList<>();
        this.mMapView = mapView;
        this.gtPath = new Path();
        this.downPoint = new PointF();
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

    }

    /**
     * @param mapX
     * @param mapY
     */
    private void doOnActionUp(float mapX, float mapY) {
        if ((mapX == downPoint.x && mapY == downPoint.y) || DataUtils.distance(downPoint.x, downPoint.y, mapX, mapY) < 10) {//点击事件
            for (VirtualWallBean vBean : gtBeans) {
                if (vBean.getDeleteIcon() != null && vBean.getDeleteIcon().contains(downPoint.x, downPoint.y)) {//点击了删除键
                    if (getDeleteGate() == -1) {//未存在待删除的门
                        if (vBean.getState() == 2) {//新增的门，还未保存到服务器，可以直接移除
                            gtBeans.remove(vBean);
                        }
                        if (vBean.getState() == 1) {//服务器上的门，可能操作会被取消掉，只需要改变状态
                            vBean.setState(3);
                            vBean.clear();
                        }
                        updateGate();
                    } else {
                        ToastUtils.showToast("只能合并两个房间");
                    }
                }

            }
        }
    }

    public void revertGate() {
        for (VirtualWallBean gate : gtBeans) {
            gate.setState(1);
        }
        updateGate();
    }

    public int getDeleteGate() {
        int gateId = -1;
        for (VirtualWallBean gate : gtBeans) {
            if (gate.getState() == 3) {
                gateId = gate.getNumber();
                break;
            }
        }
        return gateId;
    }

    /**
     * 查询到服务其电子墙数据后调用绘制电子墙
     * virtual wall encode data,need parse to virtual wall bean
     */
    public void drawGate(List<VirtualWallBean> gates) {
        this.gtBeans.clear();
        this.gtBeans.addAll(gates);
        updateGate();
    }


    /**
     * 绘制电子墙
     * 根据最新的缩放比例刷新虚拟墙
     */
    public void updateGate() {
        if (gtBeans == null) {
            return;
        }
        gtPath.reset();
        float[] coordinate;
        for (VirtualWallBean vir : gtBeans) {
            if (vir.getState() != 3) {
                coordinate = new float[]{mMapView.matrixCoordinateX(vir.getPointCoordinate()[0]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[1])
                        , mMapView.matrixCoordinateX(vir.getPointCoordinate()[2]), mMapView.matrixCoordinateY(vir.getPointCoordinate()[3])};
                gtPath.moveTo(coordinate[0], coordinate[1]);
                gtPath.lineTo(coordinate[2], coordinate[3]);
                float cx=(coordinate[0]+coordinate[2])/2f;
                float cy=(coordinate[1]+coordinate[3])/2f;
                vir.setDeleteIcon(new RectF(cx - ICON_RADIUS, cy - ICON_RADIUS, cx + ICON_RADIUS, cy + ICON_RADIUS));
            }
        }
    }
}
