package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.R;
import com.ilife.home.robot.view.TouchablePDF;

import java.util.Locale;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_protocol;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        String lan = Locale.getDefault().getLanguage();
        String fileName;
        if (lan.equals("zh")) {
            fileName = "ilife_zh.pdf";
        } else if (lan.equals("de")) {
            fileName = "ilife_de.pdf";
        } else if (lan.equals("ja")) {
            fileName = "ilife_jp.pdf";
        } else {
            fileName = "ilife_en.pdf";
        }
//        String fileName = lan.equals("zh")?"ilife_zh.pdf":"ilife_en.pdf";
        pdfView = findViewById(R.id.pdfView);
        tv_title.setText(R.string.personal_aty_protocol);
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
