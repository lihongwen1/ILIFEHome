package com.ilife.home.robot.contract;

import com.ilife.home.robot.base.BaseView;

public interface ApWifiContract {
    interface Model {
    }

    interface View extends BaseView {
        /**
         * get ssid from  text view
         * @return
         */
        String getHomeSsid();

        /**
         * get password from edit text view
         * @return
         */
        String getPassWord();
        void bindSuccess();
        void bindFail(String message);
        void bindDevice();
        void sendProgress(int progress);
        void manualConnectHomeWifi();
    }

    interface Presenter {
        void connectToDevice();
    }
}
