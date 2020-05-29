package com.ilife.home.robot.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.SelectMapAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.SlideRecyclerView;
import com.ilife.home.robot.view.SpaceItemDecoration;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

/**
 * 选择地图
 * //TODO 选择地图/删除地图
 */
public class SelectSaveMapActivity extends BackBaseActivity {
    private static final String TAG = "SelectSaveMapActivity";
    @BindView(R.id.rv_save_map)
    SlideRecyclerView rv_save_map;
    @BindView(R.id.ll_no_map)
    LinearLayout ll_no_map;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.refresh_map)
    SmartRefreshLayout refresh_map;
    private SelectMapAdapter mAdapter;
    private long selectMapId;
    private UniversalDialog mDeleteMapDialog;
    private UniversalDialog mApplyMapDialog;
    private List<SaveMapBean> saveMapBeans = new ArrayList<>();
    private int selectPosition;
    private boolean needCorrectMap;//是否需要校正服主机保存的地图数据
    private List<Integer> mapIds = new ArrayList<>();
    private int fetchMapTag = 0;//获取保存地图标记，为3代表所有地图获取完毕；

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_save_map;
    }

    private WeakHandler weakHandler;

    @Override
    public void initView() {
        tv_title.setText(R.string.map_bottom_sheet_select_map);
        mAdapter = new SelectMapAdapter(R.layout.item_save_map, saveMapBeans);
        rv_save_map.setLayoutManager(new LinearLayoutManager(this));
        rv_save_map.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 20)));
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            selectPosition = position;
            switch (view.getId()) {
                case R.id.tv_apply_this_map:
                    if (saveMapBeans.get(selectPosition).getMapId() != selectMapId) {
                        onApplyMap();
                    }
                    break;
                case R.id.iv_delete_map:
                    onDeleteMap();
                    rv_save_map.closeMenu();
                    break;
                case R.id.tv_edit_this_map:
                    Intent intent = new Intent(SelectSaveMapActivity.this, SegmentationRoomActivity.class);
                    int chooseMapId = saveMapBeans.get(position).getMapId();
                    int chooseMapIdIndex = 0;
                    for (int i = 0; i < mapIds.size(); i++) {
                        if (chooseMapId == mapIds.get(i)) {
                            chooseMapIdIndex = i;
                            break;
                        }
                    }
                    intent.putExtra(SegmentationRoomActivity.KEY_MAP_ID_INDEX, chooseMapIdIndex);
                    startActivity(intent);
                    LiveEventBus.get(SegmentationRoomActivity.KEY_SAVE_MAP_DATA, SaveMapBean.class).post(saveMapBeans.get(selectPosition));
                    break;
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {

        });
        rv_save_map.setAdapter(mAdapter);
        refresh_map.setRefreshHeader(new ClassicsHeader(this));
        refresh_map.setOnRefreshListener(refreshLayout -> getSaveMapData(false));
    }

    @Override
    public void initData() {
        super.initData();
        weakHandler = new WeakHandler(msg -> {
            if (!isDestroyed()) {
                fetchMapTag = 0;
                refresh_map.finishRefresh();
                hideLoadingDialog();
                if (saveMapBeans.size() == 0) {
                    rv_save_map.setVisibility(View.GONE);
                    ll_no_map.setVisibility(View.VISIBLE);
                    hideLoadingDialog();
                } else {

                    mAdapter.notifyDataSetChanged();
                }
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSaveMapData(true);
    }

    private void getSaveMapData(boolean isShowLoading) {
        if (isShowLoading) {
            showLoadingDialog();
        }
        if (fetchMapTag != 0) {
            return;
        }
        saveMapBeans.clear();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean bean) {
                selectMapId = bean.getSelectedMapId();
                mAdapter.setSelectMapId(selectMapId);
                String saveMapId = bean.getSaveMapId();
                if (!TextUtils.isEmpty(saveMapId)) {
                    mapIds = decodeSaveMapId(saveMapId);
                    for (int i = 0; i < mapIds.size(); i++) {
                        int mapId = mapIds.get(i);
                        if (mapId == 0) {
                            updateFetchMapTag();
                            continue;
                        }
                        String saveMapDataKey = "";
                        if (mapId == bean.getSaveMapDataMapId1()) {
                            saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_1;
                        } else if (mapId == bean.getSaveMapDataMapId2()) {
                            saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_2;
                        } else if (mapId == bean.getSaveMapDataMapId3()) {
                            saveMapDataKey = EnvConfigure.KEY_SAVE_MAP_DATA_3;
                        }
                        if (TextUtils.isEmpty(saveMapDataKey)) {
                            needCorrectMap = true;
                            mapIds.set(i, 0);
                            updateFetchMapTag();
                        } else {
                            IlifeAli.getInstance().getSaveMapData(mapId, saveMapDataKey, new OnAliResponse<String[]>() {
                                @Override
                                public void onSuccess(String[] result) {
                                    String saveMapDataInfoKey = "";
                                    if (mapId == bean.getSaveMapDataInfoMapId1()) {
                                        saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO1;
                                    } else if (mapId == bean.getSaveMapDataInfoMapId2()) {
                                        saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO2;
                                    } else if (mapId == bean.getSaveMapDataInfoMapId3()) {
                                        saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO3;
                                    }
                                    fetchSaveMapDataInfo(saveMapDataInfoKey, mapId, result);
                                }

                                @Override
                                public void onFailed(int code, String message) {
                                    updateFetchMapTag();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    private void fetchSaveMapDataInfo(String saveMapDataInfoKey, int mapId, String[] saveMapData) {
        if (!TextUtils.isEmpty(saveMapDataInfoKey)) {
            IlifeAli.getInstance().getSaveMapDataInfo(mapId, saveMapDataInfoKey, new OnAliResponse<String[]>() {
                @Override
                public void onSuccess(String[] saveMapDataInfo) {
                    if (saveMapDataInfo != null && saveMapDataInfo.length > 0) {
                        saveMapBeans.add(new SaveMapBean(saveMapData, saveMapDataInfo, mapId));
                    }
                    updateFetchMapTag();
                }

                @Override
                public void onFailed(int code, String message) {
                    updateFetchMapTag();
                }
            });
        }
    }


    private void updateFetchMapTag() {
        fetchMapTag++;
        if (fetchMapTag == 3) {
            Collections.sort(saveMapBeans, (o1, o2) -> o2.getMapId()-o1.getMapId());
            SaveMapBean selectMap=null;
            for (SaveMapBean bean:saveMapBeans) {
                if (bean.getMapId()==selectMapId){
                    selectMap=bean;
                    break;
                }
            }
            if (selectMap!=null){
                saveMapBeans.remove(selectMap);
                saveMapBeans.add(0,selectMap);
            }
            weakHandler.sendEmptyMessageDelayed(1, 200);
        }
    }

    /**
     * 解析保存地图ID
     *
     * @param data
     * @return
     */
    private List<Integer> decodeSaveMapId(String data) {
        List<Integer> result = new ArrayList<>();
        result.add(0);//初始化3个mapId;
        result.add(0);
        result.add(0);
        byte[] bt = Base64.decode(data, Base64.DEFAULT);
        if (bt == null || bt.length <= 0) {
            return result;
        }
        int index = 0;
        for (int i = 0, length = bt.length; i < length; i += 4) {
            int int1 = (bt[i] & 0xFF) << 24;
            int int2 = (bt[i + 1] & 0xFF) << 16;
            int int3 = (bt[i + 2] & 0xFF) << 8;
            int int4 = bt[i + 3] & 0xFF;
            int startTime = int1 + int2 + int3 + int4;
            result.set(index, startTime);
            index++;
            MyLogger.d(TAG, "save map id :    " + startTime);
        }
        MyLogger.d(TAG, "select map id:" + selectMapId);
        return result;

    }

    /**
     * 转为base64
     *
     * @return
     */
    private String encodeSaveMap() {
        String result;
        byte[] bt = new byte[mapIds.size() * 4];

        for (int i = 0; i < mapIds.size(); i++) {
            int item = mapIds.get(i);
            bt[i * 4] = (byte) (item >>> 24);
            bt[i * 4 + 1] = (byte) (item >>> 16);
            bt[i * 4 + 2] = (byte) (item >>> 8);
            bt[i * 4 + 3] = (byte) item;
        }
        result = Base64.encodeToString(bt, Base64.DEFAULT);
        return result.trim();
    }

    private void onDeleteMap() {
        if (mDeleteMapDialog == null) {
            mDeleteMapDialog = new UniversalDialog();
            mDeleteMapDialog.setTitle(UiUtil.getString(R.string.dialog_delete_map_title)).
                    setHintTip(UiUtil.getString(R.string.dialog_delete_map_hint))
                    .setOnRightButtonClck(() -> {
                        showLoadingDialog();
                        int index = mapIds.indexOf(saveMapBeans.get(selectPosition).getMapId());
                        if (index != -1) {
                            mapIds.set(index, 0);
                        }
                        if (saveMapBeans.get(selectPosition).getMapId() == selectMapId) {//删除选择地图
                            saveMapBeans.remove(selectPosition);
                            selectMapId = 0;
                            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(), aBoolean -> {
                                MyLogger.d(TAG, "删除地图成功：" + aBoolean);
                                hideLoadingDialog();
                                weakHandler.sendEmptyMessage(1);
                            });
                        } else {
                            saveMapBeans.remove(selectPosition);
                            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(), aBoolean -> {
                                MyLogger.d(TAG, "删除地图成功：" + aBoolean);
                                hideLoadingDialog();
                                weakHandler.sendEmptyMessage(1);
                            });
                        }

                    });
        }
        if (!mDeleteMapDialog.isAdded()) {
            mDeleteMapDialog.show(getSupportFragmentManager(), "delete_map");
        }
    }

    private void onApplyMap() {
        if (mApplyMapDialog == null) {
            mApplyMapDialog = new UniversalDialog();
            mApplyMapDialog.setTitle(UiUtil.getString(R.string.map_apply_this_map))
                    .setHintTip(UiUtil.getString(R.string.dialog_apply_map_hint))
                    .setOnRightButtonClck(() -> {
                        showLoadingDialog();
                        if (selectMapId != saveMapBeans.get(selectPosition).getMapId()) {
                            String str_id = encodeSaveMap();
                            IlifeAli.getInstance().setSelectMapId(saveMapBeans.get(selectPosition).getMapId(), str_id, aBoolean -> {
                                MyLogger.d(TAG, "选择地图成功：" + aBoolean);
                                hideLoadingDialog();
                                selectMapId = saveMapBeans.get(selectPosition).getMapId();
                                mAdapter.setSelectMapId(selectMapId);
                                SaveMapBean bean = saveMapBeans.get(selectPosition);
                                saveMapBeans.remove(bean);
                                saveMapBeans.add(0, bean);
                                weakHandler.sendEmptyMessage(1);
                            });
                        }
                    });
        }

        if (!mApplyMapDialog.isAdded()) {
            mApplyMapDialog.show(getSupportFragmentManager(), "apply_map");
        }
    }

    @Override
    protected void beforeFinish() {
        super.beforeFinish();
        if (needCorrectMap) {
            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(), aBoolean -> {
                MyLogger.d(TAG, "同步主机与服务器保存的地图" + aBoolean);
                weakHandler.sendEmptyMessage(1);
            });
        }
    }
}
