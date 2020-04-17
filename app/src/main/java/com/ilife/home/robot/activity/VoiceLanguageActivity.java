package com.ilife.home.robot.activity;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.VoiceLanguageAdapter;
import com.ilife.home.robot.base.BackBaseActivity;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.utils.UiUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class VoiceLanguageActivity extends BackBaseActivity {
    @BindView(R.id.rv_voice_language)
    RecyclerView rv_voice_language;
    @BindView(R.id.tv_top_title)
    TextView tv_title;
    private VoiceLanguageAdapter voiceLanguageAdapter;
    private static final String TAG = "VoiceLanguageActivity";
    private int curLanguage;
    private List<Integer> languages = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_voice_language;
    }

    @Override
    public void initView() {
        tv_title.setText(R.string.setting_set_language);
        rv_voice_language.setLayoutManager(new LinearLayoutManager(this));
        voiceLanguageAdapter = new VoiceLanguageAdapter(R.layout.item_text_selector, languages);
        voiceLanguageAdapter.setDefaultLanguage(curLanguage);
        rv_voice_language.setAdapter(voiceLanguageAdapter);
        voiceLanguageAdapter.setOnItemClickListener((adapter, view, position) -> {
            curLanguage = languages.get(position);
            voiceLanguageAdapter.setDefaultLanguage(curLanguage);
            voiceLanguageAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void initData() {
        super.initData();
        curLanguage = IlifeAli.getInstance().getWorkingDevice().getDeviceInfo().getLanguageCode();
        for (int i = 6; i < 20; i++) {
            if (curLanguage == i) {
                languages.add(0, i);
            } else {
                languages.add(i);
            }
        }
    }

    @OnClick(R.id.bt_save_volume)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save_volume://确认语言设置
                showLoadingDialog();
                String jsonStr = "{\"BeepType\":1}";
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                jsonObject.put(EnvConfigure.KEY_BeepType, curLanguage);
                IlifeAli.getInstance().setProperties(jsonObject, aBoolean -> {
                    hideLoadingDialog();
                    if (aBoolean) {
                        ToastUtils.showToast(UiUtil.getString(R.string.setting_success));
                        removeActivity();
                    }
                });
                break;
        }
    }
}
