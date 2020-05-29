package com.ilife.home.robot.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk._interface.OnAliResponse;
import com.aliyun.iot.aep.sdk._interface.OnAliSetPropertyResponse;
import com.aliyun.iot.aep.sdk.bean.PropertyBean;
import com.aliyun.iot.aep.sdk.contant.AliSkills;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.badoo.mobile.util.WeakHandler;
import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.bean.MapDataBean;
import com.ilife.home.robot.bean.PartitionBean;
import com.ilife.home.robot.bean.SaveMapBean;
import com.ilife.home.robot.bean.SaveMapDataInfoBean;
import com.ilife.home.robot.utils.DataUtils;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;
import com.ilife.home.robot.view.MapView;

import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * //todo 5s操作超时
 */
public class SegmentationRoomActivity extends BackBaseActivity {
    private final String TAG = "SelectRoomActivity";
    public static final String KEY_ROOM_SELECT = "room_select";
    public static final String KEY_NEW_ROOM_NAME = "new_room_name";
    public static final String KEY_FUNCTION_WORKING = "function_working";
    public static final String KEY_MAP_ID = "map_id";
    public static final String KEY_MAP_ID_INDEX = "map_id_index";
    public static final String KEY_SAVE_MAP_DATA = "save_map_data";
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    @BindView(R.id.map_segmentation_room)
    MapView map_room;

    @BindView(R.id.rg_segmentation_room)
    RadioGroup rg_segmentation_room;

    @BindView(R.id.ll_map_save_cancel)
    LinearLayout ll_map_save_cancel;
    @BindView(R.id.ll_map_updating)
    LinearLayout ll_map_updating;

    @BindView(R.id.tv_map_function_name)
    TextView tv_map_function_name;

    @BindView(R.id.tv_map_function_tip)
    TextView tv_map_function_tip;
    @BindView(R.id.iv_save_map_progress)
    ImageView iv_save_map_progress;

    @BindView(R.id.tv_combine_room)
    RadioButton tv_combine_room;
    @BindView(R.id.tv_segmentation_room)
    RadioButton tv_segmentation_room;
    @BindView(R.id.tv_name_room)
    RadioButton tv_name_room;

    private int mapId, mapIdIndex;
    private String saveMapDataInfoKey = "";
    private String saveRoomInfoKey = "";//主机保存房间信息KEY
    private HashMap<String, String> roomNames = new HashMap<>();
    private CompositeDisposable mDisposable;
    private boolean isWaitingForResult;
    private int requestTimes = 0;
    private WeakHandler weakHandler;

