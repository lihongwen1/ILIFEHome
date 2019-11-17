package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.model.bean.CleanningRobot;

import java.util.List;

public class XAdapter extends BaseQuickAdapter<CleanningRobot, BaseViewHolder> {
    public XAdapter(int layoutId, @NonNull List<CleanningRobot> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setImageResource(R.id.image_product, data.get(position).getImg());
        holder.setText(R.id.tv_product, data.get(position).getName());
    }
}
