package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SelectMapAdapter extends BaseQuickAdapter<SaveMapBean, BaseViewHolder> {
    private long selectMapId;

    public SelectMapAdapter(int layoutId, @NonNull List<SaveMapBean> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        boolean isSelect = selectMapId == data.get(position).getMapId();
        String[] mapData = data.get(position).getMapData();
        MapDataBean mapDataBean = DataUtils.parseSaveMapData(mapData);
        if (mapDataBean != null) {
            MapView mapView = holder.getView(R.id.mv_save_map);
            mapView.post(() -> {
                mapView.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                mapView.drawMapX8(mapDataBean.getCoordinates());
            });
            String time = Utils.generateTime(data.get(position).getMapId(), "yyyy-MM-dd HH:mm");
            holder.setText(R.id.tv_save_map_time, time);
            holder.setVisible(R.id.tv_apply_this_map, true);
            holder.setVisible(R.id.v_used_map, isSelect);
            holder.setText(R.id.tv_apply_this_map, isSelect ? UiUtil.getString(R.string.map_already_use_map):UiUtil.getString(R.string.map_apply_this_map));
            holder.setText(R.id.tv_map_name, isSelect ?UiUtil.getString(R.string.map_current_map):UiUtil.getString(R.string.map_history_map));
            holder.setSelect(R.id.tv_apply_this_map, isSelect);
            holder.addOnClickListener(R.id.tv_apply_this_map);
            holder.addOnClickListener(R.id.iv_delete_map);
        }
    }

    public void setSelectMapId(long selectMapId) {
        this.selectMapId = selectMapId;
    }
}
