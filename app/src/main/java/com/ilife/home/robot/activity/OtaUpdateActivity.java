package com.ilife.home.robot.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.iot.aep.sdk._interface.OnAliOtaResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.aliyun.iot.aep.sdk.delegate.OTAUpdatingDelegate;
import com.ilife.home.robot.R;
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
    @BindView(R.id.ll_loading)
    LinearLayout ll_loading;

    @BindView(R.id.ll_updating)
    LinearLayout ll_updating;
    @BindView(R.id.tv_top_title)
    TextView title;
    @BindView(R.id.pb_upgrade)
    ProgressBar pb_upgrade;
    @BindView(R.id.tv_upgrade_progress)
    TextView tv_upgrade_progress;

    @BindView(R.id.ll_pre_ensure)
    LinearLayout ll_pre_ensure;
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
        title.setText(R.string.ota_update_title);
    }

    public void initData() {
        mDisposable = new CompositeDisposable();
        mOtaDelegate = new OTAUpdatingDelegate(new OnAliOtaResponse() {
            @Override
            public void isnNewestVersion(String version) {
                updateBtnStatus(getResources().getString(R.string.ota_already_latest_ver), false, false);
                updateOtaVer(version, version);
                ll_pre_ensure.setVisibility(View.GONE);
                MyLogger.e(TAG, "isnNewestVersion");
            }

            @Override
            public void hasNewInstallPkg(String curV, String newVer) {
                updateBtnStatus(getResources().getString(R.string.ota_down_load_pkg), true, true);
                btn_update.setTag(0);
                updateOtaVer(curV, newVer);
                ll_pre_ensure.setVisibility(View.VISIBLE);
                MyLogger.e(TAG, "hasNewInstallPkg");
            }

            @Override
            public void haveEnsuredLoadingPkg() {
                UniversalDialog tipDialog = new UniversalDialog();
                tipDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON).setTitle(getResources().getString(R.string.ota_wait_loading_tip))
                        .setHintTip("").setMidText(Utils.getString(R.string.dialog_del_confirm))
                        .show(getSupportFragmentManager(), "offline");
                pollingForDownloadingProgress();
                updateBtnStatus(getResources().getString(R.string.ota_loading_pkg), false, true);
                updateLoadingProgress(0);
                MyLogger.e(TAG, "haveEnsuredLoadingPkg");

            }

            @Override
            public void loadingProgress(String curV, String newVer, int progress) {
                updateBtnStatus(getResources().getString(R.string.ota_loading_pkg), false, true);
                updateLoadingProgress(progress);
                pollingForDownloadingProgress();
                ll_pre_ensure.setVisibility(View.VISIBLE);
                MyLogger.e(TAG, "loadingProgress");
            }

            @Override
            public void loadingProgress(int progress) {
                updateLoadingProgress(progress);
                MyLogger.e(TAG, "loadingProgress");
            }

            @Override
            public void loadingSuccess() {
                updateBtnStatus(getResources().getString(R.string.ota_loading_pkg_success), false, true);
                MyLogger.d(TAG, "loadingProgress");
            }

            @Override
            public void loadingFail(String curV, String newVer) {
                MyLogger.e(TAG, "loadingFail");
                ToastUtils.showToast(getResources().getString(R.string.ota_ensure_charging_mode));
                updateBtnStatus(getResources().getString(R.string.ota_down_load_pkg), true, true);
                btn_update.setTag(0);
                updateOtaVer(curV, newVer);
            }

            @Override
            public void hasOtaUpdating(String curV, String newVer) {
                updateBtnStatus(getResources().getString(R.string.ota_immediate_upgrade), true, true);
                btn_update.setTag(1);
                updateOtaVer(curV, newVer);
                ll_pre_ensure.setVisibility(View.VISIBLE);
                MyLogger.e(TAG, "hasOtaUpdating");
            }

            @Override
            public void haveEnteredOtaMode() {
                SpUtils.saveLong(OtaUpdateActivity.this, "upgrade_timeline", System.currentTimeMillis());
                updateUpgrading();
                updateBtnStatus(getResources().getString(R.string.ota_updating), false, true);
                pollingForUpdateProgress();
                MyLogger.e(TAG, "haveEnteredOtaMode");
            }

            @Override
            public void otaUpdatingProgress(String curV, String newVer, int progress) {
                long upgradeTimeline = SpUtils.getLong(OtaUpdateActivity.this, "upgrade_timeline");
                int second = (int) ((System.currentTimeMillis() - upgradeTimeline) / 1000);
                if (second >= 120) {
                    second = 120;
                }
                int realProgress = (int) (second * 90 / 120f);
                updateUpgrading();
                updateBtnStatus(getResources().getString(R.string.ota_updating), false, true);
                ll_pre_ensure.setVisibility(View.VISIBLE);
                pollingForUpdateProgress();
                MyLogger.e(TAG, "otaUpdatingProgress");
            }

            @Override
            public void otaUpdatingProgress(int progress) {
                long upgradeTimeline = SpUtils.getLong(OtaUpdateActivity.this, "upgrade_timeline");
                int second = (int) ((System.currentTimeMillis() - upgradeTimeline) / 1000);
                if (second >= 120) {
                    second = 120;
                }
                int realProgress = (int) (second * 90 / 120f);
                updateUpgrading();
                updateBtnStatus(getResources().getString(R.string.ota_updating), false, true);
                MyLogger.e(TAG, "otaUpdatingProgress");
            }

            @Override
            public void otaUpdatingSuccess(String newVer) {
                updateBtnStatus(getResources().getString(R.string.ota_already_latest_ver), false, false);
                ll_pre_ensure.setVisibility(View.GONE);
                updateOtaVer(newVer, newVer);
                if (mDisposable != null) {
                    mDisposable.dispose();
                }
                MyLogger.e(TAG, "otaUpdatingSuccess");
            }

            @Override
            public void otaUpdatingFail(String curVer, String newestVer) {
                updateBtnStatus(getResources().getString(R.string.ota_immediate_upgrade), true, true);
                btn_update.setTag(1);
                updateOtaVer(curVer, newestVer);
                ll_pre_ensure.setVisibility(View.VISIBLE);
                ToastUtils.showToast(getResources().getString(R.string.ota_upgrade_fail_tip));
                MyLogger.e(TAG, "otaUpdatingSuccess");
            }
        });
        mOtaDelegate.checkOTA();
    }

    private void updateOtaVer(String curV, String latestV) {
        tv_current.setText(getResources().getString(R.string.ota_cur_ver, curV));
        tv_target.setText(getResources().getString(R.string.ota_lates_ver, latestV));
        ll_version.setVisibility(View.VISIBLE);
        ll_loading.setVisibility(View.GONE);
        ll_updating.setVisibility(View.GONE);
    }

    private void updateLoadingProgress(int progress) {
        tv_upgrade_progress.setText(String.valueOf(progress));
        pb_upgrade.setProgress(progress);
        ll_loading.setVisibility(View.VISIBLE);
        ll_version.setVisibility(View.GONE);
        ll_updating.setVisibility(View.GONE);
    }

    private void updateUpgrading() {
        ll_loading.setVisibility(View.GONE);
        ll_version.setVisibility(View.GONE);
        ll_updating.setVisibility(View.VISIBLE);
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
        switch ((int) view.getTag()) {
            case 0://服务器回复有可下载的OTA包，待用户确认
                if (IlifeAli.getInstance().getWorkingDevice().getWork_status() == MsgCodeUtils.STATUE_CHARGING) {
                    mOtaDelegate.ensureLoadingInstallPkg();
                } else {
                    UniversalDialog uupgradeCheckDialog = new UniversalDialog();
                    uupgradeCheckDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON).setTitle(getResources().getString(R.string.ota_ensure_charging_mode))
                            .setHintTip("").setMidText(Utils.getString(R.string.dialog_del_confirm))
                            .show(getSupportFragmentManager(), "offline");
                }
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
        Disposable disposable = Observable.interval(5, TimeUnit.SECONDS).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> mOtaDelegate.queryOtaUpdating(true));
        mDisposable.add(disposable);
    }


    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (mOtaDelegate != null) {
            mOtaDelegate.setCancel(true);
        }
        super.onDestroy();
    }
}
