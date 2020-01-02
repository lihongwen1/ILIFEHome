package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.adapter.RobotListAdapter;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.contract.MainContract;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.presenter.MainPresenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.SpaceItemDecoration;
import com.ilife.home.robot.view.SlideRecyclerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.ilife.home.robot.R;

import java.util.List;

import butterknife.BindView;


public class MainActivity extends BaseActivity<MainPresenter> implements View.OnClickListener, MainContract.View {
    private final String TAG = MainActivity.class.getSimpleName();
    private List<DeviceInfoBean> mAcUserDevices;
    public static final String KEY_PHYCIALID = "KEY_PHYCIALID";
    public static final String KEY_SUBDOMAIN = "KEY_SUBDOMAIN";
    public static final String KEY_DEVICEID = "KEY_DEVICEID";
    public static final String KEY_DEVNAME = "KEY_DEVNAME";
    public static final String KEY_DEV_WHITE = "KEY_DEV_WHITE";
    public static final String KEY_OWNER = "KEY_OWNER";
    final int TAG_REFRESH_OVER = 0x01;
    Context context;
    @BindView(R.id.bt_add)
    Button bt_add;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_notice)
    TextView tv_notice;
    @BindView(R.id.image_personal)
    FrameLayout image_personal;
    @BindView(R.id.recyclerView)
    SlideRecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    BaseQuickAdapter adapter;
    @BindView(R.id.rootView)
    LinearLayout rootView;
    @BindView(R.id.layout_no_device)
    RelativeLayout layout_no_device;
    Rect rect;
    UniversalDialog unbindDialog;
    private  int selectPosition;
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case TAG_REFRESH_OVER:
                    if (refreshLayout != null) {
                        refreshLayout.finishRefresh();
                    }
                    break;
            }
            return true;
        }
    });

    @Override
    public void setRefreshOver() {
        handler.sendEmptyMessage(TAG_REFRESH_OVER);
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void attachPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void initView() {
        context = this;
        mAcUserDevices = IlifeAli.getInstance().getmAcUserDevices();
        rect = new Rect();
        bt_add.setOnClickListener(this);
        initAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 24)));
        image_personal.setOnClickListener(this);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setOnRefreshListener(refreshLayout -> mPresenter.getDeviceList());

    }

    private void initAdapter() {
        adapter = new RobotListAdapter(context, mAcUserDevices);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (recyclerView.closeMenu()) {
                return;
            }
            if (mAcUserDevices != null && position == mAcUserDevices.size()) {//Add button
                return;
            }
            if (mAcUserDevices != null && mAcUserDevices.size() > position && mPresenter.isDeviceOnLine(mAcUserDevices.get(position))) {
                IlifeAli.getInstance().setWorkingDevice(mAcUserDevices.get(position));
                Intent i = new Intent(context, MapActivity_X8_.class);
                startActivity(i);
            } else {
                showOfflineDialog();
            }
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            selectPosition=position;
            switch (view.getId()) {
                case R.id.item_delete:
                    if (mAcUserDevices.size() < selectPosition) {
                        return;
                    }
                    recyclerView.closeMenu();
                    if (unbindDialog == null) {
                        unbindDialog = new UniversalDialog();
                        unbindDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitle(Utils.getString(R.string.main_aty_unbind_device))
                                .setHintTip(Utils.getString(R.string.main_aty_unbind_device_tip)).setOnRightButtonClck(() -> {
                            showLoadingDialog();
                            IlifeAli.getInstance().unBindDevice(mAcUserDevices.get(selectPosition).getIotId(), new OnAliResponse<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    //解绑成功
                                    MyLogger.d(TAG, "解绑成功");
                                    hideLoadingDialog();
                                    mAcUserDevices.remove(selectPosition);
                                    if (mAcUserDevices.size() == 0) {
                                        showButton();
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailed(int code, String message) {
                                    MyLogger.d(TAG, "解绑失败：" + message);
                                    ToastUtils.showToast(context, getString(R.string.main_aty_unbind_fail));
                                    hideLoadingDialog();
                                }
                            });
                        });
                    }
                    unbindDialog.show(getSupportFragmentManager(), "unbind");
                    break;

                case R.id.iv_add_device:
                    //TODO 进入选择activity
                    Intent i = new Intent(context, SelectActivity_x.class);
                    context.startActivity(i);
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeAllActivityExclude();
        mPresenter.getDeviceList();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    public void updateDeviceList(List<DeviceInfoBean> acUserDevices) {
        if (acUserDevices.size() == 0) {
            showButton();
            IlifeAli.getInstance().getmAcUserDevices().clear();
        } else {
            IlifeAli.getInstance().setmAcUserDevices(acUserDevices);
            showList();
        }
    }


    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.bt_add:
                //TODO 进入选择activity
                i = new Intent(context, SelectActivity_x.class);
                startActivity(i);
                break;
            case R.id.image_personal:
                i = new Intent(context, PersonalActivity.class);
                startActivity(i);
                break;
        }
    }

    public void showButton() {
        refreshLayout.setVisibility(View.GONE);
        layout_no_device.setVisibility(View.VISIBLE);
    }

    public void showList() {
        layout_no_device.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void showOfflineDialog() {
        UniversalDialog offLineDialog = new UniversalDialog();
        offLineDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON).setTitle(Utils.getString(R.string.dev_frag_offline))
                .setHintTip(Utils.getString(R.string.dev_frag_try)).setMidText(Utils.getString(R.string.dialog_del_confirm))
                .show(getSupportFragmentManager(), "offline");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        IlifeAli.getInstance().destroy();
    }
}
