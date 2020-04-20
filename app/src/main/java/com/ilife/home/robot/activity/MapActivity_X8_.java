package com.ilife.home.robot.activity;

import android.os.Bundle;

import android.view.View;

import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.UiUtil;

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
        iv_recharge_stand.setImageResource(UiUtil.getDrawable(mPresenter.getRobotBean().getRechargeStand()));
        int robotPic= UiUtil.getDrawable(mPresenter.getRobotBean().getFaceImg());
        iv_recharge_model.setImageResource(robotPic);
        iv_point_robot.setImageResource(robotPic);
        iv_along_robot.setImageResource(robotPic);
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
        } else {
            tv_control_x9.setVisibility(View.VISIBLE);
        }
        tv_start.setText(value);
        iv_map_start.setSelected(isSelect);
        image_center.setSelected(isSelect);//the  start button of remote control
    }

    @Override
    public void updateRecharge(boolean isRecharge) {
        if (layout_recharge.getVisibility() == View.VISIBLE && isRecharge) {//避免重复刷新UI导致异常
            return;
        }
        layout_remote_control.setVisibility(View.GONE);
        tv_bottom_recharge.setSelected(isRecharge);
        tv_bottom_recharge.setSelected(isRecharge);
        layout_recharge.setVisibility(View.VISIBLE);
    }

}
