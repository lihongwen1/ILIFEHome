package com.ilife.home.livebus.ipc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.livebus.ipc.IpcConst;
import com.ilife.home.livebus.ipc.decode.IDecoder;
import com.ilife.home.livebus.ipc.decode.ValueDecoder;
import com.ilife.home.livebus.ipc.json.JsonConverter;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class LebIpcReceiver extends BroadcastReceiver {

    private IDecoder decoder;

    public LebIpcReceiver(JsonConverter jsonConverter) {
        this.decoder = new ValueDecoder(jsonConverter);
    }

    public void setJsonConverter(JsonConverter jsonConverter) {
        this.decoder = new ValueDecoder(jsonConverter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (IpcConst.ACTION.equals(intent.getAction())) {
            try {
                String key = intent.getStringExtra(IpcConst.KEY);
                Object value = decoder.decode(intent);
                if (key != null) {
                    LiveEventBus
                            .get(key)
                            .post(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
