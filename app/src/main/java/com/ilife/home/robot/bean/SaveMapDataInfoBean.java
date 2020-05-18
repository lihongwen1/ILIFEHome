package com.ilife.home.robot.bean;


import android.graphics.Point;

import com.ilife.home.robot.model.bean.VirtualWallBean;

import java.util.List;

/**
 * 充电座位置(4bytes)+
 * 房间数(1byte)+
 * 房间 1ID(4bytes)+房间 1 坐标(4bytes)+ N
 * 房间 1 墙的坐标数(2bytes)+房间墙坐标
 * (4*n bytes)+…+
 * 门条数(1byte)+
 * 门 1ID(1byte)+门 1 坐标(8byte)+…+
 * 虚拟墙条数(1byte)+
 * 虚拟墙 1 坐标(8byte)+…+
 * 禁区条数(1byte)+
 * 禁区 1 类型(1byte)+禁区 1 坐标
 * (4*4byte)
 */
public class SaveMapDataInfoBean {
    private int mapId;
    private Point chargePoint;
    private List<PartitionBean> rooms;
    private List<VirtualWallBean> gates;

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public Point getChargePoint() {
        return chargePoint;
    }

    public void setChargePoint(Point chargePoint) {
        this.chargePoint = chargePoint;
    }

    public List<PartitionBean> getRooms() {
        return rooms;
    }

    public void setRooms(List<PartitionBean> rooms) {
        this.rooms = rooms;
    }

    public List<VirtualWallBean> getGates() {
        return gates;
    }

    public void setGates(List<VirtualWallBean> gates) {
        this.gates = gates;
    }
}
