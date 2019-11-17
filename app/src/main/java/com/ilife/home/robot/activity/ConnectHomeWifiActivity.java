package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UserUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.WifiUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by c on 2017/7/15.
 */
//Done
public class ConnectHomeWifiActivity extends BackBaseActivity {
    private final String TAG = ConnectHomeWifiActivity.class.getSimpleName();
    public static final String EXTRA_SSID = "EXTRA_SSID";
    public static final String EXTRA_PASS = "EXTRA_PASS";
    Context context;
    @BindView(R.id.image_show_pass)
    ImageView image_show;

    @BindView(R.id.tv_ssid)
    TextView tv_ssid;
    @BindView(R.id.et_pass)
    EditText et_pass;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.bt_next)
    Button bt_next;

    String ssid;
    String pass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        checkGps();
    }

    public void initData() {
        context = this;
    }

    private void checkGps() {
        // 用户已经同意该权限
        String ssid = WifiUtils.getSsid(context);
        if (!TextUtils.isEmpty(ssid) && !ssid.contains("unknown")) {
            tv_ssid.setText(ssid);
        }
        if (!checkGpsIsOpen()) {
            goSetGps();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_connect_home_wifi;
    }

    @Override
    public void initView() {
        Utils.setTransformationMethod(et_pass, false);
        tv_title.setText(R.string.ap_wifi_guide);
        bt_next.setSelected(false);
        bt_next.setClickable(false);
        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() >= 8) {
                    bt_next.setSelected(true);
                    bt_next.setClickable(true);
                } else {
                    bt_next.setSelected(false);
                    bt_next.setClickable(false);
                }

            }
        });
    }

    @OnClick({R.id.image_show_pass, R.id.bt_next, R.id.rl_select_wifi})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_show_pass:
                boolean isSelected = !image_show.isSelected();
                int curIndex = et_pass.getSelectionStart();
                image_show.setSelected(isSelected);
                Utils.setTransformationMethod(et_pass, isSelected);
                et_pass.setSelection(curIndex);
                break;
            case R.id.rl_select_wifi:
                Intent i = new Intent();
                i.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(i);
                break;
            case R.id.bt_next:
                if (!checkGpsIsOpen()) {
                    goSetGps();
                    return;
                } else {
                    ssid = tv_ssid.getText().toString();
                    if (TextUtils.isEmpty(ssid) || ssid.contains("unknown")) {
                        ToastUtils.showToast(context, getString(R.string.add_aty_no_wifi));
                        return;
                    }
                }
                pass = et_pass.getText().toString().trim();
                if (TextUtils.isEmpty(pass)) {
                    ToastUtils.showToast(context, getString(R.string.ap_aty_input_pass));
                    return;
                }

                if (!UserUtils.rexCheckPassword(pass)) {
                    ToastUtils.showToast(context, getString(R.string.add_aty_wrong_wifi_pass));
                    return;
                }
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    String bssid = info.getBSSID();
                    if (!TextUtils.isEmpty(bssid)) {
                        if (bssid.startsWith("02")) {
                            goSetGps();
                        } else {
                            SpUtils.put(this, EXTRA_SSID, ssid);
                            SpUtils.put(this, EXTRA_PASS, pass);
                            Intent i_ap = new Intent(context, ApGuideOpenPowerActivity.class);
                            startActivity(i_ap);
                        }
                    }
                }

                break;
        }
    }


    private boolean checkGpsIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void goSetGps() {
        UniversalDialog universalDialog = new UniversalDialog();
        universalDialog.setDialogType(UniversalDialog.TYPE_NORMAL_MID_BUTTON)
                .setCanEdit(false).setHintTip(Utils.getString(R.string.open_gps_location_tip))
                .setMidText(Utils.getString(R.string.ap_aty_setting)).setOnMidButtonClck(() -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1);
        });
        universalDialog.show(getSupportFragmentManager(), "gps");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 用户已经同意该权限
        String ssid = WifiUtils.getSsid(context);
        if (!TextUtils.isEmpty(ssid) && !ssid.contains("unknown")) {
            tv_ssid.setText(ssid);
        }
    }
}
