package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.R;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.TouchablePDF;

import butterknife.BindView;

/**
 * Created by chengjiaping on 2017/11/30.
 */

public class ProtocolActivity extends BackBaseActivity {
    TouchablePDF pdfView;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.tv_page_indicator)
    TextView tv_page_indicator;
    public static final String KEY_TYPE = "type";
    private int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_protocol;
    }


    @Override
    public void initData() {
        super.initData();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        type = getIntent().getIntExtra(KEY_TYPE, 0);
        String fileName = "";
        String titile = "";
        switch (type) {
            case 0:
                fileName = "ilife_zh.pdf";
                titile = Utils.getString(R.string.personal_aty_protocol);
                break;
            case 1:
                fileName = "user_agreement.pdf";
                titile = Utils.getString(R.string.personal_aty_protocol_agreement);
                break;
            case 2:
                fileName = "privacy_policy.pdf";
                titile = Utils.getString(R.string.personal_aty_protocol_privacy);

                break;
        }
        pdfView = findViewById(R.id.pdfView);
        tv_title.setText(titile);
        pdfView.fromAsset(fileName)// all pages are displayed by default
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(4)
                .onPageChange((page, pageCount) -> tv_page_indicator.setText(page + 1 + "/" + pageCount)
                )
                .load();
        pdfView.setOnActionUpLister(new TouchablePDF.OnActionUp() {
            @Override
            public void actionUp() {
                tv_page_indicator.startAnimation(
                        AnimationUtils.loadAnimation(ProtocolActivity.this, R.anim.alpha_text));
            }

            @Override
            public void actionDown() {
                tv_page_indicator.clearAnimation();
            }
        });
    }

}
