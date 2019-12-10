package com.ilife.home.robot.model.bean;

public class CleanningRobot {
    private int img;
    private String name;
    private String productKey;

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public CleanningRobot(int img, String name, String productKey) {
        this.img = img;
        this.name = name;
        this.productKey = productKey;
    }
}
