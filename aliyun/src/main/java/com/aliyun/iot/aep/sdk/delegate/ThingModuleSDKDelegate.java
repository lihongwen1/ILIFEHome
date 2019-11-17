package com.aliyun.iot.aep.sdk.delegate;

import android.app.Application;

import com.aliyun.alink.linksdk.tmp.TmpSdk;
import com.aliyun.alink.linksdk.tmp.api.TmpInitConfig;
import com.aliyun.alink.linksdk.tmp.extbone.BoneSubDeviceService;
import com.aliyun.alink.linksdk.tmp.extbone.BoneThing;
import com.aliyun.alink.linksdk.tmp.extbone.BoneThingDiscovery;
import com.aliyun.alink.linksdk.tools.ALog;
import com.aliyun.alink.sdk.jsbridge.BonePluginRegistry;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author guikong on 18/4/7.
 */

public class ThingModuleSDKDelegate extends SimpleSDKDelegateImp {

    @Override
    public int init(Application application, SDKConfigure sdkConfigure, Map<String, String> map) {
        ALog.setLevel(ALog.LEVEL_DEBUG);
        TmpSdk.init(application, new TmpInitConfig(TmpInitConfig.DAILY));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TmpSdk.getDeviceManager().discoverDevices(null,false,10000,null);
            }
        },0,60000);
//        TmpSdk.getDeviceManager().discoverDevices(null,5000,null);

        BonePluginRegistry.register("BoneThing", BoneThing.class);
        BonePluginRegistry.register("BoneSubDeviceService", BoneSubDeviceService.class);
        BonePluginRegistry.register("BoneThingDiscovery", BoneThingDiscovery.class);
        return 0;
    }
}
