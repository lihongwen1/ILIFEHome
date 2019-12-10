package com.ilife.home.robot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.base.BaseActivity;
import com.ilife.home.robot.utils.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class BindFailActivity extends BaseActivity {
    @Override
    protected boolean canGoBack() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_fail;
    }

    @Override
    public void initView() {
    }

    @OnClick(R.id.bt_retry)
    public void onclick(View v) {
        if (v.getId() == R.id.bt_retry) {
            Intent intent = new Intent(this, ConnectHomeWifiActivity.class);
            startActivity(intent);
            removeActivity();
        }
    }
}
