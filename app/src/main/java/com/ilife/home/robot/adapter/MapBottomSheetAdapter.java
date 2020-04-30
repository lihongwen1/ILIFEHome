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
        MyLogger.d("MapBottomSheetAdapter", "function：" + data.get(position));
        int iconId=0;
        switch (position){
            case 0:
                iconId=R.drawable.operation_btn_virtual_wall;
                break;
            case 1:
                iconId=R.drawable.operation_btn_select_room;
                break;
            case 2:
                iconId=R.drawable.operation_btn_edit;
                break;
            case 3:
                iconId=R.drawable.operation_btn_map;
                break;
            case 4:
                iconId=R.drawable.operation_btn_location;
                break;
            case 5:
                iconId = R.drawable.operation_btn_location;
                break;
        }
        holder.setImageResource(R.id.iv_map_function,iconId);
        holder.setText(R.id.tv_map_function, data.get(position));
    }

    @Override
    public int getItemCount() {
        MyLogger.d("MapBottomSheetAdapter", "item条数：" + super.getItemCount());
        return super.getItemCount();
    }
}
