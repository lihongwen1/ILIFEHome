package com.aliyun.iot.aep.sdk.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by chenjiaping on 2017/7/17.
 */

public class WifiUtils {
    public static final String TAG = WifiUtils.class.getSimpleName();
    public static final int WIFICIPHER_NOPASS = 1;
    public static final int WIFICIPHER_WEP = 3;
    public static final int WIFICIPHER_WPA = 2;

    /**
     * 查找特定的wifi
     *
     * @param wifiManager
     * @return
     */
    public static String searchTargetWifi(WifiManager wifiManager) {
        String targetSsid = "";
        List<ScanResult> list = wifiManager.getScanResults();//get wifi list
        String ssid;
        String mac;
        for (ScanResult scResult : list) {
            ssid = scResult.SSID;
            mac = scResult.BSSID;
            Log.d(TAG, "SSID:  " + ssid + "       mac:     " + mac);
            if (ssid != null && ssid.length() == 10 && ssid.contains("Robot") && (mac.contains("84:5d:d7") || mac.contains("98:d8:63"))) {
                targetSsid = scResult.SSID;
                break;
            }
        }
        return targetSsid;
    }


    /**
     * 获取wifi加密方式
     *
     * @param ssid
     * @return
     */
    public static int getCipherType(String ssid, WifiManager wifiManager) {
        int type = 0;
        List<ScanResult> list = wifiManager.getScanResults();//get wifi list
        for (ScanResult scResult : list) {
            if (!TextUtils.isEmpty(scResult.SSID) && scResult.SSID.equals(ssid)) {
                String capabilities = scResult.capabilities;
                if (!TextUtils.isEmpty(capabilities)) {
                    if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                        type = 2;
                    } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = 3;
                    } else {
                        type = 1;
                    }
                }
            }
        }
        return type;
    }

    public static WifiConfiguration isExist(WifiManager wifiManager, String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        WifiConfiguration tempConfig = null;
        if (configs != null && configs.size() > 0) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.equals("\"" + ssid + "\"")) {
                    tempConfig = config;
                    return tempConfig;
                }
            }
        }
        return null;
    }

    public static boolean connectToAp(WifiManager wifiManager, String ssid) {
        //can use
        boolean isConnSuc;
        WifiConfiguration tempConfig = WifiUtils.isExist(wifiManager, ssid);
        if (tempConfig != null) {
            isConnSuc = wifiManager.enableNetwork(tempConfig.networkId, true);
        } else {
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + ssid + "\"";
            config.preSharedKey = "\"" + "333666999QQQ" + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

            int netId = wifiManager.addNetwork(config);
            wifiManager.disconnect();
            isConnSuc = wifiManager.enableNetwork(netId, true);
        }
        return isConnSuc;
    }


    /**
     * 断开之前的wifi，连接到指定的wifi
     * @param wifiManager
     * @param ssid
     * @param pass
     * @param type
     * @return
     */
    public static boolean forceConnectWifi(WifiManager wifiManager, String ssid, String pass, int type) {
//        wifiManager.disconnect();
        return createWifiConfig(wifiManager, ssid, pass, type);
    }

    public static boolean createWifiConfig(WifiManager mWifiManager, String ssid, String password, int type) {
        boolean isSucc = false;
        //初始化WifiConfiguration
        mWifiManager.startScan();
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        //如果之前有类似的配置
        WifiConfiguration tempConfig = isExist(mWifiManager, ssid);
        if (tempConfig != null) {
            //则清除旧有配置
//            mWifiManager.removeNetwork(tempConfig.networkId);
            isSucc = mWifiManager.enableNetwork(tempConfig.networkId, true);
            Log.e(TAG, "createWifiConfig: " + isSucc);
        } else {
            //不需要密码的场景
            if (type == WIFICIPHER_NOPASS) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                //以WEP加密的场景
            } else if (type == WIFICIPHER_WEP) {
                config.hiddenSSID = true;
                config.wepKeys[0] = "\"" + password + "\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
            } else if (type == WIFICIPHER_WPA) {
                config.preSharedKey = "\"" + password + "\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            }
            int netId = mWifiManager.addNetwork(config);
            mWifiManager.disconnect();
            isSucc = mWifiManager.enableNetwork(netId, true);
            Log.e(TAG, "createWifiConfig: " + isSucc);
        }
        return isSucc;
    }

    /**
     * 获取SSID
     *
     * @param activity 上下文
     * @return WIFI 的SSID
     */
    public static String getSsid(Context activity) {
        String ssid = "unknown id";
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            int networkId = info.getNetworkId();
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration.networkId == networkId) {
                    ssid = wifiConfiguration.SSID;
                    break;
                }
            }
            if (ssid.contains("\"")) {
                return ssid.replace("\"", "");
            }
        }
        return ssid;
    }


}
