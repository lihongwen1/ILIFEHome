package com.ilife.home.robot.presenter;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.base.BasePresenter;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.contract.MainContract;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {
    /**
     * 请求设备列表，刷新设备状态
     */
    @Override
    public void getDeviceList() {
        Single.create((SingleOnSubscribe<List<DeviceInfoBean>>) e -> IlifeAli.getInstance().listDeviceByAccount(new OnAliResponse<List<DeviceInfoBean>>() {
            @Override
            public void onSuccess(List<DeviceInfoBean> result) {
                e.onSuccess(result);
            }

            @Override
            public void onFailed(int code, String message) {
                e.onError(new Exception(message));
            }
        })).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<List<DeviceInfoBean>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(List<DeviceInfoBean> result) {
                MyLogger.d("MainPresenter", "设备数量：" + result.size());
                mView.updateDeviceList(result);
                mView.setRefreshOver();
            }

            @Override
            public void onError(Throwable e) {
                if (!isViewAttached()) {
                    return;
                }
                mView.setRefreshOver();
            }
        });

    }

    @Override
    public boolean isDeviceOnLine(DeviceInfoBean deviceInfoBean) {
        return deviceInfoBean.getStatus() == 1;
    }
}
