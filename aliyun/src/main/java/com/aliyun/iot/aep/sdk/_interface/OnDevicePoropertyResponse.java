package com.aliyun.iot.aep.sdk._interface;

/**
 * 地图页面注册设备属性变化监听时用到
 */
//TODO data转化为bean
public interface OnDevicePoropertyResponse {
    /**
     * 状态变化json,包含状态(work mode)和属性的变化（etc:max，battery,water）
     *
     * @param data
     */
    void onStatusChange(int data);

    /**
     * 实时地图开始时间
     * 此时需要清空地图数据，刷新地图
     */
    void onRealTimeMapStart(long mapStarTime);

    /**
     * 实时地图数据
     */
    void onRealMap(String mapBeanData);

    void onBatterState(int batteryLevel);

    void onMaxChange(boolean isMax);

    void onVirtualWallChange(String virtualWall);

    void onForbiddenAreaChange(String fbdArea);

    void onCleanAreaChange(String clanArea);

    void onCleanRoomChange(String cleanRoom);

    void onInitStatusChange(int initStatus);

    /**
     * 错误码 为0时不用处理
     */
    void onError(int errorcode);
}
