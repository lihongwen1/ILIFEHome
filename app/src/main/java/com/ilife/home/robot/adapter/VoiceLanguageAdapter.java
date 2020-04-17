package com.ilife.home.robot.adapter;

import androidx.annotation.NonNull;

import com.ilife.home.robot.R;
import com.ilife.home.robot.base.BaseQuickAdapter;
import com.ilife.home.robot.base.BaseViewHolder;
import com.ilife.home.robot.utils.DataUtils;

import java.util.List;

public class VoiceLanguageAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {
    private int defaultLanguage;

    public VoiceLanguageAdapter(int layoutId, @NonNull List<Integer> data) {
        super(layoutId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder holder, int position) {
        Integer code = data.get(position);
        holder.setText(R.id.tv_text_selector, DataUtils.getLanguageByCode(code));
        holder.setSelect(R.id.iv_text_selector, defaultLanguage == code);
    }

    public void setDefaultLanguage(int defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
