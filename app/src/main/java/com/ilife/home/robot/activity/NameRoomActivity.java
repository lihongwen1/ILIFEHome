package com.ilife.home.robot.activity;

import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.android.hms.agent.common.UIUtils;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.RoomNameAdapter;
import com.ilife.home.robot.adapter.TextSelectorAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.fragment.UniversalEditDialog;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

public class NameRoomActivity extends BackBaseActivity {
    @BindView(R.id.rv_select_name)
    RecyclerView rv_select_name;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private String[] names;
    private UniversalEditDialog editDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_name_room;
    }

    private String roomName;

    @Override
    public void initView() {
        tv_title.setText("房间命名");
        names = UiUtil.getStringArray(R.array.default_room_name);
        List<String> nameList = new ArrayList<>(Arrays.asList(names));
        RoomNameAdapter adapter = new RoomNameAdapter(R.layout.item_room_name, nameList, new SparseIntArray());
        rv_select_name.setLayoutManager(new LinearLayoutManager(this));
        rv_select_name.setAdapter(adapter);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            if (position == names.length - 1) {

                if (editDialog == null) {
                    editDialog = new UniversalEditDialog();
                    editDialog.setTitle("房间命名").setHint("自定义").setOnRightButtonClick(new UniversalEditDialog.OnRightButtonClick() {
                        @Override
                        public void onClick(String value) {
                            roomName = value;
//                            if (roomName.getBytes().length>20){
//                                ToastUtils.showToast("输入字符过长");
//                            }
                            nameList.set(position,value);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                editDialog.show(getSupportFragmentManager(),"et_input_value");
            } else {
                roomName = names[position];
            }
            adapter1.notifyDataSetChanged();
        });
    }

    @Override
    protected void beforeFinish() {
        super.beforeFinish();
        if (!TextUtils.isEmpty(roomName)) {
            LiveEventBus.get(SegmentationRoomActivity.KEY_NEW_ROOM_NAME, String.class).post(roomName);
        }
    }
}
