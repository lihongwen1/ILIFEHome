package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.fragment.LoadingDialogFragment;
import com.aliyun.iot.aep.sdk.page.ToggleRadioButton;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class ApGuideReadyRobotWifi extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_next)
    Button bt_next;
    @BindView(R.id.rb_next_tip)
    ToggleRadioButton rb_next_tip;
    @BindView(R.id.iv_pic_start)
    ImageView iv_pic_start;
    @BindView(R.id.text_tip2)
    TextView text_tip2;
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_ready_robot_wifi;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isNeedWaiForPower = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(IlifeAli.getInstance().getBindingProductKey()).isWaitForOpenPower();
        if (isNeedWaiForPower) {
            if (loadingDialogFragment == null) {
                loadingDialogFragment = new LoadingDialogFragment();
            }
            loadingDialogFragment.showNow(getSupportFragmentManager(), "loading");
        }
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
        RobotConfigBean.RobotBean rBean = robotConfig.getRobotBeanByPk(productKey);
        int pic_product = UiUtil.getDrawable(rBean.getReadyWifiImg());
        int tip2 = UiUtil.getString(rBean.getPrepareWifiTip());
        int next_tip = UiUtil.getString(rBean.getReadyForWifiTip());
        if (pic_product != 0) {
            text_tip2.setText(tip2);
            rb_next_tip.setText(next_tip);
            iv_pic_start.setBackground(getResources().getDrawable(pic_product));
        }
    }

    @OnClick({R.id.bt_next})
    public void onViewClick() {
        startActivity(new Intent(ApGuideReadyRobotWifi.this, ConnectHomeWifiActivity.class));
    }
}
