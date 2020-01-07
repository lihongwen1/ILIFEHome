package com.ilife.home.robot.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;

public class ConsumerModel extends ViewModel {
    private MutableLiveData<String> consumeData=new MutableLiveData<>();

    public MutableLiveData<String> getConsumeData() {
        return consumeData;
    }
    public void queryConsumer() {
        IlifeAli.getInstance().queryConsumer(new OnAliResponse<String>() {
            @Override
            public void onSuccess(String response) {
               consumeData.setValue(response);
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }
}
