package com.ilife.home.robot.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.UiUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 三年质保界面
 */
public class ActivityWarranty extends BackBaseActivity {
    @BindView(R.id.tv_top_title)
    TextView tv_title;

    @Override
    public int getLayoutId() {
        return R.layout.activity_warranty;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.personal_aty_warranty);
    }

    @OnClick({R.id.tv_warranty1, R.id.tv_warranty2})
    public void onClick(View view) {
        Intent intent=null;
        switch (view.getId()) {
            case R.id.tv_warranty1:
                intent = new Intent(ActivityWarranty.this, WebActivity.class);
                intent.putExtra(WebActivity.KEY_WEB_TITLE, UiUtil.getString(R.string.personal_aty_warranty));
                intent.putExtra(WebActivity.KEY_WEB_URL, "https://garantie.zacorobot.eu/");
                break;
            case R.id.tv_warranty2:
                intent = new Intent(ActivityWarranty.this, WebPdfActivity.class);
                intent.putExtra(WebPdfActivity.KEY_PDF_TITLE, UiUtil.getString(R.string.personal_aty_warranty));
                intent.putExtra(WebPdfActivity.KEY_PDF_URL, "https://garantie.zacorobot.eu/media/infos_zur_garantieverlaengerung_zaco.pdf");
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
