package com.ilife.home.robot.model.bean;

public class CleanningRobot {
    private int img;
    private String name;

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

    public CleanningRobot(int img, String name) {
        this.img = img;
        this.name = name;
    }
}
