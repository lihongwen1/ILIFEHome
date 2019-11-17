package com.ilife.home.robot.model.bean;

public class AreaContact {
    private String phone1;
    private String phone2;
    private String serverTime1;
    private String serverTime2;
    private String email;

    public AreaContact(String phone1, String phone2, String serverTime1, String serverTime2, String email) {
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.serverTime1 = serverTime1;
        this.serverTime2 = serverTime2;
        this.email = email;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public String getServerTime1() {
        return serverTime1;
    }

    public String getServerTime2() {
        return serverTime2;
    }

    public String getEmail() {
        return email;
    }
}
