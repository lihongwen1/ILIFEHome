package com.ilife.home.robot.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
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
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
        } else if (isSelect) {
            tv_control_x9.setVisibility(View.GONE);
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.white));
            tv_start.setText(R.string.map_aty_stop);
            tv_start.setTextColor(getResources().getColor(R.color.white));
            tv_control_x9.setTextColor(getResources().getColor(R.color.white));
        } else {
            tv_start.setText(R.string.map_aty_start);
            tv_start.setText(value);
            tv_start.setTextColor(getResources().getColor(R.color.color_33));
            tv_bottom_recharge.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setTextColor(getResources().getColor(R.color.color_33));
            tv_control_x9.setVisibility(View.VISIBLE);
        }
        if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING) {
            setNavigationBarColor(R.color.color_ff1b92e2);
        } else {
            setNavigationBarColor(R.color.white);
        }
        iv_map_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//remote control start button
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        layout_remote_control.setVisibility(View.GONE);
        tv_bottom_recharge.setSelected(isRecharge);
    }
}
