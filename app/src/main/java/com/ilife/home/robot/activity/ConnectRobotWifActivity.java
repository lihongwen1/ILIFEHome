package com.ilife.home.robot.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.WifiUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ConnectRobotWifActivity extends BackBaseActivity {
    @BindView(R.id.et_robot_wifi)
    EditText et_robot_wifi;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_binding_device)
    Button bt_binding_device;
    private boolean isFirstOnresume =true;
    @Override
    public int getLayoutId() {
        return R.layout.activity_connect_robot_wifi;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.guide_ap_prepare);
    }

    @OnClick({R.id.bt_binding_device, R.id.tv_setting})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.bt_binding_device:
                String ap_ssid = et_robot_wifi.getText().toString();
                if (TextUtils.isEmpty(ap_ssid) || !ap_ssid.startsWith("adh")) {
                    ToastUtils.showToast(this, getString(R.string.third_ap_aty_port_));
                } else {
                    Intent intent=new Intent(this, ApWifiActivity.class);
                    intent.putExtra(ApWifiActivity.EXTAR_ROBOT_SSID,ap_ssid);
                    startActivity(intent);
                    removeActivity();
                }
                break;
            case R.id.tv_setting:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String ssid = WifiUtils.getSsid(this);
        if (ssid != null && !ssid.contains("unknown")&&ssid.startsWith("adh")) {
            et_robot_wifi.setText(ssid);
            bt_binding_device.setClickable(true);
            bt_binding_device.setSelected(true);
        }
        if (!isFirstOnresume){
            bt_binding_device.callOnClick();
        }else {
            isFirstOnresume =false;
        }
    }
}
