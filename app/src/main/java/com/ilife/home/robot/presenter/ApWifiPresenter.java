package com.ilife.home.robot.presenter;

import com.aliyun.iot.aep.sdk._interface.OnAliBindDeviceResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.delegate.BindDeviceDelagate;
import com.ilife.home.robot.base.BasePresenter;
import com.ilife.home.robot.contract.ApWifiContract;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 *
 */
public class ApWifiPresenter extends BasePresenter<ApWifiContract.View> implements ApWifiContract.Presenter {

    private BindDeviceDelagate bindDeviceDelagate;

    @Override
    public void connectToDevice() {
        Completable.create(e -> bindDeviceDelagate = IlifeAli.getInstance().bindDevice(mView.getHomeSsid(), mView.getPassWord(), new OnAliBindDeviceResponse<String>() {
            @Override
            public void onSuccess(String iotId) {
                IlifeAli.getInstance().setIotId(iotId);
                e.onComplete();
            }

            @Override
            public void onProgress(int progress) {
                if (isViewAttached()) {
                    mView.sendProgress(progress);
                }
            }

            @Override
            public void manualConnectHomeWifi() {
                mView.manualConnectHomeWifi();
            }

            @Override
            public void onFailed(int code, String message) {
                e.onError(new Exception(message));
            }
        })).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                if (isViewAttached()) {
                    mView.bindSuccess();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) {
                    mView.bindFail(e.getMessage());
                }
            }
        });
    }

    @Override
    public void detachView() {
        super.detachView();
        if (bindDeviceDelagate!=null){
            bindDeviceDelagate.cancel();
        }
    }
}
