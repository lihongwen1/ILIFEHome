package com.ilife.home.robot.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

/**
 * Created by chengjiaping on 2018/9/20.
 */

public class FlashUtils {
    @SuppressLint("NewApi")
    public static void changeFlashStatus(Context context, CameraManager mCameraManager, boolean open) {
        try {
            //获取CameraManager
//            CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            //获取当前手机所有摄像头设备ID
            String[] ids = mCameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                //查询该摄像头组件是否包含闪光灯
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                /*
                 * 获取相机面对的方向
                 * CameraCharacteristics.LENS_FACING_FRONT 前置摄像头
                 * CameraCharacteristics.LENS_FACING_BACK 后只摄像头
                 * CameraCharacteristics.LENS_FACING_EXTERNAL 外部的摄像头
                 */
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null && flashAvailable
                        && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    //打开或关闭手电筒
                    mCameraManager.setTorchMode(id, open ? true : false);
                }
            }

        } catch (CameraAccessException e) {
            MyLogger.e("FlashUtils", e.toString());
            e.printStackTrace();
        }
    }
}
