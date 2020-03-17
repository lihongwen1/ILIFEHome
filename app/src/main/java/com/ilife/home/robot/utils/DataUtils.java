package com.ilife.home.robot.utils;


import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by chenjiaping on 2017/8/7.
 */

public class DataUtils {
    public static ArrayList<Integer> intToHex(byte[] byte_receive) {                             //好像只要是整形就可以
        ArrayList<Integer> hexList = new ArrayList<>();
        for (int i = 4; i < byte_receive.length; i++) {                                             //这里除去了4为长度
            int hex_int = byte_receive[i] & 0xFF;
            hexList.add(hex_int);
        }
        return hexList;
    }


    //高位
    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 8)
                | (src[offset + 1] & 0xFF));
        return value;
    }

    public static int byteToIntF(byte high,byte low){
        int value;
        value = (int) (((high & 0xFF) << 8)
                | (low & 0xFF));

        if ((high & 0x80) == 0x80) {
            value = value - 65536;
        }
        return value;
    }


    //高位
    public static int bytesToInt(byte h,byte l) {
        int value;
        value = (int) (((h& 0xFF) << 8)
                | (l & 0xFF));

        if ((h & 0x80) == 0x80) {
            value = value - 65536;
        }
        return value;
    }

    //高位
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 8)
                | (src[offset + 1] & 0xFF));

        if ((src[offset] & 0x80) == 0x80) {
            value = value - 65536;
        }
        return value;
    }

    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;
        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    public static byte[] intToBytes(int value) {//电子墙坐标转byte数组
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    public static PointF midPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }



    public static int bytesToUInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 8)
                | (src[offset + 1] & 0xFF));
        return value;
    }

    /**
     * 虚拟墙，禁区 坐标数据处理
     */


    /**
     * 两点之间的距离 （x1,y1）and （x2,y2）
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 两点之间的距离
     * @param pf1
     * @param pf2
     * @return
     */
    public static float distance2PointF(PointF pf1, PointF pf2) {
        float disX = pf2.x - pf1.x;
        float disY = pf2.y - pf1.y;
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    /**
     * 计算旋转角度/选择方式是围绕中心旋转
     * @param centerP 旋转中心坐标
     * @param preMoveP 开始旋转按下坐标
     * @param curMoveP 结束旋转按下坐标
     * @return 旋转的角度
     */
    public static float getAngle(PointF centerP, PointF preMoveP, PointF curMoveP){
        double a = DataUtils.distance2PointF(centerP, preMoveP);
        double b = DataUtils.distance2PointF(preMoveP, curMoveP);
        double c = DataUtils.distance2PointF(centerP, curMoveP);
        double cosb = (a * a + c * c - b * b) / (2 * a * c);//夹角的余弦值
        if (cosb >= 1) {
            cosb = 1f;
        }
        double radian = Math.acos(cosb);//弧度（反余弦函数）
        float degree = (float) Math.toDegrees(radian);//弧度转角度
        //centerP-->preMoveP的向量
        PointF centerTopreMoveP = new PointF((preMoveP.x - centerP.x), (preMoveP.y - centerP.y));
        //centerP-->curMoveP的向量
        PointF centerTocurMoveP = new PointF((curMoveP.x - centerP.x), (curMoveP.y - centerP.y));
        //向量叉乘结果,负数为逆时针,正数为顺时针
        float result = centerTopreMoveP.x * centerTocurMoveP.y - centerTopreMoveP.y * centerTocurMoveP.x;
        if (result < 0) {
            degree = -degree;
        }
        return degree;
    }

    /**
     * 计算旋转之后的坐标
     * @param center 矩形中点坐标
     * @param source 源顶点坐标
     * @param degree 旋转角度
     * @return 源顶点旋转之后的坐标
     */
    public static Point obtainRoationPoint(Point center, Point source, float degree) {
        PointF disPoint=new PointF();
        disPoint.x = source.x - center.x;
        disPoint.y = source.y - center.y;
        double orgRadian = 0;//旋转前弧度
        double orgDegree = 0;//旋转前角度
        double aftRadian = 0; //旋转后弧度
        double aftDegree = 0;//旋转后角度
        //经过旋转之后点的坐标
        Point resultPoint = new Point();
        double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);//顶点到中点的距离

        if (disPoint.x == 0 && disPoint.y == 0) {
            return center;
        } else if (disPoint.x >= 0 && disPoint.y >= 0) {//第一象限
            orgRadian = Math.asin(disPoint.y / distance);//反正弦函数
        } else if (disPoint.x < 0 && disPoint.y >= 0) {//第二象限
            orgRadian = Math.asin(Math.abs(disPoint.x) / distance);
            orgRadian = orgRadian + Math.PI / 2;
        } else if (disPoint.x < 0 && disPoint.y < 0) {//第三象限
            orgRadian = Math.asin(Math.abs(disPoint.y) / distance);
            orgRadian = orgRadian + Math.PI;
        } else if (disPoint.x >= 0 && disPoint.y < 0) {//第四象限
            orgRadian = Math.asin(disPoint.x / distance);
            orgRadian = orgRadian + Math.PI * 3 / 2;
        }
        //弧度换算成角度
        orgDegree = Math.toDegrees(orgRadian);
        aftDegree = orgDegree + degree;
        //角度转弧度
        aftRadian = Math.toRadians(aftDegree);
        resultPoint.x = (int) Math.round(distance * Math.cos(aftRadian));//四舍五入
        resultPoint.y = (int) Math.round(distance * Math.sin(aftRadian));
        resultPoint.x += center.x;
        resultPoint.y += center.y;
        return resultPoint;
    }



}
