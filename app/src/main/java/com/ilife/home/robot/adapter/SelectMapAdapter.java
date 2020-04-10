package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.MapView;

import java.util.List;

public class SelectMapAdapter extends BaseQuickAdapter<HistoryRecordBean, BaseViewHolder> {

    public SelectMapAdapter(int layoutId, @NonNull List<HistoryRecordBean> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        HistoryRecordBean historyRecordBean = data.get(position);
        MapDataBean mapDataBean = DataUtils.parseSaveMapData(historyRecordBean.getMapDataArray());
        if (mapDataBean != null) {
            MapView mapView = holder.getView(R.id.mv_save_map);
            mapView.post(() -> {
                mapView.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                mapView.drawMapX8(mapDataBean.getCoordinates());
            });
            boolean isSelect = position == 0;
            String time = Utils.generateTime(historyRecordBean.getStartTime(), "yyyy-MM-dd HH:mm");
            holder.setText(R.id.tv_save_map_time, time);
            holder.setVisible(R.id.tv_apply_this_map, true);
            holder.setText(R.id.tv_apply_this_map, isSelect ? "已应用" : "应用此地图");
            holder.setText(R.id.tv_map_name, isSelect ? "当前地图" : "历史地图");
            holder.setSelect(R.id.tv_apply_this_map, isSelect);
            holder.addOnClickListener(R.id.tv_apply_this_map);
            holder.addOnClickListener(R.id.iv_delete_map);
        }
    }
}
