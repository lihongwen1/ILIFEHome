package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;

import java.util.List;

public class MapBottomSheetAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public MapBottomSheetAdapter(int layoutId, @NonNull List<String> data) {
        super(layoutId, data);
    }

    public MapBottomSheetAdapter(@NonNull List<String> data) {
        super(data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {

    }
}
