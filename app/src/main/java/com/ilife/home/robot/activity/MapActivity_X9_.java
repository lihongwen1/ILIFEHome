package com.ilife.home.robot.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.UiUtil;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X9_ extends BaseMapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        iv_recharge_model.setImageResource( UiUtil.getDrawable(mPresenter.getRobotBean().getFaceImg()));
    }

    @Override
    public void showRemoteView() {
        if (mPresenter.isWork(mPresenter.getCurStatus()) || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_SLEEPING) {
            ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
        } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_ || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING) {
            ToastUtils.showToast(this, getString(R.string.map_aty_charge));
        } else {
            USE_MODE = USE_MODE_REMOTE_CONTROL;
            showBottomView();
        }
    }

    @Override
    public void updateStartStatue(boolean isSelect, String value) {
        if (isSelect && mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE) {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_wall.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
            fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));
        } else if (isSelect) {
            tv_control_x9.setVisibility(View.GONE);
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge.setVisibility(View.VISIBLE);
            tv_start.setText(R.string.map_aty_stop);
            tv_start.setTextColor(getResources().getColor(R.color.white));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.white));
            tv_control_x9.setTextColor(getResources().getColor(R.color.white));
            tv_wall.setTextColor(getResources().getColor(R.color.white));
            fl_bottom_x9.setBackground(new ColorDrawable(Color.TRANSPARENT));
        } else {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_wall.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge_x8.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
            fl_bottom_x9.setBackground(new ColorDrawable(getResources().getColor(R.color.bg_color_f5f7fa)));
        }
        if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING) {
            setNavigationBarColor(R.color.color_ff1b92e2);
        } else {
            setNavigationBarColor(R.color.white);
        }
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        if (layout_recharge.getVisibility() == View.VISIBLE && isRecharge) {//避免重复刷新UI导致异常
            return;
        }
        layout_remote_control.setVisibility(View.GONE);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_bottom_recharge_x8.setSelected(isRecharge);
        if (USE_MODE == USE_MODE_REMOTE_CONTROL) {
            layout_recharge.setVisibility(View.VISIBLE);
        }
    }
}
