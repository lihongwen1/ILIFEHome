package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.LoadingDialogFragment;
import com.aliyun.iot.aep.sdk.page.ToggleRadioButton;
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
        if (!IlifeAli.getInstance().getBindingProductKey().equals(EnvConfigure.PRODUCT_KEY_X320)) {
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
        int pic_product = -1;
        String tip2 = "";
        String next_tip = "";
        switch (productKey) {
            case EnvConfigure.PRODUCT_KEY_X800:
                pic_product = R.drawable.pic_start;
                tip2 = Utils.getString(R.string.ap_guide_sty_typ2_x800);
                next_tip = Utils.getString(R.string.ap_guide_already_open_wifi);
                break;
            case EnvConfigure.PRODUCT_KEY_X800_W:
                pic_product = R.drawable.pic_start_w;
                tip2 = Utils.getString(R.string.ap_guide_sty_typ2_x800);
                next_tip = Utils.getString(R.string.ap_guide_already_open_wifi);
                break;
            case EnvConfigure.PRODUCT_KEY_X320:
                pic_product = R.drawable.pic_start_v3x;
                tip2 = Utils.getString(R.string.ap_guide_sty_typ2_x320);
                next_tip = Utils.getString(R.string.ap_guide_already_heared_didi);
                break;
            case EnvConfigure.PRODUCT_KEY_X787://TODO 更换正确
                pic_product = R.drawable.pic_start_787;
                tip2 = Utils.getString(R.string.ap_guide_sty_typ2_x320);
                next_tip = Utils.getString(R.string.ap_guide_already_heared_didi);
                break;

        }
        if (pic_product != -1) {
            text_tip2.setText(tip2);
            rb_next_tip.setText(next_tip);
            iv_pic_start.setBackground(getResources().getDrawable(pic_product));
        }
    }

    @OnClick({R.id.bt_next})
    public void onViewClick() {
        startActivity(new Intent(ApGuideReadyRobotWifi.this, ApWifiActivity.class));
    }
}
