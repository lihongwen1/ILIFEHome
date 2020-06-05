package com.ilife.home.robot.utils;


import android.graphics.Point;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.google.android.material.tabs.TabLayout;
import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.bean.Coordinate;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.model.bean.VirtualWallBean;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * 已保存地图的slam，road，obstacle等数据
     *
     * @param mapArray
     * @return
     */
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
            int x = 0, y = 0, type = 0, length = 0;
            for (int i = 2; i < byteList.size(); i += 3) {
                type = byteList.get(i - 1) & 0xff;
                length = byteList.get(i) & 0xff;
                for (int j = 0; j < length; j++) {
                    if (type != 0) {
                        if (type == 3) {//已保存地图中的门数据忽略掉,改变为已清扫
                            type = 1;
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
            /**
             * 计算坐标边界
             */
            coordinate = pointList.get(0);
            minX = coordinate.getX();
            minY = coordinate.getY();
            maxX = coordinate.getX();
            maxY = coordinate.getY();
            for (int i = 0; i < pointList.size(); i++) {
                coordinate = pointList.get(i);
                x = coordinate.getX();
                y = coordinate.getY();
                if (minX > x) {
                    minX = x;
                }
                if (maxX < x) {
                    maxX = x;
                }
                if (minY > y) {
                    minY = y;
                }
                if (maxY < y) {
                    maxY = y;
                }
            }
            MapDataBean bean = new MapDataBean(pointList, leftX, leftY, minX, minY, maxX, maxY, "");
            return bean;
        }
        return null;
    }

    /**
     * 充电座位置(4bytes)+
     * 房间数(1byte)+
     * 房间 1ID(4bytes)+房间 1 坐标(4bytes)+ N
     * 房间 1 墙的坐标数(2bytes)+房间墙坐标
     * (4*n bytes)+…+
     * 门条数(1byte)+
     * 门 1ID(1byte)+门 1 坐标(8byte)+…+
     * 虚拟墙条数(1byte)+
     * 虚拟墙 1 坐标(8byte)+…+
     * 禁区条数(1byte)+
     * 禁区 1 类型(1byte)+禁区 1 坐标
     * (4*4byte)
     *
     * @param data
     */
    public static SaveMapDataInfoBean parseSaveMapInfo(String[] data) {
        SaveMapDataInfoBean saveMapDataInfoBean=null;
        try {
            List<byte[]> bytesList = new ArrayList<>();
            int bytesNumber = 0;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != null) {
                    byte[] bytes = Base64.decode(data[i], Base64.DEFAULT);
                    bytesNumber += bytes.length;
                    bytesList.add(bytes);
                }
            }
            byte[] allBytes = new byte[bytesNumber];
            int desPos = 0;
            for (byte[] b : bytesList) {
                System.arraycopy(b, 0, allBytes, desPos, b.length);
                desPos += b.length;
            }
            int index = 0;
            int chargeX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int chargeY = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
            index += 2;
            int roomNumber = allBytes[index] & 0xff;
            index++;
            List<PartitionBean> rooms = new ArrayList<>();
            PartitionBean room;
            List<Coordinate> wallCoordinate;
            for (int i = 0; i < roomNumber; i++) {
                int roomId = DataUtils.bytesToInt(new byte[]{allBytes[index], allBytes[index + 1], allBytes[index + 2], allBytes[index + 3]});
                index += 4;
                int roomX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int roomY = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int wallNumber = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                wallCoordinate = new ArrayList<>();
                for (int j = 0; j < wallNumber; j++) {
                    int wallX = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                    index += 2;
                    int wallY = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                    index += 2;
                    wallCoordinate.add(new Coordinate(wallX, wallY, 2));
                }
                room = new PartitionBean(roomId, roomX, roomY);
                room.setWallCoordinates(wallCoordinate);
                rooms.add(room);
            }
            int gateNumber = allBytes[index] & 0xff;
            index++;
            List<VirtualWallBean> gates = new ArrayList<>();
            for (int i = 0; i < gateNumber; i++) {
                int gateId = allBytes[index] & 0xff;
                index++;
                int sx = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int sy = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int ex = DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                int ey = -DataUtils.bytesToInt(allBytes[index], allBytes[index + 1]);
                index += 2;
                gates.add(new VirtualWallBean(gateId, 4, new float[]{sx, sy, ex, ey}, 1));
            }
            saveMapDataInfoBean=new SaveMapDataInfoBean();
            saveMapDataInfoBean.setChargePoint(new Point(chargeX, chargeY));
            saveMapDataInfoBean.setGates(gates);
            saveMapDataInfoBean.setRooms(rooms);
        } catch (Exception e) {
            MyLogger.d("DataUtils","data error ,array index out of bounds exception");
        }
        return saveMapDataInfoBean;
    }

    /**
     * 解析房间名信息
     *
     * @param mapId
     * @param roomInfo
     * @return
     */
    public static boolean parseRoomInfo(String mapId, String roomInfo, HashMap<String, String> roomNames) {
        roomNames.clear();
        if (!TextUtils.isEmpty(roomInfo)) {
            byte[] roomBytes = Base64.decode(roomInfo, Base64.NO_WRAP);
            String str_room = new String(roomBytes);
            MyLogger.d("DataUtils","mapId: "+mapId+"房间信息：   "+str_room);
            String[] str_rooms = str_room.split(",");
            if (str_rooms.length > 0) {
                String str_map_id = str_rooms[0];
                if (str_map_id.equals(mapId)) {
                    int roomNum = (str_rooms.length - 1) / 2;
                    if (roomNum > 0) {
                        for (int i = 0; i < roomNum; i++) {
                            roomNames.put(str_rooms[i * 2 + 1], str_rooms[i * 2 + 2]);
                        }
                    }
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public static double lineToPointSpace(float x1, float y1, float x2, float y2, float x0, float y0) {//判断虚拟墙和圆的位置关系
        double space = 0;

        double a, b, c;

        a = lineSpace(x1, y1, x2, y2);// 线段的长度

        b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离

        c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离

        if (c <= 0.000001 || b <= 0.000001) {

            space = 0;

            return space;

        }

        if (a <= 0.000001) {

            space = b;

            return space;

        }

        if (c * c >= a * a + b * b) {

            space = b;

            return space;

        }

        if (b * b >= a * a + c * c) {

            space = c;

            return space;

        }

        double p = (a + b + c) / 2;// 半周长

        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积

        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）

        return space;
    }

    // 计算两点之间的距离
    public static double lineSpace(float x1, float y1, float x2, float y2) {

        double lineLength = 0;

        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)

                * (y1 - y2));

        return lineLength;

    }

    /**
     * @param src 低位在前，高位在后
     * @return
     */
    public static final int getRoomId(byte[] src) {
        byte b;
        int index = -1;
        for (int i = 0; i < src.length; i++) {
            b = src[i];
            for (int j = 0; j < 8; j++) {
                if (getBit(b, j) == 1) {
                    index = i * 8 + j + 1;
                    return index;
                }
            }
        }
        return index;
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
        int[] bitPosition = new int[]{6, 0, 1, 2, 3, 4, 5, 7};
        int position;
        for (int i = 0; i < bitPosition.length; i++) {
            position = bitPosition[i];
            if (DataUtils.getBit((byte) week, position) == 1) {
                switch (position) {
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
                    case 7:
                        weekStr.append(UiUtil.getString(R.string.schedule_only_once));
                        break;
                }
                weekStr.append(" ");
            }
        }
        return weekStr.toString();
    }

    public static String getScheduleWeekFull(int week) {
        StringBuilder weekStr = new StringBuilder();
        int[] bitPosition = new int[]{6, 0, 1, 2, 3, 4, 5, 7};
        int position;
        for (int i = 0; i < bitPosition.length; i++) {
            position = bitPosition[i];
            if (DataUtils.getBit((byte) week, position) == 1) {
                switch (position) {
                    case 0:
                        weekStr.append(UiUtil.getString(R.string.week_monday_full));
                        break;
                    case 1:
                        weekStr.append(UiUtil.getString(R.string.week_tuesday_full));
                        break;
                    case 2:
                        weekStr.append(UiUtil.getString(R.string.week_wednesday_full));
                        break;
                    case 3:
                        weekStr.append(UiUtil.getString(R.string.week_thursday_full));
                        break;
                    case 4:
                        weekStr.append(UiUtil.getString(R.string.week_friday_full));
                        break;
                    case 5:
                        weekStr.append(UiUtil.getString(R.string.week_saturday_full));
                        break;
                    case 6:
                        weekStr.append(UiUtil.getString(R.string.week_sunday_full));
                        break;
                    case 7:
                        weekStr.append(UiUtil.getString(R.string.schedule_only_once));
                        break;
                }
                weekStr.append(" ");
            }
        }
        return weekStr.toString();
    }

    public static String getScheduleTimes(int times) {
        String value = "";
        switch (times) {
            case 1:
                value = UiUtil.getString(R.string.schedule_onece);
                break;
            case 2:
                value = UiUtil.getString(R.string.schedule_twice);
                break;
            case 3:
                value = UiUtil.getString(R.string.schedule_third);
                break;
        }
        return value;
    }

    public static String getLanguageByCode(int language) {
        language = language - 6;
        String[] languages = MyApplication.getInstance().getResources().getStringArray(R.array.array_voice_language);
        if (language < 0 || language >= languages.length) {
            return languages[0];
        }
        return languages[language];
    }

    /**
     * 格式化清扫区域
     *
     * @param value
     * @return
     */
    public static String formateArea(float value) {
        DecimalFormat df = new DecimalFormat("0.00㎡");
        return df.format(value);
    }

    public static String formatTimeNumber(int hour,int minute) {
        DecimalFormat df_minute = new DecimalFormat("00");
        DecimalFormat df_hour = new DecimalFormat("00");
        return df_hour.format(hour)+":"+df_minute.format(minute);
    }

    /**
     * time 09:00
     * @param time
     * @return
     */
    public static int[] parseTimeString(String time,String split){
        if (!TextUtils.isEmpty(time)&&!TextUtils.isEmpty(split)){
            String[] times=time.split(split);
            if (times.length==2){
                return new int[]{Integer.parseInt(times[0]),Integer.parseInt(times[1])};
            }
        }
        return null;
    }
    public static boolean isLetter(String value) {
        char c = value.charAt(0);
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }
}
