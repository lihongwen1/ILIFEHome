package com.ilife.home.robot.app;

import android.app.Activity;
import android.content.Context;

import com.alibaba.sdk.android.openaccount.ConfigManager;
import com.aliyun.iot.SdkApplication;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.google.gson.Gson;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.utils.toast.Toasty;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.bugly.crashreport.CrashReport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MyApplication extends SdkApplication {
    private final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication instance;

    private List<Activity> activities;
    private RobotConfigBean robotConfig;
    private boolean isBackLogin = false;

    @Override
    public void onCreate() {
        super.onCreate();
        activities = new ArrayList<>();
        instance = (MyApplication) getApplicationContext();
        ConfigManager.getInstance().setBundleName("com.ilife.home.robot");
        configToast();
        closeAndroidPDialog();
        /**
         * tencent bugly crash日志上传
         */
        CrashReport.initCrashReport(getApplicationContext(), BuildConfig.BUGLY_ID, false);
        /**
         * 日志打印
         */
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("ILIFE_ALI")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
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

    private void configToast() {
        Toasty.Config.getInstance().tintIcon(true).tintIcon(false).
                setTextSize(16).allowQueue(false).apply();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        LogcatHelper.getInstance(this).stop();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(base);
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public RobotConfigBean readRobotConfig() {
        if (robotConfig == null) {
            Gson gson = new Gson();
            String configFile;
            if (getCountry().equals("CHINA")) {
                configFile = "china_robot.json";
            } else {
                configFile = "over_sea_robot_json";
            }
            robotConfig = gson.fromJson(Utils.getJson(configFile, instance), RobotConfigBean.class);
        }
        return robotConfig;
    }

    private void closeAndroidPDialog() {
//        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.P){
//            return;
//        }
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 添加Activity
     */
    public void addActivity_(Activity activity) {
        if (!activities.contains(activity)) {
            MyLogger.d("添加页面", "----" + activity.getClass().getName());
            activities.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity_(Activity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);//从集合中移除
            MyLogger.d("销毁页面", "页面：  " + activity.getClass().getSimpleName());
            activity.finish();//销毁当前Activity
        }
    }


    /**
     * 销毁所有的Activity
     */
    public void removeALLActivity_() {
        Iterator<Activity> iterator = activities.iterator();
        Activity activity;
        while (iterator.hasNext()) {
            activity = iterator.next();
            MyLogger.d("销毁页面", "页面：  " + activity.getClass().getSimpleName());
            iterator.remove();
            activity.finish();
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeALLActivityExclude(Activity excludeActivity) {
        Iterator<Activity> iterator = activities.iterator();
        Activity activity;
        while (iterator.hasNext()) {
            activity = iterator.next();
            if (activity == excludeActivity) {
                continue;
            }
            MyLogger.d("销毁页面", "页面：  " + activity.getClass().getSimpleName());
            iterator.remove();
            activity.finish();
        }
    }

    @Override
    public String getCountry() {
        return BuildConfig.BUILD_COUNTRY;
    }
}
