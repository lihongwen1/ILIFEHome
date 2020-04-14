package com.aliyun.iot.aep.sdk._interface;

public interface OnAliSetPropertyResponse {
    void onSuccess(String path, int tag, int functionCode, int responseCode);

    void onFailed(String path, int tag, int code, String message);
}
