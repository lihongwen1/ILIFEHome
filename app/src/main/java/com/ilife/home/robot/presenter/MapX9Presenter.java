package com.ilife.home.robot.presenter;

import android.text.TextUtils;
import android.util.Base64;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk._interface.OnDevicePoropertyResponse;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.bean.RealTimeMapBean;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.contant.MsgCodeUtils;
import com.google.gson.Gson;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.activity.BaseMapActivity;
import com.ilife.home.robot.activity.MainActivity;
import com.ilife.home.robot.activity.SettingActivity;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BasePresenter;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.contract.MapX9Contract;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

// TODO APP后台CPU消耗问题
//TODO 处理x800系列绘制地图不全部绘制，只绘制新增的点
//TODO 重点，延边，遥控模式下机器会清扫完成，此时可以清空地图数据(机器进入待机模式后，会从头开始清扫，此时可以清空地图数据)
public class MapX9Presenter extends BasePresenter<MapX9Contract.View> implements MapX9Contract.Presenter {
    private final String TAG = "MapX9Presenter";
    private String robotType;
    private int curStatus, errorCode, batteryNo = -1, workTime = 0, cleanArea = 0, virtualStatus;
    private ArrayList<Integer> realTimePoints, historyRoadList;
    private List<int[]> wallPointList = new ArrayList<>();
    private List<int[]> existPointList = new ArrayList<>();


    private byte[] slamBytes, virtualContentBytes;
    /**
     * x800实时地图数据
     */
    private ArrayList<Coordinate> pointList;// map集合
    private boolean isGainDevStatus, isGetHistory;
    private boolean haveMap = true;//标记机型是否有地图 V85机器没有地图，但是有地图清扫数据
    private boolean havMapData = true;//A7 无地图，也无地图清扫数据
    private int minX, maxX, minY, maxY;//数据的边界，X800系列机器会用到
    private CompositeDisposable mComDisposable;
    private int retryTimes = 1;//the retry times of gaining the device status
    private long mapStartTime;

    @Override
    public void attachView(MapX9Contract.View view) {
        super.attachView(view);
        mComDisposable = new CompositeDisposable();
        realTimePoints = new ArrayList<>();
        historyRoadList = new ArrayList<>();
        pointList = new ArrayList<>();
        robotType = DeviceUtils.getRobotType(IlifeAli.getInstance().getWorkingDevice().getProductKey());
        adjustTime();
        if (robotType.equals(Constants.V3x) || robotType.equals(Constants.V5x) || robotType.equals(Constants.V85) || robotType.equals(Constants.A7)) {
            haveMap = false;
        }
        if (robotType.equals(Constants.A7) || robotType.equals(Constants.V5x) || robotType.equals(Constants.V3x)) {
            havMapData = false;
        }
        if (robotType.equals(Constants.V5x) || robotType.equals(Constants.V3x)) {//V5x只有随机模式
            SpUtils.saveInt(MyApplication.getInstance(), IlifeAli.getInstance().getWorkingDevice().getProductKey() + SettingActivity.KEY_MODE, MsgCodeUtils.STATUE_RANDOM);
        }
    }

    @Override
    public String getRobotType() {
        return robotType;
    }


    @Override
    public boolean isX900Series() {
        return robotType.equals(Constants.X900) || robotType.equals(Constants.X910);
    }

