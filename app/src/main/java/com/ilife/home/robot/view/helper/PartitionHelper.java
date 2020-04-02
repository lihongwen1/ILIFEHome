package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Base64;
import android.util.SparseIntArray;

import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * 选房清扫帮助类
 */
public class PartitionHelper {
    private static final String TAG = "PartitionHelper";
    private MapView mMapView;
    List<PartitionBean> rooms;
    private int radius;//房间标记半径
    private SparseIntArray selecRoom;

    public PartitionHelper(MapView mapView) {
        this.mMapView = mapView;
        rooms = new ArrayList<>();
        selecRoom = new SparseIntArray();
        radius = Utils.dip2px(MyApplication.getInstance(), 24);
    }


    public int getSelectRoomId() {
        int roomId = 0;
        for (int i = 0; i < selecRoom.size(); i++) {
            roomId += selecRoom.valueAt(i);
        }
        return roomId;
    }

    /**
     * 绘制房间标识
     *
     * @param leftX
     * @param leftY
     * @param data
     */
    public void drawRoom(int leftX, int leftY, String data) {
        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
        int num = bytes.length / 8;
        int partionId, x, y;
        for (int i = 0; i < num; i++) {
            partionId = DataUtils.bytesToInt(new byte[]{bytes[i * 8], bytes[i * 8 + 1], bytes[i * 8 + 2], bytes[i * 8 + 3]});
            x = DataUtils.bytesToInt(bytes[i * 8 + 4], bytes[i * 8 + 5]);
            y = DataUtils.bytesToInt(bytes[i * 8 + 6], bytes[i * 8 + 7]);
            rooms.add(new PartitionBean(partionId, x, y));
        }
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
                ToastUtils.showToast("选择房间："+id);
                if (selecRoom.indexOfKey(id) > 0) {
                    selecRoom.delete(id);
                } else {
                    selecRoom.put(id, id);
                }
            }
        }
        mMapView.invalidateUI();
    }
}
