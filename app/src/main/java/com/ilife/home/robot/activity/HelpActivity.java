package com.ilife.home.robot.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.adapter.HelpFeedImgAdapter;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.AlertDialogUtils;
import com.ilife.home.robot.utils.AppUtils;
import com.ilife.home.robot.utils.BitmapUtils;
import com.ilife.home.robot.utils.DialogUtils;
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
    EditText et_email, et_type, et_content;
    RelativeLayout rl_type;
    File captureFile, albumFile;
    CustomPopupWindow typePop;
    BottomSheetDialog areaDialog;
    AlertDialog alertDialog;
    String[] types;
    Activity activity;
    View view;
    Uri takePicUri;
    Dialog dialog;
    @BindView(R.id.tv_numbs)
    TextView tv_numbs;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.rv_feed_image)
    RecyclerView rv_feed_image;
    @BindView(R.id.image_add)
    ImageView image_add;

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
    private List<Bitmap> images = new ArrayList<>();
    private HelpFeedImgAdapter rvAdapter;
    private int replacePosition = -1;//标记需要替换的feed image的位置
    private int permissionFlag = 0;

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
        dialog = DialogUtils.createLoadingDialog_(context);
        inflater = LayoutInflater.from(context);

        types = DeviceUtils.getSupportDevices();
        et_email = (EditText) findViewById(R.id.et_email);
        if (Utils.isSupportPhone()) {
            et_email.setHint(R.string.login_aty_input_email_phone);
        } else {
            et_email.setHint(R.string.personal_input_email);
        }
        et_type = (EditText) findViewById(R.id.et_type);
        et_content = (EditText) findViewById(R.id.et_content);
        rl_type = (RelativeLayout) findViewById(R.id.rl_type);
        view = findViewById(R.id.view);
        tv_title.setText(R.string.personal_aty_help);
        et_content.addTextChangedListener(new MyTextWatcher());
        rv_feed_image.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv_feed_image.setAdapter(rvAdapter = new HelpFeedImgAdapter(context, R.layout.item_feed_image, images));
        rv_feed_image.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 6), true));
        rvAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_feed_img:
                    replacePosition = position;
                    showPhotoDialog();
                    break;
                case R.id.iv_delete_img:
                    if (images.size() > position) {
                        images.remove(position);
                        image_add.setVisibility(images.size() == 2 ? View.GONE : View.VISIBLE);
                        rvAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        });


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


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri;
            if (requestCode == CAPTURE) {
                uri = Uri.fromFile(captureFile);
            } else {
                uri = data.getData();
            }
            if (uri == null) {
                return;
            }
            if (uri.toString().contains("video")) {
                //please select image files ,but video.
                return;
            }
            Bitmap bitmap = BitmapUtils.compressBitmap(activity, uri, 180, 180);
            if (replacePosition != -1) {
                images.remove(replacePosition);
                images.add(replacePosition, bitmap);
                replacePosition = -1;
            } else {
                images.add(bitmap);
                image_add.setVisibility(images.size() == 2 ? View.GONE : View.VISIBLE);
            }
            rvAdapter.notifyDataSetChanged();
        }
    }


    @OnClick({R.id.et_type, R.id.image_add, R.id.bt_confirm, R.id.area_contact1, R.id.area_contact2})
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
            case R.id.et_type:
                showDeviceTypePopup();
