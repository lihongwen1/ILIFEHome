package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.home.robot.R;

public class DialogFragmentUtil extends DialogFragment {
    private Builder builder;


    public DialogFragmentUtil(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(builder.cancelOutSide);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(builder.layoutId, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        View.OnClickListener onClickListener;
        int id;
        for (int i = 0; i < builder.clickMap.size(); i++) {
            id = builder.clickMap.keyAt(i);
            onClickListener = builder.clickMap.get(id);
            view.findViewById(id).setOnClickListener(onClickListener);
        }
    }

    public static class Builder {
        int layoutId;
        boolean cancelOutSide;
        SparseArray<View.OnClickListener> clickMap;

        public Builder() {
            clickMap = new SparseArray<>();
        }

        public Builder setLayoutId(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Builder setCancelOutSide(boolean cancelOutSide) {
            this.cancelOutSide = cancelOutSide;
            return this;
        }

        public Builder addClickLister(int viewId, View.OnClickListener onClickListener) {
            clickMap.put(viewId, onClickListener);
            return this;
        }

        public DialogFragmentUtil build() {
            return new DialogFragmentUtil(this);
        }

    }
}
