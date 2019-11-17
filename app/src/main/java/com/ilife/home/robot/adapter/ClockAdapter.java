package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.entity.NewClockInfo;

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
        byte open = info.getOpen();
        boolean isOpen = open == 1;
        String hour = info.getHour() < 10 ? "0" + info.getHour() : "" + info.getHour();
        String minute = info.getMinute() < 10 ? "0" + info.getMinute() : "" + info.getMinute();
        holder.setText(R.id.tv_time, hour + ":" + minute);
        holder.setSelect(R.id.tv_time, isOpen);

        holder.setText(R.id.tv_week, info.getWeek());
        holder.setSelect(R.id.tv_week, isOpen);

        holder.setSelect(R.id.image_status, isOpen);
        holder.addOnClickListener(R.id.image_status);
    }
}
