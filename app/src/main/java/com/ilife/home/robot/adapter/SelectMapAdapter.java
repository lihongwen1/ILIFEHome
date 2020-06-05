package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SelectMapAdapter extends BaseQuickAdapter<SaveMapBean, BaseViewHolder> {
    private long selectMapId;
    private static int TYPE_ROBOT = 1;
    private static int TYPE_ADD = 2;

    public SelectMapAdapter(int layoutId, @NonNull List<SaveMapBean> data) {
        super(data);
        addItemType(TYPE_ADD, R.layout.layout_add_image);
        addItemType(TYPE_ROBOT, layoutId);
    }

    @Override
    public int getItemCount() {
        if (data.size() == 0) {
            return 0;
        } else {
            return data.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            return TYPE_ROBOT;
        } else {
            return TYPE_ADD;
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        MyLogger.d("SelectMapAdapter", "convert position:  " + position);
        int type = getItemViewType(position);
        if (type == TYPE_ADD) {//添加机器人按钮
            holder.addOnClickListener(R.id.iv_add_device);
        } else {
            boolean isSelect = selectMapId == data.get(position).getMapId();
            MapView mapView = holder.getView(R.id.mv_save_map);
            mapView.post(() -> {
                MapDataBean mapDataBean = DataUtils.parseSaveMapData(data.get(position).getMapData());
                SaveMapDataInfoBean saveMapDataInfoBean = DataUtils.parseSaveMapInfo(data.get(position).getMapDataInfo());
                if (mapDataBean != null) {
                    mapView.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                    mapView.drawMapX8(mapDataBean.getCoordinates());
                }
                if (saveMapDataInfoBean != null) {
                    mapView.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
                    mapView.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
                }
            });
            String time = Utils.generateTime(data.get(position).getMapId(), "yyyy-MM-dd HH:mm");
            holder.setText(R.id.tv_save_map_time, time);
            holder.setVisible(R.id.tv_apply_this_map, true);
            holder.setVisible(R.id.v_used_map, isSelect);
            holder.setText(R.id.tv_apply_this_map, isSelect ? UiUtil.getString(R.string.map_already_use_map) : UiUtil.getString(R.string.map_apply_this_map));
            holder.setText(R.id.tv_map_name, isSelect ? UiUtil.getString(R.string.map_current_map) : UiUtil.getString(R.string.map_history_map));
            holder.setSelect(R.id.tv_apply_this_map, isSelect);
            holder.addOnClickListener(R.id.tv_apply_this_map);
            holder.addOnClickListener(R.id.iv_delete_map);
            holder.addOnClickListener(R.id.tv_edit_this_map);
        }

    }

    public void setSelectMapId(long selectMapId) {
        this.selectMapId = selectMapId;
    }
}
