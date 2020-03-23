package com.ilife.home.robot.bean;

import com.airbnb.lottie.L;

import java.util.List;

public class MapDataBean {
    private List<Coordinate> coordinates;
    private String virtualWall;
    private String forbiddenArea;
    private int leftX,leftY;
    private int minX,minY,maxX,maxY;

    public MapDataBean(List<Coordinate> coordinates, String virtualWall, String forbiddenArea, int leftX, int leftY, int minX, int minY, int maxX, int maxY) {
        this.coordinates = coordinates;
        this.virtualWall = virtualWall;
        this.forbiddenArea = forbiddenArea;
        this.leftX = leftX;
        this.leftY = leftY;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public String getVirtualWall() {
        return virtualWall;
    }

    public void setVirtualWall(String virtualWall) {
        this.virtualWall = virtualWall;
    }

    public String getForbiddenArea() {
        return forbiddenArea;
    }

    public void setForbiddenArea(String forbiddenArea) {
        this.forbiddenArea = forbiddenArea;
    }

    public int getLeftX() {
        return leftX;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public int getLeftY() {
        return leftY;
    }

    public void setLeftY(int leftY) {
        this.leftY = leftY;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }
}
