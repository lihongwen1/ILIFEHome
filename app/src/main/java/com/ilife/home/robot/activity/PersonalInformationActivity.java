package com.ilife.home.robot.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.DialogFragmentUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class PersonalInformationActivity extends BackBaseActivity implements View.OnClickListener {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private DialogFragmentUtil userIconDialog;
    private File captureFile, albumFile;
    private int CAPTURE_CODE = 16;

    @Override
    public int getLayoutId() {
        return R.layout.activity_personal_information;
    }

    @Override
    public void initView() {
        tv_title.setText(getString(R.string.personal_information));
        initFile();
    }

    @OnClick({R.id.ll_user_icon, R.id.ll_nickname})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_user_icon:
                if (userIconDialog == null) {
                    DialogFragmentUtil.Builder builder = new DialogFragmentUtil.Builder();
                    userIconDialog = builder.setLayoutId(R.layout.dialog_set_icon).setCancelOutSide(true).addClickLister(R.id.tv_take_photo,
                            PersonalInformationActivity.this).build();
                }
                userIconDialog.show(getSupportFragmentManager(), "setIcon");
                break;
            case R.id.ll_nickname:

                break;
            case R.id.tv_take_photo:
                Uri takePicUri;
                Intent intent_capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePicUri = FileProvider.getUriForFile(context, getApplication().getPackageName() + ".provider", captureFile);
                    intent_capture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent_capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    takePicUri = Uri.fromFile(captureFile);
                }
                intent_capture.putExtra(MediaStore.EXTRA_OUTPUT, takePicUri);
                startActivityForResult(intent_capture, CAPTURE_CODE);
                break;
            case R.id.tv_gallery:
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


}
