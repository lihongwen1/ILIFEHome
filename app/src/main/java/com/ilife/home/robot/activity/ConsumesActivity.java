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

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.model.ConsumerModel;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by chenjiaping on 2017/7/28.
 */

public class ConsumesActivity extends BackBaseActivity {
    final String TAG = ConsumesActivity.class.getSimpleName();

    @BindView(R.id.pb_side)
    ProgressBar pb_side;
    @BindView(R.id.pb_roll)
    ProgressBar pb_roll;
    @BindView(R.id.pb_filter)
    ProgressBar pb_filter;

    @BindView(R.id.tv_percent_side)
    TextView tv_percent_side;
    @BindView(R.id.tv_percent_roll)
    TextView tv_percent_roll;
    @BindView(R.id.tv_percent_filter)
    TextView tv_percent_filter;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;
    @BindView(R.id.tv_roll)
    TextView tv_roll;
    @BindView(R.id.tv_1)
    TextView tv_tips;
    private ConsumerModel consumerModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_consumes;
    }

    public void initView() {
        tv_top_title.setText(R.string.setting_aty_consume_detail);
        String robotType = DeviceUtils.getRobotType(IlifeAli.getInstance().getWorkingDevice().getProductKey());
        if (robotType.equals(Constants.V3x)) {// V3X V85是吸口型，没有滚刷。
            findViewById(R.id.rl_roll).setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {
        super.initData();
        consumerModel = ViewModelProviders.of(this).get(ConsumerModel.class);
        MutableLiveData<String> consumerData = consumerModel.getConsumeData();
        consumerData.observe(this, response -> {
            if (!TextUtils.isEmpty(response)) {
                MyLogger.d(TAG, "耗材数据： " + response);
                JSONObject json = JSONObject.parseObject(response);
                int sideBrushLife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_SIDE_BRUSH_LIFE);
                int rollBrushLife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_MAIN_BRUSH_LIFE);
                int filterIife = json.getJSONObject(EnvConfigure.KEY_PARTS_STATUS).getJSONObject(EnvConfigure.KEY_VALUE).getIntValue(EnvConfigure.KEY_FILTER_LIFE);
                setConsumerProgress(sideBrushLife, pb_side, tv_percent_side);
                setConsumerProgress(rollBrushLife, pb_roll, tv_percent_roll);
                setConsumerProgress(filterIife, pb_filter, tv_percent_filter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        consumerModel.queryConsumer();
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

    private void setConsumerProgress(int progress, ProgressBar pb_consumer, TextView consumerPercent) {
        pb_consumer.setProgress(progress);
        consumerPercent.setText(progress + "%");
        if (progress >= 80) {
            pb_consumer.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_grenn));
        } else if (progress >= 20) {
            pb_consumer.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_yellow));
        } else {
            pb_consumer.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bg_red));
        }
    }


}
