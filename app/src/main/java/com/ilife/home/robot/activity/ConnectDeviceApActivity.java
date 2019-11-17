package com.ilife.home.robot.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.WifiUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 连接设备热点
 */
public class ConnectDeviceApActivity extends BackBaseActivity {
    @BindView(R.id.et_ssid)
    EditText et_ssid;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_connect)
    Button bt_connect;
    private boolean isFirstOnresume =true;
    @Override
    public int getLayoutId() {
        return R.layout.activity_ap_third;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.guide_ap_prepare);
    }

    @OnClick({R.id.bt_connect, R.id.tv_set})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.bt_connect:
                String ap_ssid = et_ssid.getText().toString();
                if (TextUtils.isEmpty(ap_ssid) || !ap_ssid.startsWith("Robot")) {
                    ToastUtils.showToast(this, getString(R.string.third_ap_aty_port_));
                } else {
                    Intent intent=new Intent(this, ApWifiActivity.class);
                    intent.putExtra(ApWifiActivity.EXTAR_ROBOT_SSID,ap_ssid);
                    startActivity(intent);
                    removeActivity();
                }
                break;
            case R.id.tv_set:
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
        if (ssid != null && !ssid.contains("unknown")&&ssid.startsWith("Robot")) {
            et_ssid.setText(ssid);
            bt_connect.setClickable(true);
            bt_connect.setSelected(true);
        }
        if (!isFirstOnresume){
              bt_connect.callOnClick();
        }else {
              isFirstOnresume =false;
        }
    }
}
