package com.ilife.home.robot.activity;

import android.content.Context;
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

/**
 * Created by chenjiaping on 2017/7/28.
 */

public class ConsumesActivity extends BackBaseActivity implements View.OnLongClickListener {
    final String TAG = ConsumesActivity.class.getSimpleName();
    LayoutInflater inflater;
    ProgressBar pb_side, pb_roll, pb_filter;
    TextView tv_percent_side, tv_percent_roll, tv_percent_filter;
    LinearLayout rl_side;
    LinearLayout rl_roll;
    LinearLayout rl_filter;
    ArrayList<Integer> ids;
    byte[] bytes;
    int index;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;
    @BindView(R.id.tv_roll)
    TextView tv_roll;
    @BindView(R.id.tv_1)
    TextView tv_tips;

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

        rl_side = (LinearLayout) findViewById(R.id.rl_side);
        rl_roll = (LinearLayout) findViewById(R.id.rl_roll);
        rl_filter = (LinearLayout) findViewById(R.id.rl_filter);

        tv_percent_side = (TextView) findViewById(R.id.tv_percent_side);
        tv_percent_roll = (TextView) findViewById(R.id.tv_percent_roll);
        tv_percent_filter = (TextView) findViewById(R.id.tv_percent_filter);
        rl_side.setOnLongClickListener(this);
        rl_roll.setOnLongClickListener(this);
        rl_filter.setOnLongClickListener(this);
    }

    public void initData() {
        ids = new ArrayList<>();
        ids.add(R.id.rl_side);
        ids.add(R.id.rl_roll);
        ids.add(R.id.rl_filter);
        queryConsumer();
        tv_top_title.setText(R.string.setting_aty_consume_detail);
        if (DeviceUtils.getRobotType("").equals(Constants.V85) || DeviceUtils.getRobotType("").equals(Constants.V5x) || DeviceUtils.getRobotType("").equals(Constants.V3x)) {//V85是吸口型，没有滚刷。
            rl_roll.setVisibility(View.GONE);
            tv_tips.setText(Utils.getString(R.string.consume_aty_text_2_v85));
        }
    }

    public void showResetDialog(int tag) {
        String title = "";
        String hint = "";
        switch (tag) {
            case R.id.rl_side:
                title = Utils.getString(R.string.consume_aty_resetSide);
                hint = Utils.getString(R.string.consume_aty_resetSide_over);
                break;
            case R.id.rl_roll:
                title = Utils.getString(R.string.consume_aty_resetRoll_a9);
                hint = Utils.getString(R.string.consume_aty_resetRoll_over_a9);
//                if (DeviceUtils.getRobotType(subdomain).equals(Constants.A9) || DeviceUtils.getRobotType(subdomain).equals(Constants.A7)) {
//                    title = Utils.getString(R.string.consume_aty_resetRoll_a9);
//                    hint = Utils.getString(R.string.consume_aty_resetRoll_over_a9);
//                } else {
//                    title = Utils.getString(R.string.consume_aty_resetRoll);
//                    hint = Utils.getString(R.string.consume_aty_resetRoll_over);
//                }
                break;
            case R.id.rl_filter:
                title = Utils.getString(R.string.consume_aty_resetFilter);
                hint = Utils.getString(R.string.consume_aty_resetFilter_over);
                break;
        }
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(title).setHintTip(hint).
                setOnRightButtonClck(() -> {
                    int index=ids.indexOf(tag);
                    IlifeAli.getInstance().resetConsumer(index == 0 ? 100 : pb_side.getProgress(), index == 1 ? 100 : pb_roll.getProgress(), index == 2 ? 100 : pb_filter.getProgress(), new OnAliResponse<String>() {
                        @Override
                        public void onSuccess(String result) {
                            switch (tag){
                                case R.id.rl_side:
                                    pb_side.setProgress(100);
                                    tv_percent_side.setText(100+"%");
                                    break;
                                case R.id.rl_roll:
                                    pb_roll.setProgress(100);
                                    tv_percent_roll.setText(100+"%");
                                    break;
                                case R.id.rl_filter:
                                    pb_filter.setProgress(100);
                                    tv_percent_filter.setText(100+"%");
                                    break;
                            }
                        }
                        @Override
                        public void onFailed(int code, String message) {

                        }
                    });
                }).show(getSupportFragmentManager(), "" + tag);
    }
    @Override
    public boolean onLongClick(View v) {
        showResetDialog(v.getId());
        return false;
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

                    tv_percent_side.setText(sideBrushLife+ "%");
                    tv_percent_roll.setText(rollBrushLife+ "%");
                    tv_percent_filter.setText(filterIife + "%");
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

}
