package com.ilife.home.robot.utils;


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

    public static float distance(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }

    public static int bytesToUInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 8)
                | (src[offset + 1] & 0xFF));
        return value;
    }
}
