package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by chengjiaping on 2018/8/16.
 */

public class SettingActivity extends BackBaseActivity implements OnAliSetPropertyResponse {
    final String TAG = SettingActivity.class.getSimpleName();
    final int TAG_FIND_DONE = 0x01;
    public static final String KEY_MODE = "KEY_MODE";
    int waterLevel, mode, index;
    boolean isMaxMode, voiceOpen;
    private String productKey;
    Intent intent;
    String devName, name;
    @BindView(R.id.image_soft)
    ImageView image_soft;
    @BindView(R.id.image_standard)
    ImageView image_standard;
    @BindView(R.id.image_strong)
    ImageView image_strong;
    @BindView(R.id.image_max)
    ImageView image_max;
    @BindView(R.id.image_voice)
    ImageView image_voice;
    @BindView(R.id.image_plan)
    ImageView image_plan;
    @BindView(R.id.image_random)
    ImageView image_random;
    @BindView(R.id.image_product)
    ImageView image_product;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.tv_soft)
    TextView tv_soft;
    @BindView(R.id.tv_standard)
    TextView tv_standard;
    @BindView(R.id.tv_strong)
    TextView tv_strong;
    @BindView(R.id.tv_water)
    TextView tv_water;
    @BindView(R.id.tv_plan)
    TextView tv_plan;
    @BindView(R.id.tv_random)
    TextView tv_random;
    @BindView(R.id.tv_mode)
    TextView tv_mode;
    @BindView(R.id.tv_top_title)
    TextView tv_top_title;
    @BindView(R.id.image_down_1)
    ImageView image_down_1;
    @BindView(R.id.image_down_2)
    ImageView image_down_2;
    @BindView(R.id.rl_water)
    RelativeLayout rl_water;
    @BindView(R.id.rl_clock)
    RelativeLayout rl_clock;
    @BindView(R.id.rl_record)
    RelativeLayout rl_record;
    @BindView(R.id.rl_consume)
    RelativeLayout rl_consume;
    @BindView(R.id.rl_mode)
    RelativeLayout rl_mode;
    @BindView(R.id.rl_suction)
    RelativeLayout rl_suction;
    @BindView(R.id.rl_find)
    RelativeLayout rl_find;
    @BindView(R.id.rl_soft)
    RelativeLayout rl_soft;
    @BindView(R.id.rl_standard)
    RelativeLayout rl_standard;
    @BindView(R.id.rl_strong)
    RelativeLayout rl_strong;
    @BindView(R.id.rl_plan)
    RelativeLayout rl_plan;
    @BindView(R.id.rl_random)
    RelativeLayout rl_random;
    @BindView(R.id.rl_facReset)
    RelativeLayout rl_facReset;
    @BindView(R.id.rl_voice)
    RelativeLayout rl_voice;
    @BindView(R.id.rl_update)
    RelativeLayout rl_update;
    @BindView(R.id.ll_water)
    LinearLayout ll_water;
    @BindView(R.id.ll_mode)
    LinearLayout ll_mode;
    @BindView(R.id.iv_find_device)
    ImageView iv_find_device;
    LayoutInflater inflater;
    Animation animation;
    private CompositeDisposable mDisposable;
    private RenameActivity renameFragment;
    WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == TAG_FIND_DONE) {
                if (rl_find != null) {
                    rl_find.setClickable(true);
                    iv_find_device.setSelected(false);
                    iv_find_device.clearAnimation();
                }

            }
            return false;
        }
    });


    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //TODO 监听属性变化


    public void initView() {
        context = this;
        inflater = LayoutInflater.from(context);
        tv_top_title.setText(R.string.ap_aty_setting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        name = IlifeAli.getInstance().getWorkingDevice().getNickName();
        if (name == null || name.isEmpty()) {
            devName = IlifeAli.getInstance().getWorkingDevice().getDeviceName();
            tv_name.setText(devName);
        } else {
            tv_name.setText(name);
        }
    }

    public void initData() {
        productKey = IlifeAli.getInstance().getWorkingDevice().getProductKey();
        mDisposable = new CompositeDisposable();
        DeviceInfoBean infoBean = IlifeAli.getInstance().getWorkingDevice();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims);
        animation.setInterpolator(new LinearInterpolator());
        mode = SpUtils.getInt(context, productKey + KEY_MODE);
        waterLevel = infoBean.getDeviceInfo().getWaterLevel();
        isMaxMode = infoBean.getDeviceInfo().isMaxMode();
        voiceOpen = infoBean.getDeviceInfo().isVoiceOpen();
        setMode(mode);
        setStatus(waterLevel, isMaxMode, voiceOpen);
        String robotType = DeviceUtils.getRobotType(productKey);
        int product = DeviceUtils.getRobotPic(robotType);
        switch (robotType) {
            case Constants.X800:
                rl_mode.setVisibility(View.GONE);
                rl_update.setVisibility(View.VISIBLE);
                break;
            case Constants.X800W:
                rl_mode.setVisibility(View.GONE);
                rl_update.setVisibility(View.VISIBLE);
                robotType = Constants.X800;//X800白色款，需显示为X800
                break;
            case Constants.V3x:
                rl_record.setVisibility(View.GONE);
                rl_voice.setVisibility(View.GONE);
                rl_mode.setVisibility(View.GONE);
                rl_update.setVisibility(View.GONE);
                break;
            case Constants.X787:
                rl_voice.setVisibility(View.GONE);
                break;
            default:
                product = R.drawable.n_x800;
                break;
        }
        tv_type.setText(BuildConfig.BRAND + " " + robotType);
        image_product.setImageResource(product);
    }

    public void setMode(int mode) {
        boolean isRandom = mode == MsgCodeUtils.STATUE_RANDOM;
        tv_mode.setText(isRandom ? getString(R.string.setting_aty_random_clean)
                : getString(R.string.setting_aty_nav_clean));
        image_plan.setSelected(!isRandom);
        image_random.setSelected(isRandom);
        tv_plan.setSelected(!isRandom);
        tv_random.setSelected(isRandom);
    }

    public void setStatus(int water, boolean isMaxMode, boolean isoVoiceOpen) {
        image_max.setSelected(isMaxMode);
        image_voice.setSelected(voiceOpen);
        clearAll();
        switch (water) {
            case 100:
                tv_standard.setSelected(true);
                image_standard.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_standard));
                break;
            case 0:
                tv_soft.setSelected(true);
                image_soft.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_soft));
                break;
            case 1:
                tv_standard.setSelected(true);
                image_standard.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_standard));
                break;
            case 2:
                tv_strong.setSelected(true);
                image_strong.setSelected(true);
                tv_water.setText(getString(R.string.setting_aty_strong));
                break;
        }
    }

    public void clearAll() {
        tv_soft.setSelected(false);
        tv_standard.setSelected(false);
        tv_strong.setSelected(false);
        image_soft.setSelected(false);
        image_standard.setSelected(false);
        image_strong.setSelected(false);
    }

    @OnClick({R.id.tv_name, R.id.rl_water, R.id.rl_clock, R.id.rl_record, R.id.rl_consume, R.id.rl_mode, R.id.rl_find,
            R.id.rl_plan, R.id.rl_random, R.id.rl_facReset, R.id.rl_voice, R.id.rl_update, R.id.rl_suction, R.id.rl_soft
            , R.id.rl_standard, R.id.rl_strong})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_name:
                Intent intent = new Intent(SettingActivity.this, RenameActivity.class);
                intent.putExtra(RenameActivity.KEY_RENAME_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.rl_water:
                if (ll_water.getVisibility() == View.GONE) {
                    image_down_1.setRotation(-90);
                    ll_water.setVisibility(View.VISIBLE);
                } else {
                    image_down_1.setRotation(90);
                    ll_water.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_clock:
                intent = new Intent(context, ClockingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_record:
                intent = new Intent(context, HistoryActivity_x9.class);
                startActivity(intent);
                break;
            case R.id.rl_consume:
                intent = new Intent(context, ConsumesActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_mode:
                if (ll_mode.getVisibility() == View.GONE) {
                    ll_mode.setVisibility(View.VISIBLE);
                    image_down_2.setRotation(-90);
                } else {
                    image_down_2.setRotation(90);
                    ll_mode.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_find:
                IlifeAli.getInstance().findDevice(this);
                rl_find.setClickable(false);
                iv_find_device.setSelected(true);
                iv_find_device.startAnimation(animation);
                break;
            case R.id.rl_plan:
                if (!image_plan.isSelected()) {
                    SpUtils.saveInt(context, productKey + KEY_MODE, MsgCodeUtils.STATUE_PLANNING);
                    mode = MsgCodeUtils.STATUE_PLANNING;
                    setMode(mode);
                }
                break;
            case R.id.rl_random:
                if (!image_random.isSelected()) {
                    SpUtils.saveInt(context, productKey + KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
                    mode = MsgCodeUtils.STATUE_RANDOM;
                    setMode(mode);
                }
                break;
            case R.id.rl_facReset:
                if (IlifeAli.getInstance().getWorkingDevice().getOwned() == 1) {
                    showResetDialog();
                } else {
                    ToastUtils.showToast(context, getString(R.string.setting_aty_only_admin));
                }
                break;
            case R.id.rl_voice:
                IlifeAli.getInstance().setVoiceOpen(!voiceOpen, this);
                //TODO 待实现
                break;
            case R.id.rl_suction:
                if (canOperateSuction()) {
                    IlifeAli.getInstance().setMaxMode(isMaxMode ? 1 : 0, this);
                } else {
                    ToastUtils.showToast(Utils.getString(R.string.settiing_change_suction_tip));
                }
                break;
            case R.id.rl_soft:
                IlifeAli.getInstance().waterControl(0, this);
                break;
            case R.id.rl_standard:
                IlifeAli.getInstance().waterControl(1, this);
                break;
            case R.id.rl_strong:
                IlifeAli.getInstance().waterControl(2, this);
                break;
            case R.id.rl_update:
                if (IlifeAli.getInstance().getWorkingDevice().getOwned() == 1) {
                    startActivity(new Intent(context, OtaUpdateActivity.class));
                } else {
                    ToastUtils.showToast(context, getString(R.string.setting_aty_only_admin));
                }
                break;
        }
    }


    private void showResetDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.setting_aty_confirm_reset))
                .setHintTip(Utils.getString(R.string.setting_aty_reset_hint)).setOnRightButtonClck(() -> {
            showLoadingDialog();
            IlifeAli.getInstance().resetDeviceToFactory(this);
        }).show(getSupportFragmentManager(), "reset");
    }


    @Override
    public void onSuccess(String path, int tag, int value, int responseCode) {
        switch (path) {
            case EnvConfigure.PATH_SET_DEV_NICK_NAME:
                tv_name.setText(name);
                ToastUtils.showToast(context, getString(R.string.bind_aty_reName_suc));
                break;
            case EnvConfigure.PATH_SET_PROPERTIES:
                switch (tag) {
                    case EnvConfigure.VALUE_SET_WATER:
                        if (responseCode == 200) {
                            waterLevel = value;
                            setStatus(waterLevel, isMaxMode, voiceOpen);
                        }
                        break;
                    case EnvConfigure.VALUE_SET_MAX:
                        if (responseCode == 200) {
                            isMaxMode = value == 1;
                            setStatus(waterLevel, isMaxMode, voiceOpen);
                        }
                        break;
                    case EnvConfigure.VALUE_FIND_ROBOT:
                        if (responseCode == 200) {
                            Disposable disposable = Observable.timer(10, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
                                        findDone();
                                    });
                            mDisposable.add(disposable);
                        } else {
                            findDone();
                        }
                        break;
                    case EnvConfigure.VALUE_FAC_RESET:
                        if (responseCode == 200) {
                            MyLogger.d(TAG, "恢复出厂设置成功");
                            Disposable dis = Observable.timer(6, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
                                MyLogger.d(TAG, "进入主页面------------");
                                goToMain();
                            });
                            mDisposable.add(dis);
                        }
                        break;
                    case EnvConfigure.VALUE_SET_VOICE_SWITCH:
                        if (responseCode == 200) {
                            voiceOpen = value == 0;
                            setStatus(waterLevel, isMaxMode, voiceOpen);
                        }
                        break;

                }
                break;
        }
    }

    @Override
    public void onFailed(String path, int tag, int code, String message) {
        if (path.equals(EnvConfigure.PATH_SET_PROPERTIES) && tag == EnvConfigure.VALUE_FIND_ROBOT) {
            findDone();
        } else if (path.equals(EnvConfigure.PATH_SET_DEV_NICK_NAME)) {
            ToastUtils.showToast(context, getString(R.string.bind_aty_reName_fail));
        } else {
            ToastUtils.showToast(context, "网络错误");
        }
    }

    private boolean canOperateSuction() {
        int curWorkMode = IlifeAli.getInstance().getWorkingDevice().getWork_status();
        if ((curWorkMode == MsgCodeUtils.STATUE_POINT || curWorkMode == MsgCodeUtils.STATUE_RECHARGE) && (productKey.equals(EnvConfigure.PRODUCT_KEY_X787) || productKey.equals(EnvConfigure.PRODUCT_KEY_X320))) {
            return false;
        } else {
            return true;
        }
    }

    public void findDone() {
        rl_find.setClickable(true);
        iv_find_device.setSelected(false);
        iv_find_device.clearAnimation();
    }

    public void goToMain() {
        //MainActivity的启动模式是SingleTask
        hideLoadingDialog();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EnvConfigure.KEY_IS_FAC_RESET, true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
