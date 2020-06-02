package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.google.android.gms.common.util.IOUtils;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.view.TouchablePDF;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WebPdfActivity extends BackBaseActivity {
    TouchablePDF pdfView;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private String title;
    private String webUrl;
    public static final String KEY_PDF_TITLE = "web_title";
    public static final String KEY_PDF_URL = "web_url";
    private Disposable disposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_web_pdf;
    }


    @Override
    public void initData() {
        super.initData();
        showLoadingDialog();
        title = getIntent().getStringExtra(KEY_PDF_TITLE);
        webUrl = getIntent().getStringExtra(KEY_PDF_URL);
        disposable = Single.create((SingleOnSubscribe<InputStream>) emitter -> {
            InputStream inputStream = null;
            try {
                URL url = new URL(webUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
                if (inputStream == null) {
                    emitter.onError(new Exception("load pdf data fail"));
                } else {
                    emitter.onSuccess(inputStream);
                }
            } catch (IOException e) {
                emitter.onError(new Exception("load pdf data fail"));
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<InputStream>() {
            @Override
            public void accept(InputStream inputStream) {
                pdfView.fromStream(inputStream)
                        .enableSwipe(true) // allows to block changing pages using swipe
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                        .password(null)
                        .scrollHandle(null).onLoad(nbPages -> hideLoadingDialog()).load();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        pdfView = findViewById(R.id.v_pdf);
        tv_title.setText(title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
