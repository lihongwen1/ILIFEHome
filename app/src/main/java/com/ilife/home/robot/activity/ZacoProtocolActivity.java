package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;

import butterknife.BindView;

public class ZacoProtocolActivity extends BackBaseActivity {
    private ImageView image_back;
    private WebView webView;
    private FrameLayout frameLayout;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
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
        tv_title.setText(R.string.personal_aty_protocol);
        frameLayout = findViewById(R.id.web_frame);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_back.setOnClickListener(v -> removeActivity());
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false);
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        frameLayout.addView(webView);
        webView.loadUrl("https://www.zacorobot.eu/privacy/");

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
