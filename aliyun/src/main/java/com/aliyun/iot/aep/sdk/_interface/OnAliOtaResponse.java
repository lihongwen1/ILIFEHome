package com.aliyun.iot.aep.sdk._interface;

public interface OnAliOtaResponse {
    /**
     * 已是最新版本
     *
     * @param version
     */
    void isnNewestVersion(String version);

    /**
     * 需用户确认下载安装包
     *
     * @param curV
     * @param newVer
     */
    void hasNewInstallPkg(String curV, String newVer);

    void haveEnsuredLoadingPkg();

    /**
     * 下载安装包中，更新进度
     *
     * @param curV
     * @param newVer
     * @param progress
     */
    void loadingProgress(String curV, String newVer, int progress);

    /**
     * 下载安装包中，更新进度
     *
     * @param progress
     */
    void loadingProgress(int progress);

    /**
     * 下载成功，需检查更新
     */
    void loadingSuccess();

    /**
     * 下载失败，需重新下
     */
    void loadingFail(String curV, String newVer);

    /**
     * 有新的主机更新版本(安装包已下载)
     *
     * @param curV
     * @param newVer
     */
    void hasOtaUpdating(String curV, String newVer);

    void haveEnteredOtaMode();

    /**
     * 主机OTA更新中
     * //TODO 该进度需要计算，主机未上传
     *
     * @param progress
     */
    void otaUpdatingProgress(String curV, String newVer, int progress);

    /**
     * 主机OTA更新中
     * //TODO 该进度需要计算，主机未上传
     *
     * @param progress
     */
    void otaUpdatingProgress(int progress);

    /**
     * 主机OTA更新成功，显示最新版本
     *
     * @param newVer
     */
    void otaUpdatingSuccess(String newVer);

    /**
     * 主机OTA更新失败
     *
     * @param curVer
     * @param newestVer
     */
    void otaUpdatingFail(String curVer, String newestVer);

}
