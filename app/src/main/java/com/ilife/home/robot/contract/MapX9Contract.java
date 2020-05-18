package com.ilife.home.robot.contract;

import android.content.Context;

import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.base.BaseView;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.model.bean.VirtualWallBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MapX9Contract {
    interface Model {

    }

    interface View extends BaseView {
        void setTestText(String text);

        void setDevName();

        void showRemoteView();

        void updateSlam(int xMin, int xMax, int yMin, int yMax);


        void updateCleanTime(String value);

        void updateCleanArea(String value);

        void updateStatue(String value);

        void updateStartStatue(boolean isSelect, String value);

        void cleanMapView();


        void setBatteryImage(int curStatus, int batteryNo);

        void hideVirtualEdit();

        void clearAll(int curStatus);

        void showVirtualEdit();

        void setMapViewVisible(boolean isViesible);


        void showBottomView();

        void updateOperationViewStatue(int surStatu);

        void showErrorPopup(int errorCode);

        void drawVirtualWall(String vwStr);

        void drawChargePort(int x,int y,boolean isDisplay);
        void drawGates(List<VirtualWallBean> gates);
        void invalidMap();

        void updateAlong(boolean isAlong);

        void updatePoint(boolean isPoint);

        void updateRecharge(boolean isRecharge);

        void updateMaxButton(boolean isMaXMode);

        void setCurrentBottom(int bottom);

        void showVirtualWallTip();

        void drawMapX9(ArrayList<Integer> roadList, ArrayList<Integer> historyRoadList, byte[] slamBytes);

        void drawMapX8(ArrayList<Coordinate> dataList,ArrayList<Coordinate> slamList);

        boolean isActivityInteraction();

        void setUnconditionalRecreate(boolean recreate);
        void drawForbiddenArea(String data);
        void drawCleanArea(String data);
        void updateCleanTimes(boolean isDisplay,int cleanedTimes,int settingCleanTimes);
        void showTipDialog();
    }

    interface Presenter {
        MapDataBean getMapDataBean();
        void adjustTime();

        String getRobotType();

        void getHistoryRoadX9();

        void queryVirtualWall();


        void getDevStatus();

        void setStatus(int curStatus, int batteryNo);

        boolean isWork(int curStatus);

        void registerPropReceiver();

        void setPropertiesWithParams(HashMap<String, Object> parmas);

        void enterVirtualMode();

        void sendVirtualWallData(String vwStr);

        void sendToDeviceWithOptionVirtualWall();

        int getCurStatus();

        /**
         * 进入沿边模式
         */
        void enterAlongMode();

        /**
         * 进入重点模式
         */
        void enterPointMode();

        void enterRechargeMode();

        boolean isMaxMode();

        void reverseMaxMode();

        boolean isRandomMode();

        boolean isLowPowerWorker();

        /**
         * 是否绘制map
         *
         * @return
         */
        boolean isDrawMap();

        void updateSlamX8(ArrayList<Coordinate> src, int offset);

        boolean isX900Series();

        boolean pointToAlong(boolean reverse);

        boolean isLongPressControl();

        void prepareToReloadData();

        boolean isVirtualWallOpen();


        void refreshStatus();

        boolean isSupportPause();

        boolean planningToAlong();

        RobotConfigBean.RobotBean getRobotBean();
        void setAppRemind();
        PropertyBean getMDevicePropertyBean();
    }

}
