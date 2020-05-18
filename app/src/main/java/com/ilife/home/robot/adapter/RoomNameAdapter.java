package com.ilife.home.robot.adapter;

import android.util.SparseIntArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;

import java.util.List;

public class RoomNameAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    private final int[] icons = new int[]{R.drawable.icon_name_living_room, R.drawable.icon_name_restaurant, R.drawable.icon_name_bedroom
            , R.drawable.icon_name_kichten, R.drawable.icon_name_bathe, R.drawable.icon_name_study, R.drawable.icon_name_laundry
            , R.drawable.icon_name_rest, R.drawable.icon_name_play, R.drawable.icon_name_children, R.drawable.icon_name_storage, R.drawable.icon_name_other};

    public RoomNameAdapter(int layoutId, @NonNull List<String> data) {
        super(layoutId, data);
    }

    public RoomNameAdapter(int layoutId, @NonNull List<String> data, SparseIntArray selectPosition) {
        super(layoutId, data, selectPosition);
        isMultipleSelect = true;
    }

    public RoomNameAdapter(@NonNull List<String> data) {
        super(data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        holder.setText(R.id.tv_room_name, data.get(position));
        holder.setImageResource(R.id.iv_room_icon,icons[position]);
        holder.setVisible(R.id.iv_room_selected, selectPosition.indexOfKey(position) >= 0);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}

