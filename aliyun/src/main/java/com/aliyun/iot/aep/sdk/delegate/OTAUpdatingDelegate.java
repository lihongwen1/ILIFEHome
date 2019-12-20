package com.aliyun.iot.aep.sdk.delegate;

import android.util.Log;

import com.aliyun.iot.aep.sdk._interface.OnAliOtaResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.OTAInfoBean;
import com.aliyun.iot.aep.sdk.bean.OTAUpgradeBean;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;

public class OTAUpdatingDelegate {
    private OnAliOtaResponse onAliOtaResponse;
    private String curFirmwareVer, newestFirmwareVer;
    private String wholeNewestVer;//云端回复的安装包的最新版本，用于向云端查询安装包下载进度
    private boolean isUpdating = false;
    private boolean isCancel = false;

    public OTAUpdatingDelegate(OnAliOtaResponse onAliOtaResponse) {
        this.onAliOtaResponse = onAliOtaResponse;
    }

    public void checkOTA() {
        IlifeAli.getInstance().queryOTAInstallPkg(new OnAliResponse<OTAInfoBean>() {
            @Override
            public void onSuccess(OTAInfoBean result) {
                curFirmwareVer = result.getCurrentVer() + "";
                newestFirmwareVer = result.getTargetVer() + "";
                wholeNewestVer = result.getWholeTargetVer();
                if (result.getTargetVer() > result.getCurrentVer()) {//发现新的安装包，需进一步查询是否已经在下载安装包中
                    queryLoadingProgress(false);
                } else {//没有发现新的安装包
                    queryOtaUpdating(false);
                }
            }

            @Override
            public void onFailed(int code, String message) {
                Log.d("OTAUpdatingDelegate", "获阿里云OTA信息------" + message);
                queryOtaUpdating(false);
            }
        });
    }

    /**
     * 查询安装包下载进度
     *
     * @param isPolling 标记是轮询，还是首次调用
     */
    public void queryLoadingProgress(boolean isPolling) {
        IlifeAli.getInstance().queryDownloadProgress(wholeNewestVer, new OnAliResponse<OTAUpgradeBean>() {
            @Override
            public void onSuccess(OTAUpgradeBean result) {
                switch (result.getUpgradeStatus()) {
                    case 0://待用户确认
                        if (!isCancel) {
                            onAliOtaResponse.hasNewInstallPkg(curFirmwareVer, newestFirmwareVer);
                        }
                        break;
                    case 1://下载安装包中
                        if (!isCancel) {
                            if (isPolling) {
                                onAliOtaResponse.loadingProgress(result.getStep());
                            } else {
                                onAliOtaResponse.loadingProgress(curFirmwareVer, newestFirmwareVer, result.getStep());
                            }
                        }
                        break;
                    case 2://下载安装包异常
                    case 3://下载安装包失败
                        if (!isCancel) {
                            if (isPolling) {
                                onAliOtaResponse.loadingFail(curFirmwareVer, newestFirmwareVer);
                            } else {//首次调用时允许用户再次单机下载
                                onAliOtaResponse.hasNewInstallPkg(curFirmwareVer, newestFirmwareVer);
                            }
                        }
                        break;
                    case 4://下载安装包成功,进一步查询主机更新版本
                        /**
                         * 上报固件版本已完成下载
                         */
                        /**
                         * 查询主机OTA更新
                         */
                        queryOtaUpdating(false);
//                        onAliOtaResponse.loadingSuccess();
                        break;


                }
            }

            @Override
            public void onFailed(int code, String message) {
                /*
                 * 失败认为新安装包未开始下载
                 */
                if (!isCancel) {
                    onAliOtaResponse.hasNewInstallPkg(curFirmwareVer, newestFirmwareVer);
                }
            }
        });
    }


    public void queryOtaUpdating(boolean isPolling) {
        IlifeAli.getInstance().queryRobotOtaVer(new OnAliResponse<OTAInfoBean>() {
            @Override
            public void onSuccess(OTAInfoBean bean) {
                curFirmwareVer = Integer.toHexString(bean.getCurrentVer());
                newestFirmwareVer = Integer.toHexString(bean.getTargetVer());
                switch (bean.getUpdateState()) {
                    case 0:
                        if (!isCancel) {
                            onAliOtaResponse.isnNewestVersion(newestFirmwareVer);
                        }
                        break;
                    case 1:
                        if (!isCancel) {
                            if (!isUpdating) {//避免开始升级后，开始几次查询OTA回复的错误state
                                onAliOtaResponse.hasOtaUpdating(curFirmwareVer, newestFirmwareVer);
                            } else {
                                onAliOtaResponse.otaUpdatingProgress(bean.getUpdateProgess());
                            }
                        }
                        break;
                    case 2:
                        //TODO 该进度需要计算，主机未上传
                        if (!isCancel) {
                            if (isPolling) {
                                onAliOtaResponse.otaUpdatingProgress(bean.getUpdateProgess());
                            } else {
                                onAliOtaResponse.otaUpdatingProgress(curFirmwareVer, newestFirmwareVer, bean.getUpdateProgess());
                            }
                        }
                        break;
                    case 3:
                        if (!isCancel) {
                            onAliOtaResponse.otaUpdatingFail(curFirmwareVer, newestFirmwareVer);
                        }
                        break;
                    case 4:
                        /**
                         * 更新成功，上班版本号
                         */
                        IlifeAli.getInstance().reportInstallPkgVer(wholeNewestVer);
                        if (!isCancel) {
                            onAliOtaResponse.otaUpdatingSuccess(newestFirmwareVer);
                        }
                        break;

                }
            }

            @Override
            public void onFailed(int code, String message) {
                Log.d("OTAUpdatingDelegate", "获取OTA更新进度异常------" + message);
            }
        });
    }

    public void ensureLoadingInstallPkg() {

        IlifeAli.getInstance().ensureDownloadOTA(new OnAliResponse<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d("OTAUpdatingDelegate", "开始下载OTA安装包。。。");
                if (!isCancel) {
                    onAliOtaResponse.haveEnsuredLoadingPkg();
                }
            }

            @Override
            public void onFailed(int code, String message) {
                Log.d("OTAUpdatingDelegate", "进入OTA下载模式失败" + message);
            }
        });
    }

    public void ensureInstallOTA() {
        IlifeAli.getInstance().setProperties(AliSkills.get().enterOTAMode(IlifeAli.getInstance().getIotId()), new OnAliSetPropertyResponse() {
            @Override
            public void onSuccess(String path, int tag, int functionCode, int responseCode) {
                Log.d("OTAUpdatingDelegate", "进入OTA升级模式.....");
                isUpdating = true;
                if (!isCancel) {
                    onAliOtaResponse.haveEnteredOtaMode();
                }
            }

            @Override
            public void onFailed(String path, int tag, int code, String message) {
                isUpdating = true;
                Log.d("OTAUpdatingDelegate", "进入OTA升级模式失败" + message);
            }
        });
    }


    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}
