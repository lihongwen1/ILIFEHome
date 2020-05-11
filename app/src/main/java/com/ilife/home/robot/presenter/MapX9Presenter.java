package com.ilife.home.robot.presenter;

import android.text.TextUtils;
import android.util.Base64;

import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.bean.RealTimeMapBean;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.google.gson.Gson;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.activity.BaseMapActivity;
import com.ilife.home.robot.activity.SettingActivity;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.base.BasePresenter;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.contract.MapX9Contract;
import com.ilife.home.robot.model.MapX9Model;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;

import org.reactivestreams.Publisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

// TODO APP后台CPU消耗问题
//TODO 处理x800系列绘制地图不全部绘制，只绘制新增的点
//TODO 重点，延边，遥控模式下机器会清扫完成，此时可以清空地图数据(机器进入待机模式后，会从头开始清扫，此时可以清空地图数据)
//TODO make save map data seprate with real time map data
public class MapX9Presenter extends BasePresenter<MapX9Contract.View> implements MapX9Contract.Presenter {
    private final String TAG = "MapX9Presenter";
    private String robotType;
    private int curStatus, errorCode, batteryNo = -1, workTime = 0, cleanArea = 0;
    private ArrayList<Integer> realTimePoints, historyRoadList;
    private ExecutorService singleThread;
    private byte[] slamBytes, virtualContentBytes;
    /**
     * x800实时地图数据
     */
    private ArrayList<Coordinate> pointList;// map集合
    private ArrayList<Coordinate> slamPointList;// map集合
    private boolean isGainDevStatus, isGetHistory;
    private boolean haveMap = true;//标记机型是否有地图 V85机器没有地图，但是有地图清扫数据
    private boolean havMapData = true;//A7 无地图，也无地图清扫数据
    private int minX, maxX, minY, maxY;//数据的边界，X800系列机器会用到
    private CompositeDisposable mComDisposable;
    private int retryTimes = 1;//the retry times of gaining the device status
    private long mapStartTime;
    private MapX9Model mapX9Model;
    private RobotConfigBean.RobotBean rBean;//robot config
    private PropertyBean mDevicePropertyBean;//设置信息

