package com.ilife.home.robot.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk.bean.HistoryRecordBean;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.adapter.HistoryAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.able.DeviceUtils;
import com.ilife.home.robot.utils.SpUtils;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.RecyclerViewDivider;
import com.ilife.home.robot.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chenjiaping on 2017/8/18.
 */

public class HistoryActivity_x9 extends BackBaseActivity implements View.OnClickListener {
    final String TAG = HistoryActivity_x9.class.getSimpleName();
    int index;
    List<HistoryRecordBean> recordList;
    private Long[] startTimes;
    private HistoryRecordBean[] records;
    RecyclerView recyclerView;
    HistoryAdapter adapter;
    LinearLayout fl_noRecord;
    TextView tv_title;
    SmartRefreshLayout refreshLayout;
    private long start, end;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start = IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().getHistoryMapTimeLine();
        showLoadingDialog();
        getHistoryRecord();

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history;
    }

    public void initView() {
        context = this;

        fl_noRecord = findViewById(R.id.ll_noRecord);
        tv_title = findViewById(R.id.tv_top_title);
        tv_title.setText(R.string.setting_aty_clean_record);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setOnRefreshListener(refreshLayout -> getHistoryRecord());
        recordList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new HistoryAdapter(R.layout.layout_histroy_item, recordList);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(HistoryActivity_x9.this, HistoryDetailActivity_x9.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("Record", recordList.get(position));
            intent.putExtras(bundle);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.VERTICAL, getResources().getDimensionPixelOffset(R.dimen.dp_8),
                ContextCompat.getColor(this, R.color.bg_color_f5f7fa)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_back:
                removeActivity();
                break;
        }
    }

    private void getHistoryRecord() {
        end = System.currentTimeMillis();
        IlifeAli.getInstance().getHistoryRecords(start, end, new OnAliResponse<List<HistoryRecordBean>>() {
            @Override
            public void onSuccess(List<HistoryRecordBean> result) {
                recordList.clear();
                recordList.addAll(result);
                showList(recordList);
                hideLoadingDialog();
                refreshLayout.finishRefresh();
            }

            @Override
            public void onFailed(int code, String message) {
                if (code != -1) {//-1 is a custom error,no need to handle it
                    ToastUtils.showErrorToast(context, code);
                }
                showList(recordList);
                hideLoadingDialog();
                refreshLayout.finishRefresh();
            }
        });
    }

    public void showList(List<HistoryRecordBean> recordList) {
        if (recordList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            fl_noRecord.setVisibility(View.VISIBLE);
        } else {
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            }
            fl_noRecord.setVisibility(View.GONE);
            bubbleSort(recordList);
            adapter.notifyDataSetChanged();
        }
    }

    public void bubbleSort(List<HistoryRecordBean> recordList) {
        int size = recordList.size();
        startTimes = new Long[size];
        records = new HistoryRecordBean[size];
        for (int i = 0; i < size; i++) {
            startTimes[i] = (long) recordList.get(i).getStartTime();
            records[i] = recordList.get(i);
        }
        for (int i = 0; i < startTimes.length - 1; i++) {
            for (int j = 0; j < startTimes.length - i - 1; j++) {
                if (startTimes[j] < startTimes[j + 1]) {
                    long temp = startTimes[j];
                    HistoryRecordBean tempRecord = records[j];
                    startTimes[j] = startTimes[j + 1];
                    records[j] = records[j + 1];
                    startTimes[j + 1] = temp;
                    records[j + 1] = tempRecord;
                }
            }
        }
        recordList.clear();
        for (int i = 0; i < size; i++) {
            recordList.add(records[i]);
        }
    }
}
