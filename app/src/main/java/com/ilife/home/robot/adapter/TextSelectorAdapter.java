package com.ilife.home.robot.adapter;

import android.view.View;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;

import java.util.List;

public class TextSelectorAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public TextSelectorAdapter(int layoutId, @NonNull List<String> data) {
        super(layoutId, data);
    }

    public TextSelectorAdapter(@NonNull List<String> data) {
        super(data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setText(R.id.tv_text, data.get(position));
        if (position == data.size() - 1) {
            holder.setVisible(R.id.v_divider, false);

        }
    }
}