    @Override
    public void attachView(MapX9Contract.View view) {
        super.attachView(view);
        mDevicePropertyBean = new PropertyBean();
        mapX9Model = new MapX9Model();
        mComDisposable = new CompositeDisposable();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        pointList = new ArrayList<>();
        slamPointList = new ArrayList<>();
        rBean = MyApplication.getInstance().readRobotConfig().getRobotBeanByPk(IlifeAli.getInstance().getWorkingDevice().getProductKey());
        robotType = rBean.getRobotType();
        adjustTime();
        singleThread = Executors.newSingleThreadExecutor();
        haveMap = rBean.isIsHaveMap();
        havMapData = rBean.isIsHaveMapData();
        if (rBean.isIsOnlyRandomMode()) {//V3x只有随机模式
            SpUtils.saveInt(MyApplication.getInstance(), IlifeAli.getInstance().getWorkingDevice().getProductKey() + SettingActivity.KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
        }
    }

    @Override
    public String getRobotType() {
        return robotType;
    }

    @Override
    public RobotConfigBean.RobotBean getRobotBean() {
        return rBean;
    }

    /**
     * TODO 按照实际机型返回是否是X900系列
     *
     * @return
     */
    @Override
    public boolean isX900Series() {
        return false;
    }

    @Override
    public boolean isLongPressControl() {
        return rBean.isIsLongPressControl();
    }


    @Override
    public void updateSlamX8(ArrayList<Coordinate> src, int offset) {
        if (src == null || src.size() < 2) {
            return;
        }
        Coordinate coordinate;
        if (minX == 0 && minY == 0 && maxX == 0 && maxY == 0) {
            coordinate = src.get(0);
            minX = coordinate.getX();
            minY = coordinate.getY();
            maxX = coordinate.getX();
            maxY = coordinate.getY();
            MyLogger.d(TAG, "data is  clear, and  need to reset all params");
        }
        int x, y;
        for (int i = 0; i < src.size(); i++) {
            coordinate = src.get(i);
            x = coordinate.getX();
            y = coordinate.getY();
            if (minX > x) {
                minX = x;
            }
            if (maxX < x) {
                maxX = x;
            }
            if (minY > y) {
                minY = y;
            }
            if (maxY < y) {
                maxY = y;
            }
        }
        mView.updateSlam(minX, maxX, minY, maxY);
    }

    private int pageNo = 1;// 900 800等机器分页请求历史地图

    /**
     * Repetition only happens after each success.
     */
    private void getHistoryDataX8() {
        mapX9Model.queryHistoryData(mapStartTime, cleaningDataX8 -> {
            MyLogger.d(TAG, "getHistoryDataX8-------------------------------success");
            isGetHistory = true;
            int tempArea = cleaningDataX8.getCleanArea();
            int tempTime = cleaningDataX8.getWorkTime();
            if (tempArea > cleanArea) {
                cleanArea = tempArea;
            }
            if (tempTime > workTime) {
                workTime = tempTime;
            }
            if (curStatus != MsgCodeUtils.STATUE_PLANNING) {//避免跑机的时候显示不符合实际的清扫时间和清扫区域
                mView.updateCleanTime(getTimeValue());
                mView.updateCleanArea(getAreaValue());
            }
            pointList.addAll(cleaningDataX8.getCoordinates());
            updateSlamX8(pointList, 0);
            if (haveMap && isViewAttached() && isDrawMap()) {
                mView.drawMapX8(pointList, slamPointList);
            }
        });
    }

    /**
     * x900获取历史地图
     * 考虑多线程同步问题
     */
    @Override
    public void getHistoryRoadX9() {
    }


    /**
     * //TODO 电子墙实时更新
     * 查询电子墙
     */
    public void queryVirtualWall() {

    }


    /**
     * x800绘制黄方格地图
     *
     * @param mapData
     */
    private void parseRealTimeMapX8(String mapData) {
        byte[] pointCoor = new byte[2];
        if (!TextUtils.isEmpty(mapData)) {
            byte[] bytes = Base64.decode(mapData, Base64.DEFAULT);
            Coordinate coordinate;
            boolean isFirstRoadPoint = true;//第一个路径点
            if (bytes != null && bytes.length > 0) {
                for (int i = 4; i < bytes.length; i += 5) {
                    int type = bytes[i];
                    if (type == 0) {
                        continue;
                    }
                    pointCoor[0] = bytes[i - 4];
                    pointCoor[1] = bytes[i - 3];
                    int x = DataUtils.bytesToInt(pointCoor, 0);
                    pointCoor[0] = bytes[i - 2];
                    pointCoor[1] = bytes[i - 1];
                    int y = DataUtils.bytesToInt(pointCoor, 0);
                    if (isFirstRoadPoint && type == 4) {
                        isFirstRoadPoint = false;
                        coordinate = new Coordinate(x, -y, -1);
                    } else {
                        coordinate = new Coordinate(x, -y, type);
                    }
                    pointList.add(coordinate);
                }
            }
        }
    }

    /**
     * x900绘制slam地图
     *
     * @param mapSrc
     */
    private void parseRealTimeMapX9(String mapSrc) {

    }

    @Override
    public void prepareToReloadData() {
        isGetHistory = false;
        workTime = 0;
        cleanArea = 0;
        historyRoadList.clear();
        realTimePoints.clear();//X900 series
        pointList.clear();//X800 series
//        slamPointList.clear();//底图
    }


    /**
     * 获取设备状态
     */
    @Override
    public void getDevStatus() {
        retryTimes = 1;//reset the number of retries for getting devices status
        Single.create((SingleOnSubscribe<PropertyBean>) emitter -> {
            MyLogger.d(TAG, "gain the device status");
            IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
                @Override
                public void onSuccess(PropertyBean result) {
                    emitter.onSuccess(result);
                }

                @Override
                public void onFailed(int code, String message) {
                    emitter.onError(new Exception(message));
                }
            });
        }).retryWhen(tf -> tf.flatMap((Function<Throwable, Publisher<?>>) throwable -> (Publisher<Boolean>) s -> {
            MyLogger.d(TAG, "GAIN DEVICE STATUS ERROR-----:" + throwable.getMessage());
            if (retryTimes > 5) {
                s.onError(throwable);
            } else {
                Disposable disposable = Observable.timer(1, TimeUnit.SECONDS).subscribe(aLong -> {
                    s.onNext(true);
                    retryTimes++;
                });
                mComDisposable.add(disposable);
            }
        })).subscribe(new SingleObserver<PropertyBean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(PropertyBean bean) {
                if (!isViewAttached()) {
                    return;
                }
                mDevicePropertyBean = bean;
                isGainDevStatus = true;
                registerPropReceiver();
                /**
                 * To avoid the failure of the first registration property listener, re-register it after the status is checked
                 */
                IlifeAli.getInstance().getErrorEvent(code -> {
                    MyLogger.d(TAG, "get the error event code ,and the error code is :" + code);
                    errorCode = code;
                    mView.showErrorPopup(errorCode);
                    if (errorCode != 0) {
                        setStatus(curStatus, batteryNo);
                    }
                });
                if (mDevicePropertyBean != null) {
                    mapStartTime = mDevicePropertyBean.getRealTimeMapTimeLine();
                    errorCode = 0;
                    batteryNo = mDevicePropertyBean.getBattery();
                    curStatus = mDevicePropertyBean.getWorkMode();
                    MyLogger.d(TAG, "gain the device status success and the status is :" + curStatus + "--------");
                    IlifeAli.getInstance().getWorkingDevice().setDeviceInfo(mDevicePropertyBean);
                    setStatus(curStatus, batteryNo);

                    /**
                     * 请求设备相关数据
                     */
                    if (isX900Series()) {
                        if (!isGetHistory) {
                            getHistoryRoadX9();
                        }
                        queryVirtualWall();
                    } else {//x800系列
                        if (curStatus == MsgCodeUtils.STATUE_CLEAN_ROOM || curStatus == MsgCodeUtils.STATUE_CLEAN_AREA || curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_PAUSE) {
                            if (!isGetHistory && havMapData) {
                                getHistoryDataX8();
                            }
                        }
                        LiveEventBus.get(EnvConfigure.KEY_AppRemind, Integer.class)
                                .post(mDevicePropertyBean.getAppRemind());
                        /**
                         * 处理保存地图
                         */
                        if (mDevicePropertyBean.isInitStatus() && mDevicePropertyBean.getSelectedMapId() != 0) {
                            doAboutSlam();
                        } else {
                            LiveEventBus.get(EnvConfigure.VirtualWallData, String.class).post(mDevicePropertyBean.getVirtualWall());
                            LiveEventBus.get(EnvConfigure.KEY_FORBIDDEN_AREA, String.class).post(mDevicePropertyBean.getForbiddenArea());
                            LiveEventBus.get(EnvConfigure.ChargerPiont, String.class).post(mDevicePropertyBean.getChargePort());
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!isViewAttached()) {
                    return;
                }
                /**
                 * 避免第一次注册属性监听失败，查询到状态后重新注册
                 */
                registerPropReceiver();
                isGainDevStatus = false;
                MyLogger.d(TAG, "To gain the device status fail ,and the reason is: " + e.getMessage());
            }
        });
    }


