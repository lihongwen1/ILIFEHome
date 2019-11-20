package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chenjiaping on 2017/7/28.
 */

public class ConsumesActivity extends BackBaseActivity {
    final String TAG = ConsumesActivity.class.getSimpleName();
    LayoutInflater inflater;
    ProgressBar pb_side, pb_roll, pb_filter;
    TextView tv_percent_side, tv_percent_roll, tv_percent_filter;
    byte[] bytes;
    int index;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;
    @BindView(R.id.tv_roll)
    TextView tv_roll;
    @BindView(R.id.tv_1)
    TextView tv_tips;
    @BindView(R.id.tv_filter_remain_time)
    TextView tv_filter_remain_time;
    @BindView(R.id.tv_side_remain_time)
    TextView tv_side_remain_time;
    @BindView(R.id.tv_roll_remain_time)
    TextView tv_roll_remain_time;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_consumes;
    }

    public void initView() {
        context = this;
        inflater = LayoutInflater.from(context);
        pb_side = (ProgressBar) findViewById(R.id.pb_side);
        pb_roll = (ProgressBar) findViewById(R.id.pb_roll);
        pb_filter = (ProgressBar) findViewById(R.id.pb_filter);


        tv_percent_side = (TextView) findViewById(R.id.tv_percent_side);
        tv_percent_roll = (TextView) findViewById(R.id.tv_percent_roll);
        tv_percent_filter = (TextView) findViewById(R.id.tv_percent_filter);

    }

    public void initData() {
        queryConsumer();
        tv_top_title.setText(R.string.setting_aty_consume_detail);
    }


    @OnClick({R.id.rl_roll, R.id.rl_filter, R.id.rl_side})
    public void onClick(View view) {
        int consumerType = 1;
        switch (view.getId()) {
            case R.id.rl_side:
                consumerType = 1;
                break;
            case R.id.rl_roll:
                consumerType = 2;
                break;
            case R.id.rl_filter:
                consumerType = 3;
                break;
        }
        Intent intent = new Intent(ConsumesActivity.this, ConsumerDetailActivity.class);
        intent.putExtra(ConsumerDetailActivity.KEY_CONSUMER_TYPE, consumerType);
        intent.putExtra(ConsumerDetailActivity.KEY_CONSUMER_PROGRESS, new int[]{pb_side.getProgress(), pb_roll.getProgress(), pb_filter.getProgress()});
        startActivity(intent);

    }


    private void queryConsumer() {
        IlifeAli.getInstance().queryConsumer(new OnAliResponse<String>() {
            @Override
            public void onSuccess(String response) {
                if (!TextUtils.isEmpty(response)) {
                    JSONObject json = JSONObject.parseObject(response);
                    int sideBrushLife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SIDE_BRUSH_LIFE);
                    int rollBrushLife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_MAIN_BRUSH_LIFE);
                    int filterIife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_FILTER_LIFE);
                    pb_side.setProgress(sideBrushLife);
                    pb_roll.setProgress(rollBrushLife);
                    pb_filter.setProgress(filterIife);
                    tv_percent_side.setText(sideBrushLife + "%");
                    tv_percent_roll.setText(rollBrushLife + "%");
                    tv_percent_filter.setText(filterIife + "%");
                    tv_filter_remain_time.setText(getResources().getString(R.string.consumer_remain_time, (int) (300 * filterIife / 100f) + ""));
                    tv_roll_remain_time.setText(getResources().getString(R.string.consumer_remain_time, (int) (300 * rollBrushLife / 100f) + ""));
                    tv_side_remain_time.setText(getResources().getString(R.string.consumer_remain_time, (int) (300 * sideBrushLife / 100f) + ""));
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

}
