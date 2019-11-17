package com.ilife.home.robot.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;


/**
 * Created by chenjiaping on 2017/7/20.
 */

public class FirstActivity extends BaseActivity {
    private final String TAG = FirstActivity.class.getSimpleName();
    private final int GOTOMAIN = 0x11;
    @BindView(R.id.iv_launcher)
    ImageView iv_launcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                removeActivity();
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.avtivity_first;
    }

    @Override
    public void initView() {
        if (Utils.isChinaEnvironment()) {
            iv_launcher.setImageResource(R.drawable.launcher_page_zh);
        } else {
            iv_launcher.setImageResource(R.drawable.launcher_page);
        }
    }

    private WeakHandler handler = new WeakHandler(msg -> {
        switch (msg.what) {
            case GOTOMAIN:
                gotoMain();
                break;
        }
        return false;
    });

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(aBoolean -> {
            if (aBoolean) {
                handler.sendEmptyMessageDelayed(GOTOMAIN, 1000);
            } else {
                ToastUtils.showToast(this, getString(R.string.access_location));
                //未授权处理
            }
        }).dispose();
    }

    public void gotoMain() {
        IlifeAli.getInstance().login(new OnAliResponse<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Intent i;
                i = new Intent(FirstActivity.this, MainActivity.class);
                startActivity(i);
                removeActivity();
            }


            @Override
            public void onFailed(int code, String message) {
                removeALLActivity();
            }
        });

    }

}
