package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryDetailActivity_x9 extends BackBaseActivity {
    private final String TAG = HistoryDetailActivity_x9.class.getSimpleName();
    private byte[] slamBytes;
    private byte[] roadBytes;
    private String[] mapArray;
    private List<Integer> historyPointsList;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.mv_history_detail)
    MapView mapView;

    @BindView(R.id.tv_history_date)
    TextView tv_history_date;
    @BindView(R.id.tv_end_reason)
    TextView tv_end_reason;
    @BindView(R.id.iv_cleaning_flag)
    ImageView iv_cleaning_flag;
    @BindView(R.id.tv_clean_time)
    TextView tv_clean_time;
    @BindView(R.id.tv_lean_area)
    TextView tv_lean_area;
    private String subdomain;
    private boolean isDrawMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history_detail_x9;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isDrawMap) {//避免多次刷新UI
            return;
        }
        isDrawMap = true;
        switch (DeviceUtils.getRobotType(IlifeAli.getInstance().getWorkingDevice().getProductKey())) {
            case Constants.X800:
            case Constants.X800W:
                drawHistoryMapX8();
                break;
            case Constants.V3x:
                //V3X没有地图
                break;
            case Constants.X787:
                drawHistoryMapX7();
                break;
        }
    }

    private void drawHistoryMapX7() {
        if (mapArray == null || mapArray.length <= 0) {
            return;
        }
        List<Byte> byteList = new ArrayList<>();
        List<Coordinate> pointList = new ArrayList<>();
        int lineCount = 0;
        String data;
        for (String s : mapArray) {
            data = s;
            if (data == null || data.isEmpty()) {
                continue;
            }

            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            if (bytes.length < 2) {
                MyLogger.e(TAG, "数据异常。。。。。。。");
                continue;
            }
            lineCount = bytes[1] & 0xff;
            for (int j = 2; j < bytes.length; j++) {
                byteList.add(bytes[j]);
            }
        }
        MyLogger.d(TAG, "LINE COUNT:" + lineCount);
        lineCount *= 4;
        Coordinate coordinate;
        if (byteList.size() > 0) {
            int x = 0, y = 0, type;
            for (int i = 0; i < byteList.size(); i++) {
                byte b = byteList.get(i);
                MyLogger.e(TAG, "十六进制字节：" + byteToBit(b));
                for (int j = 0; j < 4; j++) {//一个字节解析出4个坐标（坐标类型）
                    type = getBits(b, 6 - j * 2);
                    MyLogger.d(TAG, "坐标---" + "x:" + x + "---y---" + y + "---type---" + type);
                    if (type != 0) {
                        coordinate = new Coordinate(x, y, type);
                        pointList.add(coordinate);
                    }
                    if (x < lineCount - 1) {
                        x++;
                    } else {
                        x = 0;
                        y++;
                    }

                }
            }
            xMin = 0;
            xMax = lineCount;
            yMin = 0;
            yMax = y;
        }
        mapView.updateSlam(xMin, xMax, yMin, yMax);
        mapView.setNeedEndPoint(false);
        mapView.drawMapX8(pointList);
        MyLogger.e(TAG, "字节数：   " + byteList.size() + "-----总点数：  ");
    }

    /**
     * 把byte转为字符串的bit
     */
    public String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    //b为传入的字节，start是起始位，length是长度，如要获取bit0-bit4的值，则start为0，length为5
    public int getBits(byte b, int start) {
        return (b >>> start) & 0x3;
    }


    private void drawHistoryMapX8() {
        Single.create((SingleOnSubscribe<List<Coordinate>>) emitter -> {
            int lineCount = 0;
            List<Byte> byteList = new ArrayList<>();
            List<Coordinate> pointList = new ArrayList<>();
            if (mapArray != null) {
                if (mapArray.length > 0) {
                    for (String data : mapArray) {
                        if (data == null) {
                            continue;
                        }
                        byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                        if (bytes.length < 7) {
                            MyLogger.e(TAG, "数据异常。。。。。。。");
                            continue;
                        }
                        int bj = bytes[0] & 0xff;
                        if (bj != 1) {
                            MyLogger.e(TAG, "包含路径数据。。。。。。。。");
                            continue;
                        }
                        int lx = DataUtils.bytesToInt(new byte[]{bytes[1], bytes[2]}, 0);
                        int ly = DataUtils.bytesToInt(new byte[]{bytes[3], bytes[4]}, 0);
                        lineCount = DataUtils.bytesToInt2(new byte[]{bytes[5], bytes[6]}, 0);
                        for (int j = 7; j < bytes.length; j++) {
                            byteList.add(bytes[j]);
                        }
                    }
                }
            }
            int totalLength = 0;
            Coordinate coordinate;
            if (byteList.size() > 0) {
                int x = 0, y = 0, type = 0, length = 0;
                for (int i = 2; i < byteList.size(); i += 3) {
                    type = byteList.get(i - 1) & 0xff;
                    length = byteList.get(i) & 0xff;
                    totalLength += length;

                    for (int j = 0; j < length; j++) {
                        if (type != 0) {
                            coordinate = new Coordinate(x, y, type);
                            pointList.add(coordinate);
                        }
                        if (x < lineCount - 1) {
                            x++;
                        } else {
                            x = 0;
                            y++;
                        }

                    }
                }
                xMin = 0;
                xMax = lineCount;
                yMin = 0;
                yMax = y;
            }
            MyLogger.e(TAG, "字节数：   " + byteList.size() + "-----总点数：  " + totalLength);
            emitter.onSuccess(pointList);
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Coordinate>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<Coordinate> pointList) {
                        mapView.updateSlam(xMin, xMax, yMin, yMax);
                        mapView.setNeedEndPoint(false);
                        mapView.drawMapX8(pointList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void initData() {//取出传递过来的集合
        historyPointsList = new ArrayList<>();
        subdomain = "";
        String robotType = DeviceUtils.getRobotType(subdomain);
        mapView.setRobotSeriesX9(robotType.equals(Constants.X900) || robotType.equals(Constants.X910));
        mapView.setNeedRestore(false);
        Intent intent = getIntent();
        if (intent != null) {
            HistoryRecordBean record = (HistoryRecordBean) intent.getSerializableExtra("Record");
            mapArray = record != null ? record.getMapDataArray() : new String[0];
            MyLogger.e(TAG, "getDate===:" + xMin + "<--->" + xMax + "<--->" + yMin + "<--->" + yMax + "<--->");
            long time_ = record.getStartTime();
            String date = generateTime(time_, "yyyy/MM/dd HH:mm");
            tv_title.setText(R.string.history_detail_title);
            tv_history_date.setText(date);
            tv_end_reason.setText(getResources().getString(R.string.setting_aty_end_reason, gerRealErrortTip(record.getStopCleanReason())));
            tv_clean_time.setText(record.getCleanTotalTime() + "min");
            tv_lean_area.setText(record.getCleanTotalArea() + "㎡");
            iv_cleaning_flag.setImageResource(record.getStopCleanReason() == 1 ? R.drawable.annal_icon_finish : R.drawable.annal_icon_problem);
        }

    }

    public String generateTime(long time, String strFormat) {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        String str = format.format(new Date((time + 10) * 1000));
        return str;
    }

    private String gerRealErrortTip(int number) {
        String text = "";
        switch (number) {
            case 1:
                text = getResources().getString(R.string.stop_work_reason1);
                break;
            case 2:
                text = getResources().getString(R.string.stop_work_reason2);
                break;
            case 3:
                text = getResources().getString(R.string.stop_work_reason3);
                break;
            case 4:
                text = getResources().getString(R.string.stop_work_reason4);
                break;
            case 5:
                text = getResources().getString(R.string.stop_work_reason5);
                break;
            case 6:
                text = getResources().getString(R.string.stop_work_reason6);
                break;
            default:
                text = getResources().getString(R.string.stop_work_reason1);
                break;
        }
        return text;
    }

    public void initView() {

    }
}
