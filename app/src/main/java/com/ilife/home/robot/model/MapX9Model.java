package com.ilife.home.robot.model;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ilife.home.robot.bean.CleaningDataX8;
import com.ilife.home.robot.contract.MapX9Contract;
import com.ilife.home.robot.respository.HistoryMapX8Respository;

/**
 *for store data
 */
public class MapX9Model extends ViewModel implements MapX9Contract.Model {
    private MutableLiveData<CleaningDataX8> historyLiveData;

    public MapX9Model() {
        historyLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<CleaningDataX8> getHistoryLiveData() {
        return historyLiveData;
    }

    public void queryHistoryData(long mapStartTime) {
        HistoryMapX8Respository historyMapX8Respository=new HistoryMapX8Respository();
        historyMapX8Respository.getHistoryData(historyLiveData,mapStartTime);
    }
}
