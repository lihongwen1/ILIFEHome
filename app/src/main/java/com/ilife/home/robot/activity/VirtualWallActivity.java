package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.view.MapView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 编辑虚拟墙，禁区的界面
 */
public class VirtualWallActivity extends BackBaseActivity {
    public static final String KEY_MAP_BUNDLE = "map_bundle";
    private static final  String TAG="VirtualWallActivity";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_edit_vw)
    MapView mMapView;
    @BindView(R.id.fl_top_menu)
    FrameLayout save;
    @Override
    public int getLayoutId() {
        return R.layout.activity_virtual_wall;
    }

    @Override
    public void initView() {
        tv_title.setText("虚拟墙/禁区");
        save.setVisibility(View.VISIBLE);
        LiveEventBus.get(KEY_MAP_BUNDLE, MapDataBean.class).observeSticky(this, mapDataBean -> {
            mMapView.post(new Runnable() {
                @Override
                public void run() {
                    mMapView.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                    mMapView.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                    mMapView.drawMapX8(mapDataBean.getCoordinates());
                }
            });
        });
    }

    @Override
    public void initData() {
        super.initData();

    }

    @OnClick({R.id.tv_virtual, R.id.tv_move_map, R.id.tv_mop_area, R.id.map_global_area,R.id.fl_top_menu})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_move_map:
                mMapView.setmOT(MapView.OT.MAP);
                break;
            case R.id.tv_virtual:
                mMapView.setmOT(MapView.OT.VIRTUAL_WALL);
                break;
            case R.id.tv_mop_area:
                mMapView.setmOT(MapView.OT.MOP_FORBIDDEN_AREA);
                break;
            case R.id.map_global_area:
                mMapView.setmOT(MapView.OT.GLOBAL_FORBIDDEN_AREA);
                break;
            case R.id.fl_top_menu:
                MyLogger.d(TAG,"虚拟墙数据"+mMapView.getVirtualWallPointfs());
                MyLogger.d(TAG,"抹地禁区数据"+mMapView.getMopFBDData());
                MyLogger.d(TAG,"全局禁区数据"+mMapView.getGlobalFBDData());
                break;
        }
    }

}
