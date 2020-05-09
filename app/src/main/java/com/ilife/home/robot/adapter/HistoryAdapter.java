package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HistoryAdapter extends BaseQuickAdapter<HistoryRecordBean, BaseViewHolder> {
    private boolean needDivide100;

    public HistoryAdapter(int layoutId, @NonNull List<HistoryRecordBean> data) {
        super(layoutId, data);
        String productKey = IlifeAli.getInstance().getWorkingDevice().getProductKey();
        needDivide100 = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(productKey).isNeedDivide100();
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        HistoryRecordBean historyRecord = data.get(position);
        holder.setText(R.id.tv_duration, historyRecord.getCleanTotalTime() + "min");
        if (needDivide100) {
            holder.setText(R.id.tv_area, DataUtils.formateArea(historyRecord.getCleanTotalArea() / 100f));
        } else {
            holder.setText(R.id.tv_area, DataUtils.formateArea(historyRecord.getCleanTotalArea()));
        }
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