//                showAreaPopup(new String[]{"中国","美国","德国"});
                break;
            case R.id.image_add:
                permissionFlag = 0;
                new RxPermissions(this).requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA).subscribe(permission -> {
                    if (permission.granted) {
//                        11 - 3 10 - 2 01 - 1 00 - 0
                        if (permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            permissionFlag += 2;
                        } else {
                            permissionFlag++;
                        }
                    }
                    if (permission.name.equals(Manifest.permission.CAMERA)) {
                        switch (permissionFlag) {
                            case 0:
                                ToastUtils.showToast(context, getString(R.string.access_camera_storage));
                                break;
                            case 1:
                                ToastUtils.showToast(context, getString(R.string.access_storage));
                                break;
                            case 2:
                                ToastUtils.showToast(context, getString(R.string.access_camera));
                                break;
                            case 3:
                                replacePosition = -1;
                                showPhotoDialog();
                                break;
                        }
                    }

                });
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
                String email = et_email.getText().toString().trim();
                if (!Utils.checkAccountUseful(email)) {
                    return;
                }
                String type = et_type.getText().toString().trim();
                if (TextUtils.isEmpty(type)) {
                    ToastUtils.showToast(context, getString(R.string.feedback_aty_type_null));
                    return;
                }
                String contents = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(contents)) {
                    ToastUtils.showToast(context, getString(R.string.help_aty_content_isnull));
                    return;
                }
                if (Utils.isChinaEnvironment()) {
                    if (!UserUtils.isEmail(email) && !UserUtils.isPhone(email)) {
                        if (Utils.isSupportPhone()) {
                            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_account));
                        } else {
                            ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_email));
                        }
                        return;
                    }
                } else {
                    if (!UserUtils.isEmail(email)) {
                        ToastUtils.showToast(context, getString(R.string.regist_wrong_email));
                        return;
                    }
                }
                dialog.show();
                IlifeAli.getInstance().commitFeedback(email, contents, type, AppUtils.getVersion(this), "", aBoolean -> {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        if (aBoolean) {
                            ToastUtils.showToast(context, getString(R.string.help_aty_commit_suc));
                            removeActivity();
                        } else {
                            ToastUtils.showToast(context, getString(R.string.help_aty_commit));
                        }
                    });

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


    public void showDeviceTypePopup() {
        KeyboardUtils.hideSoftInput(this);
        if (typePop == null) {
            CustomPopupWindow.Builder builder = new CustomPopupWindow.Builder(this);
            typePop = builder.setBgDarkAlpha(0.6f).setView(R.layout.typelist).size(et_type.getWidth() + 4, 0).setOutsideTouchable(true).setFocusable(true).create();
            typePop.initView(() -> {
                ListView listView = typePop.getPopupWindow().getContentView().findViewById(R.id.listView);
                listView.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item, R.id.simple_list_item_textView, types));
                listView.setOnItemClickListener((parent, view1, position, id) -> {
                    et_type.setText(types[position]);
                    typePop.dissmiss();
                });
            });
        }
        if (!typePop.isShowing()) {
            int xOff = (int) (rl_type.getLeft() + getResources().getDimension(R.dimen.dp_30) - 2);
            typePop.showAsDropDown(rl_type, xOff, Utils.dip2px(this, 4));
        }

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

    class MyTextWatcher implements TextWatcher {
        private CharSequence temp;//监听前的文本
        private int editStart;//光标开始位置
        private int editEnd;//光标结束位置
        private final int charMaxNum = 140;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            tv_numbs.setText(getString(R.string.help_aty_text_count, (charMaxNum - s.length()) + ""));
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = et_content.getSelectionStart();
            editEnd = et_content.getSelectionEnd();
            if (temp.length() > charMaxNum) {
                ToastUtils.showToast(context, getString(R.string.feedback_aty_count_limit));
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                et_content.setText(s);
                et_content.setSelection(tempSelection);
            }
        }
    }


    public void showPhotoDialog() {
        if (alertDialog == null) {
            View contentView = inflater.inflate(R.layout.dialog_helt_photo, null);
            contentView.findViewById(R.id.rl_photo).setOnClickListener(this);
            contentView.findViewById(R.id.rl_album).setOnClickListener(this);
            int width = (int) getResources().getDimension(R.dimen.dp_300);
            int height = (int) getResources().getDimension(R.dimen.dp_80);
            int yOffset = (int) getResources().getDimension(R.dimen.dp_30);
            alertDialog = AlertDialogUtils.showDialogBottom(context, contentView, width, height, yOffset);
        } else {
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }
}
