package com.ilife.home.robot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.RelativeGuide;
import com.badoo.mobile.util.WeakHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.huawei.android.hms.agent.common.UIUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.adapter.MapBottomSheetAdapter;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.contract.MapX9Contract;
import com.ilife.home.robot.fragment.DialogFragmentUtil;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.fragment.UseTipDialogFragment;
import com.ilife.home.robot.presenter.MapX9Presenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.CustomPopupWindow;
import com.ilife.home.robot.view.MapView;

import java.util.ArrayList;
import java.util.Arrays;
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
    @BindView(R.id.iv_map_start)
    ImageView iv_map_start;
    @BindView(R.id.tv_status)
    TextView tv_status;
    @BindView(R.id.tv_point_x9)
    TextView tv_point;
    @BindView(R.id.tv_along_x9)
    TextView tv_along;
    @BindView(R.id.image_ele)
    ImageView image_ele;//battery
    @BindView(R.id.tv_control_x9)
    TextView tv_control_x9;
    @BindView(R.id.fl_top_menu)
    FrameLayout fl_setting;
    Animation animation, animation_alpha;
    @BindView(R.id.layout_remote_control)
    View layout_remote_control;
    @BindView(R.id.v_map)
    MapView mMapView;
    @BindView(R.id.fl_control_x9)
    FrameLayout fl_control_x9;
    @BindView(R.id.image_center)
    ImageView image_center;
    PopupWindow errorPopup;
    @BindView(R.id.image_forward)
    ImageView image_forward;
    @BindView(R.id.image_left)
    ImageView image_left;
    @BindView(R.id.image_right)
    ImageView image_right;

    @BindView(R.id.iv_extension)
    ImageView iv_extension;
    @BindView(R.id.tv_bottom_recharge)
    TextView tv_bottom_recharge;

    @BindView(R.id.rv_bottom_sheet)
    RecyclerView rv_bottom_sheet;
    @BindView(R.id.map_bottom_sheet)
    LinearLayout ll_bottom_sheet;
    @BindView(R.id.fl_clean_times)
    FrameLayout fl_clean_times;
    @BindView(R.id.tv_cleaned_times)
    TextView tv_cleaned_times;
    @BindView(R.id.tv_setting_times)
    TextView tv_setting_times;
    public static final int USE_MODE_NORMAL = 1;
    public static final int USE_MODE_REMOTE_CONTROL = 2;
    protected int USE_MODE = USE_MODE_NORMAL;
    private WeakHandler weakHandler;
    private BottomSheetBehavior mBottomSheetBehavior;
    private UseTipDialogFragment useTipDialogFragment;
    private DialogFragmentUtil findRobotDialog;
    private DialogFragmentUtil tipDialog;

    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new MapX9Presenter();
        mPresenter.attachView(this);
        weakHandler = new WeakHandler(msg -> {
            if (!isDestroyed()) {
                switch (msg.what) {
                    case 1://更新清扫面积
                        tv_area.setText((String) msg.obj);
                        break;
                    case 2://清扫时长
                        tv_time.setText((String) msg.obj);
                        break;
                    case 3:
                        mMapView.drawMapX8((List<Coordinate>) msg.obj);
                        break;
                    case 4:
                        if (findRobotDialog != null && findRobotDialog.isAdded()) {
                            findRobotDialog.dismissAllowingStateLoss();
                        }
                        break;
                    case 5:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case 6:
                        if (tipDialog == null) {
                            DialogFragmentUtil.Builder builder = new DialogFragmentUtil.Builder();
                            tipDialog = builder.setLayoutId(R.layout.dialog_no_title).setCancelOutSide(false)
                                    .addClickLister(R.id.tv_dialog_ok, v -> {
                                        tipDialog.dismiss();
                                        mPresenter.setAppRemind();
                                    }).build();
                        }
                        if (!tipDialog.isAdded()) {
                            tipDialog.show(getSupportFragmentManager(), "app_remind");
                        }
                        break;
                }
            }

            return false;
        });
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
            mPresenter.getDevStatus();
        }
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
        setDevName();
        fl_setting.setVisibility(View.VISIBLE);
        mMapView.setmOT(MapView.OT.MAP);
        mMapView.setRobotSeriesX9(mPresenter.isX900Series());
        initBottomSheet();
        showGuide();
    }

    private void showGuide() {
        NewbieGuide.with(BaseMapActivity.this)
                .setLabel("guide")
                .addGuidePage(GuidePage.newInstance()
                        .setLayoutRes(R.layout.layer_guide_step, R.id.tv_guide_next)
                        .addHighLight(fl_setting, new RelativeGuide(R.layout.layer_map_setting, Gravity.BOTTOM, 0))
                        .addHighLight(findViewById(R.id.iv_map_start), HighLight.Shape.CIRCLE, new RelativeGuide(R.layout.layer_map_start, Gravity.TOP, 0))
                        .setEverywhereCancelable(false)
                )
                .addGuidePage(GuidePage.newInstance().setLayoutRes(R.layout.layer_guide_step_2, R.id.tv_guide_next2).setEverywhereCancelable(false))
                .alwaysShow(false).show();
    }


    /**
     * 初始化底部操作栏
     */
    private void initBottomSheet() {
        String[] functions = getResources().getStringArray(R.array.text_map_sheet_function);
        rv_bottom_sheet.setLayoutManager(new GridLayoutManager(this, 3));
        MapBottomSheetAdapter adapter = new MapBottomSheetAdapter(R.layout.item_map_function, Arrays.asList(functions));
        rv_bottom_sheet.setAdapter(adapter);
        ll_bottom_sheet = findViewById(R.id.map_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(ll_bottom_sheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                switch (state) {
                    case BottomSheetBehavior.STATE_HIDDEN://隐藏状态
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED://展开状态
                        iv_extension.setSelected(true);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED://折叠状态
                        iv_extension.setSelected(false);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING://拖拽状态
                        iv_extension.setSelected(!iv_extension.isSelected());
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            switch (position) {
                case 0:
                    if (mPresenter.isVirtualWallOpen()) {
                        mPresenter.setPropertiesWithParams(AliSkills.get().enterVirtualEditMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                        startActivity(new Intent(BaseMapActivity.this, VirtualWallActivity.class));
                        weakHandler.sendEmptyMessageDelayed(5, 300);
                    } else {
                        ToastUtils.showToast(UiUtil.getString(R.string.map_enter_virtual_tip));
                    }

                    break;
                case 1://选房清扫
                    if (mPresenter.getMDevicePropertyBean().getSelectedMapId() == 0) {
                        ToastUtils.showToast(UiUtil.getString(R.string.map_tip_no_map_yet));
                    } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP) {
                        startActivity(new Intent(BaseMapActivity.this, SelectRoomActivity.class));
                        weakHandler.sendEmptyMessageDelayed(5, 300);
                    } else {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.toast_ensure_robot_on_charging_dock));
                    }
                    break;
                case 2://划区清扫
                    if (mPresenter.getMDevicePropertyBean().getSelectedMapId() == 0) {
                        ToastUtils.showToast(UiUtil.getString(R.string.map_tip_no_map_yet));
                    } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP) {
                        startActivity(new Intent(BaseMapActivity.this, CleanAreaActivity.class));
                        weakHandler.sendEmptyMessageDelayed(5, 300);
                    } else {
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.toast_ensure_robot_on_charging_dock));
                    }
                    break;
                case 3://选择地图
                    int status = mPresenter.getCurStatus();
                    if (status == MsgCodeUtils.STATUE_CHARGING || status == MsgCodeUtils.STATUE_CHARGING_ || status == MsgCodeUtils.STATUE_SLEEPING ||
                            status == MsgCodeUtils.STATUE_CHARGING_ADAPTER_SLEEP || status == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP
                            || status == MsgCodeUtils.STATUE_WAIT) {
                        startActivity(new Intent(BaseMapActivity.this, SelectSaveMapActivity.class));
                        weakHandler.sendEmptyMessageDelayed(5, 300);
                    } else {
                        startActivity(new Intent(BaseMapActivity.this, SelectSaveMapActivity.class));
                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.toast_ensure_in_no_work_mode));
                    }
                    break;
                case 4://寻找机器人
                    if (findRobotDialog == null) {
                        DialogFragmentUtil.Builder builder = new DialogFragmentUtil.Builder();
                        findRobotDialog = builder.setCancelOutSide(false).setLayoutId(R.layout.toast_layout_finding_robot).build();
                    }
                    if (!findRobotDialog.isAdded()) {
                        findRobotDialog.show(getSupportFragmentManager(), "find_robot");
                        weakHandler.sendEmptyMessageDelayed(4, 300);
                        IlifeAli.getInstance().findDevice(null);
                    }
                    break;
            }
        });
    }

    @Override
    public void updateCleanArea(String value) {
        Message message = new Message();
        message.what = 1;
        message.obj = value;
        weakHandler.sendMessage(message);
    }

    @Override
    public void updateCleanTime(String value) {
        Message message = new Message();
        message.what = 2;
        message.obj = value;
        weakHandler.sendMessage(message);
    }

    /**
     * @param value 设备当前状态，empty代表刷新
     */
    @Override
    public void updateStatue(String value) {
        if (USE_MODE == USE_MODE_REMOTE_CONTROL) {
            tv_status.setTextColor(getResources().getColor(R.color.color_33));
        } else {
            tv_status.setTextColor(getResources().getColor(R.color.white));
        }
        if (!value.isEmpty()) {
            tv_status.setText(value);
        }
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
    public void drawVirtualWall(String vwStr) {
        mMapView.drawVirtualWall(vwStr);
    }

    @Override
    public void drawForbiddenArea(String data) {
        mMapView.drawForbiddenArea(data);
    }

    @Override
    public void drawCleanArea(String data) {
        mMapView.drawCleanArea(data);
    }

    @Override
    public void drawChargePort(int x, int y, boolean isDisplay) {
        mMapView.drawChargePort(x, y, isDisplay);
    }

    /**
     * 该功能暂时用不到
     *
     * @param isDisplay
     * @param cleanedTimes
     * @param settingCleanTimes
     */
    @Override
    public void updateCleanTimes(boolean isDisplay, int cleanedTimes, int settingCleanTimes) {
//        fl_clean_times.setVisibility(isDisplay ? View.VISIBLE : View.INVISIBLE);
//        if (isDisplay) {
//            tv_cleaned_times.setText(String.valueOf(cleanedTimes));
//            tv_setting_times.setText(String.valueOf(settingCleanTimes));
//        }
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

    @Override
    public void showTipDialog() {
        Message message = new Message();
        message.what = 6;
        weakHandler.removeMessages(6);
        weakHandler.sendMessageDelayed(message, 1000);
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
                setMapViewVisible(true);
                ll_bottom_sheet.setVisibility(View.VISIBLE);
                break;
            case USE_MODE_REMOTE_CONTROL:
                ll_bottom_sheet.setVisibility(View.GONE);
                fl_control_x9.setVisibility(View.VISIBLE);
                layout_remote_control.setVisibility(View.VISIBLE);
                updateOperationViewStatue(mPresenter.getCurStatus());
                setMapViewVisible(false);
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
    }


    @Override
    /**
     * 设置电池图标
     */
    public void setBatteryImage(int curStatus, int batteryNo) {
        MyLogger.d(TAG, "当前电量：    " + batteryNo);
        if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {//充电座模式&直冲模式
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.battery_ing_empty);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.battery_ing_1);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.battery_ing_2);   //两格
            } else {
                image_ele.setImageResource(R.drawable.battery_ing_full);   //满格
            }
        } else {
            if (batteryNo <= 6) {
                image_ele.setImageResource(R.drawable.battery_empty);   //红色
            } else if (batteryNo < 35) {
                image_ele.setImageResource(R.drawable.battery_1);   //一格
            } else if (batteryNo < 75) {
                image_ele.setImageResource(R.drawable.battery_2);   //两格
            } else {
                image_ele.setImageResource(R.drawable.battery_3);   //满格
            }
        }

    }

    @OnClick({R.id.image_center, R.id.ll_map_start, R.id.tv_control_x9, R.id.fl_top_menu, R.id.tv_along_x9,
            R.id.tv_point_x9, R.id.iv_control_close_x9, R.id.tv_bottom_recharge
            , R.id.iv_operation_help
    })
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.image_center:
                image_center.setSelected(image_center.isSelected());
            case R.id.ll_map_start: //done
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_POINT || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_ALONG) {//延边重点，直接进入待机模式
                    mPresenter.setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_TEMPORARY_POINT) {//临时重点进入规划
                    mPresenter.setPropertiesWithParams(AliSkills.get().enterPlanningMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE) {//回冲直接暂停
                    mPresenter.setPropertiesWithParams(AliSkills.get().enterPauseMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                } else if (mPresenter.isWork(mPresenter.getCurStatus())) {
                    if (mPresenter.isSupportPause()) {//支持暂停弹框
                        UniversalDialog universalDialog = new UniversalDialog();
                        universalDialog.setTitle(Utils.getString(R.string.choose_your_action)).setHintTip(Utils.getString(R.string.please_set_task))
                                .setLeftText(Utils.getString(R.string.finsh_cur_task)).setRightText(Utils.getString(R.string.pause_cur_task))
                                .setOnLeftButtonClck(() -> mPresenter.setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()))).setOnRightButtonClck(() ->
                                mPresenter.setPropertiesWithParams(AliSkills.get().enterPauseMode(IlifeAli.getInstance().getWorkingDevice().getIotId())))
                                .show(getSupportFragmentManager(), "choose_action");
                    } else {
                        mPresenter.setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                    }
                } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {//适配器充电模式不允许启动机器
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                } else {
                    if (mPresenter.isLowPowerWorker()) {
                        ToastUtils.showToast(getString(R.string.low_power));
                    }
                    mPresenter.setPropertiesWithParams(mPresenter.isRandomMode() ? AliSkills.get().enterRandomMode(IlifeAli.getInstance().getWorkingDevice().getIotId()) : AliSkills.get().enterPlanningMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
                }
                break;
            case R.id.tv_bottom_recharge:
                mPresenter.enterRechargeMode();
                break;
            case R.id.tv_control_x9://进入二级操作界面
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
                } else if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE) {
                    ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
                } else {
                    USE_MODE = USE_MODE_REMOTE_CONTROL;
                    updateStatue("");
                    showRemoteView();
                }
                break;
            case R.id.iv_control_close_x9://退出二级操作界面
                USE_MODE = USE_MODE_NORMAL;
                if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_WAIT || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PAUSE) {
                    mPresenter.refreshStatus();
                }
                updateStatue("");
                showBottomView();
                break;
            case R.id.fl_top_menu:
                Intent i = new Intent(this, SettingActivity.class);
                startActivity(i);
                break;
            case R.id.tv_along_x9:  //done
                if (mPresenter.isLowPowerWorker()) {
                    ToastUtils.showToast(getString(R.string.low_power));
                }
                mPresenter.enterAlongMode();
                break;
            case R.id.tv_point_x9:  //done
                if (mPresenter.isLowPowerWorker()) {
                    ToastUtils.showToast(getString(R.string.low_power));
                }
                mPresenter.enterPointMode();
                break;
            case R.id.iv_operation_help://操作提示弹框
                onOperationHelpClick();
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
    @OnTouch({R.id.image_right, R.id.image_left, R.id.image_forward})
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
                            if (!mPresenter.isWork(mPresenter.getCurStatus())) {
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnLeft(IlifeAli.getInstance().getIotId()));
                            } else {
                                ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
                            }
                            break;
                        case R.id.image_right:
                            if (!mPresenter.isWork(mPresenter.getCurStatus())) {
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnRight(IlifeAli.getInstance().getIotId()));
                            } else {
                                ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
                            }
                            break;
                        case R.id.image_forward:
                            if (!mPresenter.isWork(mPresenter.getCurStatus())) {
                                mPresenter.setPropertiesWithParams(AliSkills.get().turnForward(IlifeAli.getInstance().getIotId()));
                            } else {
                                ToastUtils.showToast(this, getString(R.string.map_aty_can_not_execute));
                            }
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
    }

    @Override
    public void updateAlong(boolean isAlong) {
    }

    @Override
    public void updatePoint(boolean isPoint) {
    }


    @Override
    public void showVirtualWallTip() {

    }

    @Override
    public void showVirtualEdit() {
    }

    @Override
    public void hideVirtualEdit() {

    }


    @Override
    public void updateOperationViewStatue(int surStatues) {
        tv_point.setSelected(surStatues == MsgCodeUtils.STATUE_POINT || surStatues == MsgCodeUtils.STATUE_TEMPORARY_POINT);
        tv_along.setSelected(surStatues == MsgCodeUtils.STATUE_ALONG);
        tv_bottom_recharge.setSelected(surStatues == MsgCodeUtils.STATUE_RECHARGE);
    }

    @Override
    public void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes) {
        mMapView.drawMapX9(roadList, historyRoadList, slamBytes);
    }

    @Override
    public void drawMapX8(ArrayList<Coordinate> dataList, ArrayList<Coordinate> slamList) {
        Message message = new Message();
        message.what = 3;
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.addAll(slamList);
        coordinates.addAll(dataList);
        message.obj = coordinates;
        weakHandler.removeMessages(3);//移除未执行完成的刷新操作
        weakHandler.sendMessage(message);
    }

    private void onOperationHelpClick() {
        if (useTipDialogFragment == null) {
            useTipDialogFragment = new UseTipDialogFragment();
        }
        if (!useTipDialogFragment.isAdded()) {
            useTipDialogFragment.show(getSupportFragmentManager(), "use_tip");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
