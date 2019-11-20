package com.ilife.home.robot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class ConsumerDetailActivity extends BackBaseActivity {
    @BindView(R.id.iv_consumer)
    ImageView iv_consumer;
    @BindView(R.id.tv_consumer)
    TextView tv_consumer;
    @BindView(R.id.pb_consumer)
    ProgressBar pb_consumer;
    @BindView(R.id.tv_consumer_time)
    TextView tv_consumer_time;
    @BindView(R.id.consumer_percent)
    TextView consumer_percent;
    @BindView(R.id.tv_consumer_detail_tip)
    TextView tv_consumer_detail_tip;
    @BindView(R.id.tv_top_title)
    TextView title;
    @BindView(R.id.btn_reset_consumer)
    Button btn_reset_consumer;
    public static final String KEY_CONSUMER_TYPE = "consumer_type";
    public static final String KEY_CONSUMER_PROGRESS = "consumer_progress";
    private int type = 1;
    private int[] progresss;
    private String dialogTitle, dialogHint;

    @Override
    public int getLayoutId() {
        return R.layout.activity_consumer_detail;
    }

    @Override
    public void initData() {
        super.initData();
        Intent intent = getIntent();
        type = intent.getIntExtra(KEY_CONSUMER_TYPE, 1);
        progresss = intent.getIntArrayExtra(KEY_CONSUMER_PROGRESS);
    }

    @Override
    public void initView() {
        int consumerId = -1;
        int consumerTime = (int) (300 * progresss[type - 1] / 100f);
        int consumerTipId = -1;
        int consumerImgId = -1;
        switch (type) {
            case 1:
                consumerId = R.string.consume_aty_side_time;
                consumerTipId = R.string.consumer_tip_side;
                consumerImgId = R.drawable.n_icon_bianshua;
                break;
            case 2:
                consumerId = R.string.consume_aty_roll_time;
                consumerTipId = R.string.consumer_tip_roll;
                consumerImgId = R.drawable.n_icon_gunshua;
                break;
            case 3:
                consumerId = R.string.consume_aty_filter_time;
                consumerTipId = R.string.consumer_tip_fillter;
                consumerImgId = R.drawable.n_icon_lvwang;
                break;
        }
        iv_consumer.setImageResource(consumerImgId);
        tv_consumer.setText(consumerId);
        tv_consumer_time.setText(consumerTime+"");
        pb_consumer.setProgress(progresss[type - 1]);
        consumer_percent.setText(progresss[type - 1]+"");
        tv_consumer_detail_tip.setText(consumerTipId);
        title.setText(consumerId);
        btn_reset_consumer.setSelected(true);


        switch (type) {
            case 1:
                dialogTitle = Utils.getString(R.string.consume_aty_resetSide);
                dialogHint = Utils.getString(R.string.consume_aty_resetSide_over);
                break;
            case 2:
                dialogTitle = Utils.getString(R.string.consume_aty_resetRoll_a9);
                dialogHint = Utils.getString(R.string.consume_aty_resetRoll_over_a9);
                break;
            case 3:
                dialogTitle = Utils.getString(R.string.consume_aty_resetFilter);
                dialogHint = Utils.getString(R.string.consume_aty_resetFilter_over);
                break;
        }
    }

    @OnClick(R.id.btn_reset_consumer)
    public void onClick(View view) {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(dialogTitle).setHintTip(dialogHint).
                setOnRightButtonClck(() -> {

                    IlifeAli.getInstance().resetConsumer(progresss[0], progresss[1], progresss[2], new OnAliResponse<String>() {
                        @Override
                        public void onSuccess(String result) {
                            pb_consumer.setProgress(100);
                            tv_consumer_time.setText("300h");
                            consumer_percent.setText("100%");
                        }

                        @Override
                        public void onFailed(int code, String message) {

                        }
                    });


                }).show(getSupportFragmentManager(), "resetconsumer");


    }
}
