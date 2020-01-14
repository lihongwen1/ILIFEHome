package com.ilife.home.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.alink.business.devicecenter.api.add.AddDeviceBiz;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.contract.ApWifiContract;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.presenter.ApWifiPresenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.WifiUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

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
    private WifiStateReceiver mWifiStateReceiver;
    private boolean isConnectToAp;
    private CompositeDisposable mComposi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        bindDevice();
        registerWifiReceiver();
    }


    private void registerWifiReceiver() {
        mWifiStateReceiver = new WifiStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, filter);
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
        mComposi = new CompositeDisposable();
        robot_ssid = getIntent().getStringExtra(EXTAR_ROBOT_SSID);
        homeSsid = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_SSID, "unknown");
        homePassword = (String) SpUtils.get(this, ConnectHomeWifiActivity.EXTRA_PASS, "unknown");
        progressHandler = new WeakHandler(msg -> {
            int progress = msg.what;
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
    public void manualConnectHomeWifi() {

        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setTitle("连接家庭WiFi").setHintTip("请连接家庭wifi").setRightText("去设置")
                .setOnRightButtonClck(() -> {
                    Intent i = new Intent();
                    i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                    startActivity(i);
                });
        universalDialog.show(getSupportFragmentManager(),"apwifi");
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
        MyLogger.d("ILIFE_ALI", "onProgress 线程：" + (Looper.getMainLooper() == Looper.myLooper()) + "progress:   " + progress);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifiStateReceiver != null) {
            unregisterReceiver(mWifiStateReceiver);
        }
        if (mComposi != null && !mComposi.isDisposed()) {
            mComposi.dispose();
        }
    }

    /**
     * 监听网络变化
     */
    class WifiStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                        String ssid = AddDeviceBiz.getInstance().getCurrentSsid(context);
                        MyLogger.e(TAG, "onReceive ssid = " + ssid);
                        if (!TextUtils.isEmpty(ssid) && ssid.startsWith("adh") && !isConnectToAp) {
                            isConnectToAp = true;
                            Disposable disposable = Completable.timer(4, TimeUnit.SECONDS).subscribe(new Action() {
                                @Override
                                public void run() {
                                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                    String curSsid = WifiUtils.getSsid(context);
                                    if (!curSsid.equals(homeSsid)) {//如果连接的不是家庭wifi，则需要在连接一次
//                                      wifiManager.disconnect();//重启WIFI
                                        if (wifiManager != null) {
                                            WifiUtils.connectToAp_(wifiManager, homeSsid, homePassword, WifiUtils.getCipherType(homeSsid, wifiManager));
                                        }
                                    }
                                }
                            });
                            mComposi.add(disposable);
                        }
                    }
                }
            }
        }
    }

}
