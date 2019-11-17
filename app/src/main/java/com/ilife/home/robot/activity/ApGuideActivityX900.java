package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.LoadingDialogFragment;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.GifView;
import com.ilife.home.robot.view.ToggleRadioButton;
import com.ilife.home.robot.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chengjiaping on 2018/8/28.
 */
//DONE
public class ApGuideActivityX900 extends BackBaseActivity {
    final String TAG = ApGuideActivityX900.class.getSimpleName();
    Context context;
    @BindView(R.id.bt_next)
    Button bt_next;
    @BindView(R.id.text_tip1)
    TextView text_tip1;
    @BindView(R.id.text_tip2)
    TextView text_tip2;
    @BindView(R.id.rb_next_tip)
    ToggleRadioButton rb_next_tip;
    @BindView(R.id.ll_ap_step1)
    LinearLayout ll_ap_step1;
    @BindView(R.id.ll_ap_step2)
    LinearLayout ll_ap_step2;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.gif_open_key)
    GifView gif_open_key;
    @BindView(R.id.gif_click_wifi)
    GifView gif_click_wifi;
    @BindView(R.id.tv_guide_tip4)
    TextView tv_guide_tip4;
    private int curStep;
    private int tip3_id;
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_guide_x900;
    }

    @Override
    public void initView() {
        context = this;
        int open_key_id, click_wifi_id, tip1_id, tip2_id;
        String robotType = DeviceUtils.getRobotType("");
        switch (robotType) {
            case Constants.A9:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_a9;
                tip3_id = R.string.ap_guide_already_open_wifi_a9;
                open_key_id = R.drawable.gif_open_key_800;
                click_wifi_id = R.drawable.gif_click_wifi_800;
                break;
            case Constants.X800://800 a9
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x900;
                tip3_id = R.string.ap_guide_already_open_wifi;
                open_key_id = R.drawable.gif_open_key_800;
                click_wifi_id = R.drawable.gif_click_wifi_800;
                break;
            case Constants.X910:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x900;
                tip3_id = R.string.ap_guide_already_open_wifi;
                open_key_id = R.drawable.gif_open_key_910;
                click_wifi_id = R.drawable.gif_click_wifi_910;
                break;
            case Constants.X900:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x900;
                tip3_id = R.string.ap_guide_already_open_wifi;
                open_key_id = R.drawable.gif_open_key;
                click_wifi_id = R.drawable.gif_click_wifi;
                break;
            case Constants.A9s:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_a9s;
                tip3_id = R.string.ap_guide_already_open_wifi_a9s;
                open_key_id = R.drawable.gif_open_key_800;
                click_wifi_id = R.drawable.gif_click_wifi_800;
                break;
            case Constants.A8s:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_a9s;
                tip3_id = R.string.ap_guide_already_open_wifi_a9s;
                open_key_id = R.drawable.gif_open_key_a8s;
                click_wifi_id = R.drawable.gif_click_wifi_a8s;
                break;
            case Constants.V85: //v85
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_v85;
                tip3_id = R.string.ap_guide_have_heard_didi_v85;
                open_key_id = R.drawable.gif_open_key_v85;
                click_wifi_id = R.drawable.gif_click_wifi_v85;
                break;
            case Constants.V3x:
            case Constants.V5x:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x7;
                tip3_id = R.string.ap_guide_have_heard_didi;
                open_key_id = R.drawable.gif_open_key_787;
                click_wifi_id = R.drawable.gif_click_wifi_v5x;
                break;
            case Constants.X785:
            case Constants.X787:
            case Constants.A7:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x7;
                tip3_id = R.string.ap_guide_have_heard_didi;
                open_key_id = R.drawable.gif_open_key_787;
                click_wifi_id = R.drawable.gif_click_wifi_787;
                break;
            default:
                tip1_id = R.string.ap_guide_aty_tip1_x900;
                tip2_id = R.string.ap_guide_aty_tip2_x7;
                tip3_id = R.string.ap_guide_have_heard_didi;
                open_key_id = R.drawable.gif_open_key_787;
                click_wifi_id = R.drawable.gif_click_wifi_787;
                break;
        }
        text_tip1.setText(tip1_id);
        text_tip2.setText(tip2_id);
        gif_open_key.setMovieResource(open_key_id);
        gif_click_wifi.setMovieResource(click_wifi_id);
        tv_title.setText(R.string.guide_ap_prepare);
        curStep = 1;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_next:
                if (curStep == 1) {
                    ll_ap_step1.setVisibility(View.GONE);
                    ll_ap_step2.setVisibility(View.VISIBLE);
                    rb_next_tip.setText(tip3_id);
                    if (Utils.isIlife() && Utils.isChinaEnvironment() && Constants.IS_FIRST_AP) {
                        bt_next.setText(R.string.add_aty_start_connect);
                    }
                    tv_guide_tip4.setVisibility(View.VISIBLE);
                    rb_next_tip.setChecked(false);
                    curStep = 2;
                    if (loadingDialogFragment == null) {
                        loadingDialogFragment = new LoadingDialogFragment();
                    }
                    loadingDialogFragment.showNow(getSupportFragmentManager(), "loading");
                } else {
                    Intent i = new Intent(context, ApWifiActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void clickBackBtn() {
        if (curStep == 2) {
            if (loadingDialogFragment != null) {
                loadingDialogFragment.dismiss();
            }
            curStep = 1;
            ll_ap_step1.setVisibility(View.VISIBLE);
            ll_ap_step2.setVisibility(View.GONE);
            tv_guide_tip4.setVisibility(View.INVISIBLE);
            rb_next_tip.setText(R.string.ap_guide_already_open_device);
            bt_next.setText(R.string.guide_aty_next);
        } else {
            super.clickBackBtn();
        }
    }
}
