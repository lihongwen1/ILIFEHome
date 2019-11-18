package com.ilife.home.robot.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.OTAInfoBean;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.MyLogger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OtaUpdateActivity extends BackBaseActivity {
    private final String TAG = OtaUpdateActivity.class.getSimpleName();
    @BindView(R.id.tv_cur_version)
    TextView tv_current;
    @BindView(R.id.tv_target_version)
    TextView tv_target;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.fl_version)
    LinearLayout fl_version;
    @BindView(R.id.ll_update)
    LinearLayout ll_update;
    @BindView(R.id.tv_top_title)
    TextView title;

    protected CompositeDisposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLoadingDialog();
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
        IlifeAli.getInstance().queryOtaVersion(new OnAliResponse<OTAInfoBean>() {
            @Override
            public void onSuccess(OTAInfoBean result) {
                hideLoadingDialog();
                updateOtaVer(result.getCurrentVer(), result.getTargetVer());
                updateBtnStatus(result.getUpdateState(), 0);
            }

            @Override
            public void onFailed(int code, String message) {
                hideLoadingDialog();
            }
        });
    }

    private void updateOtaVer(int curV, int latestV) {
        ;
        tv_current.setText("当前版本: " + Integer.toHexString(curV));
         tv_target.setText("最新版本: " + Integer.toHexString(latestV));
        fl_version.setVisibility(View.VISIBLE);
    }

    private void updateBtnStatus(int status, int progress) {
        btn_update.setTag(status);
        btn_update.setSelected(status == 1);
        btn_update.setClickable(status == 1);
        switch (status) {
            case 0://无更新
                btn_update.setText("无更新");
                break;
            case 1://有更新
                btn_update.setText("立即更新");
                break;
            case 2://更新中
                btn_update.setText("%"+progress);
                break;
            case 3:
                btn_update.setText("升级失败");
                if (mDisposable != null && !mDisposable.isDisposed()) {
                    mDisposable.dispose();
                }
                break;
            case 4://更新成功
                btn_update.setText("升级成功");
                if (mDisposable != null && !mDisposable.isDisposed()) {
                    mDisposable.dispose();
                }
                break;
        }
    }

    /**
     * 轮序获取OTA进度
     */
    private void pollingForUpdateProgress() {
        Disposable disposable = Observable.interval(3, TimeUnit.SECONDS).observeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        IlifeAli.getInstance().queryOtaVersion(new OnAliResponse<OTAInfoBean>() {
                            @Override
                            public void onSuccess(OTAInfoBean result) {
                                if (result.getUpdateState() != 1) {
                                    updateOtaVer(result.getCurrentVer(), result.getTargetVer());
                                    updateBtnStatus(result.getUpdateState(), result.getUpdateProgess());
                                }
                            }

                            @Override
                            public void onFailed(int code, String message) {

                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLogger.e(TAG, "轮询OTA进度异常。。。。。。。。。。");
                    }
                });
        mDisposable.add(disposable);
    }

    @OnClick(R.id.btn_update)
    public void onClick(View view) {
        if ((int) view.getTag() == 1) {
            IlifeAli.getInstance().setProperties(AliSkills.get().enterOTAMode(IlifeAli.getInstance().getIotId()), new OnAliSetPropertyResponse() {
                @Override
                public void onSuccess(String path, int tag, int functionCode, int responseCode) {
                    MyLogger.d(TAG, "进入OTA升级模式成功");
                    updateBtnStatus(2, 0);
                    pollingForUpdateProgress();
                }

                @Override
                public void onFailed(String path, int tag, int code, String message) {
                    MyLogger.e(TAG, "进入OTA升级模式失败" + message);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }
}
