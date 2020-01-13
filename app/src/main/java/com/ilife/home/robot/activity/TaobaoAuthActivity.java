package com.ilife.home.robot.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponseSingle;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTUIThreadCallback;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;

import java.util.Map;

import butterknife.BindView;

public class TaobaoAuthActivity extends BackBaseActivity {
    private ImageView image_back;
    private WebView webView;
    private FrameLayout frameLayout;
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    private String mAuthCode;
    private int RESULT_CODE=200;
    private final String aUrl="https://oauth.taobao.com/authorize?response_type=code&client_id=26006876&redirect_uri=https://www.iliferobot.cn&view=wap";
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
        tv_title.setText(Utils.getString(R.string.personal_tiamao));
        frameLayout = findViewById(R.id.web_frame);
        image_back = findViewById(R.id.image_back);
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
        webView.setWebViewClient(new WebViewClient() {
            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
//                tv_title.setText(view.getTitle());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isTokenUrl(url)) {
                    Intent intent = new Intent();
                    intent.putExtra("AuthCode", mAuthCode);
                    setResult(RESULT_CODE, intent);
                    bindAccount(mAuthCode);
                    finish();
                    return true;
                }
                view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl(aUrl);
        frameLayout.addView(webView);
    }


    private boolean isTokenUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            if ( url.contains("code=")) {
                String[] urlArray = url.split("code=");
                if (urlArray.length > 1) {
                    String[] paramArray = urlArray[1].split("&");
                    if (paramArray.length > 1) {
                        mAuthCode = paramArray[0];
                        return true;
                    }
                }
            }
        }
        return false;
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


    public void bindAccount(String authCode) {
        MyLogger.d("TaobaoAuthActivity","绑定淘宝账号----auth_code:"+authCode);
        if (null != authCode) {
            IlifeAli.getInstance().taobaoAuthorization(authCode, aBoolean -> {
                if (aBoolean){
                    ToastUtils.showToast(getString(R.string.taobao_authorization_succeeded));
                }else {
                    ToastUtils.showToast(getString(R.string.taobao_authorization_failed));
                }
            });
        }

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
