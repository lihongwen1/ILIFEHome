package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.utils.MyLogger;

import java.util.List;

public class MapBottomSheetAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public MapBottomSheetAdapter(int layoutId, @NonNull List<String> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        MyLogger.d("MapBottomSheetAdapter","function："+data.get(position));
      holder.setText(R.id.tv_map_function,data.get(position));
    }

    @Override
    public int getItemCount() {
        MyLogger.d("MapBottomSheetAdapter","item条数："+super.getItemCount());
        return super.getItemCount();
    }
}
