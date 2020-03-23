package com.ilife.home.robot.adapter;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class SelectMapAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {
    private long selectMapId;
    public SelectMapAdapter(int layoutId, @NonNull List<Integer> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        int id = data.get(position);
        IlifeAli.getInstance().getSelectMap(id, new OnAliResponse<List<HistoryRecordBean>>() {
            @Override
            public void onSuccess(List<HistoryRecordBean> result) {
                List<Coordinate> pointList = new ArrayList<>();
                int minX, maxX, minY, maxY;//数据的边界，X800系列机器会用到
                String[] mapArray = result.get(0).getMapDataArray();
                int lineCount = 0;
                List<Byte> byteList = new ArrayList<>();
                int leftX = 0, leftY = 0;
                if (mapArray != null) {
                    if (mapArray.length > 0) {
                        for (String data : mapArray) {
                            if (data == null) {
                                continue;
                            }
                            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                            int bj = bytes[0] & 0xff;
                            if (bj == 1) {//map数据
                                leftX = DataUtils.bytesToInt(new byte[]{bytes[1], bytes[2]}, 0);
                                leftY = DataUtils.bytesToInt(new byte[]{bytes[3], bytes[4]}, 0);
                                lineCount = DataUtils.bytesToInt(new byte[]{bytes[5], bytes[6]}, 0);
                                for (int j = 7; j < bytes.length; j++) {
                                    byteList.add(bytes[j]);
                                }
                            }
                        }
                    }
                }
                Coordinate coordinate;
                if (byteList.size() > 0) {
                    int x = 0, y = 0, type = 0, length = 0;
                    for (int i = 2; i < byteList.size(); i += 3) {
                        type = byteList.get(i - 1) & 0xff;
                        length = byteList.get(i) & 0xff;
                        for (int j = 0; j < length; j++) {
                            if (type != 0) {
                                coordinate = new Coordinate(x, y, type);
                                pointList.add(coordinate);
                            }
                            if (x < lineCount - 1) {
                                x++;
                            } else {
                                x = 0;
                                y++;
                            }

                        }
                    }
                    minX = 0;
                    maxX = lineCount;
                    minY = 0;
                    maxY = y;
                    MapView mapView = holder.getView(R.id.mv_save_map);
                    mapView.setLeftTopCoordinate(leftX, leftY);
                    mapView.updateSlam(minX, maxX, minY, maxY);
                    mapView.drawMapX8(pointList);

                    boolean isSelect=id == selectMapId;
                    String time= Utils.generateTime(id,"yyyy-MM-dd HH:mm");
                    holder.setText(R.id.tv_save_map_time,time);
                    holder.setText(R.id.tv_apply_this_map, isSelect?"已应用":"应用此地图");
                    holder.setSelect(R.id.tv_apply_this_map,isSelect);
                    holder.addOnClickListener(R.id.tv_apply_this_map);
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    public long getSelectMapId() {
        return selectMapId;
    }

    public void setSelectMapId(long selectMapId) {
        this.selectMapId = selectMapId;
    }
}
