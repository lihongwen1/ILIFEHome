package com.ilife.home.robot.activity;

import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;

import butterknife.BindView;

public class SelectSaveMapActivity extends BackBaseActivity {
    @BindView(R.id.rv_save_map)
    RecyclerView rv_save_map;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @Override
    public int getLayoutId() {
        return R.layout.activity_select_save_map;
    }

    @Override
    public void initView() {
         tv_title.setText("");
    }
}
