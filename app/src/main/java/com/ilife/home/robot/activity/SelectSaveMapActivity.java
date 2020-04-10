package com.ilife.home.robot.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.SelectMapAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.fragment.UniversalDialog;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.Utils;
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
    RecyclerView rv_save_map;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private SelectMapAdapter mAdapter;
    private List<Integer> ids = new ArrayList<>();
    private long selectMapId;
    private UniversalDialog mDeleteMapDialog;
    private List<HistoryRecordBean> saveMapBeans = new ArrayList<>();
    private int selectPosition;

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_save_map;
    }

    private WeakHandler weakHandler;

    @Override
    public void initView() {
        tv_title.setText("选择地图");
        mAdapter = new SelectMapAdapter(R.layout.item_save_map, saveMapBeans);
        rv_save_map.setLayoutManager(new LinearLayoutManager(this));
        rv_save_map.addItemDecoration(new SpaceItemDecoration(Utils.dip2px(this, 20)));
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            selectPosition = position;
            switch (view.getId()) {
                case R.id.tv_apply_this_map:
                    if (selectMapId == ids.get(selectPosition)) {
                        ToastUtils.showToast("已应用此地图");
                    } else {
                        String str_id = encodeSaveMap(ids);
                        IlifeAli.getInstance().setSelectMapId(ids.get(selectPosition), str_id, aBoolean -> {
                            MyLogger.d(TAG, "选择地图成功：" + aBoolean);
                            selectMapId = ids.get(selectPosition);
                            HistoryRecordBean bean = saveMapBeans.get(selectPosition);
                            saveMapBeans.remove(bean);
                            saveMapBeans.add(0, bean);
                            weakHandler.sendEmptyMessage(1);
                        });
                    }
                    break;
                case R.id.iv_delete_map:
                    onDeleteMap();
                    break;
            }
        });
        rv_save_map.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
            @Override
            public void onSuccess(PropertyBean result) {
                selectMapId = result.getSelectedMapId();
                String saveMapId = result.getSaveMapId();
                if (!TextUtils.isEmpty(saveMapId)) {
                    ids.clear();
                    ids.addAll(decodeSaveMapId(saveMapId));
                    for (int id : ids) {
                        IlifeAli.getInstance().getSelectMap(id, new OnAliResponse<List<HistoryRecordBean>>() {
                            @Override
                            public void onSuccess(List<HistoryRecordBean> result) {
                                if (result != null && result.size() > 0) {
                                    if (id == selectMapId) {
                                        saveMapBeans.add(0, result.get(0));
                                    } else {
                                        saveMapBeans.add(result.get(0));
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

    @Override
    public void initData() {
        super.initData();
        weakHandler = new WeakHandler(msg -> {
            mAdapter.notifyDataSetChanged();
            return false;
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

    //转为base64
    private String encodeSaveMap(List<Integer> data) {
        String result = "";
        if (data == null || data.size() <= 0) {
            return result;
        }
        byte[] bt = new byte[data.size() * 4];

        for (int i = 0; i < data.size(); i++) {
            int item = data.get(i);
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
            mDeleteMapDialog.setTitle("删除地图").setHintTip("删除后将不再保存该地图记录")
                    .setOnRightButtonClck(() -> {
                        if (selectMapId == ids.get(selectPosition)) {
                            ToastUtils.showToast("不能删除当前地图");
                        } else {
                            ids.remove(selectPosition);
                            saveMapBeans.remove(selectPosition);
                            IlifeAli.getInstance().setSelectMapId(selectMapId, encodeSaveMap(ids), aBoolean -> {
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
}
