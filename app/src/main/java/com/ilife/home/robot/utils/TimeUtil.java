package com.ilife.home.robot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by chenjiaping on 2017/8/8.
 */

public class TimeUtil {
    private static final String TAG = TimeUtil.class.getSimpleName();
    private static final String FORMAT = "HH:mm";

    public static void getExitWeekList(ArrayList<Byte> exitWeekList, byte b) {
        if (exitWeekList == null) {
            return;
        }
        if ((b & 0x01) == 0x01) {
            if (!exitWeekList.contains((byte) 0x01)) {
                exitWeekList.add((byte) 0x01);
            }
        }

        if ((b & 0x02) == 0x02) {
            if (!exitWeekList.contains((byte) 0x02)) {
                exitWeekList.add((byte) 0x02);
            }
        }

        if ((b & 0x04) == 0x04) {
            if (!exitWeekList.contains((byte) 0x04)) {
                exitWeekList.add((byte) 0x04);
            }
        }

        if ((b & 0x08) == 0x08) {
            if (!exitWeekList.contains((byte) 0x08)) {
                exitWeekList.add((byte) 0x08);
            }
        }

        if ((b & 0x10) == 0x10) {
            if (!exitWeekList.contains((byte) 0x10)) {
                exitWeekList.add((byte) 0x10);
            }
        }

        if ((b & 0x20) == 0x20) {
            if (!exitWeekList.contains((byte) 0x20)) {
                exitWeekList.add((byte) 0x20);
            }
        }

        if ((b & 0x40) == 0x40) {
            if (!exitWeekList.contains(0x40)) {
                exitWeekList.add((byte) 0x40);
            }
        }

        if ((b & 0x80) == 0x80) {
            if (!exitWeekList.contains((byte) 0x80)) {
                exitWeekList.add((byte) 0x80);
            }
        }
    }

    //将data字节型数据转换为0~255
    public static int getHour(byte data) {
        return data & 0x0FF;
    }

    public static int getMinute(byte data) {
        return data & 0x0FF;
    }

    public static String Local2UTC(byte hour, byte minute) {
        SimpleDateFormat utcSdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat localSdf = new SimpleDateFormat("HH:mm");
        utcSdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        localSdf.setTimeZone(TimeZone.getDefault());
        String gmtTime = null;
        try {
            gmtTime = utcSdf.format(localSdf.parse(hour + ":" + minute));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return gmtTime;
    }

    //将日期字符串转换为Date对象
    public static Date parseDate(String date) {
        Date dt = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT);
        try {
            dt = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    //获取更改时区后的时间
    public static Date changeTimeZone(String str, TimeZone oldZone, TimeZone newZone) {
        Date date = parseDate(str);
        Date dateTmp = null;
        if (date != null) {
            int timeOffset = oldZone.getRawOffset() - newZone.getRawOffset();
            dateTmp = new Date(date.getTime() - timeOffset);
        }
        return dateTmp;
    }

    //获取预约时间的格式
    public static String genExistTime(byte hour, byte minute) {
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String time = strHour + ":" + strMinute;   //hh:mm

        Date date = TimeUtil.changeTimeZone(time, TimeZone.getTimeZone("GMT"), TimeZone.getDefault());
        int newHour = date.getHours();
        int newMinute = date.getMinutes();
        String newStrHour = newHour < 12 ? "0" + newHour : "" + newHour;
        String newStrMinute = newMinute < 12 ? "0" + newMinute : "" + newMinute;
        return newStrHour + ":" + newStrMinute;
    }

    public static String genExistTime_(byte hour, byte minute) {
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String time = strHour + ":" + strMinute;   //hh:mm
        return time;
    }

    public static int getDifferMinutes() {
        int differ = (int) (getMinuteTime(TimeZone.getDefault()) - getMinuteTime(TimeZone.getTimeZone("GMT")));
//        MyLogger.e(TAG, "getDifferMinutes:  相差的分钟数为"+differ);
        return differ;
    }

    public static long getMinuteTime(TimeZone timeZone) {
        long minute = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(timeZone);
        String time = sdf.format(new Date());

        SimpleDateFormat sdf_local = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = sdf_local.parse(time);
            minute = date.getTime() / 1000 / 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return minute;
    }

    public static int getLocalTime(int hour, int minute) {
        return hour * 60 + minute;
    }

    public static byte[] getTimeBytes() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            dayOfWeek = 7;
        } else {
            dayOfWeek = dayOfWeek - 1;
        }
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        MyLogger.d(TAG,dayOfMonth+"---"+dayOfWeek+"---"+hour+"---"+minute+"--"+second);
        byte h_year = (byte) ((year & 0xff00) >> 8);
        byte l_year = (byte) (year & 0x00ff);

        byte[] bytes = new byte[]{h_year, l_year, (byte) month, (byte) dayOfMonth,
                (byte) dayOfWeek, (byte) hour, (byte) minute, (byte) second};
        return bytes;
    }

    public static byte getWeeks(int position) {
        byte b = 0x01;
        switch (position) {
            case 0:
                b = 0x40;
                break;
            case 1:
                b = 0x01;
                break;
            case 2:
                b = 0x02;
                break;
            case 3:
                b = 0x04;
                break;
            case 4:
                b = 0x08;
                break;
            case 5:
                b = 0x10;
                break;
            case 6:
                b = 0x20;
                break;
        }
        return b;
    }
}
