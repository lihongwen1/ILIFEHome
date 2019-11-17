package com.ilife.home.robot.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.AlertDialogUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.utils.DisplayUtil;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by chengjiaping on 2018/8/13.
 */

public class PersonalActivity extends BackBaseActivity implements View.OnClickListener {
    final String TAG = PersonalActivity.class.getSimpleName();
    int dialog_width, dialog_height, dialog_height_;
    boolean isShow;
    File tempFile;
    String userName;
    String content;
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.tv_userName)
    TextView tv_userName;
    @BindView(R.id.image_forward)
    ImageView image_forward;
    @BindView(R.id.tv_user_email)
    TextView tv_user_email;
    @BindView(R.id.ll_device)
    LinearLayout ll_device;
    IntentIntegrator integrator;
    AlertDialog alertDialog;
    ArrayList<DeviceInfoBean> mDeviceList;
    ArrayList<String> formats;


    @Override
    public int getLayoutId() {
        return R.layout.activity_personal;
    }

    @Override
    public void initView() {
        ((TextView) findViewById(R.id.tv_top_title)).setText(R.string.personal_aty_personal_center);
        findViewById(R.id.ll_title).setBackgroundColor(getResources().getColor(R.color.zxing_transparent));

    }

    public void initData() {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/pic");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        tempFile = new File(dir, "temp.png");
        formats = new ArrayList<>();
        mDeviceList = new ArrayList<>();
        integrator = new IntentIntegrator(this);
        formats.add("CODE_128");
        formats.add("QR_CODE");
        integrator.setDesiredBarcodeFormats(formats);
        integrator.setCaptureActivity(CaptureActivity.class);
        dialog_width = (int) getResources().getDimension(R.dimen.dp_300);
        dialog_height = (int) getResources().getDimension(R.dimen.dp_140);
        dialog_height_ = (int) getResources().getDimension(R.dimen.dp_146);
        String userName = IlifeAli.getInstance().getUserInfo().userNick;
        if (!TextUtils.isEmpty(userName)) {
            tv_userName.setText(userName);
        }
        String version = getVersion();
        if (!TextUtils.isEmpty(version)) {
            tv_version.setText(getString(R.string.personal_aty_version, version, BuildConfig.Area==0?"CN":"OVERSEA"));
        }
        /**
         * user contact information,email or phone number
         */
        String phone = IlifeAli.getInstance().getUserInfo().userPhone;
        if (phone == null || phone.isEmpty()) {
            phone = IlifeAli.getInstance().getUserInfo().userEmail;
        }
        tv_user_email.setText(phone);
    }


    @OnClick({R.id.rl_user_information, R.id.rl_help, R.id.rl_scan, R.id.bt_logout, R.id.rl_protocol, R.id.tv_userName, R.id.rl_share})
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.rl_user_information:
                startActivity(new Intent(PersonalActivity.this, PersonalInformationActivity.class));
                break;
            case R.id.rl_help:
                i = new Intent(context, HelpActivity.class);
                startActivity(i);
                break;
            case R.id.rl_scan:
                new RxPermissions(this).requestEach(Manifest.permission.CAMERA).subscribe(permission -> {
                    if (permission.granted) {
                        Intent i1 = new Intent(context, CaptureActivity.class);
                        startActivityForResult(i1, CaptureActivity.RESULT_CODE_QR_SCAN);
                    } else {
                        ToastUtils.showToast(context, getString(R.string.access_camera));
                    }
                });
                break;
            case R.id.bt_logout:
                showLogoutDialog();
                break;
            case R.id.rl_protocol:
                if (Utils.isIlife()) {
                    i = new Intent(context, ProtocolActivity.class);
                    startActivity(i);
                } else {
                    i = new Intent(context, ZacoProtocolActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.tv_userName:
                showRenameDialog();
                break;
            case R.id.rl_share:
                if (IlifeAli.getInstance().isLogin()) {
                    getOwnerList();
                    if (mDeviceList.size() > 0) {
                        if (!isShow) {
                            showDeviceList();
                        }
                        ll_device.setVisibility(!isShow ? View.VISIBLE : View.GONE);
                        image_forward.setRotation(!isShow ? -90 : 0);
                        isShow = !isShow;
                    } else {
                        ToastUtils.showToast(context, getString(R.string.personal_aty_no_shareable));
                    }
                } else {
                    ToastUtils.showToast(context, getString(R.string.personal_aty_login_first));
                }
                break;
        }
    }

    private void showLogoutDialog() {
        UniversalDialog logoutDialog = new UniversalDialog();
        logoutDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setTitleColor(getResources().getColor(R.color.color_ff4d00)).
                setTitle(Utils.getString(R.string.personal_acy_exit)).setHintTip(Utils.getString(R.string.personal_aty_exit_content)).
                setOnRightButtonClck(() -> {
                    IlifeAli.getInstance().logOut(new OnAliResponse<String>() {
                        @Override
                        public void onSuccess(String result) {
                            //login successful,should jump to login page or not
                            startActivity(new Intent(PersonalActivity.this, FirstActivity.class));
                            removeALLActivity();
                        }

                        @Override
                        public void onFailed(int code, String message) {
                            // remind user that login failed with the message the method return
                            ToastUtils.showToast(message);
                        }
                    });
                }).show(getSupportFragmentManager(), "logout");
    }

    private void showRenameDialog() {
        UniversalDialog logoutDialog = new UniversalDialog();
        logoutDialog.setDialogType(UniversalDialog.TYPE_NORMAL).setCanEdit(true).setTitle(Utils.getString(R.string.personal_aty_set_name))
                .setHintTip(Utils.getString(R.string.user_nickname)).setOnRightButtonWithValueClck((name) -> {
            if (TextUtils.isEmpty(name)) {
                ToastUtils.showToast(context, getString(R.string.setting_aty_devName_null));
                return;
            }

            if (name.length() > Utils.getInputMaxLength()) {
                ToastUtils.showToast(getResources().getString(R.string.name_max_length, Utils.getInputMaxLength() + ""));
                return;
            }
            if (!name.equals(userName)) {
                changeNickName(name);
            }
            logoutDialog.dismiss();
        }).show(getSupportFragmentManager(), "rename");
    }


    public void changeNickName(final String name) {
        IlifeAli.getInstance().resetNickName(name, isSuccess -> runOnUiThread(() -> {
            if (isSuccess) {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_suc));
                userName = name;
                tv_userName.setText(name);
            } else {
                ToastUtils.showToast(context, getString(R.string.personal_aty_reset_fail));
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.RESULT_CODE_QR_SCAN && resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
            if (data != null) {
                String shareCode = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                if (!TextUtils.isEmpty(shareCode)) {
                    MyLogger.e(TAG, "onActivityResult shareCode = " + shareCode);
                    IlifeAli.getInstance().qrBindDevice(shareCode, isBindSuccessful -> runOnUiThread(() -> {
                        if (isBindSuccessful) {
                            ToastUtils.showToast(getString(R.string.personal_aty_bind_done));
                            removeActivity();
                        } else {
                            ToastUtils.showToast(getString(R.string.personal_aty_bind_fail));
                        }
                    }));
                }
            }
        }

    }


    public void getOwnerList() {
        mDeviceList.clear();
        List<DeviceInfoBean> mAcUserDevices = MyApplication.getInstance().getmAcUserDevices();
        if (mAcUserDevices != null && mAcUserDevices.size() > 0) {
            for (int i = 0; i < mAcUserDevices.size(); i++) {
                long ownerId = mAcUserDevices.get(i).getOwned();
                if (ownerId == 1) {
                    mDeviceList.add(mAcUserDevices.get(i));
                }
            }
        }
    }

    public void showDeviceList() {
        ll_device.removeAllViews();
        for (int i = 0; i < mDeviceList.size(); i++) {
            if (i != 0) {
                addShortLine();
            }
            DeviceInfoBean deviceInfoBean = mDeviceList.get(i);
            if (deviceInfoBean.getOwned() != 1) {
                continue;
            }
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextSize(14);
            textView.setTextColor(getResources().getColor(R.color.color_ac));
            textView.setBackgroundColor(getResources().getColor(R.color.color_f6));
            int height = (int) getResources().getDimension(R.dimen.dp_60);
            int paddingStart = (int) getResources().getDimension(R.dimen.dp_80);
            ViewGroup.LayoutParams lp_text = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            textView.setPadding(paddingStart, 0, 0, 0);
            textView.setLayoutParams(lp_text);
            String devName = deviceInfoBean.getDeviceName();
            String nickName = deviceInfoBean.getNickName();
            textView.setText(TextUtils.isEmpty(nickName) ? devName : nickName);
            textView.setTag(deviceInfoBean.getIotId());
            textView.setOnClickListener(v -> IlifeAli.getInstance().showShareQrCode((String) v.getTag(), new OnAliResponse<String>() {
                @Override
                public void onSuccess(String result) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    showQrDialog(jsonObject.getString(EnvConfigure.KEY_QR_KEY));
                }

                @Override
                public void onFailed(int code, String message) {
                    ToastUtils.showToast(message);
                }
            }));
            ll_device.addView(textView);
        }
    }


    public void addShortLine() {
        View line = new View(context);
        int margin = (int) getResources().getDimension(R.dimen.dp_30);
        line.setBackgroundColor(getResources().getColor(R.color.color_e2));
        LinearLayout.LayoutParams lp_line = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtil.dip2px(context, 1));
        lp_line.setMargins(margin, 0, margin, 0);
        line.setLayoutParams(lp_line);
        ll_device.addView(line);
    }


    public void showQrDialog(String shareCode) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.layout_qr_code, null);
        ImageView imageView = (ImageView) contentView.findViewById(R.id.image_map);
        createCode(imageView, shareCode);
        int width = (int) getResources().getDimension(R.dimen.dp_260);
        int height = (int) getResources().getDimension(R.dimen.dp_260);
        alertDialog = AlertDialogUtils.showDialog(context, contentView, width, height);
    }

    public void createCode(ImageView imageView, String info) {
        Bitmap bitmap;
        BitMatrix matrix;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            int width = (int) getResources().getDimension(R.dimen.dp_188);
            int height = (int) getResources().getDimension(R.dimen.dp_188);
            matrix = writer.encode(info, BarcodeFormat.QR_CODE, width, height, hints);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
//                image_map.setBackgroundColor(getResources().getColor(R.color.color_ef8200));
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    public String getVersion() {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