    @Override
    public boolean isMaxMode() {
        return IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().isMaxMode();
    }

    @Override
    public void reverseMaxMode() {
        if (isMaxMode()) {
            setPropertiesWithParams(AliSkills.get().cleaningNormal(IlifeAli.getInstance().getWorkingDevice().getIotId()));
        } else {
            setPropertiesWithParams(AliSkills.get().cleaningMax(IlifeAli.getInstance().getWorkingDevice().getIotId()));
        }
    }

    @Override
    public boolean isSupportPause() {
        return rBean.isIsSupportPause();
    }

    private void refreshMap() {
        if (slamBytes != null && realTimePoints != null && historyRoadList != null) {
            if (isX900Series()) {
                mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
            } else {
                mView.drawMapX8(pointList, slamPointList);
            }
        }
    }

    @Override
    public void refreshStatus() {
        setStatus(curStatus, batteryNo);
    }

    public void setStatus(int curStatus, int batteryNo) {
        MyLogger.d(TAG, "setStatus----------curStatus" + curStatus);
        if (!isViewAttached()) {
            return;
        }
        IlifeAli.getInstance().getWorkingDevice().setWork_status(curStatus);//保存工作状态
        mView.updateCleanArea(getAreaValue());
        mView.updateCleanTime(getTimeValue());
        if (batteryNo != -1) {//set battery icon
            mView.setBatteryImage(curStatus, batteryNo);
        }
        if (curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_CHARGING_ || curStatus == MsgCodeUtils.STATUE_CHARGING || (curStatus == MsgCodeUtils.STATUE_RECHARGE && !isX900Series())) {
            mView.setCurrentBottom(BaseMapActivity.USE_MODE_NORMAL);//进入一级控制界面
        }
        if (curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL || curStatus == MsgCodeUtils.STATUE_POINT
                || curStatus == MsgCodeUtils.STATUE_ALONG) {//进入二级控制界面
            mView.setCurrentBottom(BaseMapActivity.USE_MODE_REMOTE_CONTROL);
        }
        mView.clearAll(curStatus);//清空所有不常显示布局，以便根据status更新显示布局
        boolean isWork = isWork(curStatus);
        mView.updateStatue(DeviceUtils.getStatusStr(MyApplication.getInstance(), curStatus, errorCode));//待机，规划
        mView.updateStartStatue(isWork, isWork ? Utils.getString(R.string.map_aty_stop) : Utils.getString(R.string.map_aty_start));
        mView.updateOperationViewStatue(curStatus);
        mView.showBottomView();
//        if (haveMap && isViewAttached() && isDrawMap()) {
//            mView.setMapViewVisible(true);
//            refreshMap();
//        } else {
//            mView.cleanMapView();
//            mView.setMapViewVisible(false);
//        }
        if (curStatus != MsgCodeUtils.STATUE_CLEAN_AREA && curStatus != MsgCodeUtils.STATUE_CLEAN_ROOM && curStatus != MsgCodeUtils.STATUE_PAUSE) {//非划区，选房清扫清除数据
            mView.updateCleanTimes(false, 0, 0);
            mView.drawCleanArea("");
        }
        switch (curStatus) {
            case MsgCodeUtils.STATUE_RECHARGE://回充
                mView.updateRecharge(true);
                break;
            case MsgCodeUtils.STATUE_VIRTUAL_EDIT://电子墙编辑模式
                mView.showVirtualEdit();
                break;
            case MsgCodeUtils.STATUE_TEMPORARY_POINT://临时重点
            case MsgCodeUtils.STATUE_POINT://重点
                mView.updatePoint(true);
                break;
            case MsgCodeUtils.STATUE_ALONG://沿墙模式
                mView.updateAlong(true);
                break;
            case MsgCodeUtils.STATUE_RANDOM:
                break;
            case MsgCodeUtils.STATUE_WAIT:
                mView.setUnconditionalRecreate(true);
                break;
            case MsgCodeUtils.STATUE_CLEAN_AREA://更新划区次数
                if (mDevicePropertyBean != null && !TextUtils.isEmpty(mDevicePropertyBean.getCleanArea())) {
                    JSONObject json = JSON.parseObject(mDevicePropertyBean.getCleanArea());
                    int times = json.getIntValue("CleanLoop");
                    int cleanedTimes = times >> 4;
                    int settingTimes = times & 0x0f;
                    boolean enable = json.getIntValue("Enable") != 0;//0-无效/没有进行 1-开始 2-进行中
                    mView.updateCleanTimes(enable, cleanedTimes, settingTimes);
                }
                break;
            case MsgCodeUtils.STATUE_CLEAN_ROOM://更新选房次数
                if (mDevicePropertyBean != null && !TextUtils.isEmpty(mDevicePropertyBean.getCleanRoomData())) {
                    JSONObject json = JSON.parseObject(mDevicePropertyBean.getCleanRoomData());
                    int times = json.getIntValue("CleanLoop");
                    int cleanedTimes = times >> 4;
                    int settingTimes = times & 0x0f;
                    boolean enable = json.getIntValue("Enable") != 0;//0-无效/没有进行 1-开始 2-进行中
                    mView.updateCleanTimes(enable, cleanedTimes, settingTimes);
                }
                break;
        }

    }


