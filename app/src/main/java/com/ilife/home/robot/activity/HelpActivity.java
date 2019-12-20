package com.ilife.home.robot.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.aliyun.iot.aep.sdk.bean.DeviceInfoBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.adapter.HelpFeedImgAdapter;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.TextSelectorDialog;
import com.ilife.home.robot.utils.AlertDialogUtils;
import com.ilife.home.robot.utils.AppUtils;
import com.ilife.home.robot.utils.BitmapUtils;
import com.ilife.home.robot.utils.KeyboardUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UserUtils;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.CustomPopupWindow;
import com.ilife.home.robot.view.SpaceItemDecoration;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by chengjiaping on 2018/8/13.
 */

public class HelpActivity extends BackBaseActivity implements View.OnClickListener {
    final String TAG = HelpActivity.class.getSimpleName();
    final int CAPTURE = 0x01;
    final int ALBUM = 0x02;
    final int CROP_PIC = 0x03;
    Context context;
    LayoutInflater inflater;
    EditText et_email, et_content;
    FrameLayout rl_type;
    File captureFile, albumFile;
    CustomPopupWindow typePop;
    BottomSheetDialog areaDialog;
    AlertDialog alertDialog;
    String[] types;
    Activity activity;
    View view;
    Uri takePicUri;
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    @BindView(R.id.tv_telNum)
    TextView tv_telNum;

    @BindView(R.id.tv_phone_time_pre)
    TextView tv_phone_time_pre;
    @BindView(R.id.tv_phone_time2_pre)
    TextView tv_phone_time_pre2;

