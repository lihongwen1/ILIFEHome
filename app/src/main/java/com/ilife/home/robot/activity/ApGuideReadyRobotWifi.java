package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.LoadingDialogFragment;
import com.ilife.home.robot.view.ToggleRadioButton;

import butterknife.BindView;
import butterknife.OnClick;

public class ApGuideReadyRobotWifi extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_next)
    Button bt_next;
    @BindView(R.id.rb_next_tip)
    ToggleRadioButton rb_next_tip;
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_ready_robot_wifi;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (loadingDialogFragment == null) {
            loadingDialogFragment = new LoadingDialogFragment();
        }
        loadingDialogFragment.showNow(getSupportFragmentManager(), "loading");
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
    }

    @OnClick({R.id.bt_next})
    public void onViewClick() {
        startActivity(new Intent(ApGuideReadyRobotWifi.this, ApWifiActivity.class));
    }
}
