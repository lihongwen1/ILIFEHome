package com.ilife.home.robot.model;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.ilife.home.robot.bean.CleaningDataX8;
import com.ilife.home.robot.contract.MapX9Contract;
import com.ilife.home.robot.respository.HistoryMapX8Respository;

/**
 *for store data
 */
public class MapX9Model extends ViewModel implements MapX9Contract.Model {
    public void queryHistoryData(long mapStartTime, OnAliResponseSingle<CleaningDataX8> onResponse) {
        HistoryMapX8Respository historyMapX8Respository=new HistoryMapX8Respository();
        historyMapX8Respository.getHistoryData(onResponse,mapStartTime);
    }
}
