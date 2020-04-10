package com.aliyun.iot;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.aliyun.alink.linksdk.tools.ThreadTools;
import com.aliyun.alink.sdk.bone.plugins.config.BoneConfig;
import com.aliyun.iot.aep.component.router.IUrlHandler;
import com.aliyun.iot.aep.oa.OALanguageHelper;
import com.aliyun.iot.aep.routerexternal.RouterExternal;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientImpl;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.bundle.BundleManager;
import com.aliyun.iot.aep.sdk.framework.bundle.PageConfigure;
import com.aliyun.iot.aep.sdk.helper.SDKInitHelper;
import com.aliyun.iot.aep.sdk.log.ALog;

import java.util.ArrayList;
import java.util.Locale;

public abstract class SdkApplication extends AApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        // 其他 SDK, 仅在 主进程上初始化

        String packageName = this.getPackageName();
        if (!packageName.equals(ThreadTools.getProcessName(this, android.os.Process.myPid()))) {
            return;
        }
        IlifeAli.getInstance().init(this);
        SDKInitHelper.init(this, getCountry());

        /* 加载Native页面 */
        BundleManager.init(this, (application, configure) -> {
            if (null == configure || null == configure.navigationConfigures)
                return;

            ArrayList<String> nativeUrls = new ArrayList<>();
            ArrayList<PageConfigure.NavigationConfigure> configures = new ArrayList<>();

            PageConfigure.NavigationConfigure deepCopyItem = null;
            for (PageConfigure.NavigationConfigure item : configure.navigationConfigures) {
                if (null == item.navigationCode || item.navigationCode.isEmpty() || null == item.navigationIntentUrl || item.navigationIntentUrl.isEmpty())
                    continue;

                deepCopyItem = new PageConfigure.NavigationConfigure();
                deepCopyItem.navigationCode = item.navigationCode;
                deepCopyItem.navigationIntentUrl = item.navigationIntentUrl;
                deepCopyItem.navigationIntentAction = item.navigationIntentAction;
                deepCopyItem.navigationIntentCategory = item.navigationIntentCategory;

                configures.add(deepCopyItem);

                nativeUrls.add(deepCopyItem.navigationIntentUrl);

                ALog.d("BundleManager", "register-native-page: " + item.navigationCode + ", " + item.navigationIntentUrl);

                RouterExternal.getInstance().registerNativeCodeUrl(deepCopyItem.navigationCode, deepCopyItem.navigationIntentUrl);
                RouterExternal.getInstance().registerNativePages(nativeUrls, new NativeUrlHandler(deepCopyItem));
            }
        });
//        Locale locale = Locale.getDefault();
//        String lan = locale.getLanguage();
//        if (!lan.equals("zh")) {//
//            //初始化之后，可以改变显示语言,目前支持中文“zh-CN”, 英文"en-US"，法文"fr-FR",德文"de-DE",日文"ja-JP",韩文"ko-KR",西班牙文"es-ES",俄文"ru-RU"，八种语言
//            switchLanguage("en-US");
//            //修改OA多语言 目前支持Locale.US 英文、Locale.SIMPLIFIED_CHINESE 中文、Locale.FRANCE 法语、Locale.JAPAN 日语、Locale.GERMANY 德语、Locale.KOREA 韩语  、new Locale("ru","RU") 俄语、new Locale("es","ES") 西班牙语
//            OALanguageHelper.setLanguageCode(Locale.US);
//        }

    }

    /**
     * help class
     */
    static final private class NativeUrlHandler implements IUrlHandler {

        private final String TAG = "ApplicationHelper$NativeUrlHandler";

        private final PageConfigure.NavigationConfigure navigationConfigure;

        NativeUrlHandler(PageConfigure.NavigationConfigure configures) {
            this.navigationConfigure = configures;
        }

        @Override
        public void onUrlHandle(Context context, String url, Bundle bundle, boolean startActForResult, int reqCode) {
            ALog.d(TAG, "onUrlHandle: url: " + url);
            if (null == context || null == url || url.isEmpty())
                return;

            /* prepare the intent */
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));

            if (null != this.navigationConfigure.navigationIntentAction)
                intent.setAction(this.navigationConfigure.navigationIntentAction);
            if (null != this.navigationConfigure.navigationIntentCategory)
                intent.addCategory(this.navigationConfigure.navigationIntentCategory);

            if (Build.VERSION.SDK_INT >= 26) {//解决android8.0路由冲突问题，将intent行为限制在本应用内
                intent.setPackage(context.getPackageName());
            }

            /* start the navigated activity */
            ALog.d(TAG, "startActivity(): url: " + this.navigationConfigure.navigationIntentUrl + ", startActForResult: " + startActForResult + ", reqCode: " + reqCode);
            this.startActivity(context, intent, bundle, startActForResult, reqCode);
        }

        private void startActivity(Context context, Intent intent, Bundle bundle, boolean startActForResult, int reqCode) {
            if (null == context || null == intent)
                return;


            if (null != bundle) {
                intent.putExtras(bundle);
            }
            /* startActivityForResult() 场景，只能被 Activity 调用 */
            if (startActForResult) {
                if (false == (context instanceof Activity))
                    return;

                ((Activity) context).startActivityForResult(intent, reqCode);

                return;
            }

            /* startActivity 被 Application 调用时的处理 */
            if (context instanceof Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            /* startActivity 被 Activity、Service 调用时的处理 */
            else if (context instanceof Activity || context instanceof Service) {
                context.startActivity(intent);
            }
            /* startActivity 被其他组件调用时的处理 */
            else {
                // 暂不支持
            }
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

    public abstract String getCountry();

}