    @Override
    public int getLayoutId() {
        return R.layout.activity_segmentation_room;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.map_edit_map);
        iv_save_map_progress.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anims_ni));
        rg_segmentation_room.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.tv_segmentation_room://分割房间
                    map_room.setmOT(MapView.OT.SEGMENT_ROOM);
                    map_room.getmSegmentHelper().onRoomClick();
                    tv_map_function_name.setText(R.string.segmentation_room);
                    map_room.invalidateUI();
                    tv_map_function_tip.setText(R.string.segmentation_room_tip);
                    tv_combine_room.setSelected(false);
                    tv_segmentation_room.setSelected(true);
                    tv_name_room.setSelected(false);
                    tv_combine_room.setEnabled(true);
                    break;
                case R.id.tv_combine_room://合并房间
                    map_room.setmOT(MapView.OT.MERGE_ROOM);
                    tv_map_function_name.setText(R.string.merge_room);
                    map_room.invalidateUI();
                    tv_map_function_tip.setText(R.string.merge_room_tip);
                    tv_combine_room.setSelected(true);
                    tv_segmentation_room.setSelected(false);
                    tv_name_room.setSelected(false);
                    break;
                case R.id.tv_name_room:
                    map_room.setmOT(MapView.OT.NAME_ROOM);
                    tv_map_function_name.setText(R.string.name_room);
                    if (map_room.getmRoomHelper().getSelecRoom().size() > 0) {
                        LiveEventBus.get(SegmentationRoomActivity.KEY_ROOM_SELECT, Boolean.class).post(true);
                    }
                    map_room.invalidateUI();
                    tv_map_function_tip.setText(R.string.name_room_tip);
                    tv_combine_room.setSelected(false);
                    tv_segmentation_room.setSelected(false);
                    tv_name_room.setSelected(true);
                    tv_combine_room.setEnabled(true);
                    break;
            }

        });
        map_room.setmOT(MapView.OT.SELECT_ROOM);
        LiveEventBus.get(EnvConfigure.KEY_ADD_ROOM_DOOR, Integer.class).observe(this,
                value -> {
                    MyLogger.d(TAG, "添加房间门");
                    weakHandler.removeMessages(1);
                    if (isWaitingForResult) {
                        switch (value) {
                            case 1:
                                ToastUtils.showSettingSuccess(true);
                                break;
                            case 2:
                                ToastUtils.showToast(UiUtil.getString(R.string.segmentation_room_error1));
                                break;
                            case 3:
                                ToastUtils.showToast(UiUtil.getString(R.string.segmentation_room_error2));
                                break;
                            case 4:
                                ToastUtils.showToast(UiUtil.getString(R.string.segmentation_room_error3));
                                break;
                            case 5:
                            case 6:
                                ToastUtils.showSettingSuccess(false);
                                break;
                        }
                    }
                    map_room.getmSegmentHelper().reset();
                    fetchSaveMapDataInfo();
                });
        LiveEventBus.get(EnvConfigure.KEY_DELETE_ROOM_DOOR, Integer.class).observe(this, value -> {
            MyLogger.d(TAG, "删除房间门");
            weakHandler.removeMessages(1);
            if (isWaitingForResult) {
                if (value == 1) {
                    ToastUtils.showSettingSuccess(true);
                } else {
                    ToastUtils.showSettingSuccess(false);
                }
            }
            map_room.getmGateHelper().revertGate();
            fetchSaveMapDataInfo();
        });

        LiveEventBus.get(KEY_ROOM_SELECT, Boolean.class).observe(this, value -> {
            if (map_room.getmOT() == MapView.OT.NAME_ROOM) {
                switchBottomUi(R.id.ll_map_save_cancel);
                startActivity(new Intent(SegmentationRoomActivity.this, NameRoomActivity.class));
            } else if (map_room.getmOT() == MapView.OT.SELECT_ROOM) {
                tv_combine_room.setEnabled(false);
            }

        });
        LiveEventBus.get(KEY_NEW_ROOM_NAME, String.class).observe(this, (String value) -> {
            if (map_room.getmOT() == MapView.OT.NAME_ROOM) {
                map_room.getmRoomHelper().updateSelectRoomTag(value);
                map_room.invalidateUI();
            }
        });

        LiveEventBus.get(SegmentationRoomActivity.KEY_FUNCTION_WORKING, Boolean.class).observe(this, status -> {
            if (status) {
                switchBottomUi(R.id.ll_map_save_cancel);
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        weakHandler = new WeakHandler(msg -> {
            switchBottomUi(-1);
            ToastUtils.showSettingSuccess(false);
            fetchSaveMapDataInfo();
            return false;
        });
        mDisposable = new CompositeDisposable();
        mapIdIndex = getIntent().getIntExtra(KEY_MAP_ID_INDEX, 0);
        LiveEventBus.get(SegmentationRoomActivity.KEY_SAVE_MAP_DATA, SaveMapBean.class).observeSticky(this, saveMapBean -> {
            MyLogger.d(TAG, "received save map info data bean");
            mapId = saveMapBean.getMapId();
            if (mapId != 0) {
                IlifeAli.getInstance().getProperties(new OnAliResponse<PropertyBean>() {
                    @Override
                    public void onSuccess(PropertyBean bean) {
                        //保存地图信息KEY
                        if (mapId == bean.getSaveMapDataInfoMapId1()) {
                            saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO1;
                        } else if (mapId == bean.getSaveMapDataInfoMapId2()) {
                            saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO2;
                        } else if (mapId == bean.getSaveMapDataInfoMapId3()) {
                            saveMapDataInfoKey = EnvConfigure.KEY_SAVE_MAP_DATA_INFO3;
                        }
                        //解析房间信息
                        if (DataUtils.parseRoomInfo(String.valueOf(mapId), bean.getMapRoomInfo1(), roomNames)) {
                            saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO1;
                        }
                        if (DataUtils.parseRoomInfo(String.valueOf(mapId), bean.getMapRoomInfo2(), roomNames)) {
                            saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO2;
                        }
                        if (DataUtils.parseRoomInfo(String.valueOf(mapId), bean.getMapRoomInfo3(), roomNames)) {
                            saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO3;
                        }
                        //解析地图信息
                        MapDataBean mapDataBean = DataUtils.parseSaveMapData(saveMapBean.getMapData());
                        if (mapDataBean != null) {
                            map_room.setLeftTopCoordinate(mapDataBean.getLeftX(), mapDataBean.getLeftY());
                            map_room.updateSlam(mapDataBean.getMinX(), mapDataBean.getMaxX(), mapDataBean.getMinY(), mapDataBean.getMaxY());
                            map_room.drawMapX8(mapDataBean.getCoordinates());
                        }
                        //解析门，房间信息
                        SaveMapDataInfoBean saveMapDataInfoBean = DataUtils.parseSaveMapInfo(saveMapBean.getMapDataInfo());
                        if (saveMapDataInfoBean != null) {
                            for (PartitionBean room : saveMapDataInfoBean.getRooms()) {//绑定设置的房间名
                                String roomName = roomNames.get(String.valueOf(room.getPartitionId()));
                                if (roomName == null) {
                                    roomName = "";
                                }
                                room.setTag(roomName);
                            }
                            map_room.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
                            map_room.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
                            map_room.getmRoomHelper().drawRoom(saveMapDataInfoBean.getRooms());
                            map_room.getmRoomHelper().setSingleChoice(true);
                            map_room.invalidateUI();
                        }
                    }

                    @Override
                    public void onFailed(int code, String message) {
                    }
                });
            }
        });
    }


    private void fetchSaveMapDataInfo() {
        if (requestTimes != 0) {//重复请求
            return;
        }
        requestTimes++;
        Single.create((SingleOnSubscribe<String[]>) emitter -> {
            if (!TextUtils.isEmpty(saveMapDataInfoKey)) {
                IlifeAli.getInstance().getSaveMapDataInfo(mapId, saveMapDataInfoKey, true, new OnAliResponse<String[]>() {
                    @Override
                    public void onSuccess(String[] result) {
                        emitter.onSuccess(result);
                    }

                    @Override
                    public void onFailed(int code, String message) {
                        emitter.onError(new Exception(message));
                    }
                });
            }
        }).retryWhen(tf -> tf.flatMap((Function<Throwable, Publisher<?>>) throwable -> (Publisher<Boolean>) s -> {
            MyLogger.d(TAG, "get room info data error-----:" + throwable.getMessage());
            if (requestTimes > 5) {
                s.onError(throwable);
            } else {
                Disposable timerDisposable = Observable.timer(2, TimeUnit.SECONDS).subscribe(aLong -> {
                    s.onNext(true);
                    MyLogger.d(TAG, "retry to get the room info data");
                    requestTimes++;
                });
                mDisposable.add(timerDisposable);
            }
        })).subscribe(new SingleObserver<String[]>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String[] result) {
                requestTimes = 0;
                MyLogger.d(TAG, "获取到数据保存地图数据---");
                if (isDestroyed() || map_room == null) {
                    return;
                }
                if (result != null && result.length > 0) {
                    SaveMapDataInfoBean saveMapDataInfoBean = DataUtils.parseSaveMapInfo(result);
                    if (saveMapDataInfoBean == null) {
                        return;
                    }
                    for (PartitionBean room : saveMapDataInfoBean.getRooms()) {//绑定设置的用户名
                        String roomName = roomNames.get(String.valueOf(room.getPartitionId()));
                        if (roomName == null) {
                            roomName = "";
                        }
                        room.setTag(roomName);
                    }
                    map_room.drawChargePort(saveMapDataInfoBean.getChargePoint().x, saveMapDataInfoBean.getChargePoint().y, true);
                    map_room.getmGateHelper().drawGate(saveMapDataInfoBean.getGates());
                    map_room.getmRoomHelper().drawRoom(saveMapDataInfoBean.getRooms());
                    map_room.getmRoomHelper().setSingleChoice(true);
                    map_room.invalidateUI();
                    switchBottomUi(-1);
                }
            }

            @Override
            public void onError(Throwable e) {
                requestTimes = 0;
                MyLogger.d(TAG, "获取数据保存地图数据失败---");
                switchBottomUi(-1);
            }
        });
    }


    @OnClick({R.id.iv_map_save_function, R.id.iv_map_cancel_function})
    public void onClick(View view) {
        //TODO 发送合并房间数据
        switch (view.getId()) {
            case R.id.iv_map_save_function:
                isWaitingForResult = true;
                switch (map_room.getmOT()) {
                    case SEGMENT_ROOM:
                        String json = "{\"AddRoomDoor\":{\"ModifyResult\":0,\"CmdId\":1588726604,\"ModifyInfo\":\"AAAAAf///+v//wAE\",\"MapId\":1588571932}}";
                        JSONObject jsonObject = JSONObject.parseObject(json);
                        jsonObject.getJSONObject(EnvConfigure.KEY_ADD_ROOM_DOOR).put("MapId", mapId);
                        jsonObject.getJSONObject(EnvConfigure.KEY_ADD_ROOM_DOOR).put("CmdId", (int) (System.currentTimeMillis() / 1000f));
                        jsonObject.getJSONObject(EnvConfigure.KEY_ADD_ROOM_DOOR).put("ModifyInfo", map_room.getmSegmentHelper().getSegmentationData());
                        IlifeAli.getInstance().setProperties(jsonObject, aBoolean -> {
                            weakHandler.sendEmptyMessageDelayed(1, 10 * 1000);
                        });
                        break;
                    case MERGE_ROOM:
                        String merge_json = "{\"DeleteRoomDoor\":{\"ModifyResult\":0,\"CmdId\":1588726604,\"ModifyInfo\":\"AAAAAf///+v//wAE\",\"MapId\":1588571932}}";
                        JSONObject merge_jo = JSONObject.parseObject(merge_json);
                        merge_jo.getJSONObject(EnvConfigure.KEY_DELETE_ROOM_DOOR).put("MapId", mapId);
                        merge_jo.getJSONObject(EnvConfigure.KEY_DELETE_ROOM_DOOR).put("CmdId", (int) (System.currentTimeMillis() / 1000f));
                        merge_jo.getJSONObject(EnvConfigure.KEY_DELETE_ROOM_DOOR).put("ModifyInfo", map_room.getmGateHelper().getDeleteGate());
                        IlifeAli.getInstance().setProperties(merge_jo, aBoolean -> {
                            weakHandler.sendEmptyMessageDelayed(1, 10 * 1000);
                        });
                        break;
                    case NAME_ROOM:
                        List<PartitionBean> rooms = map_room.getmRoomHelper().getRooms();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(mapId);
                        for (PartitionBean room : rooms) {
                            if (!room.isTagDefault()) {
                                stringBuilder.append(",");
                                stringBuilder.append(room.getPartitionId());
                                stringBuilder.append(",");
                                stringBuilder.append(room.getTag());
                            }
                        }
                        String roomValue = Base64.encodeToString(stringBuilder.toString().getBytes(), Base64.NO_WRAP);
                        MyLogger.d(TAG, "ROOMINFO:  " + stringBuilder.toString() + "  64value:  " + roomValue);
                        if (TextUtils.isEmpty(saveRoomInfoKey)) {
                            switch (mapIdIndex) {
                                case 0:
                                    saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO1;
                                    break;
                                case 1:
                                    saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO2;
                                    break;
                                case 2:
                                    saveRoomInfoKey = EnvConfigure.KEY_SAVE_MAP_ROOM_INFO3;
                                    break;
                            }
                        }

                        IlifeAli.getInstance().setProperties(AliSkills.get().roomDataInfo(IlifeAli.getInstance().getIotId(), saveRoomInfoKey, roomValue), new OnAliSetPropertyResponse() {
                            @Override
                            public void onSuccess(String path, int tag, int functionCode, int responseCode) {
                                ToastUtils.showSettingSuccess(true);
                                switchBottomUi(-1);
                            }

                            @Override
                            public void onFailed(String path, int tag, int code, String message) {
                                ToastUtils.showSettingSuccess(true);
                                switchBottomUi(-1);

                            }
                        });
                        break;
                }
                switchBottomUi(R.id.ll_map_updating);
                break;
            case R.id.iv_map_cancel_function:
                map_room.getmSegmentHelper().reset();
                map_room.getmGateHelper().revertGate();
                map_room.invalidateUI();
                switchBottomUi(-1);
                break;

        }

    }

    private void switchBottomUi(int viewId) {
        ll_map_save_cancel.setVisibility(ll_map_save_cancel.getId() == viewId ? View.VISIBLE : View.GONE);
        ll_map_updating.setVisibility(ll_map_updating.getId() == viewId ? View.VISIBLE : View.GONE);
        rg_segmentation_room.setVisibility(viewId == -1 ? View.VISIBLE : View.GONE);
    }


    private void disposableRx() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableRx();
    }
}
