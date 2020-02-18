package com.aliyun.iot.aep.sdk.bean;

public class DeviceInfoBean {


    /**
     * gmtModified : 1531360639000
     * categoryImage : http://iotx-paas-admin.oss-cn-shanghai.aliyuncs.com/publish/image/1526474704326.png
     * netType : NET_WIFI
     * nodeType : DEVICE
     * productKey : a1XoFUJWkPr
     * deviceName : VD_l2c4LuifwY
     * productName : 风扇-Demo
     * identityAlias : 15700192592
     * iotId : g1VQsQvQvHdkuGjI5unE0010eedc00
     * owned : 0
     * identityId : 5072op548214e63f60e5c5bb17631d9201ea1295
     * thingType : VIRTUAL
     * status : 1 设备的状态 0：未激活 ；1：在线；3：离线；8：禁用
     */

    private long gmtModified;
    private String categoryImage;
    private String netType;
    private String nodeType;
    private String productKey;
    private String deviceName;
    private String productName;
    private String identityAlias;
    private String iotId;
    private int owned;
    private String identityId;
    private String thingType;
    private int status;
    private String nickName;
    private PropertyBean deviceInfo;
    private int work_status;
    private  int battery;
    public int getWork_status() {
        return work_status;
    }

    public void setWork_status(int work_status) {
        this.work_status = work_status;
    }

    public long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getProductKey() {
        if (productKey==null){
            return "";
        }
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getIdentityAlias() {
        return identityAlias;
    }

    public void setIdentityAlias(String identityAlias) {
        this.identityAlias = identityAlias;
    }

    public String getIotId() {
        return iotId;
    }

    public void setIotId(String iotId) {
        this.iotId = iotId;
    }

    public int getOwned() {
        return owned;
    }

    public void setOwned(int owned) {
        this.owned = owned;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getThingType() {
        return thingType;
    }

    public void setThingType(String thingType) {
        this.thingType = thingType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public PropertyBean getDeviceInfo() {
        if (deviceInfo == null) {
            return new PropertyBean();
        } else {
            return deviceInfo;

        }
    }

    public void setDeviceInfo(PropertyBean deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String toString() {
        return "DeviceInfoBean{" +
                "gmtModified=" + gmtModified +
                ", categoryImage='" + categoryImage + '\'' +
                ", netType='" + netType + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", productKey='" + productKey + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", productName='" + productName + '\'' +
                ", identityAlias='" + identityAlias + '\'' +
                ", iotId='" + iotId + '\'' +
                ", owned=" + owned +
                ", identityId='" + identityId + '\'' +
                ", thingType='" + thingType + '\'' +
                ", status=" + status +
                ", nickName='" + nickName + '\'' +
                ", deviceInfo=" + deviceInfo +
                '}';
    }
}
