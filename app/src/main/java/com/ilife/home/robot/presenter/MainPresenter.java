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
    private boolean isBackLogin = false;

    @Override
    public void attachView(MainContract.View view) {
        super.attachView(view);
        IlifeAli.getInstance().settTokenInvalidListener(aBoolean -> {
            MyLogger.d("ILIFE_ALI_", "用户登录会话失效。。。。");
            if (isBackLogin) {
                return;
            }
            //登录失效，弹框，重新登录
            isBackLogin = true;
            IlifeAli.getInstance().login(new OnAliResponse<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    //重新登录成功
                    isBackLogin = false;
                    MyLogger.d("ILIFE_ALI_", "重新登录成功。。。。");
                }

                @Override
                public void onFailed(int code, String message) {
                    //重新登录失败
                    isBackLogin = false;
                    MyLogger.d("ILIFE_ALI_", "重新登录失败。。。。");
                }
            });
        });
    }

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
