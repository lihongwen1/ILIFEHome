package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.entity.NewClockInfo;
import com.ilife.home.robot.utils.DataUtils;

import java.util.List;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class ClockAdapter extends BaseQuickAdapter<NewClockInfo, BaseViewHolder> {
    public ClockAdapter(int layoutId, @NonNull List<NewClockInfo> data) {
        super(layoutId, data);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        NewClockInfo info = data.get(position);
        byte open = (byte) info.getOpen();
        boolean isOpen = open == 1;
        String hour = info.getHour() < 10 ? "0" + info.getHour() : "" + info.getHour();
        String minute = info.getMinute() < 10 ? "0" + info.getMinute() : "" + info.getMinute();
        holder.setText(R.id.tv_time, hour + ":" + minute);
        holder.setSelect(R.id.tv_time, isOpen);
        int week = info.getWeek();
        holder.setText(R.id.tv_week, DataUtils.getScheduleWeek(week));
        holder.setSelect(R.id.tv_week, isOpen);
        StringBuilder sb = new StringBuilder();
        holder.setText(R.id.tv_mode_,getModeString(info));
        holder.setSelect(R.id.image_status, isOpen);
        holder.addOnClickListener(R.id.image_status);
    }

    private String getModeString(NewClockInfo info) {
        StringBuilder sb = new StringBuilder();
        String area = "";
        switch (info.getType()) {
            case 0:
                area = "默认";
                break;
            case 1:
                area = "划区";
                break;
            case 2:
                area = "选房";
                break;
        }
        if (info.getMode() == 0) {
            info.setMode(MsgCodeUtils.STATUE_PLANNING);
        }
        sb.append(area).append("|")
                .append(info.getTimes() + "次");
    return sb.toString();
    }
}
