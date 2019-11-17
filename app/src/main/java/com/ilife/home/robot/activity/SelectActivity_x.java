package com.ilife.home.robot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.adapter.XAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.model.bean.CleanningRobot;
import com.ilife.home.robot.utils.SpUtils;
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
    public static final String KEY_BIND_WHITE = "key_bind_white";
    Context context;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    BaseQuickAdapter adapter;
    @BindView(R.id.tv_top_title)
    TextView tvTitle;
    private List<CleanningRobot> robots = new ArrayList<>();
    private String[] supportRobots;

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
        supportRobots = DeviceUtils.getSupportDevices();
        String robotName;

        for (String deviceType : supportRobots) {
            robotName = BuildConfig.BRAND + " " + deviceType;
            switch (deviceType) {
                case Constants.X900:
                    robots.add(new CleanningRobot(R.drawable.n_x900, robotName));
                    break;
                case Constants.X800://国内X800包含 黑色款和白色款
                    robots.add(new CleanningRobot(R.drawable.n_x800, robotName));
//                    robots.add(new CleanningRobot(R.drawable.n_x800_white, robotName + " • 白"));
                    break;
                case Constants.X787:
                    robots.add(new CleanningRobot(R.drawable.n_x787, robotName));
                    break;
                case Constants.X785:
                    robots.add(new CleanningRobot(R.drawable.n_x785, robotName));
                    break;
                case Constants.A8s:
                    robots.add(new CleanningRobot(R.drawable.n_a8s, robotName));
                    break;
                case Constants.A9s:
                    if (Utils.isIlife()) {
                        robots.add(new CleanningRobot(R.drawable.n_x800, robotName));
                    } else {
                        robots.add(new CleanningRobot(R.drawable.n_a9s, robotName));
                    }
                    break;

                case Constants.V3x:
                    robots.add(new CleanningRobot(R.drawable.n_v5x, robotName));
                    break;
                case Constants.V85:
                    robots.add(new CleanningRobot(R.drawable.n_v85, robotName));
                    break;
                case Constants.X910:
                    robots.add(new CleanningRobot(R.drawable.n_x910, robotName));
                    break;
                case Constants.V5x:
                    robots.add(new CleanningRobot(R.drawable.n_v5x, robotName));
                    break;
                case Constants.A9:
                    robots.add(new CleanningRobot(R.drawable.n_x800, robotName));
                    break;
                case Constants.A7:
                    robots.add(new CleanningRobot(R.drawable.n_x787, robotName));
                    break;

            }
        }
        adapter = new XAdapter(R.layout.x_series_item, robots);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (robots.get(position).getName().contains("白")) {
                SpUtils.saveBoolean(context, KEY_BIND_WHITE, true);
            }else {
                SpUtils.saveBoolean(context, KEY_BIND_WHITE, false);
            }
            Intent i = new Intent(context, ConnectHomeWifiActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.IS_FIRST_AP = true;
    }


}
