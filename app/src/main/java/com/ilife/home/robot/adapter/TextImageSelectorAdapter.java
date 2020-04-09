package com.ilife.home.robot.adapter;

import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;

import java.util.List;

public class TextImageSelectorAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public TextImageSelectorAdapter(int layoutId, @NonNull List<String> data, SparseIntArray selectPosition) {
        super(layoutId, data, selectPosition);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setText(R.id.tv_text_selector, data.get(position));
        holder.setSelect(R.id.tv_text_selector, selectPosition.indexOfKey(position) >= 0);
        holder.setSelect(R.id.iv_text_selector, selectPosition.indexOfKey(position) >= 0);
    }
}
