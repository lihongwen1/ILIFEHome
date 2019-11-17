package com.ilife.home.robot.model.bean;

public class SlamLineBean {
    private int type;
    private int startX;
    private int endX;

    public SlamLineBean(int type, int startX, int endX) {
        this.type = type;
        this.startX = startX;
        this.endX = endX;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }
}
