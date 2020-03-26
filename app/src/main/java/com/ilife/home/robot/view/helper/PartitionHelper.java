package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.util.SparseIntArray;

import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.List;

public class PartitionHelper {
    private static final String TAG = "PartitionHelper";
    private android.graphics.Path roomPath;//房间标识
    private MapView mMapView;
    private PointF downPoint;
    List<PartitionBean> rooms;
    private int radius;//房间标记半径
    private SparseIntArray selecRoom;
    public enum PTOT {
        PARTITION0(41),//划区
        ROOM(42);//选房
        final int type;

        PTOT(int type) {
            this.type = type;
        }
    }

    public PartitionHelper(MapView mapView) {
        this.mMapView = mapView;
        this.roomPath = new Path();
        this.downPoint = new PointF();
        radius = Utils.dip2px(MyApplication.getInstance(), 24);
    }

    /**
     * 绘制房间标识
     *
     * @param leftX
     * @param leftY
     * @param data
     */
    public void drawRoom(int leftX, int leftY, String data) {
        rooms = DataUtils.parsePartitionData(data);
        selecRoom=new SparseIntArray();
        Path circle = new Path();
        Region region;
        int cx, cy;
        if (rooms != null && rooms.size() > 0) {
            roomPath.reset();
            for (PartitionBean pb : rooms) {
                cx = (int) mMapView.matrixCoordinateX(pb.getX() - leftX);
                cy = (int) mMapView.matrixCoordinateY(leftY - pb.getY());
                circle.addCircle(cx, cy, radius, Path.Direction.CW);
                region = new Region(cx - radius, cy - radius, cx + radius, cy + radius);
                region.setPath(circle, region);
                pb.setRegion(region);
                roomPath.addCircle(cx, cy, radius, Path.Direction.CW);
            }
        }

    }


    public Path getRoomPath() {
        return roomPath;
    }

    public void setRoomPath(Path roomPath) {
        this.roomPath = roomPath;
    }

    public void clickRoomTag(float mapX, float mapY) {
        for (PartitionBean room : rooms) {
            if (room.getRegion().contains((int) mapX, (int) mapY)){
                ToastUtils.showToast("点击了房间："+room.getPartitionId());
            }
        }
    }
}
