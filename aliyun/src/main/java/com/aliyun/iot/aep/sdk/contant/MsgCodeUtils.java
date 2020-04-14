package com.aliyun.iot.aep.sdk.contant;

/**
 * Created by chenjiaping on 2017/8/3.
 */

public class MsgCodeUtils {
    //下发
    public static final int WorkMode = 0x46;
    public static final int RoomMode = 0x47;
    public static final int CleanForce = 0x48;
    public static final int Proceed = 0x49;
    public static final int RestLifeTime = 0x4B;
    public static final int NoDisturbing = 0x4E;
    public static final int FactoryReset = 0x4F;
    public static final int SetVirtualWall = 0x53;//下发电子墙
    public static final int CheckMachineInfo = 0x54;//获取主机固件信息
    public static final int DeviceUpdate = 0x55;//下发升级
    //下发预约指令
    public static final int Appointment = 0x4A;
    //下发上传实时信息指令
    public static final int UPLOADMSG = 0x4D;
    public static final int AdjustTime = 0x4C;

    //查询
    public static final int DevStatus = 0x41;
    public static final int MatConditions = 0x44;
    public static final int ClockInfos = 0x42;
    public static final int HistoryRecord = 0x43;
    public static final int QueryVirtualWall = 0x45;//查询电子墙

    //模式
    public static final int STATUE_POINT = 0x05;
    public static final int STATUE_ALONG = 0x04;
    public static final int STATUE_SLEEPING = 0x01;
    public static final int STATUE_OFF_LINE = 0x00;
    public static final int STATUE_WAIT = 0x02;
    public static final int STATUE_RANDOM = 0x03;
    public static final int STATUE_PLANNING = 0x06;
    public static final int STATUE_VIRTUAL_EDIT = 0x07;
    public static final int STATUE_RECHARGE = 0x08;
    public static final int STATUE_CHARGING = 0x09;
    public static final int STATUE_REMOTE_CONTROL = 0x0A;
    public static final int STATUE_CHARGING_ = 0x0B;
    public static final int STATUE_PAUSE = 0x0C;
    public static final int STATUE_TEMPORARY_POINT = 0x0D;
    public static final int STATUE_OTA = 0x0E;
    public static final int STATUE_CLEAN_AREA_EDIT = 0x0F;//划区清扫编辑模式
    public static final int STATUE_CHARGING_BASE_SLEEP = 0X10;//充电座休眠
    public static final int STATUE_CHARGING_ADAPTER_SLEEP = 0X11;//适配器休眠
    public static final int STATUE_CLEAN_AREA = 0x13;
    public static final int STATUE_CLEAN_ROOM = 0x14;
    //遥控器
    public static final int PROCEED_NO_RESPONSE = 0x00;
    public static final int PROCEED_FORWARD = 0x01;
    public static final int PROCEED_BACK = 0x02;
    public static final int PROCEED_LEFT = 0x03;
    public static final int PROCEED_RIGHT = 0x04;
    public static final int PROCEED_PAUSE = 0x05;
    //清洁
    public static final int CLEANNING_CLEANING_NORMAL = 0x00;
    public static final int CLEANNING_CLEANING_MAX = 0x01;

    //VIRTUAL_WALL
    public static final int VIRTUAL_WALL_OPEN = 0x01;
    public static final int VIRTUAL_WALL_CLOSE = 0x00;


}
