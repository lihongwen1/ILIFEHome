package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.SparseIntArray;

import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class PartitionHelper {
    private static final String TAG = "PartitionHelper";
    private MapView mMapView;
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
        rooms=new ArrayList<>();
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
        rooms.addAll(DataUtils.parsePartitionData(data));
        selecRoom = new SparseIntArray();
        Path circle = new Path();
        Region region;
        int cx, cy;
        if (rooms != null && rooms.size() > 0) {
            for (PartitionBean pb : rooms) {
                cx = (int) mMapView.matrixCoordinateX(pb.getX() - leftX);
                cy = (int) mMapView.matrixCoordinateY(leftY - pb.getY());
                circle.addCircle(cx, cy, radius, Path.Direction.CW);
                region = new Region(cx - radius, cy - radius, cx + radius, cy + radius);
                region.setPath(circle, region);
                pb.setRegion(region);
                pb.setTagIcon(new RectF(cx - radius, cy - radius, cx + radius, cy + radius));
            }
        }

    }

    public List<PartitionBean> getRooms() {
        return rooms;
    }

    public SparseIntArray getSelecRoom() {
        return selecRoom;
    }

    public int getRadius() {
        return radius;
    }

    public void clickRoomTag(float mapX, float mapY) {
        int id;
        for (PartitionBean room : rooms) {
            if (room.getRegion().contains((int) mapX, (int) mapY)) {
                id = room.getPartitionId();
                if (selecRoom.indexOfKey(id) < 0) {
                    selecRoom.delete(id);
                } else {
                    selecRoom.put(id, id);
                }
            }
        }
    }
}
