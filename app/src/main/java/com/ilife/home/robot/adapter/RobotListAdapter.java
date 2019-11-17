package com.ilife.home.robot.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.R;

import java.util.List;

public class RobotListAdapter extends BaseQuickAdapter<DeviceInfoBean, BaseViewHolder> {
    private static int TYPE_ROBOT = 1;
    private static int TYPE_ADD = 2;
    private Context context;

    public RobotListAdapter(Context context, @NonNull List<DeviceInfoBean> data) {
        super(data);
        addItemType(TYPE_ADD, R.layout.layout_add_image);
        addItemType(TYPE_ROBOT, R.layout.device_list_item);
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            return TYPE_ROBOT;
        } else {
            return TYPE_ADD;
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_ADD) {//添加机器人按钮
            holder.addOnClickListener(R.id.iv_add_device);
        } else {
            holder.setImageResource(context, R.id.image_product, R.drawable.n_x800);
            String nickName=data.get(position).getNickName();
            holder.setText(R.id.tv_name, TextUtils.isEmpty(nickName)? data.get(position).getDeviceName():nickName);
            int states = data.get(position).getStatus();
            holder.setText(R.id.tv_status2, states == 1 ? R.string.device_adapter_device_online:R.string.device_adapter_device_offline);
            holder.setTextColor(R.id.tv_status2, states == 1 ?  context.getResources().getColor(R.color.color_ff4d00):context.getResources().getColor(R.color.color_81));
            holder.setSelect(R.id.tv_status2,states==1);
            holder.addOnClickListener(R.id.item_delete);
        }
    }
}
