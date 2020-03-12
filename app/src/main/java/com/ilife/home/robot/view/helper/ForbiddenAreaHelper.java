package com.ilife.home.robot.view.helper;

import android.view.MotionEvent;

import com.ilife.home.robot.model.bean.VirtualWallBean;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * contains global forbidden area and mop forbidden area
 */
public class ForbiddenAreaHelper {
    private List<VirtualWallBean> vwBeans;
    private MapView mMapView;
    private FAOT vwot = FAOT.NOON;

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

    public List<VirtualWallBean> getVwBeans() {
        return vwBeans;
    }

    public ForbiddenAreaHelper(MapView mapView) {
        vwBeans = new ArrayList<>();
        this.mMapView = mapView;
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
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        mMapView.invalidateUI();
    }

    private void doOnActionDown() {
        switch (vwot) {
            case ADD:
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

    private void doOnActionMove() {
        switch (vwot) {
            case ADD:
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

    private void doOnActionUp() {
        switch (vwot) {
            case ADD:
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
}
