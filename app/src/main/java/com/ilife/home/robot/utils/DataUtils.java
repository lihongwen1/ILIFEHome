package com.ilife.home.robot.utils;


import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Base64;
import android.view.MotionEvent;

import com.ilife.home.robot.R;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    public static int byteToIntF(byte high, byte low) {
        int value;
        value = (int) (((high & 0xFF) << 8)
                | (low & 0xFF));

        if ((high & 0x80) == 0x80) {
            value = value - 65536;
        }
        return value;
    }


    //高位
    public static int bytesToInt(byte h, byte l) {
        int value;
        value = (int) (((h & 0xFF) << 8)
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

    /**
     * 字节数组转int
     *
     * @param src 原始字节数组，要求数组长度需为4
     * @return
     */
    public static int bytesToInt(byte[] src) {
        int value1 = src[0] << 24 & 0xff000000;
        int value2 = src[1] << 16 & 0xff0000;
        int value3 = src[2] << 8 & 0xff00;
        int value4 = src[3] & 0xff;
        return value1 + value2 + value3 + value4;
    }

    /**
     * 将int类型的数据转换为byte数组
     *
     * @param n int数据
     * @return 生成的byte数组，长度为4
     */
    public static byte[] intToBytes4(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
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


    /**
     * int转byte数组
     *
     * @param value
     * @return 长度为2的字节数组
     */
    public static byte[] intToBytes(int value) {
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
     *
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
     *
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
     *
     * @param centerP  旋转中心坐标
     * @param preMoveP 开始旋转按下坐标
     * @param curMoveP 结束旋转按下坐标
     * @return 旋转的角度
     */
    public static float getAngle(PointF centerP, PointF preMoveP, PointF curMoveP) {
        double a = DataUtils.distance2PointF(centerP, preMoveP);
        double b = DataUtils.distance2PointF(preMoveP, curMoveP);
        double c = DataUtils.distance2PointF(centerP, curMoveP);
        //a==c？
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
     * 矩阵【x1,y1,x2,y2】
     * 中心点坐标为【(x1+x2)/2,(y1+y2)/2】
     * 计算旋转之后的坐标
     *
     * @param center 矩形中点坐标
     * @param source 源顶点坐标
     * @param degree 旋转角度
     * @return 源顶点旋转之后的坐标
     */
    public static Point calculateRoationPoint(Point center, Point source, float degree) {
        PointF disPoint = new PointF();
        disPoint.x = source.x - center.x;
        disPoint.y = source.y - center.y;
        double orgRadian = 0;//旋转前弧度
        double orgDegree = 0;//旋转前角度
        double aftRadian = 0; //旋转后弧度
        double aftDegree = 0;//旋转后角度
        //经过旋转之后点的坐标
        Point resultPoint = new Point();
        double distance = Math.sqrt(disPoint.x * disPoint.x + disPoint.y * disPoint.y);//顶点到中心点的距离

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

    public static MapDataBean parseSaveMapData(String[] mapArray) {
        int minX, minY, maxX, maxY;
        List<Coordinate> pointList = new ArrayList<>();
        int lineCount = 0;
        List<Byte> byteList = new ArrayList<>();
        int leftX = 0, leftY = 0;
        if (mapArray != null) {
            if (mapArray.length > 0) {
                for (String data : mapArray) {
                    if (data == null) {
                        continue;
                    }
                    byte[] bytes = Base64.decode(data, Base64.DEFAULT);
                    int bj = bytes[0] & 0xff;
                    if (bj == 1) {//map数据
                        leftX = DataUtils.bytesToInt(new byte[]{bytes[1], bytes[2]}, 0);
                        leftY = DataUtils.bytesToInt(new byte[]{bytes[3], bytes[4]}, 0);
                        lineCount = DataUtils.bytesToInt(new byte[]{bytes[5], bytes[6]}, 0);
                        for (int j = 7; j < bytes.length; j++) {
                            byteList.add(bytes[j]);
                        }
                    }
                }
            }
        }
        Coordinate coordinate;
        if (byteList.size() > 0) {
            int xMax = 0, yMax = 0;
            int x = 0, y = 0, type = 0, length = 0;
            for (int i = 2; i < byteList.size(); i += 3) {
                type = byteList.get(i - 1) & 0xff;
                length = byteList.get(i) & 0xff;
                for (int j = 0; j < length; j++) {
                    if (type != 0) {
                        if (x > xMax) {
                            xMax = x;
                        }
                        if (y > yMax) {
                            yMax = y;
                        }
                        coordinate = new Coordinate(x + leftX, y - leftY, type);
                        pointList.add(coordinate);
                    }
                    if (x < lineCount - 1) {
                        x++;
                    } else {
                        x = 0;
                        y++;
                    }

                }
            }
            minX = leftX;
            maxX = xMax + leftX;
            minY = -leftY;
            maxY = yMax - leftY;
            MapDataBean bean = new MapDataBean(pointList, leftX, leftY, minX, minY, maxX, maxY, "");
            return bean;
        }
        return null;
    }

    public static int judgedVirAndCharge(int sx, int sy, int ex, int ey, Point point) {//判断虚拟墙和圆的位置关系
        double judDis = 0;
        double k = (ey - sy) * 1.0 / (ex - sx);//虚拟墙斜率
        double bn = ey - k * ex;//虚拟墙的截距b
        //垂足坐标
        double chuiX = (k * k * sx + k * (point.y - sy) + point.x) / (k * k + 1);
        double chuiY = k * (chuiX - sx) + sy;
        int cx = (int) chuiX;
        int cy = (int) chuiY;
        if ((Math.min(sx, ex) < cx && cx < Math.max(sx, ex)) || (Math.min(sy, ey) < cy && cy < Math.max(sy, ey))) {
            judDis = Math.sqrt((point.x - chuiX) * (point.x - chuiX) + (point.y - chuiY) * (point.y - chuiY));
        } else {
            double p1dis = Math.sqrt((point.x - sx) * (point.x - sx) + (point.y - sy) * (point.y - sy));
            double p2dis = Math.sqrt((point.x - ex) * (point.x - ex) + (point.y - ey) * (point.y - ey));
            judDis = p1dis > p2dis ? p2dis : p1dis;
        }
        return (int) judDis;
    }

    /**
     * b为传入的字节，i为第几位（范围0-7），如要获取bit0，则i=0
     *
     * @param b
     * @param i
     * @return
     */
    public static int getBit(byte b, int i) {
        int bit = (int) ((b >> i) & 0x1);
        return bit;
    }

    /**
     * 设置src中的第index位为1
     *
     * @param src
     * @param index
     */
    public static int setBitTo1(int src, int index) {
        int a = 1 << index;
        return src | a;
    }

    public static String getScheduleWeek(int week) {
        StringBuilder weekStr = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (DataUtils.getBit((byte) week, i) == 1) {
                switch (i) {
                    case 0:
                        weekStr.append(UiUtil.getString(R.string.week_monday));
                        break;
                    case 1:
                        weekStr.append(UiUtil.getString(R.string.week_tuesday));
                        break;
                    case 2:
                        weekStr.append(UiUtil.getString(R.string.week_wednesday));
                        break;
                    case 3:
                        weekStr.append(UiUtil.getString(R.string.week_thursday));
                        break;
                    case 4:
                        weekStr.append(UiUtil.getString(R.string.week_friday));
                        break;
                    case 5:
                        weekStr.append(UiUtil.getString(R.string.week_saturday));
                        break;
                    case 6:
                        weekStr.append(UiUtil.getString(R.string.week_sunday));
                        break;
                }
            }
        }
        return weekStr.toString();
    }

    public static String getScheduleTimes(int times) {
        String value="";
        switch (times) {
            case 0:
            case 1:
                value=UiUtil.getString(R.string.schedule_onece);
                break;
            case 2:
                value=UiUtil.getString(R.string.schedule_twice);
                break;
            case 3:
                value=UiUtil.getString(R.string.schedule_third);
                break;
        }
        return value;
    }
}
