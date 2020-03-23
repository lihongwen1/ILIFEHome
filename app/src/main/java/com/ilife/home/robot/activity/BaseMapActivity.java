package com.ilife.home.robot.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.badoo.mobile.util.WeakHandler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.adapter.MapBottomSheetAdapter;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
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
import com.ilife.home.robot.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @BindView(R.id.tv_status)
    TextView tv_status;
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
    @BindView(R.id.fl_top_menu)
    FrameLayout fl_setting;
    Animation animation, animation_alpha;
    @BindView(R.id.layout_recharge)
    View layout_recharge;
    @BindView(R.id.layout_remote_control)
    View layout_remote_control;
    @BindView(R.id.tv_recharge_x9)
    TextView tv_recharge_x9;
    @BindView(R.id.v_map)
    MapView mMapView;
    @BindView(R.id.fl_control_x9)
    FrameLayout fl_control_x9;
    @BindView(R.id.fl_virtual_wall)
    FrameLayout fl_virtual_wall;
    @BindView(R.id.image_center)
    ImageView image_center;
    PopupWindow errorPopup;
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

    @BindView(R.id.tv_bottom_recharge)
    TextView tv_bottom_recharge;
    @BindView(R.id.iv_recharge_model)
    ImageView iv_recharge_model;
    @BindView(R.id.iv_recharge_stand)
    ImageView iv_recharge_stand;
    @BindView(R.id.layout_point)
    View layout_point;
    @BindView(R.id.iv_point_robot)
    ImageView iv_point_robot;
    @BindView(R.id.layout_along)
    View layout_along;
    @BindView(R.id.iv_along_robot)
    ImageView iv_along_robot;
    @BindView(R.id.rv_bottom_sheet)
    RecyclerView rv_bottom_sheet;
    @BindView(R.id.map_bottom_sheet)
    LinearLayout ll_bottom_sheet;
    public static final int USE_MODE_NORMAL = 1;
    public static final int USE_MODE_REMOTE_CONTROL = 2;
    protected int USE_MODE = USE_MODE_NORMAL;
    private WeakHandler weakHandler;
    private BottomSheetBehavior mBottomSheetBehavior;


    @Override
    public void attachPresenter() {
        super.attachPresenter();
        mPresenter = new MapX9Presenter();
        mPresenter.attachView(this);
        weakHandler = new WeakHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
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
                }

                return false;
            }
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
        setDevName();
        fl_setting.setVisibility(View.VISIBLE);
        mMapView.setRobotSeriesX9(mPresenter.isX900Series());
        initBottomSheet();

    }

    /**
     * 初始化底部操作栏
     */
    private void initBottomSheet() {
        String[] functions = new String[]{"虚拟墙/禁区", "选房清扫", "划区清扫", "选择地图", "定位机器"};
        rv_bottom_sheet.setLayoutManager(new GridLayoutManager(this, 3));
        MapBottomSheetAdapter adapter = new MapBottomSheetAdapter(R.layout.item_map_function, Arrays.asList(functions));
        rv_bottom_sheet.setAdapter(adapter);
        ll_bottom_sheet = findViewById(R.id.map_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(ll_bottom_sheet);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            switch (position) {
                case 0:
                    LiveEventBus.get(VirtualWallActivity.KEY_MAP_BUNDLE, MapDataBean.class)
                            .post(mPresenter.getMapDataBean());
                    startActivity(new Intent(BaseMapActivity.this,VirtualWallActivity.class));
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;

                case 4:
                    break;
            }
            ToastUtils.showToast(functions[position]);
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
        if (USE_MODE == USE_MODE_REMOTE_CONTROL || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_RECHARGE
                || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_POINT || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_TEMPORARY_POINT
                || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_ALONG) {
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
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.map_aty_set_wall))
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
                setMapViewVisible(true);
                ll_bottom_sheet.setVisibility(View.VISIBLE);
                break;
            case USE_MODE_REMOTE_CONTROL:
                fl_virtual_wall.setVisibility(View.GONE);
                ll_bottom_sheet.setVisibility(View.GONE);
                fl_control_x9.setVisibility(View.VISIBLE);
                layout_remote_control.setVisibility(View.VISIBLE);
                image_max.setSelected(mPresenter.isMaxMode());
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
        if (curStatus != MsgCodeUtils.STATUE_VIRTUAL_EDIT) {
//            mMapView.setMAP_MODE(MapView.MODE_NONE);
//            mMapView.undoAllOperation();
            hideVirtualEdit();
        }
        if (curStatus != MsgCodeUtils.STATUE_RECHARGE) {
            tv_bottom_recharge.setSelected(false);
            layout_recharge.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLeftTopCoordinate(int x, int y) {
        mMapView.setLeftTopCoordinate(x, y);
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
                image_ele.setImageResource(R.drawable.battery_ing_3);   //满格
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

    @OnClick({R.id.image_center, R.id.tv_start_x9, R.id.tv_control_x9, R.id.fl_top_menu, R.id.tv_recharge_x9, R.id.tv_along_x9,
            R.id.tv_point_x9, R.id.tv_close_virtual_x9, R.id.ib_virtual_wall_tip
            , R.id.tv_add_virtual_x9, R.id.tv_delete_virtual_x9, R.id.iv_control_close_x9, R.id.tv_bottom_recharge
            , R.id.tv_appointment_x9
    })
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.image_center:
                image_center.setSelected(image_center.isSelected());
            case R.id.tv_start_x9: //done
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
            case R.id.ib_virtual_wall_tip://显示电子墙提示
                showVirtualWallTip();
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
            case R.id.tv_recharge_x9://回冲
                mPresenter.enterRechargeMode();
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
            case R.id.tv_appointment_x9://预约
                Intent intent = new Intent(this, ClockingActivity.class);
                startActivity(intent);
                break;
            //TODO 实现电子墙编辑功能
//            case R.id.tv_virtual_wall_x9://电子墙编辑模式
//                if (mPresenter.isVirtualWallOpen() && (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PLANNING ||
//                        mPresenter.getCurStatus() == MsgCodeUtils.STATUE_PAUSE)) {
//                    showSetWallDialog();
//                } else {
//                    if (mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING || mPresenter.getCurStatus() == MsgCodeUtils.STATUE_CHARGING_) {
//                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
//                    } else {
//                        ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
//                    }
//                }
//                break;
            case R.id.tv_add_virtual_x9://增加电子墙模式
                if (mMapView.isInMode(MapView.MODE_ADD_VIRTUAL)) {
                    tv_add_virtual.setSelected(false);
                    mMapView.setMAP_MODE(MapView.MODE_NONE);
                } else {
//                    showAddWallDialog();
                    tv_add_virtual.setSelected(true);
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMAP_MODE(MapView.MODE_ADD_VIRTUAL);
                }
                break;
            case R.id.tv_delete_virtual_x9://删除电子墙模式
                if (mMapView.isInMode(MapView.MODE_DELETE_VIRTUAL)) {
                    tv_delete_virtual.setSelected(false);
                    mMapView.setMAP_MODE(MapView.MODE_NONE);
                } else {
//                    showDeleteWallDialog();
                    tv_delete_virtual.setSelected(true);
                    tv_add_virtual.setSelected(false);
                    mMapView.setMAP_MODE(MapView.MODE_DELETE_VIRTUAL);
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
        mMapView.setMAP_MODE(MapView.MODE_ADD_VIRTUAL);
        fl_virtual_wall.setVisibility(View.VISIBLE);
        fl_control_x9.setVisibility(View.GONE);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
        tv_point.setSelected(surStatues == MsgCodeUtils.STATUE_POINT || surStatues == MsgCodeUtils.STATUE_TEMPORARY_POINT);
        tv_along.setSelected(surStatues == MsgCodeUtils.STATUE_ALONG);
        tv_recharge_x9.setSelected(surStatues == MsgCodeUtils.STATUE_RECHARGE);
        layout_along.setVisibility(surStatues == MsgCodeUtils.STATUE_ALONG ? View.VISIBLE : View.GONE);
        layout_point.setVisibility(surStatues == MsgCodeUtils.STATUE_POINT || surStatues == MsgCodeUtils.STATUE_TEMPORARY_POINT ? View.VISIBLE : View.GONE);

    }

    @Override
    public void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes) {
        mMapView.drawMapX9(roadList, historyRoadList, slamBytes);
    }

    @Override
    public void drawMapX8(ArrayList<Coordinate> dataList) {
        Message message = new Message();
        message.what = 3;
        message.obj = dataList;
        weakHandler.removeMessages(3);//移除未执行完成的刷新操作
        weakHandler.sendMessage(message);
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
