package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends BaseQuickAdapter<HistoryRecordBean, BaseViewHolder> {
    public HistoryAdapter(int layoutId, @NonNull List<HistoryRecordBean> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        HistoryRecordBean historyRecord = data.get(position);
        holder.setText(R.id.tv_duration, historyRecord.getCleanTotalTime() / 60 + "min");
        holder.setText(R.id.tv_area, historyRecord.getCleanTotalArea() + "㎡");
        long time_ = historyRecord.getStartTime();
        holder.setText(R.id.tv_date, generateTime(time_, Utils.getString(R.string.history_adapter_month_day)));
        holder.setText(R.id.tv_time, generateTime(time_,"HH:mm:ss"));
        holder.setImageResource(R.id.iv_cleaning_tag,historyRecord.getStopCleanReason()==1?R.drawable.annal_icon_finish:R.drawable.annal_icon_problem);
    }


    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date(time * 1000));
        return str;
    }
}
