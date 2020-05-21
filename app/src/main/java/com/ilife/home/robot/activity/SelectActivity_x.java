package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.adapter.XAdapter;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.bean.RobotConfigBean;
import com.ilife.home.robot.model.bean.CleanningRobot;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chengjiaping on 2018/8/9.
 */
//DONE
public class SelectActivity_x extends BackBaseActivity {
    final String TAG = SelectActivity_x.class.getSimpleName();
    Context context;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    BaseQuickAdapter adapter;
    @BindView(R.id.tv_top_title)
    TextView tvTitle;
    private List<CleanningRobot> robots = new ArrayList<>();
    private static final String KEY_BINDING_PK = "product_key";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_x;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initView() {
        context = this;
        tvTitle.setText(R.string.select_x_aty_add);
        recyclerView.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 6)));
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        initAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void initAdapter() {
        String[] supportRobots = DeviceUtils.getSupportDevices();
        RobotConfigBean.RobotBean robotBean;
        RobotConfigBean robotConfig = MyApplication.getInstance().readRobotConfig();
        for (String deviceType : supportRobots) {
            robotBean = robotConfig.getRobotBeanByRt(deviceType);
            robots.add(new CleanningRobot(UiUtil.getDrawable(robotBean.getFaceImg()), BuildConfig.BRAND + " " + deviceType, robotBean.getProductKey()));
        }
        adapter = new XAdapter(R.layout.x_series_item, robots);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent i = new Intent(context, ApGuideOpenPowerActivity.class);
            IlifeAli.getInstance().setBindingProductKey(robots.get(position).getProductKey());
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
