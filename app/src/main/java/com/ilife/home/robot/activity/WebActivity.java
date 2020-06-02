package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.MyLogger;

import butterknife.BindView;

public class WebActivity extends BackBaseActivity {
    private WebView webView;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private String url;
    private String title;
    public static final String KEY_WEB_TITLE="web_title";
    public static final String KEY_WEB_URL="web_url";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_zaco_protocol;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initView() {
        tv_title.setText(title);
        FrameLayout frameLayout = findViewById(R.id.web_frame);
        ImageView image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(v -> removeActivity());
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false);
        settings.setJavaScriptEnabled(true);
        showLoadingDialog();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                MyLogger.d("webpage","shouldOverrideUrlLoading  "+url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                MyLogger.d("webpage","onPageStarted   "+url);
                hideLoadingDialog();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                MyLogger.d("webpage","onPageFinished    "+url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });
        frameLayout.addView(webView);
        webView.loadUrl(url);

    }

    @Override
    public void initData() {
        super.initData();
        url=getIntent().getStringExtra(KEY_WEB_URL);
        title=getIntent().getStringExtra(KEY_WEB_TITLE);
    }

    //监听BACK按键，有可以返回的页面时返回页面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.setTag(null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

}
