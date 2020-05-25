package com.ilife.home.robot.view.helper;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseIntArray;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.activity.SegmentationRoomActivity;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.utils.BitmapUtils;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean isSingleChoice;
    private Map<String, Bitmap> roomIcons, roomIconsWhite;

    public RoomHelper(MapView mapView) {
        this.mMapView = mapView;
        rooms = new ArrayList<>();
        selecRoom = new SparseIntArray();
        radius = MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.dp_16);
        textSize = MyApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.dp_16);
        roomIcons = new HashMap<>();
        roomIcons.put(UiUtil.getString(R.string.room_name_living_room), decodeRoomBitmap(R.drawable.icon_name_living_room_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_restaurant), decodeRoomBitmap(R.drawable.icon_name_restaurant_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_bed_room), decodeRoomBitmap(R.drawable.icon_name_bedroom_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_kitchen), decodeRoomBitmap(R.drawable.icon_name_kichten_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_bath_room), decodeRoomBitmap(R.drawable.icon_name_bathe_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_book_room), decodeRoomBitmap(R.drawable.icon_name_study_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_laundary_room), decodeRoomBitmap(R.drawable.icon_name_laundry_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_rest_room), decodeRoomBitmap(R.drawable.icon_name_rest_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_play_room), decodeRoomBitmap(R.drawable.icon_name_play_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_child_room), decodeRoomBitmap(R.drawable.icon_name_children_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_store_room), decodeRoomBitmap(R.drawable.icon_name_storage_orange));
        roomIcons.put(UiUtil.getString(R.string.room_name_other), decodeRoomBitmap(R.drawable.icon_name_other_orange));
        roomIconsWhite = new HashMap<>();
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_living_room), decodeRoomBitmap(R.drawable.icon_name_living_room_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_restaurant), decodeRoomBitmap(R.drawable.icon_name_restaurant_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_bed_room), decodeRoomBitmap(R.drawable.icon_name_bedroom_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_kitchen), decodeRoomBitmap(R.drawable.icon_name_kichten_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_bath_room), decodeRoomBitmap(R.drawable.icon_name_bathe_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_book_room), decodeRoomBitmap(R.drawable.icon_name_study_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_laundary_room), decodeRoomBitmap(R.drawable.icon_name_laundry_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_rest_room), decodeRoomBitmap(R.drawable.icon_name_rest_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_play_room), decodeRoomBitmap(R.drawable.icon_name_play_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_child_room), decodeRoomBitmap(R.drawable.icon_name_children_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_store_room), decodeRoomBitmap(R.drawable.icon_name_storage_white));
        roomIconsWhite.put(UiUtil.getString(R.string.room_name_other), decodeRoomBitmap(R.drawable.icon_name_other_white));
    }


    private Bitmap decodeRoomBitmap(int id) {
        return BitmapUtils.decodeBitmapFromResource(MyApplication.getInstance().getResources(), id, radius * 2, radius * 2);
    }

    public Bitmap getRoomBitmap(PartitionBean room) {
        Bitmap bitmap;
        if (room.isTagDefault()) {
            return null;
        }
        if (isRoomSelected(room.getPartitionId())) {
            bitmap = roomIcons.get(room.getTag());
            if (bitmap == null) {
                bitmap = roomIcons.get(UiUtil.getString(R.string.room_name_other));
            }
        } else {
            bitmap = roomIconsWhite.get(room.getTag());
            if (bitmap == null) {
                bitmap = roomIconsWhite.get(UiUtil.getString(R.string.room_name_other));
            }
        }
        return bitmap;
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
    public void drawRoom(String data, int checkedRoom) {
        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
        int num = bytes.length / 8;
        int partionId, x, y;

        for (int i = 0; i < num; i++) {
            partionId = DataUtils.bytesToInt(new byte[]{bytes[i * 8], bytes[i * 8 + 1], bytes[i * 8 + 2], bytes[i * 8 + 3]});
            if (partionId == -1) {
                continue;
            }
            x = DataUtils.bytesToInt(bytes[i * 8 + 4], bytes[i * 8 + 5]);
            y = -DataUtils.bytesToInt(bytes[i * 8 + 6], bytes[i * 8 + 7]);
            rooms.add(new PartitionBean(partionId, x, y));
            if ((partionId & checkedRoom) == partionId) {
                selecRoom.put(partionId, partionId);
            }
        }
        Collections.sort(rooms);
        int index = 0;
        String[] tags = UiUtil.getStringArray(R.array.array_room_tag);
        for (PartitionBean room : rooms) {
            room.setTag(tags[index]);
            index++;
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

    public void drawRoom(List<PartitionBean> rooms, int checkedRoom) {
        this.rooms.clear();
        this.rooms.addAll(rooms);
        Collections.sort(rooms);
        int index = 0;
        String[] tags = UiUtil.getStringArray(R.array.array_room_tag);
        for (PartitionBean room : rooms) {
            if (TextUtils.isEmpty(room.getTag())) {
                room.setTag(tags[index]);
                room.setTagDefault(true);
            }
            index++;
        }
        Path circle = new Path();
        Region region;
        int cx, cy;
        if (rooms.size() > 0) {
            for (PartitionBean pb : rooms) {
                if ((pb.getPartitionId() & checkedRoom) == pb.getPartitionId()) {
                    selecRoom.put(pb.getPartitionId(), pb.getPartitionId());
                }
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

    public void drawRoom(List<PartitionBean> rooms) {
        drawRoom(rooms, 0);
    }

    /**
     * 绘制房间标识
     *
     * @param data
     */
    public void drawRoom(String data) {
        drawRoom(data, 0);
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
        boolean isClickedRoom = false;
        for (PartitionBean room : rooms) {
            if (getMatrixIcon(room.getTagIcon()).contains((int) mapX, (int) mapY)) {
                isClickedRoom = true;
                id = room.getPartitionId();
                if (isSingleChoice) {
                    selecRoom.clear();
                    selecRoom.put(id, id);
                } else if (selecRoom.indexOfKey(id) >= 0) {
                    selecRoom.delete(id);
                } else {
                    selecRoom.put(id, id);
                }
            }
        }
        if (isClickedRoom && selecRoom.size() > 0) {
            LiveEventBus.get(SegmentationRoomActivity.KEY_ROOM_SELECT, Boolean.class).post(true);
        }
        mMapView.invalidateUI();
    }


    private RectF getMatrixIcon(RectF rectF) {
        if (rectF == null) {
            return null;
        }
        float[] reC = new float[]{rectF.left, rectF.top, rectF.right, rectF.bottom};
        Matrix scareMatrix = new Matrix();
        scareMatrix.setScale(mMapView.getRealScare(), mMapView.getRealScare(), rectF.centerX(), rectF.centerY());
        scareMatrix.invert(scareMatrix);
        scareMatrix.mapPoints(reC);
        return new RectF(reC[0], reC[1], reC[2], reC[3]);
    }


    public boolean isRoomSelected(int roomId) {
        return selecRoom.indexOfKey(roomId) >= 0;

    }

    public void setSingleChoice(boolean singleChoice) {
        isSingleChoice = singleChoice;
    }

    /**
     * single choose 模式可用
     *
     * @return
     */
    public PartitionBean getSelectRoom() {
        PartitionBean chooseRoom = null;
        if (selecRoom.size() > 0) {
            int chooseId = selecRoom.valueAt(0);
            for (PartitionBean room : rooms) {
                if (room.getPartitionId() == chooseId) {
                    chooseRoom = room;
                    break;
                }
            }
        }
        return chooseRoom;
    }

    public void putSelectRoom(int roomId) {
        selecRoom.put(roomId, roomId);
    }

    public void cleanSelectRoom() {
        selecRoom.clear();
    }
}
