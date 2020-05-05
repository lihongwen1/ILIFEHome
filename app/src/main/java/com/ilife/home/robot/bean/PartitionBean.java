package com.ilife.home.robot.bean;

import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

import java.util.List;

public class PartitionBean implements Comparable<PartitionBean>{
    private int partitionId;
    private int x;
    private int y;
    private String tag;
    private Region region;
    private RectF tagIcon;
    public PartitionBean(int partitionId, int x, int y) {
        this.partitionId = partitionId;
        this.x = x;
        this.y = y;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public RectF getTagIcon() {
        return tagIcon;
    }

    public void setTagIcon(RectF tagIcon) {
        this.tagIcon = tagIcon;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int compareTo(PartitionBean o) {
        return partitionId-o.getPartitionId();
    }
}
