package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.ScheduleBean;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.huawei.android.hms.agent.common.UIUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.entity.NewClockInfo;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.UiUtil;

import java.util.List;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class ClockAdapter extends BaseQuickAdapter<ScheduleBean, BaseViewHolder> {
    public ClockAdapter(int layoutId, @NonNull List<ScheduleBean> data) {
        super(layoutId, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        ScheduleBean info = data.get(position);
        byte open = (byte) info.getEnable();
        boolean isOpen = open == 1;
        String hour = info.getHour() < 10 ? "0" + info.getHour() : "" + info.getHour();
        String minute = info.getMinutes() < 10 ? "0" + info.getMinutes(): "" + info.getMinutes();
        holder.setText(R.id.tv_time, hour + ":" + minute);
        holder.setSelect(R.id.tv_time, isOpen);
        int week = info.getWeek();
        holder.setText(R.id.tv_week, DataUtils.getScheduleWeek(week));
        holder.setSelect(R.id.tv_week, isOpen);
        holder.setText(R.id.tv_mode_,getModeString(info));
        holder.setSelect(R.id.image_status, isOpen);
        holder.addOnClickListener(R.id.image_status);
        holder.addOnClickListener(R.id.iv_delete_schedule);
    }

    private String getModeString(ScheduleBean info) {
        StringBuilder sb = new StringBuilder();
        String area = "";
        switch (info.getType()) {
            case 0:
                area = UiUtil.getString(R.string.clock_area_default);
                break;
            case 1:
                area = UiUtil.getString(R.string.clock_area_clean_area);
                break;
            case 2:
                area =UiUtil.getString(R.string.clock_area_choose_room);
                break;
        }
        if (info.getMode() == 0) {
            info.setMode(MsgCodeUtils.STATUE_PLANNING);
        }
        sb.append(area).append(" | ")
                .append(DataUtils.getScheduleTimes(info.getLoop()));
    return sb.toString();
    }
}