    @Override
    public boolean isLongPressControl() {
        return getRobotType().equals(Constants.V85) || getRobotType().equals(Constants.X785) || getRobotType().equals(Constants.X787) || getRobotType().equals(Constants.A7);
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
            offset = 0;
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
        Disposable d = Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            if (!isViewAttached()) {//page has been destroyed
                emitter.onError(new Exception("you need to retry after a while as the view is not attach"));
            } else {
                IlifeAli.getInstance().getCleaningHistory(mapStartTime, System.currentTimeMillis(), new OnAliResponse<List<RealTimeMapBean>>() {
                    @Override
                    public void onSuccess(List<RealTimeMapBean> result) {
                        MyLogger.d(TAG, "getHistoryDataX8-------------------------------success");
                        for (RealTimeMapBean bean : result) {
                            parseRealTimeMapX8(bean.getMapData());
                            if (workTime == 0) {
                                workTime = bean.getCleanTime();
                            }
                            if (cleanArea == 0) {
                                cleanArea = bean.getCleanArea();
                            }
                        }
                        updateSlamX8(pointList, 0);
                        isGetHistory = true;
                        mView.updateCleanTime(getTimeValue());
                        mView.updateCleanArea(getAreaValue());
                        if (haveMap && isViewAttached() && isDrawMap()) {
                            mView.drawMapX8(pointList);
                        }
                        emitter.onSuccess(true);
                    }

                    @Override
                    public void onFailed(int code, String message) {
                        emitter.onError(new Exception(message));
                    }
                });
            }
        }).retry(2).subscribe(aBoolean -> isGetHistory = true, throwable -> {
            isGetHistory = false;
            MyLogger.e(TAG, "Failed to get history map data,and you need to retry sometime");
        });
        mComDisposable.add(d);
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
            int index;
            if (bytes != null && bytes.length > 0) {
                for (int i = 4; i < bytes.length; i += 5) {
                    int type = bytes[i];
                    if (type == 0) {
                        continue;
                    }
                    pointCoor[0] = bytes[i - 4];
                    pointCoor[1] = bytes[i - 3];
                    int x = DataUtils.bytesToInt(pointCoor, 0) + 750;
                    pointCoor[0] = bytes[i - 2];
                    pointCoor[1] = bytes[i - 1];
                    int y = DataUtils.bytesToInt(pointCoor, 0) + 750;
                    coordinate = new Coordinate(1500 - x, y, type);
                    index = pointList.indexOf(coordinate);
                    if (index == -1) {
                        MyLogger.d("清扫数据", type + "-(" + (x - 750) + "," + (y - 750) + ")");
                        pointList.add(coordinate);
                    } else {
                        MyLogger.d("清扫数据-重复点", type + "-(" + (x - 750) + "," + (y - 750) + ")");
                        pointList.remove(index);
                        pointList.add(coordinate);
//                        pointList.get(index).setType(type);
                    }
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
            public void onSuccess(PropertyBean propertyBean) {
                if (!isViewAttached()) {
                    return;
                }
                isGainDevStatus = true;

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
                registerPropReceiver();
                if (propertyBean != null) {
                    mapStartTime = propertyBean.getRealTimeMapTimeLine();
                    errorCode = 0;
                    batteryNo = propertyBean.getBattery();
                    curStatus = propertyBean.getWorkMode();
                    MyLogger.d(TAG, "gain the device status success and the status is :" + curStatus + "--------");
                    IlifeAli.getInstance().getWorkingDevice().setDeviceInfo(propertyBean);
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
                        if (!isGetHistory && havMapData) {
                            getHistoryDataX8();
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
        return robotType.equals(Constants.X800) || robotType.equals(Constants.X800W);
    }

    private void refreshMap() {
        if (slamBytes != null && realTimePoints != null && historyRoadList != null && existPointList != null) {
            if (isX900Series()) {
                mView.drawMapX9(realTimePoints, historyRoadList, slamBytes);
            } else {
                mView.drawMapX8(pointList);
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
        if (haveMap && isViewAttached() && isDrawMap()) {
//            mView.setMapViewVisible(true);
            refreshMap();
        } else {
            mView.cleanMapView();
//            mView.setMapViewVisible(false);
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
        }

    }


    @Override
    public boolean isVirtualWallOpen() {
        return virtualStatus == MsgCodeUtils.VIRTUAL_WALL_OPEN;
    }

    @Override
    public boolean isLowPowerWorker() {
        return batteryNo != -1 && batteryNo <= 6 && (curStatus == MsgCodeUtils.STATUE_SLEEPING || curStatus == MsgCodeUtils.STATUE_WAIT);
    }

    /**
     * 判断设备状态是否需要绘制地图
     *
     * @return
     */
    @Override
    public boolean isDrawMap() {
        return (curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING
                || curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_VIRTUAL_EDIT
                || (curStatus == MsgCodeUtils.STATUE_RECHARGE && isX900Series())) && mView.isActivityInteraction();
    }


    @Override
    public boolean isWork(int curStatus) {
        return curStatus == MsgCodeUtils.STATUE_RANDOM || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_TEMPORARY_POINT || curStatus == MsgCodeUtils.STATUE_PLANNING ||
                curStatus == MsgCodeUtils.STATUE_RECHARGE;
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
        BigDecimal bg = new BigDecimal(cleanArea / 100.0f);
        double area = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            String areaStr = area + "㎡";
            if (areaStr.equals("0.0㎡")) {
                areaStr = "0.00㎡";
            }
            return areaStr;
        }
    }

    private String getTimeValue() {
        int min = (int) (workTime / 60f);
        if (curStatus == MsgCodeUtils.STATUE_RECHARGE || !havMapData || (!isDrawMap() && curStatus != MsgCodeUtils.STATUE_RANDOM && curStatus != MsgCodeUtils.STATUE_TEMPORARY_POINT)) {
            return Utils.getString(R.string.map_aty_gang);
        } else {
            return min + "min";
        }
    }


    @Override
    public void registerPropReceiver() {
        IlifeAli.getInstance().registerDownStream(new OnDevicePoropertyResponse() {
            @Override
            public void onStatusChange(int workmode) {
                if (workmode == curStatus) {
                    return;
                }
                MyLogger.e(TAG, "registerPropReceiver----onStatusChange" + workmode);
                curStatus = workmode;
                setStatus(curStatus, batteryNo);
            }

            @Override
            public void onRealTimeMapStart(long startTime) {
                MyLogger.d(TAG, "registerPropReceiver----onRealTimeMapStart");
                //TODO 清空地图相关数据
                mapStartTime = startTime;
                prepareToReloadData();
                mView.updateCleanTime(getTimeValue());
                mView.updateCleanArea(getAreaValue());
                if (haveMap && pointList != null && isDrawMap()) {
                    mView.drawMapX8(pointList);
                }
            }

            @Override
            public void onBatterState(int batteryLevel) {
                batteryNo = batteryLevel;
                setStatus(curStatus, batteryNo);
            }

            @Override
            public void onRealMap(RealTimeMapBean mapBean) {
                if (mapBean == null) {
                    return;
                }
                MyLogger.d(TAG, "onRealMap----:  " + mapBean.toString());
                int offset = pointList.size();
                parseRealTimeMapX8(mapBean.getMapData());
                updateSlamX8(pointList, offset);
                cleanArea = mapBean.getCleanArea();
                workTime = mapBean.getCleanTime();
                mView.updateCleanTime(getTimeValue());
                mView.updateCleanArea(getAreaValue());
                if (haveMap && pointList != null && isDrawMap()) {
                    mView.drawMapX8(pointList);
                }
            }

            @Override
            public void onError(int code) {
                MyLogger.d(TAG, "ERRORCODE-----" + code);
                errorCode = code;
                mView.showErrorPopup(errorCode);
                setStatus(curStatus, batteryNo);
            }
        });
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
        setPropertiesWithParams(AliSkills.get().enterVirtualMode());
    }


    /**
     * @param list SEND_VIR添加电子墙时为新增电子墙集合，EXIT_VIR 时，为null
     */
    public void sendVirtualWallData(final List<int[]> list) {
        wallPointList.clear();
        wallPointList.addAll(list);
        new Thread(() -> {
            virtualContentBytes = new byte[82];
//                if (sendLists != null && sendLists.size() > 0) {
            if (wallPointList != null && wallPointList.size() > 0) {
//                    int size = sendLists.size();
                int size = wallPointList.size();
                byte open = (byte) 0x01;
                byte counts = (byte) size;
                virtualContentBytes[0] = open;
                virtualContentBytes[1] = counts;
                for (int t = 1; t < size + 1; t++) {
//                        int[] floats = sendLists.get(t - 1);
                    int[] floats = wallPointList.get(t - 1);
                    int x1 = (int) floats[0] - 750;
                    int y1 = (int) 1500 - floats[1] - 750;
                    int x2 = (int) floats[2] - 750;
                    int y2 = (int) 1500 - floats[3] - 750;
                    byte[] startxBytes = DataUtils.intToBytes(x1);
                    byte[] startyBytes = DataUtils.intToBytes(y1);
                    byte[] endxBytes = DataUtils.intToBytes(x2);
                    byte[] endyBytes = DataUtils.intToBytes(y2);
                    virtualContentBytes[(t - 1) * 8 + 2] = startxBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 3] = startxBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 4] = startyBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 5] = startyBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 6] = endxBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 7] = endxBytes[1];
                    virtualContentBytes[(t - 1) * 8 + 8] = endyBytes[0];
                    virtualContentBytes[(t - 1) * 8 + 9] = endyBytes[1];
                }
            } else {
                MyLogger.e(TAG, "sendLists is null");
            }
//            sendToDeviceWithOptionVirtualWall(AliSkills.get().setVirtualWall(virtualContentBytes), physicalId);
        }).start();
    }


    /**
     * 申请添加/删除电子墙
     */
    public void sendToDeviceWithOptionVirtualWall() {

    }

    @Override
    public boolean pointToAlong(boolean reverse) {
        if (reverse) {//延边切重点
            return robotType.equals(Constants.X800) || robotType.equals(Constants.V3x)|| robotType.equals(Constants.X787);
        } else {//重点切延边
            return robotType.equals(Constants.V3x)||robotType.equals(Constants.X787);
        }
    }

    @Override
    public void enterAlongMode() {
        if ((curStatus == MsgCodeUtils.STATUE_POINT && pointToAlong(false)) || curStatus == MsgCodeUtils.STATUE_WAIT || curStatus == MsgCodeUtils.STATUE_ALONG ||
                curStatus == MsgCodeUtils.STATUE_PAUSE || curStatus == MsgCodeUtils.STATUE_RANDOM) {
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


    @Override
    public void enterRechargeMode() {
        if (curStatus == MsgCodeUtils.STATUE_CHARGING || curStatus == MsgCodeUtils.STATUE_CHARGING_) {
            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.map_aty_charge));
        } else if (curStatus == MsgCodeUtils.STATUE_POINT || curStatus == MsgCodeUtils.STATUE_ALONG) {
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
    public void detachView() {
        if (mComDisposable != null) {
            mComDisposable.dispose();
        }
        IlifeAli.getInstance().reset();
        super.detachView();
    }

}
