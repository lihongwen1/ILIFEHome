package com.ilife.home.robot.activity;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.aliyun.iot.aep.sdk.page.ToggleRadioButton;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class ApGuideOpenPowerActivity extends BackBaseActivity {

    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_next)
    Button bt_next;
    @BindView(R.id.rb_next_tip)
    ToggleRadioButton rb_next_tip;
    @BindView(R.id.iv_open_power)
    ImageView iv_open_power;

    @Override
    public int getLayoutId() {
        return R.layout.activity_open_power;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.guide_ap_prepare);
        bt_next.setSelected(false);
        bt_next.setClickable(false);
        rb_next_tip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                bt_next.setSelected(true);
                bt_next.setClickable(true);
            } else {
                bt_next.setSelected(false);
                bt_next.setClickable(false);
            }
        });
        String productKey = IlifeAli.getInstance().getBindingProductKey();
        RobotConfigBean robotConfig = MyApplication.getInstance().readRobotConfig();
        int picOpenPower = UiUtil.getDrawable(robotConfig.getRobotBeanByPk(productKey).getOpenPowerImg());
        if (picOpenPower!=0){
               iv_open_power.setBackground(getResources().getDrawable(picOpenPower));
        }
    }

    @OnClick({R.id.bt_next})
    public void onViewClick() {
        startActivity(new Intent(ApGuideOpenPowerActivity.this, ApGuideReadyRobotWifi.class));
    }
}