    @Override
    public boolean isVirtualWallOpen() {
        return mDevicePropertyBean.getVirtualWallEn() == MsgCodeUtils.VIRTUAL_WALL_OPEN;
    }

    @Override
    public boolean isLowPowerWorker() {
        return batteryNo != -1 && batteryNo <= 6 && (curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT);
    }

    /**
     * 判断设备状态是否需要绘制地图
     * //TODO 根据状态判断是否需要绘制地图
     *
     * @return
     */
    @Override
    public boolean isDrawMap() {
        return true;/*(curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING
                || curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT
                || (curStatus == MsgCodeUtils.STATUE_RECHARGE && isX900Series())) && mView.isActivityInteraction();*/
    }


    @Override
    public boolean isWork(int curStatus) {
        return curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING ||
                curStatus == MsgCodeUtils.STATUE_CLEAN_AREA || curStatus == MsgCodeUtils.STATUE_CLEAN_ROOM || curStatus == MsgCodeUtils.STATUE_RECHARGE;
    }

    /**
     * 设置时钟
     */
    @Override
    public void adjustTime() {
        IlifeAli.getInstance().setTimeZone();
    }


    private boolean isNeedQueryVirtual(int curStatus, int lastStatus) {
        //退出电子墙编辑模式时查询电子墙
        return curStatus == MsgCodeUtils.STATUE_RECHARGE || curStatus == MsgCodeUtils.STATUE_REMOTE_CONTROL || curStatus == MsgCodeUtils.STATUE_ALONG || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING || lastStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT;
    }

