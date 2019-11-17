package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.contract.ApWifiContract;
import com.ilife.home.robot.presenter.ApWifiPresenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.tencent.bugly.crashreport.CrashReport;

import butterknife.BindView;

/**
 * 直接连wifi的ap配网模式
 */
public class ApWifiActivity extends BackBaseActivity<ApWifiPresenter> implements ApWifiContract.View {
    private final String TAG = ApWifiActivity.class.getSimpleName();
    public static final String EXTAR_DEVID = "EXTAR_DEVID";
    public static final String EXTAR_ROBOT_SSID = "EXTAR_ROBOT_SSID";
    Context context;
    @BindView(R.id.tv_bind_progress)
    TextView tv_bind_progress;
    @BindView(R.id.pb_bind_progress)
    ProgressBar pb_BindProgress;
    private String homeSsid;
    private String robot_ssid;
    private String homePassword;
   private WeakHandler progressHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        bindDevice();
    }

    @Override
    public void attachPresenter() {
        mPresenter = new ApWifiPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected boolean isChildPage() {
        return true;
    }

    public void initData() {
        context = this;
        robot_ssid = getIntent().getStringExtra(EXTAR_ROBOT_SSID);
        homeSsid = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_SSID, "unknown");
        homePassword = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_PASS, "unknown");
        progressHandler=new WeakHandler(msg -> {
            int progress=msg.what;
            updateBindProgress(progress);
            return false;
        });
    }



    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_wifi;
    }

    @Override
    public void initView() {
    }

    /**
     * 绑定设备
     */
    @Override
    public void bindDevice() {
        mPresenter.connectToDevice();
    }

    @Override
    public String getHomeSsid() {
        return homeSsid;
    }

    @Override
    public String getPassWord() {
        return homePassword;
    }

    @Override
    public void bindSuccess() {
        Intent i = new Intent(context, BindSucActivity.class);
        startActivity(i);
        removeActivity();
    }

    @Override
    public void bindFail(String message) {
        CrashReport.postCatchedException(new Exception(message));
        MyLogger.d(TAG, "配网失败：    " + message);
        startActivity(new Intent(this, BindFailActivity.class));
        removeActivity();
    }

    @Override
    public void sendProgress(int progress) {
        progressHandler.sendEmptyMessage(progress);
    }

    public void updateBindProgress(int progress) {
        MyLogger.d("ILIFE_ALI","onProgress 线程："+(Looper.getMainLooper() == Looper.myLooper())+"progress:   "+progress);
        if (tv_bind_progress == null) {
            return;
        }
        tv_bind_progress.setText(String.valueOf(progress));
        pb_BindProgress.setProgress(progress);
    }

    @Override
    protected void beforeFinish() {
        //取消配网，并结束页面
    }

}
