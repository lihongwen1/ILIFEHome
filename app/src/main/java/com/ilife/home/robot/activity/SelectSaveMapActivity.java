package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.util.Base64;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.SelectMapAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.utils.Utils;
import com.ilife.home.robot.view.SlideRecyclerView;
import com.ilife.home.robot.view.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
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
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private SelectMapAdapter mAdapter;
    private long selectMapId;
    private UniversalDialog mDeleteMapDialog;
    private UniversalDialog mApplyMapDialog;
    private List<SaveMapBean> saveMapBeans = new ArrayList<>();
    private int selectPosition;

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
                   onApplyMap();
                    break;
                case R.id.iv_delete_map:
                    onDeleteMap();
                    rv_save_map.closeMenu();
                    break;
            }
        });
        rv_save_map.setAdapter(mAdapter);
    }

    @Override
    public void initData() {
        super.initData();
        weakHandler = new WeakHandler(msg -> {
            mAdapter.notifyDataSetChanged();
            return false;
        });

        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                selectMapId = result.getSelectedMapId();
                mAdapter.setSelectMapId(selectMapId);
                String saveMapId = result.getSaveMapId();
                if (!TextUtils.isEmpty(saveMapId)) {
                    for (int mapId : decodeSaveMapId(saveMapId)) {
                        if (mapId==0){
                            continue;
                        }
                        IlifeAli.getInstance().getSelectMap(mapId, new OnAliResponse<List<HistoryRecordBean>>() {
                            @Override
                            public void onSuccess(List<HistoryRecordBean> result) {
                                if (result != null && result.size() > 0) {
                                    if (mapId == selectMapId) {
                                        saveMapBeans.add(0, new SaveMapBean(result.get(0), mapId));
                                    } else {
                                        saveMapBeans.add(new SaveMapBean(result.get(0), mapId));
                                    }
                                }
                                weakHandler.sendEmptyMessageDelayed(1, 200);
                            }

                            @Override
                            public void onFailed(int code, String message) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    /**
     * 解析保存地图ID
     *
     * @param data
     * @return
     */
    private List<Integer> decodeSaveMapId(String data) {
        List<Integer> result = new ArrayList<>();
        byte[] bt = Base64.decode(data, Base64.DEFAULT);

        if (bt == null || bt.length <= 0) {
            return result;
        }

        for (int i = 0, length = bt.length; i < length; i += 4) {

            int int1 = (bt[i] & 0xFF) << 24;
            int int2 = (bt[i + 1] & 0xFF) << 16;
            int int3 = (bt[i + 2] & 0xFF) << 8;
            int int4 = bt[i + 3] & 0xFF;


            int startTime = int1 + int2 + int3 + int4;

            result.add(startTime);
        }
        Collections.sort(result);

        return result;

    }

    /**
     * 转为base64
     *
     * @return
     */
    private String encodeSaveMap() {
        String result = "";
        if (saveMapBeans == null || saveMapBeans.size() <= 0) {
            return result;
        }
        byte[] bt = new byte[saveMapBeans.size() * 4];

        for (int i = 0; i < saveMapBeans.size(); i++) {
            int item = (int) saveMapBeans.get(i).getMapId();
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

                        if (saveMapBeans.get(selectPosition).getMapId() == selectMapId) {//删除选择地图
                            saveMapBeans.remove(selectPosition);
                            selectMapId = 0;
                            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(), aBoolean -> {
                                MyLogger.d(TAG, "删除地图成功：" + aBoolean);
                                weakHandler.sendEmptyMessage(1);
                            });
                        } else {
                            saveMapBeans.remove(selectPosition);
                            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(), aBoolean -> {
                                MyLogger.d(TAG, "删除地图成功：" + aBoolean);
                                weakHandler.sendEmptyMessage(1);
                            });
                        }

                    });
        }
        if (!mDeleteMapDialog.isAdded()) {
            mDeleteMapDialog.show(getSupportFragmentManager(), "delete_map");
        }
    }

    private void onApplyMap(){
        if (mApplyMapDialog==null){
            mApplyMapDialog=new UniversalDialog();
            mApplyMapDialog.setTitle(UiUtil.getString(R.string.map_apply_this_map))
                    .setHintTip(UiUtil.getString(R.string.dialog_apply_map_hint))
                    .setOnRightButtonClck(() -> {
                        if (selectMapId != saveMapBeans.get(selectPosition).getMapId()) {
                            String str_id = encodeSaveMap();
                            IlifeAli.getInstance().setSelectMapId(saveMapBeans.get(selectPosition).getMapId(), str_id, aBoolean -> {
                                MyLogger.d(TAG, "选择地图成功：" + aBoolean);
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

        if (!mApplyMapDialog.isAdded()){
            mApplyMapDialog.show(getSupportFragmentManager(),"apply_map");
        }
    }
}
