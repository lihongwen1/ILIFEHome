package com.ilife.home.robot.contract;

import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.ilife.home.robot.base.BaseView;

import java.util.List;

public interface MainContract {
    interface Model {
    }

    interface View extends BaseView {
        void showButton();
        void showList();
        void setRefreshOver();
        void updateDeviceList(List<DeviceInfoBean> acUserDevices);
    }

    interface Presenter{
        void getDeviceList();
        boolean isDeviceOnLine(DeviceInfoBean acdvice);
    }
}
