package com.ilife.home.robot.view.helper;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Base64;
import android.util.SparseIntArray;

import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选房清扫帮助类
 */
public class RoomHelper {
    private static final String TAG = "RoomHelper";
    private MapView mMapView;
    List<PartitionBean> rooms;
    private int radius;//房间标记半径
    private int textSize;//房间标记半径
    private SparseIntArray selecRoom;

    public RoomHelper(MapView mapView) {
        this.mMapView = mapView;
        rooms = new ArrayList<>();
        selecRoom = new SparseIntArray();
        radius = MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.dp_20);
        textSize = MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.dp_20);
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
     * @param data
     */
    public void drawRoom(String data) {
        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
        int num = bytes.length / 8;
        int partionId, x, y;

        for (int i = 0; i < num; i++) {
            partionId = DataUtils.bytesToInt(new byte[]{bytes[i * 8], bytes[i * 8 + 1], bytes[i * 8 + 2], bytes[i * 8 + 3]});
            x = DataUtils.bytesToInt(bytes[i * 8 + 4], bytes[i * 8 + 5]);
            y = -DataUtils.bytesToInt(bytes[i * 8 + 6], bytes[i * 8 + 7]);
            rooms.add(new PartitionBean(partionId, x, y));

        }
        Collections.sort(rooms);
        int tag = 65;
        for (PartitionBean room : rooms) {
            room.setTag(String.valueOf((char)tag));
            tag++;
        }
        Path circle = new Path();
        Region region;
        int cx, cy;
        if (rooms != null && rooms.size() > 0) {
            for (PartitionBean pb : rooms) {
                cx = (int) mMapView.matrixCoordinateX(pb.getX());
                cy = (int) mMapView.matrixCoordinateY(pb.getY());
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

    public int getTextSize() {
        return textSize;
    }

    public void clickRoomTag(float mapX, float mapY) {
        int id;
        for (PartitionBean room : rooms) {
            if (room.getRegion().contains((int) mapX, (int) mapY)) {
                id = room.getPartitionId();
                if (selecRoom.indexOfKey(id) >= 0) {
                    selecRoom.delete(id);
                } else {
                    selecRoom.put(id, id);
                }
            }
        }
        mMapView.invalidateUI();
    }

    public boolean isRoomSelected(int roomId) {
        return selecRoom.indexOfKey(roomId) >= 0;

    }

}
