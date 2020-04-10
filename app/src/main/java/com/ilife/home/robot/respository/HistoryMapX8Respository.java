package com.ilife.home.robot.respository;

import android.text.TextUtils;
import android.util.Base64;

import androidx.lifecycle.MutableLiveData;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.aliyun.iot.aep.sdk.bean.RealTimeMapBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.bean.CleaningDataX8;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * query cleaning data form server ,and parse it with multithreading
 */
public class HistoryMapX8Respository {
    private String TAG = "MapX9PresenterHelper";
    private ExecutorService fixedThread;//用户解析实时地图的历史数据
    private int pageNo;
    private volatile boolean isStop;//同步
    private CleaningDataX8[] dataX8s;
    private final int SUB_LENGTH = 200;//每200包数据开一个线程去解析
    /**
     * 线程计数器，用于监听到所有线程任务执行完毕后，继续执行当前线程;
     * 改变量的初始值需为已知的线程数量，不然会导致当前线程无限等待；
     */
    private CountDownLatch countDownLatch;


    /**
     * 时间加一分钟，避免由于时间误差导致丢包
     * @param onResponse
     * @param mapStartTime
     */
    public void getHistoryData(OnAliResponseSingle<CleaningDataX8> onResponse, long mapStartTime) {
        IlifeAli.getInstance().getCleaningHistory(mapStartTime, System.currentTimeMillis()+ 60 * 1000, new OnAliResponse<List<RealTimeMapBean>>() {
            @Override
            public void onSuccess(List<RealTimeMapBean> historyData) {

                MyLogger.d(TAG, "4444------------数据量：" + historyData.size());
                if (historyData.size() > 0) {//有数据，开启多线程去解析数据
                    int threadNumbs = historyData.size() / SUB_LENGTH;
                    if (historyData.size() % SUB_LENGTH != 0) {
                        threadNumbs += 1;
                    }
                    fixedThread = Executors.newFixedThreadPool(threadNumbs);
                    dataX8s = new CleaningDataX8[threadNumbs];
                    countDownLatch = new CountDownLatch(threadNumbs);
                    for (int i = 0; i < threadNumbs; i++) {//多线程分段处理数据
                        fixedThread.execute(new ParseRunnable(historyData.subList(SUB_LENGTH * i, i == threadNumbs - 1 ? historyData.size() : SUB_LENGTH * (i + 1)), i));
                    }
                    try {
                        MyLogger.e(TAG, "确保方法不是在主线程调用，避免阻塞Ui,阻塞当前线程,等待数据处理完毕----------------");
                        countDownLatch.await();
                        MyLogger.e(TAG, "数据已经处理完成，线程继续执行-------------------");

                        CleaningDataX8 wholeData = new CleaningDataX8();
                        List<Coordinate> pointList = wholeData.getCoordinates();
                        for (CleaningDataX8 dataX8 : dataX8s) {
                            if (dataX8 == null) {
                                continue;
                            }
                            if (dataX8.isHaveClearFlag()) {
                                pointList.clear();
                            }
                            wholeData.setWorkTime(dataX8.getWorkTime());
                            wholeData.setCleanArea(dataX8.getCleanArea());
                            if (dataX8.getCoordinates() != null) {
                                for (Coordinate coor : dataX8.getCoordinates()) {//去重
                                    if (!pointList.contains(coor)) {
                                        pointList.add(coor);
                                    }
                                }
                            }
                        }
                        onResponse.onResponse(wholeData);
                        cancelOrFinish();
                    } catch (InterruptedException e) {
                        MyLogger.e(TAG, "线程被中断-----------------");
                    }
                } else {
                    cancelOrFinish();
                }
            }

            @Override
            public void onFailed(int code, String message) {
            }
        });
    }


    public void cancelOrFinish() {
        isStop = true;
        if (fixedThread != null) {
            fixedThread.shutdownNow();
        }
        dataX8s = null;
        MyLogger.e(TAG, "cancelOrFinish：页面销毁或者数据处理完成!");
        //TODO 中断所有解析数据的线程
    }

    class ParseRunnable implements Runnable {
        private List<RealTimeMapBean> subData;
        private CleaningDataX8 dataX8;
        private int index;

        ParseRunnable(List<RealTimeMapBean> subData, int index) {
            this.subData = subData;
            this.index = index;
            this.dataX8 = new CleaningDataX8();
            dataX8s[index] = dataX8;
        }

        @Override
        public void run() {
            MyLogger.d(TAG, "------------开始解析数据：" + index);
            if (!isStop) {
                RealTimeMapBean data;
                for (int i = 0; i < subData.size(); i++) {
                    if (isStop) {
                        break;//退出线程
                    }
                    data = subData.get(i);
                    dataX8.setCleanArea(data.getCleanArea());
                    dataX8.setWorkTime(data.getCleanTime());
                    parseRealTimeMapX8(subData.get(i).getMapData());
                }
            }
            countDownLatch.countDown();//当前线程执行完毕需要-1；
        }

        /**
         * 解析x800黄方格地图数据
         *
         * @param mapData
         */
        private void parseRealTimeMapX8(String mapData) {
            byte[] pointCoor = new byte[2];
            if (!TextUtils.isEmpty(mapData)) {
                byte[] bytes = Base64.decode(mapData, Base64.DEFAULT);
                Coordinate coordinate;
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
                        if ((x == 0x7fff) & (y == 0x7fff)) {//数据清空标记，遇到此标记，该数据包以前的数据都是无效的。
                            MyLogger.e(TAG, "the map data has been cleaned and reset");
                            dataX8.setHaveClearFlag(true);
                        } else {
                            coordinate = new Coordinate(x, -y, type);
                            if (isStop) {
                                MyLogger.e(TAG, "the page has been destroyed，the data processing will make no sense/the data processing is no longer meaningful!");
                                break;
                            } else {
                                dataX8.addCoordinate(coordinate);
                            }
                        }
                    }
                }
            }
        }
    }
}