    @BindView(R.id.tv_phone_time)
    TextView tv_phone_time;
    @BindView(R.id.tv_telNum2)
    TextView tv_telNum2;
    @BindView(R.id.tv_phone_time2)
    TextView tv_phone_time2;
    @BindView(R.id.tv_email)
    TextView tv_email;
    @BindView(R.id.ll_area_container)
    LinearLayout ll_area_container;
    @BindView(R.id.tv_area)
    TextView tv_area;
    @BindView(R.id.tv_type)
    TextView tv_type;
    @BindView(R.id.tv_question_type)
    TextView tv_question_type;
    private TextSelectorDialog deviceTypeDialog, questionTypeDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFile();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_help;
    }

    public void initView() {
        context = this;
        activity = this;
        inflater = LayoutInflater.from(context);

        types = DeviceUtils.getSupportDevices();
        et_email = (EditText) findViewById(R.id.et_email);
        if (Utils.isSupportPhone()) {
            et_email.setHint(R.string.login_aty_input_email_phone);
        } else {
            et_email.setHint(R.string.personal_input_email);
        }
        et_content = (EditText) findViewById(R.id.et_content);
        rl_type = findViewById(R.id.rl_type);
        view = findViewById(R.id.view);
        tv_title.setText(R.string.personal_aty_help);
        UserUtils.setInputFilter(et_content, 600);
        if (Utils.isIlife()) {
            switch (BuildConfig.Area) {
                case EnvConfigure.AREA_CHINA:
                    tv_telNum.setText("400-963-8886");
                    tv_phone_time.setText(Utils.getString(R.string.help_aty_time1));
                    tv_email.setText("support@iliferobot.com");
                    break;
                case EnvConfigure.AREA_AMERICA://US
                    tv_telNum.setText("1-800-631-9676");
                    tv_phone_time.setText("(Mon-Fri 09:00-17:00,CST)");
                    tv_email.setText("support@iliferobot.com");
                    break;
                case EnvConfigure.AREA_SOUTH_EAST:
                    tv_telNum.setText("072-730-2277");
                    tv_phone_time_pre.setText(Utils.getString(R.string.service_time_ja));
                    tv_phone_time.setText(Utils.getString(R.string.service_time1_ja));
                    tv_email.setText("support@iliferobot.com");
                    break;
                case EnvConfigure.AREA_EUROPE:
                    tv_area.setText(getString(R.string.area_russia));
                    tv_telNum.setText("89299401228");
                    tv_phone_time.setText(Utils.getString(R.string.russia_phone_server_time));
                    tv_email.setText("service_russia@iliferobot.com");
                    ll_area_container.setVisibility(View.VISIBLE);
                    ll_area_container.setOnClickListener(v -> showAreaPopup());
                    break;
            }
        } else {//ZACO
            findViewById(R.id.area_contact2).setVisibility(View.VISIBLE);
            tv_telNum.setText("00800-42377961");
            tv_phone_time_pre.setText(Utils.getString(R.string.help_aty_all_eu));
            tv_phone_time.setText(Utils.getString(R.string.zaco_phone_server_time));
            tv_telNum2.setText("0209-513038-380");
            tv_phone_time_pre2.setText(Utils.getString(R.string.help_aty_dir_de));
            tv_phone_time2.setText(Utils.getString(R.string.zaco_phone_server_time));
            tv_email.setText("support@zacorobot.eu");
        }
        tv_type.setHint(getResources().getString(IlifeAli.getInstance().getmAcUserDevices().size() == 0 ? R.string.help_aty_chose_product_type : R.string.help_aty_chose_product));
        et_content.setOnTouchListener(touchListener);
    }


    /**
     * 设置触摸事件，由于EditView与TextView都处于ScollView中，
     * 所以需要在OnTouch事件中通知父控件不拦截子控件事件
     */
    private View.OnTouchListener touchListener = (v, event) -> {
        if(event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE){
            //按下或滑动时请求父节点不拦截子节点
            v.getParent().requestDisallowInterceptTouchEvent(true);
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            //抬起时请求父节点拦截子节点
            v.getParent().requestDisallowInterceptTouchEvent(false);
        }
        return false;
    };

    @OnClick({R.id.rl_type, R.id.bt_confirm, R.id.area_contact1, R.id.area_contact2, R.id.rl_question_type})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.area_contact1:
            case R.id.area_contact2:
                new RxPermissions(this).requestEach(Manifest.permission.CALL_PHONE).subscribe(permission -> {
                    if (permission.granted) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        String phoneNumber = v.getId() == R.id.area_contact1 ? tv_telNum.getText().toString() : tv_telNum2.getText().toString();
                        Uri data = Uri.parse("tel:" + phoneNumber);
                        intent.setData(data);
                        startActivity(intent);
                    } else {
                        // 用户拒绝了该权限，并且选中『不再询问』
                        ToastUtils.showToast(context, getString(R.string.access_photo));
                    }
                }).dispose();
                break;
            case R.id.rl_type:

                if (deviceTypeDialog == null) {
                    TextSelectorDialog.Builder builder = new TextSelectorDialog.Builder();
                    List<DeviceInfoBean> devices = IlifeAli.getInstance().getmAcUserDevices();
                    String[] array;
                    if (devices.size() == 0) {
                        array = getResources().getStringArray(R.array.array_device_type);
                    } else {
                        array = new String[devices.size()];
                        String niclName;
                        for (int i = 0; i < devices.size(); i++) {
                            niclName = devices.get(i).getNickName();
                            array[i] = TextUtils.isEmpty(niclName) ? devices.get(i).getDeviceName() : niclName;
                        }
                    }
                    deviceTypeDialog = builder.setArray(array).setCancelOutSide(false).setOnTextSelect(
                            (position, text) -> {
                                tv_type.setTag(position);
                                tv_type.setText(text);
                            }
                    ).build();
                }
                if (!deviceTypeDialog.isAdded()) {
                    deviceTypeDialog.show(getSupportFragmentManager(), "text_device");
                }
                break;
            case R.id.rl_question_type:
                if (questionTypeDialog == null) {
                    TextSelectorDialog.Builder builder = new TextSelectorDialog.Builder();
                    questionTypeDialog = builder.setArray(getResources().getStringArray(R.array.array_question_type)).setCancelOutSide(false).setOnTextSelect(new TextSelectorDialog.OnTextSelect() {
                        @Override
                        public void onSelect(int position, String text) {
                            tv_question_type.setText(text);
                            tv_question_type.setTag(position + 1);
                        }

                    }).build();
                }
                if (!questionTypeDialog.isAdded()) {
                    questionTypeDialog.show(getSupportFragmentManager(), "text_question");
                }
                break;
            case R.id.rl_photo:
                AlertDialogUtils.hidden(alertDialog);
                Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePicUri = FileProvider.getUriForFile(context, getApplication().getPackageName() + ".provider", captureFile);
                    intent_capture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent_capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    takePicUri = Uri.fromFile(captureFile);
                }
                intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, takePicUri);
                startActivityForResult(intent_capture, CAPTURE);
                break;
            case R.id.rl_album:
                AlertDialogUtils.hidden(alertDialog);
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, ALBUM);
                break;
            case R.id.bt_confirm:

                String robotType = tv_type.getText().toString().trim();
                if (TextUtils.isEmpty(robotType)) {
                    ToastUtils.showToast(context, getString(IlifeAli.getInstance().getmAcUserDevices().size() == 0 ? R.string.help_aty_chose_product_type : R.string.help_aty_chose_product));
                    return;
                }
                String question_type = tv_question_type.getText().toString().trim();
                if (TextUtils.isEmpty(question_type)) {
                    ToastUtils.showToast(context, getString(R.string.help_aty_choose_question_type));
                    return;
                }
                int type = (int) tv_question_type.getTag();//问题类型；
                String contents = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(contents)) {
                    ToastUtils.showToast(context, getString(R.string.help_aty_content));
                    return;
                }
                String email = et_email.getText().toString().trim();
                if (!Utils.checkAccountUseful(email)) {
                    return;
                }
                showLoadingDialog();
                //TODO 反馈iot id修改
                boolean isHaveRobot = IlifeAli.getInstance().getmAcUserDevices().size() > 0;
                String iotId = isHaveRobot ? IlifeAli.getInstance().getmAcUserDevices().get((Integer) tv_type.getTag()).getIotId() : "";
                String robotName = isHaveRobot ? robotType : "";
                String productKey =isHaveRobot? IlifeAli.getInstance().getmAcUserDevices().get((Integer) tv_type.getTag()).getProductKey():DeviceUtils.getProductKeyByRobotType(robotType);
                IlifeAli.getInstance().commitFeedback(email, contents, type, robotName, AppUtils.getVersion(this), iotId,
                        productKey, aBoolean -> {
                            hideLoadingDialog();
                            if (aBoolean) {
                                ToastUtils.showToast(context, getString(R.string.help_aty_commit_suc));
                                removeActivity();
                            } else {
                                ToastUtils.showToast(context, getString(R.string.help_aty_commit));
                            }
                        });
                break;
        }
    }


    private void initFile() {
        File imageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ilife");
        if (!imageFile.exists()) {
            imageFile.mkdirs();
        }
        captureFile = new File(imageFile, "capture.png");
        albumFile = new File(imageFile, "album.jpg");
    }


    public void showAreaPopup() {
        String[] area = new String[]{getString(R.string.area_russia), getString(R.string.area_spanish), getString(R.string.area_other)};
        KeyboardUtils.hideSoftInput(this);
        if (areaDialog == null) {
            areaDialog = new BottomSheetDialog(this);
            View view = View.inflate(this, R.layout.typelist, null);
            ListView listView = view.findViewById(R.id.listView);
            listView.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.simple_list_item, R.id.simple_list_item_textView, area));
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                tv_area.setText(area[position]);
                switch (position) {
                    case 0:
                        tv_telNum.setText("89299401228");
                        tv_phone_time_pre.setText("");
                        tv_phone_time.setText(Utils.getString(R.string.russia_phone_server_time));
                        tv_email.setText("service_russia@iliferobot.com");
                        break;
                    case 1:
                        tv_telNum.setText("0034-918-607768");
                        tv_phone_time_pre.setText(Utils.getString(R.string.spanish_server_time_sat));
                        tv_phone_time.setText("(" + Utils.getString(R.string.zaco_phone_server_time) + ")");
                        tv_email.setText("serviciotecnico.ilife@edawms.com");
                        break;
                    case 2:
                        tv_telNum.setText("400-963-8886");
                        tv_phone_time_pre.setText("");
                        tv_phone_time.setText(Utils.getString(R.string.help_aty_time1));
                        tv_email.setText("support@iliferobot.com");
                        break;
                }
                areaDialog.dismiss();
            });
            areaDialog.setContentView(view);
        }
        if (!areaDialog.isShowing()) {
            areaDialog.show();
        }


    }

}
