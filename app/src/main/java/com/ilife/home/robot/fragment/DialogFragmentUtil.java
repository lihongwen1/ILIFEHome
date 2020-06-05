package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ilife.home.robot.R;

public class DialogFragmentUtil extends DialogFragment {
    protected Builder builder;

    public DialogFragmentUtil(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (builder.needGrayThem) {
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
        } else {
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.normal_dialog);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(builder.cancelOutSide);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(builder.layoutId, container, false);
        initView(view);
        return view;
    }

    protected void initView(View view) {
        View.OnClickListener onClickListener;
        int id;
        for (int i = 0; i < builder.clickMap.size(); i++) {
            id = builder.clickMap.keyAt(i);
            onClickListener = builder.clickMap.get(id);
            view.findViewById(id).setOnClickListener(onClickListener);
        }
        for (int i = 0; i < builder.textViewMap.size(); i++) {
            id = builder.textViewMap.keyAt(i);
            String text = builder.textViewMap.get(id);
            TextView tv = view.findViewById(id);
            tv.setText(text);
        }
    }

    public static class Builder {
        int layoutId;
        boolean cancelOutSide;
        int arrayId;
        boolean needGrayThem;
        SparseArray<View.OnClickListener> clickMap;
        SparseArray<String> textViewMap;

        public Builder() {
            clickMap = new SparseArray<>();
            textViewMap = new SparseArray<>();
        }

        public Builder setLayoutId(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Builder setNeedGrayThem(boolean needGrayThem) {
            this.needGrayThem = needGrayThem;
            return this;
        }

        public Builder setArrayId(int arrayId) {
            this.arrayId = arrayId;
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

        public Builder setText(int viewId, String text) {
            textViewMap.put(viewId, text);
            return this;
        }

        public DialogFragmentUtil build() {
            return new DialogFragmentUtil(this);
        }

    }
}
