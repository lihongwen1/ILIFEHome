package com.ilife.home.robot.activity;

import android.os.Bundle;

import android.view.View;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.R;

/**
 * Created by chengjiaping on 2018/8/15.
 */

public class MapActivity_X8_ extends BaseMapActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        super.initView();
        if (mPresenter.getRobotType().equals(Constants.A8s)) {
            ll_map_container.setBackground(getResources().getDrawable(R.drawable.shape_gradient_map_bg_mokka));
        }
        if (mPresenter.getRobotType().equals(Constants.V3x)) {//V3X的充电底座单独不一样
            iv_recharge_stand.setImageResource(R.drawable.recharge_stand_v3x);
        }
        int robotPic = DeviceUtils.getRobotPic(mPresenter.getRobotType());
        iv_recharge_model.setImageResource(robotPic);
        iv_point_robot.setImageResource(robotPic);
        iv_along_robot.setImageResource(robotPic);
        tv_bottom_recharge_x8.setVisibility(View.VISIBLE);
        tv_wall.setVisibility(View.GONE);
        tv_appointment_x9.setVisibility(View.VISIBLE);
        tv_recharge_x9.setVisibility(View.GONE);
    }

    @Override
    public void showRemoteView() {
        if (/*mPresenter.isWork(mPresenter.getCurStatus()) ||*/ mPresenter.getCurStatus() == MsgCodeUtils.STATUE_SLEEPING) {
            ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
        } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_ || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING) {
            ToastUtils.showToast(this, getString(R.string.map_aty_charge));
        } else {
            showBottomView();
        }
    }

    //TODO 设置navigation bar color
    @Override
    public void updateStartStatue(boolean isSelect, String value) {
        if (isSelect && mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE) {
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
        } else {
            tv_control_x9.setVisibility(View.VISIBLE);
            tv_bottom_recharge.setVisibility(View.GONE);
        }
        tv_start.setText(value);
        tv_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//the  start button of remote control
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        if (layout_recharge.getVisibility() == View.VISIBLE && isRecharge) {//避免重复刷新UI导致异常
            return;
        }
        layout_remote_control.setVisibility(View.GONE);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_bottom_recharge_x8.setSelected(isRecharge);
        layout_recharge.setVisibility(View.VISIBLE);
    }

}
