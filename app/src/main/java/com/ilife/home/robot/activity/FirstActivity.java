package com.ilife.home.robot.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aliyun.alink.sdk.bone.plugins.config.BoneConfig;
import com.aliyun.iot.aep.oa.OALanguageHelper;
import com.aliyun.iot.aep.sdk.IoTSmart;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientImpl;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.config.GlobalConfig;
import com.aliyun.iot.aep.sdk.helper.SDKInitHelper;
import com.aliyun.iot.aep.sdk.page.CountryListActivity;
import com.aliyun.iot.aep.sdk.threadpool.ThreadPool;
import com.aliyun.iot.link.ui.component.LinkToast;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

import static com.aliyun.iot.aep.sdk.IoTSmart.REGION_CHINA_ONLY;


/**
 * Created by chenjiaping on 2017/7/20.
 */
// TODO 隐私政策翻译

public class FirstActivity extends BaseActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private final int GOTOMAIN = 0x11;
    private UniversalDialog protocolDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                removeActivity();
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.avtivity_first;
    }

    @Override
    public void initView() {

    }

    private void showProtocolDialog() {
        if (protocolDialog == null) {
            protocolDialog = new UniversalDialog();
            String text = "您即将使用ILIFE的智能设备，并通过ILIFEHome应用程序享受服务。为了您在使用ILIFE服务时得到充分的信任，请您仔细阅读《服务协议》和《隐私政策》。如您同意，请点击“接受”开始享受我们的服务。";
            SpannableString sb = new SpannableString(text);
            sb.setSpan(new MyClickText(1), 65, 71, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new MyClickText(2), 72, 78, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            protocolDialog.setTitle("ILIFEHome隐私政策与用户协议").setHintTip(sb, Gravity.START, getResources().getColor(R.color.color_33)).setLeftText("取消").setRightText("接受")
                    .setRightDrawable(R.drawable.shape_bg_color)
                    .setOnLeftButtonClck(this::finish).setOnRightButtonClck(() -> {
                SpUtils.saveBoolean(FirstActivity.this, "key_agree_protocol", true);
                checkPermission();
            });
        }
        if (!protocolDialog.isAdded()) {
            protocolDialog.show(getSupportFragmentManager(), "protocol");
        }
    }

    private WeakHandler handler = new WeakHandler(msg -> {
        if (msg.what == GOTOMAIN) {
            gotoMain();
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isChinaEnvironment() && !SpUtils.getBoolean(this, "key_agree_protocol")) {
            showProtocolDialog();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(aBoolean -> {
            if (aBoolean) {
                handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
            } else {
                ToastUtils.showToast(this, getString(R.string.access_location));
                //未授权处理
            }
        }).dispose();
    }

    public void gotoMain() {
        if (IlifeAli.getInstance().isLogin()) {//this value will be true ,when the region type is REGION_CHINA_ONLY,and the user have login before.
            Intent i;
            i = new Intent(FirstActivity.this, MainActivity.class);
            startActivity(i);
            removeActivity();
        } else {
            if (GlobalConfig.getInstance().getInitConfig().getRegionType() == REGION_CHINA_ONLY) {//CHINA
                startLogin();
            } else {//OVER SEA
                IlifeAli.getInstance().selectACountry(new OnAliResponse<String>() {
                    @Override
                    public void onSuccess(String selectCountry) {
                        MyLogger.d(TAG, "the select country is : " + selectCountry);
                        Locale locale = Locale.getDefault();
                        String lan = locale.getLanguage();
                        if (!lan.equals("zh")) {//
                            try {
                                //初始化之后，可以改变显示语言,目前支持中文“zh-CN”, 英文"en-US"，法文"fr-FR",德文"de-DE",日文"ja-JP",韩文"ko-KR",西班牙文"es-ES",俄文"ru-RU"，八种语言
                                switchLanguage("en-US");
                                //修改OA多语言 目前支持Locale.US 英文、Locale.SIMPLIFIED_CHINESE 中文、Locale.FRANCE 法语、Locale.JAPAN 日语、Locale.GERMANY 德语、Locale.KOREA 韩语  、new Locale("ru","RU") 俄语、new Locale("es","ES") 西班牙语
                                OALanguageHelper.setLanguageCode(Locale.US);
                            } catch (Exception e) {

                            }
                        }
                        startLogin();
                    }

                    @Override
                    public void onFailed(int code, String message) {//结束进程
                        removeALLActivity();
                        ThreadPool.MainThreadHandler.getInstance().post(() -> Process.killProcess(Process.myPid()), 2000);
                    }
                });
            }


        }


    }

    private void startLogin() {
        IlifeAli.getInstance().login(new OnAliResponse<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {//LOGIN Success
                Intent i;
                i = new Intent(FirstActivity.this, MainActivity.class);
                startActivity(i);
                removeActivity();
            }


            @Override
            public void onFailed(int code, String message) {//Login failed
                MyLogger.d(TAG, "登录失败------------");
                removeALLActivity();
            }
        });
    }


    private class MyClickText extends ClickableSpan {
        private int type;

        public MyClickText(int type) {
            this.type = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            //设置文本的颜色
            ds.setColor(getResources().getColor(R.color.color_ff4d00));
            //超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(FirstActivity.this, ProtocolActivity.class);
            intent.putExtra(ProtocolActivity.KEY_TYPE, type);
            startActivity(intent);
        }
    }


    /**
     * 改变语言，目前支持中文“zh-CN”, 英文"en-US"，法文"fr-FR",德文"de-DE",日文"ja-JP",韩文"ko-KR",西班牙文"es-ES",俄文"ru-RU"，八种语言
     *
     * @param locale: “zh-CN” 或 "en-US"
     */
    private void switchLanguage(String locale) {
        IoTAPIClientImpl.getInstance().setLanguage(locale); // 全局配置，设置后立即起效

        // 容器更改语言
        BoneConfig.set("language", locale);

        // APIClient更改语言后，push通道重新绑定即可更改push语言
//        PushManager.getInstance().bindUser();
    }
}
