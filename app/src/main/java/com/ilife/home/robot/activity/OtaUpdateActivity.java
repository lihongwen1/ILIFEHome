package com.ilife.home.robot.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.iot.aep.sdk._interface.OnAliOtaResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.aliyun.iot.aep.sdk.delegate.OTAUpdatingDelegate;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OtaUpdateActivity extends BackBaseActivity {
    private final String TAG = OtaUpdateActivity.class.getSimpleName();
    @BindView(R.id.tv_cur_version)
    TextView tv_current;
    @BindView(R.id.tv_target_version)
    TextView tv_target;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.ll_version)
    LinearLayout ll_version;
    @BindView(R.id.ll_updating)
    LinearLayout ll_updating;
    @BindView(R.id.tv_top_title)
    TextView title;
    @BindView(R.id.pb_upgrade)
    ProgressBar pb_upgrade;
    @BindView(R.id.tv_upgrade_progress)
    TextView tv_upgrade_progress;

    protected CompositeDisposable mDisposable;

    private OTAUpdatingDelegate mOtaDelegate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.ota_activity;
    }


    public void initView() {
        btn_update.setClickable(false);
        title.setText(R.string.setting_aty_ota_update);
    }

    public void initData() {
        mDisposable = new CompositeDisposable();
        mOtaDelegate = new OTAUpdatingDelegate(new OnAliOtaResponse() {
            @Override
            public void isnNewestVersion(String version) {
                updateBtnStatus("已是最新版本", false, false);
                updateOtaVer(version, version);
                MyLogger.e(TAG,"isnNewestVersion");
            }

            @Override
            public void hasNewInstallPkg(String curV, String newVer) {
                updateBtnStatus("下载安装包", true, true);
                btn_update.setTag(0);
                updateOtaVer(curV, newVer);
                MyLogger.e(TAG,"hasNewInstallPkg");
            }

            @Override
            public void haveEnsuredLoadingPkg() {
                pollingForDownloadingProgress();
                updateBtnStatus("下载中", false, true);
                updateUpdatingProgress(0);
                MyLogger.e(TAG,"haveEnsuredLoadingPkg");
            }

            @Override
            public void loadingProgress(String curV, String newVer, int progress) {
                updateBtnStatus("下载中", false, true);
                updateUpdatingProgress(progress);
                pollingForDownloadingProgress();
                MyLogger.e(TAG,"loadingProgress");
            }

            @Override
            public void loadingProgress(int progress) {
                updateUpdatingProgress(progress);
                MyLogger.e(TAG,"loadingProgress");
            }

            @Override
            public void loadingSuccess() {
                updateBtnStatus("下载成功", false, true);
                MyLogger.d(TAG,"loadingProgress");
            }

            @Override
            public void loadingFail(String curV, String newVer) {
                MyLogger.e(TAG,"loadingFail");
                ToastUtils.showToast("请确保机器扫地机处于充电模式。");
                updateBtnStatus("下载安装包失败", false, false);
                updateOtaVer(curV, newVer);
            }

            @Override
            public void hasOtaUpdating(String curV, String newVer) {
                updateBtnStatus("立刻升级", true, true);
                btn_update.setTag(1);
                updateOtaVer(curV, newVer);
                MyLogger.e(TAG,"hasOtaUpdating");
            }

            @Override
            public void haveEnteredOtaMode() {
                SpUtils.saveLong(OtaUpdateActivity.this, "upgrade_timeline", System.currentTimeMillis());
                updateUpdatingProgress(0);
                updateBtnStatus("升级中", false, true);
                pollingForUpdateProgress();
                MyLogger.e(TAG,"haveEnteredOtaMode");
            }

            @Override
            public void otaUpdatingProgress(String curV, String newVer, int progress) {
                long upgradeTimeline = SpUtils.getLong(OtaUpdateActivity.this, "upgrade_timeline");
                int second = (int) ((System.currentTimeMillis() - upgradeTimeline) / 1000);
                if (second >= 120) {
                    second = 120;
                }
                int realProgress = (int) (second * 90 / 120f);
                updateUpdatingProgress(realProgress);
                updateBtnStatus("升级中", false, true);
                pollingForUpdateProgress();
                MyLogger.e(TAG,"otaUpdatingProgress");
            }

            @Override
            public void otaUpdatingProgress(int progress) {
                long upgradeTimeline = SpUtils.getLong(OtaUpdateActivity.this, "upgrade_timeline");
                int second = (int) ((System.currentTimeMillis() - upgradeTimeline) / 1000);
                if (second >= 120) {
                    second = 120;
                }
                int realProgress = (int) (second * 90 / 120f);
                updateUpdatingProgress(realProgress);
                updateBtnStatus("升级中", false, true);
                MyLogger.e(TAG,"otaUpdatingProgress");
            }

            @Override
            public void otaUpdatingSuccess(String newVer) {
                updateBtnStatus("已是最新版本", false, false);
                ToastUtils.showToast("升级成功,请重启！！！");
                updateOtaVer(newVer, newVer);
                MyLogger.e(TAG,"otaUpdatingSuccess");
            }

            @Override
            public void otaUpdatingFail(String curVer, String newestVer) {
                updateBtnStatus("升级失败", false, false);
                ToastUtils.showToast("升级失败，请退出后重新升级！！！");
                updateOtaVer(curVer, newestVer);
                MyLogger.e(TAG,"otaUpdatingSuccess");
            }
        });
        mOtaDelegate.checkOTA();
    }

    private void updateOtaVer(String curV, String latestV) {
        tv_current.setText("当前版本: " + curV);
        tv_target.setText("最新版本: " + latestV);
        ll_version.setVisibility(View.VISIBLE);
        ll_updating.setVisibility(View.GONE);
    }

    private void updateUpdatingProgress(int progress) {
        tv_upgrade_progress.setText(String.valueOf(progress));
        pb_upgrade.setProgress(progress);
        ll_updating.setVisibility(View.VISIBLE);
        ll_version.setVisibility(View.GONE);
    }

    private void updateBtnStatus(String text, boolean isClickAble, boolean isEnable) {
        btn_update.setSelected(isClickAble);
        btn_update.setClickable(isClickAble);
        btn_update.setEnabled(isEnable);
        btn_update.setTextColor(isEnable ? getResources().getColor(R.color.white) : getResources().getColor(R.color.color_666666));
        btn_update.setText(text);
    }


    @OnClick(R.id.btn_update)
    public void onClick(View view) {
        switch ((int)view.getTag()) {
            case 0://服务器回复有可下载的OTA包，待用户确认
                mOtaDelegate.ensureLoadingInstallPkg();
                break;
            case 1://主机回复有更新
                mOtaDelegate.ensureInstallOTA();
                break;
        }
    }


    private void pollingForDownloadingProgress() {
        Disposable disposable = Observable.interval(3, TimeUnit.SECONDS).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(aLong -> mOtaDelegate.queryLoadingProgress(true));
        mDisposable.add(disposable);
    }


    /**
     * 轮序获取OTA进度
     */
    private void pollingForUpdateProgress() {
        Disposable disposable = Observable.interval(3, TimeUnit.SECONDS).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> mOtaDelegate.queryOtaUpdating(true));
        mDisposable.add(disposable);
    }


    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
