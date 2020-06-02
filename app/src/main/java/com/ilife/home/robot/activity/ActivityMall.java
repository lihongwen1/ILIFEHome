package com.ilife.home.robot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class ActivityMall extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @Override
    public int getLayoutId() {
        return R.layout.activity_mall;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.personal_aty_shopping_center);
    }

    @OnClick({R.id.cv_zaco_mall,R.id.cv_amazon_mall})
    public void onClick(View view){
        Intent intent=new Intent(ActivityMall.this,WebActivity.class);
        switch (view.getId()){
            case R.id.cv_zaco_mall:
                intent.putExtra(WebActivity.KEY_WEB_TITLE, UiUtil.getString(R.string.personal_aty_shopping_center));
                intent.putExtra(WebActivity.KEY_WEB_URL,"https://shop.zacorobot.eu");
                break;
            case R.id.cv_amazon_mall:
                intent.putExtra(WebActivity.KEY_WEB_TITLE, UiUtil.getString(R.string.personal_aty_shopping_center));
                intent.putExtra(WebActivity.KEY_WEB_URL,"https://www.amazon.de/stores/page/2616BB70-D22C-4AEE-8DE0-05C31F98366B?channel=ZACO%20App");
                break;
        }
        startActivity(intent);
    }
}
