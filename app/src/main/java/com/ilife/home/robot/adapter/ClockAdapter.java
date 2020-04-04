package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
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
        StringBuilder weekStr = new StringBuilder();
        int week = info.getWeek();
        for (int i = 0; i < 7; i++) {
            if (DataUtils.getBit((byte) week, i) == 1) {
                switch (i) {
                    case 0:
                        weekStr.append("周一");
                        break;
                    case 1:
                        weekStr.append("周二");
                        break;
                    case 2:
                        weekStr.append("周三");
                        break;
                    case 3:
                        weekStr.append("周四");
                        break;
                    case 4:
                        weekStr.append("周五");
                        break;
                    case 5:
                        weekStr.append("周六");
                        break;
                    case 6:
                        weekStr.append("周日");
                        break;
                }
            }
        }
        holder.setText(R.id.tv_week,weekStr);
        holder.setSelect(R.id.tv_week, isOpen);

        holder.setSelect(R.id.image_status, isOpen);
        holder.addOnClickListener(R.id.image_status);
    }
}
