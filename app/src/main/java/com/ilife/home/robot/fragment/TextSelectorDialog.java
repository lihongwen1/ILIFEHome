package com.ilife.home.robot.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.TextSelectorAdapter;
import com.ilife.home.robot.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextSelectorDialog extends DialogFragment {
    protected TextSelectorDialog.Builder builder;
    private Context context;
    private RecyclerView recyclerView;
    private TextSelectorAdapter adapter;
    private LinearLayout ll_cancel;
    private List<String> data;

    public TextSelectorDialog(TextSelectorDialog.Builder builder) {
        this.builder = builder;
    }

    private OnTextSelect onTextSelect;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, com.aliyun.iot.R.style.window_background);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_text_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_alternative_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (builder.array != null) {
            data = Arrays.asList(builder.array);
        } else {
            data = new ArrayList<>();
        }
        adapter = new TextSelectorAdapter(R.layout.litem_text, data);
        recyclerView.setAdapter(adapter);
        this.onTextSelect = builder.onTextSelect;
        adapter.setOnItemClickListener((adapter, view1, position) -> {
            if (onTextSelect != null) {
                onTextSelect.onSelect(position, data.get(position));
            }
            dismissAllowingStateLoss();
        });
        ll_cancel = view.findViewById(R.id.ll_cancel);
        ll_cancel.setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
    }


    public static class Builder {
        boolean cancelOutSide;
        String[] array;
        OnTextSelect onTextSelect;

        public Builder() {
        }

        public TextSelectorDialog.Builder setOnTextSelect(OnTextSelect onTextSelect) {
            this.onTextSelect = onTextSelect;
            return this;
        }

        public TextSelectorDialog.Builder setArray(String[] array) {
            this.array = array;
            return this;
        }

        public TextSelectorDialog.Builder setCancelOutSide(boolean cancelOutSide) {
            this.cancelOutSide = cancelOutSide;
            return this;
        }

        public TextSelectorDialog build() {
            return new TextSelectorDialog(this);
        }

    }

    public interface OnTextSelect {
        void onSelect(int position, String text);

    }
}