    private String getAreaValue() {
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_CHARGING_ADAPTER_SLEEP ||
                curStatus == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP || curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return "——";
        } else {
            return DataUtils.formateArea(cleanArea / 100.0f);
        }
    }

    private String getTimeValue() {
        int min = (int) (workTime / 60f);
        if (curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_CHARGING_ADAPTER_SLEEP ||
                curStatus == MsgCodeUtils.STATUE_CHARGING_BASE_SLEEP || curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return "——";
        } else {
            return min + "min";
        }
    }


    /**
     * 注册/订阅实时地图数据监听
     */
    @Override
    public void registerPropReceiver() {
        IlifeAli.getInstance().registerDownStream();
        LiveEventBus.get(EnvConfigure.KEY_VirtualWallEN, Integer.class).observe((BaseActivity) mView, enable -> {
            if (mDevicePropertyBean != null) {
                mDevicePropertyBean.setVirtualWallEn(enable);
            }
        });

        LiveEventBus.get(EnvConfigure.KEY_WORK_MODE, Integer.class).observe((BaseActivity) mView, workMode -> {
            if (workMode == curStatus) {
                return;
            }
            MyLogger.e(TAG, "registerPropReceiver----onStatusChange" + workMode);
            curStatus = workMode;
            setStatus(curStatus, batteryNo);
        });

        LiveEventBus.get(EnvConfigure.KEY_REAL_TIME_MAP_START, Long.class).observe((BaseActivity) mView, new Observer<Long>() {
            @Override
            public void onChanged(Long startTime) {
                MyLogger.d(TAG, "registerPropReceiver----onRealTimeMapStart");
                //TODO 清空地图相关数据
                mapStartTime = startTime;
                mView.updateCleanTime(getTimeValue());
                mView.updateCleanArea(getAreaValue());
                prepareToReloadData();
                if (haveMap && pointList != null && isDrawMap()) {
                    mView.drawMapX8(pointList, slamPointList);
                }
            }
        });

        LiveEventBus.get(EnvConfigure.KEY_BATTERY_STATE, Integer.class).observe((BaseActivity) mView, new Observer<Integer>() {
            @Override
            public void onChanged(Integer batteryLevel) {
                batteryNo = batteryLevel;
                IlifeAli.getInstance().getWorkingDevice().setBattery(batteryNo);
                setStatus(curStatus, batteryNo);
            }
        });
        LiveEventBus.get(EnvConfigure.KEY_REALTIMEMAP, String.class).observe((BaseActivity) mView, mapBeanData -> {
            if (mapBeanData == null) {
                return;
            }
            MyLogger.d(TAG, "实时地图数据改变---");
            singleThread.execute(new ParseDataRunnable(mapBeanData));
        });
        LiveEventBus.get(EnvConfigure.KEY_ERRORCODE, Integer.class).observe((BaseActivity) mView, new Observer<Integer>() {
            @Override
            public void onChanged(Integer code) {
                MyLogger.d(TAG, "ERRORCODE-----" + code);
                errorCode = code;
                mView.showErrorPopup(errorCode);
                setStatus(curStatus, batteryNo);
            }
        });

        LiveEventBus.get(EnvConfigure.CleanAreaData, String.class).observe((BaseActivity) mView, cleanAreaData -> {
            if (mDevicePropertyBean != null) {
                //区域清扫
                MyLogger.d(TAG, "划区数据改变:  " + cleanAreaData);
                mDevicePropertyBean.setCleanArea(cleanAreaData);
            }
        });
        LiveEventBus.get(EnvConfigure.KEY_FORBIDDEN_AREA, String.class).observe((BaseActivity) mView, fbdArea -> {
            MyLogger.d(TAG, "禁区改变:  " + fbdArea);
            mView.drawForbiddenArea(fbdArea);
        });
        LiveEventBus.get(EnvConfigure.VirtualWallData, String.class).observe((BaseActivity) mView, virtualWall -> {
            MyLogger.d(TAG, "虚拟墙改变:  " + virtualWall);
            mView.drawVirtualWall(virtualWall);
        });
        LiveEventBus.get(EnvConfigure.CleanPartitionData, String.class).observe((BaseActivity) mView, cleanRoomData -> {
            MyLogger.d(TAG, "选房清扫改变");
            mDevicePropertyBean.setCleanRoomData(cleanRoomData);
        });
        LiveEventBus.get(EnvConfigure.KEY_AppRemind, Integer.class).observe((BaseActivity) mView, appRemind -> {
            MyLogger.d(TAG, "主机需要APP提示");
            if (appRemind == 1) {
                mView.showTipDialog();
            }

        });
        LiveEventBus.get(EnvConfigure.KEY_SELECT_MAP_ID, Integer.class).observe((BaseActivity) mView, selectMapId -> {
            MyLogger.d(TAG, "选择地图改变");
            if (mDevicePropertyBean!=null){
                mDevicePropertyBean.setSelectedMapId(selectMapId);
            }

        });

        LiveEventBus.get(EnvConfigure.ChargerPiont, String.class).observe((BaseActivity) mView, chargingPort -> {
            if (!TextUtils.isEmpty(chargingPort) && isViewAttached() && isDrawMap()) {
                MyLogger.d(TAG,"充电座位置改变");
                JSONObject jsonObject = JSONObject.parseObject(chargingPort);
                boolean isDisplay = jsonObject.getIntValue("DisplaySwitch") == 1;
                int xy = jsonObject.getIntValue("Piont");
                byte[] bytes = DataUtils.intToBytes4(xy);
                int x = DataUtils.bytesToInt(new byte[]{bytes[0], bytes[1]}, 0);
                int y = -DataUtils.bytesToInt(new byte[]{bytes[2], bytes[3]}, 0);
                mView.drawChargePort(x, y, isDisplay);
            }
        });


        /**
         *延迟handler来控制初始化状态改变的作用
         */
        LiveEventBus.get(EnvConfigure.KEY_INIT_STATUS, Integer.class).observe((BaseActivity) mView, initStatus -> {
            MyLogger.d(TAG, "初始化状态改变 init status：" + initStatus);
            if (initStatus == 0) {
                slamPointList.clear();
                mView.drawVirtualWall("");
                mView.drawForbiddenArea("");
                mView.drawChargePort(0, 0,false);
                mView.drawMapX8(pointList, slamPointList);
            } else {
                IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
                    @Override
                    public void onSuccess(PropertyBean result) {
                        mDevicePropertyBean = result;
                        doAboutSlam();
                    }

                    @Override
                    public void onFailed(int code, String message) {

                    }
                });
            }
        });
    }

    /**
     * 处理SLAM
     */
    private void doAboutSlam() {
        long selectId = mDevicePropertyBean.getSelectedMapId();
        IlifeAli.getInstance().getSelectMap(selectId, new OnAliResponse<List<HistoryRecordBean>>() {
            @Override
            public void onSuccess(List<HistoryRecordBean> result) {
                if (!isViewAttached()) {
                    return;
                }
                //只有一条记录才正确
                if (result == null || result.size() == 0) {
                    MyLogger.e(TAG, "保存地图数据错误！！！！！！！！！！");
                    return;
                }
                MapDataBean mapDataBean = DataUtils.parseSaveMapData(result.get(0).getMapDataArray());
                if (mapDataBean != null) {
                    minX = mapDataBean.getMinX();
                    maxX = mapDataBean.getMaxX();
                    minY = mapDataBean.getMinY();
                    maxY = mapDataBean.getMaxY();
                    mView.updateSlam(minX, maxX, minY, maxY);
                    slamPointList.clear();
                    slamPointList.addAll(mapDataBean.getCoordinates());
                    mView.drawMapX8(pointList, slamPointList);
                    /**
                     * 处理虚拟墙禁区
                     */
                    mView.drawVirtualWall(mDevicePropertyBean.getVirtualWall());
                    mView.drawForbiddenArea(mDevicePropertyBean.getForbiddenArea());
                    /**
                     * 处理充电座
                     */
                    LiveEventBus.get(EnvConfigure.ChargerPiont, String.class).post(mDevicePropertyBean.getChargePort());
                    /**
                     * 处理区域清扫数据
                     */
                    if (curStatus == MsgCodeUtils.STATUE_CLEAN_AREA) {
                        if (mDevicePropertyBean != null) {
                            String cleanAreaData = mDevicePropertyBean.getCleanArea();
                            JSONObject json = JSON.parseObject(cleanAreaData);
                            int times = json.getIntValue("CleanLoop");
                            int cleanedTimes = times >> 4;
                            int settingTimes = times & 0x0f;
                            boolean enable = json.getIntValue("Enable") != 0;//0-无效/没有进行 1-开始 2-进行中
                            mView.updateCleanTimes(enable, cleanedTimes, settingTimes);
                            String area = "";
                            if (enable) {
                                area = json.getString("AreaData");
                                if (area.equals("AAAAAAAAAAAAAAAAAAAAAA==")) {
                                    area = "";
                                }
                            }
                            mView.drawCleanArea(area);
                        }
                    }

                    /**
                     * 处理选房清扫数据
                     */
                    if (curStatus == MsgCodeUtils.STATUE_CLEAN_ROOM) {
                        String cleanRoomData = mDevicePropertyBean.getCleanRoomData();
                        if (!TextUtils.isEmpty(cleanRoomData)) {
                            JSONObject json = JSON.parseObject(cleanRoomData);
                            int times = json.getIntValue("CleanLoop");
                            int cleanedTimes = times >> 4;
                            int settingTimes = times & 0x0f;
                            boolean enable = json.getIntValue("Enable") != 0;//0-无效/没有进行 1-开始 2-进行中
                            mView.updateCleanTimes(enable, cleanedTimes, settingTimes);
                        }
                    }
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    /**
     * 解析X800系列地图数据子线程
     */
    public class ParseDataRunnable implements Runnable {
        String mapBeanData;

        public ParseDataRunnable(String mapBeanData) {
            this.mapBeanData = mapBeanData;
        }


        @Override
        public void run() {
            Gson gson = new Gson();
            RealTimeMapBean mapBean = gson.fromJson(mapBeanData, RealTimeMapBean.class);
            int offset = pointList.size();
            parseRealTimeMapX8(mapBean.getMapData());
            updateSlamX8(pointList, offset);
            cleanArea = mapBean.getCleanArea();
            workTime = mapBean.getCleanTime();
            mView.updateCleanTime(getTimeValue());
            mView.updateCleanArea(getAreaValue());
            if (haveMap && pointList != null && isDrawMap()) {
                mView.drawMapX8(pointList, slamPointList);
            }
        }
    }


    @Override
    public void setPropertiesWithParams(HashMap<String, Object> params) {
        IlifeAli.getInstance().setProperties(params, new OnAliSetPropertyResponse() {
            @Override
            public void onSuccess(String pathKey, int tag, int functionCode, int responseCode) {
                MyLogger.d(TAG, "setPropertiesWithParams-------path key is ：" + pathKey);
                switch (pathKey) {
                    case EnvConfigure.KEY_MAX_MODE://更新max mode
                        mView.updateMaxButton(tag == EnvConfigure.VALUE_SET_MAX);
                        IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().setMaxMode(tag == EnvConfigure.VALUE_SET_MAX);
                        break;
                    case EnvConfigure.KEY_CLEAN_DIRECTION://遥控器模式
                        break;
                    case EnvConfigure.KEY_WORK_MODE://下发工作模式
                        if (!isGainDevStatus) {
                            getDevStatus();
                            MyLogger.d(TAG, "gain the device status again");
                        }
                        break;
                }
            }

            @Override
            public void onFailed(String path, int tag, int code, String message) {
                // TODO 错误码处理
                ToastUtils.showToast(Utils.getString(R.string.error_toast_timeout));
            }
        });

    }


    @Override
    public void enterVirtualMode() {
//        setPropertiesWithParams(AliSkills.get().enterVirtualMode());
    }


    /**
     * @param vwStr SEND_VIR添加电子墙时为新增电子墙集合，EXIT_VIR 时，为null
     */
    public void sendVirtualWallData(String vwStr) {
        //TODO send virtual wall data to ali server
    }


    /**
     * 申请添加/删除电子墙
     */
    public void sendToDeviceWithOptionVirtualWall() {

    }

    @Override
    public boolean pointToAlong(boolean reverse) {
        if (reverse) {//延边切重点
            return rBean.isAlongToPoint();
        } else {//重点切延边
            return rBean.isPointToAlong();
        }
    }

    /**
     * X7系列支持规划时操作延边；
     * X8系列不支持规划时操作延边
     *
     * @return
     */
    @Override
    public boolean planningToAlong() {
        return rBean.isPlanningToAlong() && curStatus == MsgCodeUtils.STATUE_PLANNING;
    }

    @Override
    public void enterAlongMode() {
        if ((curStatus == MsgCodeUtils.STATUE_POINT && pointToAlong(false)) || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_RANDOM || planningToAlong()) {
            if (curStatus == MsgCodeUtils.STATUE_ALONG) {
                setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            } else {
                setPropertiesWithParams(AliSkills.get().enterAlongMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            }
        } else if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        }
    }

    @Override
    public void enterPointMode() {
        if ((curStatus == MsgCodeUtils.STATUE_ALONG && pointToAlong(true)) || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT ||
                curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_PLANNING || curStatus == MsgCodeUtils.STATUE_RANDOM) {
            if (curStatus == MsgCodeUtils.STATUE_POINT) {//重点-进入待机
                setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            } else if (curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT) {//临时重点-进入规划
                setPropertiesWithParams(AliSkills.get().enterPlanningMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            } else {
                setPropertiesWithParams(AliSkills.get().enterPointMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            }
        } else if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        }
    }


    /**
     * X7系列不限制延边，重点状态时操作回冲
     * X8系列延边，重点状态时，不可操作回冲
     */
    @Override
    public void enterRechargeMode() {
        if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else if ((curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG) && !rBean.isPointAlongToRecharge()) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_can_not_execute));
        } else {
            if (curStatus == MsgCodeUtils.STATUE_RECHARGE) {
                setPropertiesWithParams(AliSkills.get().enterWaitMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            } else {
                setPropertiesWithParams(AliSkills.get().enterRechargeMode(IlifeAli.getInstance().getWorkingDevice().getIotId()));
            }
        }
    }

    @Override
    public int getCurStatus() {
        return curStatus;
    }

    @Override
    public boolean isRandomMode() {
        return SpUtils.getInt(MyApplication.getInstance(), IlifeAli.getInstance().getWorkingDevice().getProductKey() + SettingActivity.KEY_MODE) == MsgCodeUtils.STATUE_RANDOM;
    }

    @Override
    public MapDataBean getMapDataBean() {
        MapDataBean dataBean = new MapDataBean(pointList, "", "", 0, 0
                , minX, minY, maxX, maxY);
        return dataBean;
    }



    @Override
    public PropertyBean getMDevicePropertyBean() {
        return mDevicePropertyBean;
    }

    @Override
    public void setAppRemind() {
        String appRemind1 = "{\"AppRemind\":0}";
        JSONObject json = JSONObject.parseObject(appRemind1);
        IlifeAli.getInstance().setProperties(json, aBoolean -> {
            MyLogger.d(TAG, "设置AppRemind为0成功");
        });
    }

    @Override
    public void detachView() {
        if (mComDisposable != null) {
            mComDisposable.dispose();
        }
        IlifeAli.getInstance().reset();
        super.detachView();
    }

}
