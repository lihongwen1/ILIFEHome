package com.ilife.home.robot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.contract.MapX9Contract;
import com.ilife.home.robot.presenter.MapX9Presenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.CustomPopupWindow;
import com.ilife.home.robot.R;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseMapActivity extends BackBaseActivity<MapX9Presenter> implements MapX9Contract.View {
    private long appPauseTime;//应用后台休眠时间
    final String TAG = BaseMapActivity.class.getSimpleName();
    public static final String NOT_FIRST_VIRTUAL_WALL = "virtual_wall";
    public static final int TAG_CONTROL = 0x01;
    public static final int TAG_NORMAL = 0x02;
    public static final int TAG_RECHAGRGE = 0x03;
    public static final int TAG_KEYPOINT = 0x04;
    public static final int TAG_ALONG = 0x05;
    public static final int TAG_LEFT = 0x06;
    public static final int TAG_RIGHT = 0x07;
    public static final int TAG_FORWARD = 0x08;
    public static final int TAG_RANDOM = 0x09;
    private CustomPopupWindow exitVirtualWallPop;
    private UniversalDialog virtualWallTipDialog;
    @BindView(R.id.ll_map_container)
    LinearLayout ll_map_container;
    @BindView(R.id.rl_top)
    View rl_top;
    @BindView(R.id.rl_status)
    View anchorView;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_area)
    TextView tv_area;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_start_x9)
    TextView tv_start;
    @BindView(R.id.tv_status)
    TextView tv_status;
    @BindView(R.id.tv_use_control)
    TextView tv_use_control;
    @BindView(R.id.tv_point_x9)
    TextView tv_point;
    @BindView(R.id.tv_along_x9)
    TextView tv_along;
    @BindView(R.id.tv_appointment_x9)
    TextView tv_appointment_x9;
    @BindView(R.id.image_ele)
    ImageView image_ele;//battery
    @BindView(R.id.tv_control_x9)
    TextView tv_control_x9;
    @BindView(R.id.tv_bottom_recharge_x9)
    TextView tv_bottom_recharge;
    @BindView(R.id.fl_top_menu)
    FrameLayout fl_setting;
    @BindView(R.id.image_animation)
    ImageView image_animation;
    Animation animation, animation_alpha;
    @BindView(R.id.layout_recharge)
    View layout_recharge;
    @BindView(R.id.layout_remote_control)
    View layout_remote_control;
    @BindView(R.id.tv_recharge_x9)
    TextView tv_recharge_x9;
    @BindView(R.id.v_map)
    MapView mMapView;
    @BindView(R.id.tv_virtual_wall_x9)
    TextView tv_wall;
    @BindView(R.id.fl_bottom_x9)
    FrameLayout fl_bottom_x9;
    @BindView(R.id.fl_control_x9)
    FrameLayout fl_control_x9;
    @BindView(R.id.fl_virtual_wall)
    FrameLayout fl_virtual_wall;
    @BindView(R.id.image_center)
    ImageView image_center;
    PopupWindow errorPopup;
    AnimationDrawable electricityDrawable;
    @BindView(R.id.tv_add_virtual_x9)
    TextView tv_add_virtual;
    @BindView(R.id.tv_delete_virtual_x9)
    TextView tv_delete_virtual;
    @BindView(R.id.image_control_back)
    ImageView image_max;
    @BindView(R.id.image_forward)
    ImageView image_forward;
    @BindView(R.id.image_left)
    ImageView image_left;
    @BindView(R.id.image_right)
    ImageView image_right;

    @BindView(R.id.tv_bottom_recharge_x8)
    TextView tv_bottom_recharge_x8;
    @BindView(R.id.iv_recharge_model)
    ImageView iv_recharge_model;
    @BindView(R.id.iv_recharge_stand)
    ImageView iv_recharge_stand;
    public static final int USE_MODE_NORMAL = 1;
    public static final int USE_MODE_REMOTE_CONTROL = 2;
    protected int USE_MODE = USE_MODE_NORMAL;
    @BindView(R.id.tv_test)
    TextView textView;

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new MapX9Presenter();
        mPresenter.attachView(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_map_x9;
    }


    @Override
    public boolean isActivityInteraction() {
        return isActivityInteraction;
    }

    @Override
    protected void onResume() {
        super.onResume();
        int sleepTime = (int) ((System.currentTimeMillis() - appPauseTime) / 1000f / 60f);
        appPauseTime = 0;
        if (sleepTime >= 3) {
            MyLogger.d(TAG, "prepare for first or reload history map data");
            mPresenter.prepareToReloadData();//重新获取历史map
        }
        mPresenter.getDevStatus();
        setDevName();
        updateMaxButton(mPresenter.isMaxMode());
        setDevName();
    }

    @Override
    protected void onPause() {
        super.onPause();
        appPauseTime = System.currentTimeMillis();
    }

    @Override
    public void initData() {
        super.initData();
        animation = AnimationUtils.loadAnimation(this, R.anim.anims_ni);
        animation.setInterpolator(new LinearInterpolator());
        animation_alpha = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
    }

    @Override
    public void setDevName() {
        String devName = IlifeAli.getInstance().getWorkingDevice().getNickName();
        if (devName != null && !devName.isEmpty()) {
            tv_title.setText(devName);
        } else {
            tv_title.setText(IlifeAli.getInstance().getWorkingDevice().getDeviceName());
        }
    }

    public void initView() {
        errorPopup = new PopupWindow();
        electricityDrawable = (AnimationDrawable) image_animation.getBackground();
        setDevName();
        fl_setting.setVisibility(View.VISIBLE);
        mMapView.setRobotSeriesX9(mPresenter.isX900Series());

    }

    @Override
    public void updateCleanArea(String value) {
        tv_area.setText(value);
    }

    @Override
    public void updateCleanTime(String value) {
        tv_time.setText(value);
    }

    @Override
    public void updateStatue(String value) {
        tv_status.setText(value);
    }

    @Override
    public void cleanMapView() {
        mMapView.clean();
    }

    @Override
    public void setMapViewVisible(boolean isViesible) {
        mMapView.setVisibility(isViesible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void updateSlam(int xMin, int xMax, int yMin, int yMax) {
        mMapView.updateSlam(xMin, xMax, yMin, yMax);
    }

    @Override
    public void drawVirtualWall(List<int[]> existPointList) {
        mMapView.drawVirtualWall(existPointList);
    }


    /**
     * 显示组件异常
     *
     * @param errorCode
     */
    @Override
    public void showErrorPopup(int errorCode) {
        boolean isShow = errorCode != 0;
        if (isShow) {
            if (errorPopup != null && !errorPopup.isShowing()) {
                View contentView = LayoutInflater.from(this).inflate(R.layout.layout_popup_error, null);
                errorPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                errorPopup.setContentView(contentView);
                initErrorPopup(errorCode, contentView);
                errorPopup.setOutsideTouchable(false);
                errorPopup.setFocusable(false);
                errorPopup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                errorPopup.setHeight((int) getResources().getDimension(R.dimen.dp_60));
                errorPopup.showAsDropDown(rl_top);
            }
        } else {
            if (errorPopup != null && errorPopup.isShowing()) {
                errorPopup.dismiss();
            }
        }


    }

    /**
     * 选择进入电子墙编辑模式
     * 显示电子墙操作UI
     */
    private void showSetWallDialog() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.map_aty_set_wall)).exchangeButtonColor()
                .setHintTip(Utils.getString(R.string.map_aty_will_stop)).setOnRightButtonClck(() ->
                mPresenter.enterVirtualMode()).show(getSupportFragmentManager(), "add_wall");
    }


    /**
     * 组件异常
     *
     * @param code
     * @param contentView
     */
    private void initErrorPopup(int code, View contentView) {
        ImageView image_delete = contentView.findViewById(R.id.image_delete);
        TextView tv_error = contentView.findViewById(R.id.tv_error);
        tv_error.setText(DeviceUtils.getErrorText(this, code, mPresenter.getRobotType()));
        image_delete.setOnClickListener(v -> {
            if (errorPopup != null) {
                errorPopup.dismiss();
            }
        });
    }


    @Override
    public void setCurrentBottom(int bottom) {
        this.USE_MODE = bottom;
    }

    /**
     * 显示开始等操作按钮,包含地图
     */
    public void showBottomView() {
        switch (USE_MODE) {
            case USE_MODE_NORMAL:
                layout_remote_control.setVisibility(View.GONE);
                fl_control_x9.setVisibility(View.GONE);
                fl_virtual_wall.setVisibility(View.GONE);
                fl_bottom_x9.setVisibility(View.VISIBLE);
                setMapViewVisible(true);
                break;
            case USE_MODE_REMOTE_CONTROL:
                fl_virtual_wall.setVisibility(View.GONE);
                fl_bottom_x9.setVisibility(View.GONE);
                fl_control_x9.setVisibility(View.VISIBLE);
                layout_remote_control.setVisibility(View.VISIBLE);
                image_max.setSelected(mPresenter.isMaxMode());
                updateOperationViewStatue(mPresenter.getCurStatus());
                setMapViewVisible(false);
                break;
        }
    }


    /**
     * 设置当前操作提示文字
     *
     * @param tag
     */
    @Override
    public void setTvUseStatus(int tag) {
        tv_use_control.setTextColor(getResources().getColor(R.color.white));
        switch (tag) {
            case TAG_NORMAL:
                tv_use_control.setText("");
                tv_use_control.setVisibility(View.GONE);
                break;
            case TAG_CONTROL:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_control));
                break;
            case TAG_LEFT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_left));
                break;
            case TAG_FORWARD:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_forward));
                break;
            case TAG_RIGHT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_right));
                break;
            case TAG_RECHAGRGE:
                tv_use_control.setTextColor(getResources().getColor(R.color.color_ff4d00));
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(R.string.map_aty_use_recharging_x9);
                break;
            case TAG_KEYPOINT:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(R.string.map_aty_key_pointing_x9);
                break;
            case TAG_ALONG:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.map_aty_use_along));
                break;
            case TAG_RANDOM:
                tv_use_control.setVisibility(View.VISIBLE);
                tv_use_control.setText(getString(R.string.start_random_cleaning));
                break;
        }
    }


    /**
     * 清空不常显示的布局,电子墙编辑模式，回冲动画，沿边动画，重点动画,操作提示文本（etc:重点清扫）
     *
     * @param curStatus
     */
    @Override
    public void clearAll(int curStatus) {
        if (curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
            mMapView.setMODE(MapView.MODE_NONE);
            mMapView.undoAllOperation();
            hideVirtualEdit();
        }
        if (curStatus != MsgCodeUtils.STATUE_RECHARGE) {
            tv_bottom_recharge.setSelected(false);
            tv_bottom_recharge_x8.setSelected(false);
            layout_recharge.setVisibility(View.GONE);
            if (electricityDrawable.isRunning()) {
                electricityDrawable.stop();
            }
        }
        if (curStatus != MsgCodeUtils.STATUE_REMOTE_CONTROL) {
            setTvUseStatus(TAG_NORMAL);
        }
    }


    @Override
    /**
     * 设置电池图标
     */
    public void setBatteryImage(int curStatus, int batteryNo) {
        if (curStatus == 0x09 || curStatus == 0x0b) {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1_ing);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2_ing);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery3_ing);   //两格
            } else {
                image_ele.setImageResource(R.drawable.map_aty_battery4_ing);   //满格
            }
        } else {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.map_aty_battery1);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.map_aty_battery2);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.map_aty_battery4);   //两格
            } else {
                image_ele.setImageResource(R.drawable.map_aty_battery3);   //满格
            }
        }

    }

    @OnClick({R.id.image_center, R.id.tv_start_x9, R.id.tv_control_x9, R.id.fl_top_menu, R.id.tv_recharge_x9, R.id.tv_along_x9,
            R.id.tv_point_x9, R.id.tv_virtual_wall_x9, R.id.tv_close_virtual_x9, R.id.ib_virtual_wall_tip
            , R.id.tv_add_virtual_x9, R.id.tv_delete_virtual_x9, R.id.iv_control_close_x9, R.id.tv_bottom_recharge_x9, R.id.tv_bottom_recharge_x8
            , R.id.tv_appointment_x9
    })
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.image_center:
                image_center.setSelected(image_center.isSelected());
            case R.id.tv_start_x9: //done
                if (mPresenter.isWork(mPresenter.getCurStatus())) {
                    UniversalDialog universalDialog = new UniversalDialog();
                    universalDialog.setTitle(Utils.getString(R.string.choose_your_action)).setHintTip(Utils.getString(R.string.please_set_task))
                            .setLeftText(Utils.getString(R.string.finsh_cur_task)).setRightText(Utils.getString(R.string.pause_cur_task)).exchangeButtonColor()
                            .setOnLeftButtonClck(() -> mPresenter.setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()))).setOnRightButtonClck(() ->
                            mPresenter.setPropertiesWithParams(AliSkills.get().enterPauseMode(IlifeAli.getInstance().getWorkingDevice().getIotId())))
                            .show(getSupportFragmentManager(), "choose_action");
                } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {//适配器充电模式不允许启动机器
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                } else {
                    if (mPresenter.isLowPowerWorker()) {
                        ToastUtils.showToast(getString(R.string.low_power));
                    }
                    mPresenter.setPropertiesWithParams(mPresenter.isRandomMode() ? AliSkills.get().enterRandomMode(IlifeAli.getInstance().getWorkingDevice().getIotId()) : AliSkills.get().enterPlanningMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                }
                break;
            case R.id.tv_bottom_recharge_x8:
            case R.id.tv_bottom_recharge_x9://会跳转到遥控界面
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_control_x9://显示沿边，遥控等操作UI
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                } else {
                    showRemoteView();
                }
                break;
            case R.id.ib_virtual_wall_tip://显示电子墙提示
                showVirtualWallTip();
                break;
            case R.id.iv_control_close_x9:
                USE_MODE = USE_MODE_NORMAL;
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_WAIT || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PAUSE) {
                    mPresenter.refreshStatus();
                } else {
                    mPresenter.setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                }
                break;
            case R.id.fl_top_menu:
                Intent i = new Intent(this, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.tv_recharge_x9://回冲
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_along_x9:  //done
                if (mPresenter.isLowPowerWorker()) {
                    ToastUtils.showToast(getString(R.string.low_power));
                } else {
                    mPresenter.enterAlongMode();
                }
                break;
            case R.id.tv_point_x9:  //done
                if (mPresenter.isLowPowerWorker()) {
                    ToastUtils.showToast(getString(R.string.low_power));
                } else {
                    mPresenter.enterPointMode();
                }
                break;
            case R.id.tv_appointment_x9://预约
                Intent intent = new Intent(this, ClockingActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_virtual_wall_x9://电子墙编辑模式
                if (mPresenter.isVirtualWallOpen() && (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING ||
                        mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PAUSE)) {
                    showSetWallDialog();
                } else {
                    if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                    } else {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
                    }
                }
                break;
            case R.id.tv_add_virtual_x9://增加电子墙模式
                if (mMapView.isInMode(MapView.MODE_ADD_VIRTUAL)) {
                    tv_add_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_NONE);
                } else {
//                    showAddWallDialog();
                    tv_add_virtual.setSelected(true);
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_ADD_VIRTUAL);
                }
                break;
            case R.id.tv_delete_virtual_x9://删除电子墙模式
                if (mMapView.isInMode(MapView.MODE_DELETE_VIRTUAL)) {
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_NONE);
                } else {
//                    showDeleteWallDialog();
                    tv_delete_virtual.setSelected(true);
                    tv_add_virtual.setSelected(false);
                    mMapView.setMODE(MapView.MODE_DELETE_VIRTUAL);
                }
                break;
            case R.id.tv_close_virtual_x9://弹出退出电子墙的的pop
                if (exitVirtualWallPop == null) {
                    CustomPopupWindow.Builder builder = new CustomPopupWindow.Builder(this);
                    exitVirtualWallPop = builder.enableOutsideTouchableDissmiss(false).setView(R.layout.pop_virtual_wall).
                            size(getResources().getDimensionPixelOffset(R.dimen.dp_315), 0).setBgDarkAlpha(0.6f).create();
                    exitVirtualWallPop.addViewOnclick(R.id.tv_cancel_virtual_x9, v1 -> {
                        mMapView.undoAllOperation();
                        /**
                         * 退出电子墙编辑模式，相当于撤销所有操作，电子墙数据没有变化，无需发送数据到设备端
                         */
                        mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs());
                        if (exitVirtualWallPop != null && exitVirtualWallPop.isShowing()) {
                            exitVirtualWallPop.dissmiss();
                        }
                    }).addViewOnclick(R.id.tv_ensure_virtual_x9, v12 -> {
                        if (exitVirtualWallPop != null && exitVirtualWallPop.isShowing()) {
                            exitVirtualWallPop.dissmiss();
                        }
                        mPresenter.sendVirtualWallData(mMapView.getVirtualWallPointfs());
                    }).addViewOnclick(R.id.cancel_virtual_pop, v13 -> {
                        if (exitVirtualWallPop != null && exitVirtualWallPop.isShowing()) {
                            exitVirtualWallPop.dissmiss();
                        }
                    });
                }
                if (!exitVirtualWallPop.isShowing()) {
                    exitVirtualWallPop.showAtLocation(findViewById(R.id.fl_map), Gravity.BOTTOM, 0, (int) getResources().getDimension(R.dimen.dp_10));
                }
                break;
        }
    }

    private Disposable remoteDisposable;

    /**
     * x785 x787支持长按持续前进旋转cleanMapView
     * x800，x900只支持点击前进旋转（用touch事件的up事件模拟）
     *
     * @param v
     * @param event
     */
    @OnTouch({R.id.image_right, R.id.image_left, R.id.image_control_back, R.id.image_forward})
    public void onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: //手指按下
                if (v.getId() != R.id.image_control_back) {
                    v.setSelected(true);
                }
                if (mPresenter.isLongPressControl()) {
                    remoteDisposable = Observable.interval(0, 3, TimeUnit.SECONDS).observeOn(Schedulers.io()).subscribe(aLong -> {
                        MyLogger.d(TAG, "下发方向移动指令");
                        switch (v.getId()) {
                            /* 遥控器方向键*/
                            case R.id.image_left:
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnLeft(IlifeAli.getInstance().getIotId()));
                                break;
                            case R.id.image_right:
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnRight(IlifeAli.getInstance().getIotId()));
                                break;
                            case R.id.image_forward:
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnForward(IlifeAli.getInstance().getIotId()));
                                break;
                        }
                    });

                }
                break;
            case MotionEvent.ACTION_MOVE: //手指移动（从手指按下到抬起 move多次执行）
                break;
            case MotionEvent.ACTION_UP: //手指抬起
                if (v.getId() != R.id.image_control_back) {
                    v.setSelected(false);
                }
                if (mPresenter.isLongPressControl()) {
                    if (remoteDisposable != null && !remoteDisposable.isDisposed()) {
                        remoteDisposable.dispose();
                    }
                    if (v.getId() == R.id.image_control_back) {//max吸力
                        mPresenter.reverseMaxMode();
                    } else {
                        mPresenter.setPropertiesWithParams(AliSkills.get().turnPause(IlifeAli.getInstance().getIotId()));
                    }
                } else {
                    switch (v.getId()) {
                        /* 遥控器方向键*/
                        case R.id.image_left:
                            mPresenter.setPropertiesWithParams(AliSkills.get().turnLeft(IlifeAli.getInstance().getIotId()));
                            break;
                        case R.id.image_right:
                            mPresenter.setPropertiesWithParams(AliSkills.get().turnRight(IlifeAli.getInstance().getIotId()));
                            break;
                        case R.id.image_forward:
                            mPresenter.setPropertiesWithParams(AliSkills.get().turnForward(IlifeAli.getInstance().getIotId()));
                            break;
                        case R.id.image_control_back://max吸力
                            mPresenter.reverseMaxMode();
                            break;
                    }
                }
                break;
        }
    }


    @Override
    public void updateMaxButton(boolean isMaXMode) {
        if (layout_remote_control.getVisibility() == View.VISIBLE) {
            image_max.setSelected(isMaXMode);
        }
    }

    @Override
    public void updateAlong(boolean isAlong) {
        layout_remote_control.setVisibility(View.GONE);
    }

    @Override
    public void updatePoint(boolean isPoint) {
        layout_remote_control.setVisibility(View.GONE);
    }


    @Override
    public void showVirtualWallTip() {
        if (virtualWallTipDialog == null) {
            virtualWallTipDialog = new UniversalDialog();
            virtualWallTipDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON).setTitle(Utils.getString(R.string.virtual_tip_title))
                    .setHintTip(Utils.getString(R.string.virtual_wall_use_tip), Gravity.LEFT, getResources().getColor(R.color.color_33));
        }
        virtualWallTipDialog.show(getSupportFragmentManager(), "virtual_wall_tip");
    }

    @Override
    public void showVirtualEdit() {
        if (!SpUtils.getBoolean(this, NOT_FIRST_VIRTUAL_WALL)) {
            showVirtualWallTip();
            SpUtils.saveBoolean(this, NOT_FIRST_VIRTUAL_WALL, true);
        }
        tv_add_virtual.setSelected(true);
        tv_delete_virtual.setSelected(false);
        mMapView.setMODE(MapView.MODE_ADD_VIRTUAL);
        fl_virtual_wall.setVisibility(View.VISIBLE);
        fl_control_x9.setVisibility(View.GONE);
        fl_bottom_x9.setVisibility(View.GONE);
    }

    @Override
    public void hideVirtualEdit() {
        fl_virtual_wall.setVisibility(View.GONE);
        tv_add_virtual.setSelected(false);
        tv_delete_virtual.setSelected(false);
        if (virtualWallTipDialog != null && virtualWallTipDialog.isAdded()) {
            virtualWallTipDialog.dismiss();
        }
        if (exitVirtualWallPop != null && exitVirtualWallPop.isShowing()) {
            exitVirtualWallPop.dissmiss();
        }
    }


    @Override
    public void updateOperationViewStatue(int surStatues) {
        tv_point.setSelected(surStatues == 0x05);
        tv_along.setSelected(surStatues == 0x04);
        tv_recharge_x9.setSelected(surStatues == 0x08);
    }

    @Override
    public void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes) {
        mMapView.drawMapX9(roadList, historyRoadList, slamBytes);
    }

    @Override
    public void drawMapX8(ArrayList<Integer> dataList) {
        mMapView.drawMapX8(dataList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        fl_bottom_x9.post(() -> mMapView.resetCenter(fl_bottom_x9.getHeight()));
    }

    @Override
    public void setUnconditionalRecreate(boolean recreate) {
        mMapView.setUnconditionalRecreate(recreate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setTestText(String text) {
        String s = textView.getText().toString();
        s += "\n";
        s += text;
        textView.setText(s);
    }
}
