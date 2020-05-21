package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.fragment.TextSelectorDialog;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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
    @BindView(R.id.image_max)
    ImageView image_max;
    @BindView(R.id.image_voice)
    ImageView image_voice;
    @BindView(R.id.image_product)
    ImageView image_product;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.tv_water)
    TextView tv_water;
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
    @BindView(R.id.rl_facReset)
    RelativeLayout rl_facReset;
    @BindView(R.id.rl_voice)
    RelativeLayout rl_voice;
    @BindView(R.id.rl_update)
    RelativeLayout rl_update;
    @BindView(R.id.iv_find_device)
    ImageView iv_find_device;

    @BindView(R.id.tv_brush_speed_number)
    TextView tv_brush_speed_number;//边刷速度

    @BindView(R.id.tv_max_number)
    TextView tv_max_number;//吸力强度

    @BindView(R.id.image_carpet)
    ImageView image_carpet;//地毯增压
    LayoutInflater inflater;
    Animation animation;
    private CompositeDisposable mDisposable;
    private RenameActivity renameFragment;
    private UniversalDialog resetDialog;
    private RobotConfigBean.RobotBean rBean;


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
        LiveEventBus.get(EnvConfigure.KEY_MAX_MODE, Boolean.class).observe(this, max -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            isMaxMode = max;
            setStatus(waterLevel, isMaxMode, voiceOpen);
        });

        LiveEventBus.get(EnvConfigure.KEY_SideBrushPower, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            tv_brush_speed_number.setText(String.valueOf(value));
        });
        LiveEventBus.get(EnvConfigure.KEY_CarpetControl, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            image_carpet.setSelected(value == 1);
        });
        LiveEventBus.get(EnvConfigure.KEY_FanPower, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            tv_max_number.setText(String.valueOf(value));
        });
        LiveEventBus.get(EnvConfigure.KEY_WATER_CONTROL, Integer.class).observe(this, value -> {
            MyLogger.d("LiveBus", "收到Live Bus 信息");
            waterLevel = value;
            setStatus(waterLevel, isMaxMode, voiceOpen);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceInfoBean infoBean = IlifeAli.getInstance().getWorkingDevice();
        name = infoBean.getNickName();
        if (name == null || name.isEmpty()) {
            devName = IlifeAli.getInstance().getWorkingDevice().getDeviceName();
            tv_name.setText(devName);
        } else {
            tv_name.setText(name);
        }
    }

    public void initData() {
        productKey = IlifeAli.getInstance().getWorkingDevice().getProductKey();
        rBean = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(productKey);
        mDisposable = new CompositeDisposable();
        DeviceInfoBean infoBean = IlifeAli.getInstance().getWorkingDevice();
        animation = AnimationUtils.loadAnimation(context, R.anim.anims);
        animation.setInterpolator(new LinearInterpolator());
        mode = SpUtils.getInt(context, productKey + KEY_MODE);
        waterLevel = infoBean.getDeviceInfo().getWaterLevel();
        isMaxMode = infoBean.getDeviceInfo().isMaxMode();
        voiceOpen = infoBean.getDeviceInfo().isDisturb();
        setMode(mode);
        tv_brush_speed_number.setText(String.valueOf(infoBean.getDeviceInfo().getBrushSpeed()));
        tv_max_number.setText(String.valueOf(infoBean.getDeviceInfo().getSuctionNumber()));
        image_carpet.setSelected(infoBean.getDeviceInfo().getCarpetControl() == 1);
        setStatus(waterLevel, isMaxMode, voiceOpen);
        int product = UiUtil.getDrawable(rBean.getFaceImg());
        /**
         * 所有功能均为VISIBLE，模式切换，固件升级默认GONE
         */
        rl_mode.setVisibility(rBean.isSettingMode() ? View.VISIBLE : View.GONE);
        rl_update.setVisibility(rBean.isSettingUpdate() ? View.VISIBLE : View.GONE);
        rl_record.setVisibility(rBean.isSettingRecord() ? View.VISIBLE : View.GONE);
        rl_voice.setVisibility(rBean.isSettingVoice() ? View.VISIBLE : View.GONE);
        tv_type.setText(BuildConfig.BRAND + " " + rBean.getSettingRobot());
        image_product.setImageResource(product);
    }

    public void setMode(int mode) {
        boolean isRandom = mode == MsgCodeUtils.STATUE_RANDOM;
        tv_mode.setText(isRandom ? getString(R.string.setting_aty_random_clean)
                : getString(R.string.setting_aty_nav_clean));
    }

    public void setStatus(int water, boolean isMaxMode, boolean isoVoiceOpen) {
        image_max.setSelected(isMaxMode);
        image_voice.setSelected(voiceOpen);
        if (rBean.getWaterLevelType() == 2) {//1轻柔 2标准 3强力 目前只有X787是该顺序
            switch (water) {
                case 1:
                    tv_water.setText(getString(R.string.setting_aty_soft));
                    break;
                case 2:
                    tv_water.setText(getString(R.string.setting_aty_standard));
                    break;
                case 3:
                    tv_water.setText(getString(R.string.setting_aty_strong));
                    break;
            }
        } else if (rBean.getWaterLevelType() == 3) {// X3 X4 X6 X9
            switch (water) {
                case 0:
                    tv_water.setText(getString(R.string.setting_aty_standard));
                    break;
                case 1:
                    tv_water.setText(getString(R.string.setting_aty_soft));
                    break;
                case 2:
                    tv_water.setText(getString(R.string.setting_aty_strong));
                    break;
            }
        } else {//x8
            switch (water) {
                case 0:
                    tv_water.setText(getString(R.string.setting_aty_soft));
                    break;
                case 1:
                    tv_water.setText(getString(R.string.setting_aty_standard));
                    break;
                case 2:
                    tv_water.setText(getString(R.string.setting_aty_strong));
                    break;
            }

        }
    }

    @OnClick({R.id.rl_robot_head, R.id.rl_water, R.id.rl_clock, R.id.rl_record, R.id.rl_consume, R.id.rl_mode, R.id.rl_find,
            R.id.rl_facReset, R.id.rl_voice, R.id.rl_update, R.id.rl_suction, R.id.rl_set_voice, R.id.rl_set_brush_speed, R.id.rl_set_max, R.id.rl_set_carpet})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_robot_head:
                Intent intent = new Intent(SettingActivity.this, RenameActivity.class);
                intent.putExtra(RenameActivity.KEY_RENAME_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.rl_clock:
                intent = new Intent(context, ClockingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_set_voice:
                intent = new Intent(context, VolumeSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_set_brush_speed:
                intent = new Intent(context, SettingBrushSpeedActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_set_max:
                intent = new Intent(context, SettingSuctionActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_set_carpet:
                String jsonStr = "{\"CarpetControl\":1}";
                JSONObject jo = JSONObject.parseObject(jsonStr);
                jo.put(EnvConfigure.KEY_CarpetControl, image_carpet.isSelected() ? 0 : 1);
                showLoadingDialog();
                IlifeAli.getInstance().setProperties(jo, aBoolean -> {
                    hideLoadingDialog();
                });
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
                TextSelectorDialog.Builder builder = new TextSelectorDialog.Builder();
                builder.setArray(UiUtil.getStringArray(R.array.text_work_mode)).setOnTextSelect((position, text) -> {
                    if (position == 0) {
                        SpUtils.saveInt(context, productKey + KEY_MODE, MsgCodeUtils.STATUE_PLANNING);
                        mode = MsgCodeUtils.STATUE_PLANNING;
                        setMode(mode);
                    } else {
                        SpUtils.saveInt(context, productKey + KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
                        mode = MsgCodeUtils.STATUE_RANDOM;
                        setMode(mode);
                    }
                });
                TextSelectorDialog workModeDialog = builder.build();
                workModeDialog.show(getSupportFragmentManager(), "select_work_mode");
                break;
            case R.id.rl_find:
                IlifeAli.getInstance().findDevice(this);
                rl_find.setClickable(false);
                iv_find_device.setSelected(true);
                iv_find_device.startAnimation(animation);
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
            case R.id.rl_water:
                TextSelectorDialog.Builder waterBuilder = new TextSelectorDialog.Builder();
                waterBuilder.setArray(UiUtil.getStringArray(R.array.water_level)).setOnTextSelect((position, text) -> {
                    switch (position) {
                        case 0://轻柔
                            if (rBean.getWaterLevelType() == 2) {
                                IlifeAli.getInstance().waterControl(1, this);
                            } else if (rBean.getWaterLevelType() == 3) {
                                IlifeAli.getInstance().waterControl(1, this);
                            } else {
                                IlifeAli.getInstance().waterControl(0, this);
                            }
                            break;
                        case 1://标准
                            if (rBean.getWaterLevelType() == 2) {
                                IlifeAli.getInstance().waterControl(2, this);
                            } else if (rBean.getWaterLevelType() == 3) {
                                IlifeAli.getInstance().waterControl(0, this);
                            } else {
                                IlifeAli.getInstance().waterControl(1, this);
                            }
                            break;
                        case 2://强力
                            if (rBean.getWaterLevelType() == 2) {
                                IlifeAli.getInstance().waterControl(3, this);
                            } else {
                                IlifeAli.getInstance().waterControl(2, this);
                            }
                            break;
                    }
                });
                TextSelectorDialog waterLevelDialog=waterBuilder.build();
                waterLevelDialog.show(getSupportFragmentManager(),"water_level");
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
        if (resetDialog == null) {
            resetDialog = new UniversalDialog();
            resetDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.setting_aty_confirm_reset))
                    .setHintTip(Utils.getString(R.string.setting_aty_reset_hint)).setOnRightButtonClck(() -> {
                showLoadingDialog();
                IlifeAli.getInstance().resetDeviceToFactory(this);
            });
        }
        if (!resetDialog.isAdded()) {
            resetDialog.show(getSupportFragmentManager(), "reset");
        }
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
                            Disposable dis = Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
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
        hideLoadingDialog();
        if (path.equals(EnvConfigure.PATH_SET_PROPERTIES) && tag == EnvConfigure.VALUE_FIND_ROBOT) {
            findDone();
        } else if (path.equals(EnvConfigure.PATH_SET_DEV_NICK_NAME)) {
            ToastUtils.showToast(context, getString(R.string.bind_aty_reName_fail));
        } else if (path.equals(EnvConfigure.PATH_SET_PROPERTIES) && tag == EnvConfigure.VALUE_FAC_RESET) {
            if (resetDialog != null) {
                resetDialog.dismiss();
            }
            ToastUtils.showErrorToast(this, code);
        } else {
            ToastUtils.showErrorToast(this, code);
        }
    }

    private boolean canOperateSuction() {
        return true;
//        int curWorkMode = IlifeAli.getInstance().getWorkingDevice().getWork_status();
//        if ((curWorkMode == MsgCodeUtils.STATUE_POINT || curWorkMode == MsgCodeUtils.STATUE_RECHARGE)) {
//            return false;
//        } else {
//            return true;
//        }
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
