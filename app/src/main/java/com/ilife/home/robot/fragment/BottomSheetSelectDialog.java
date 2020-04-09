package com.ilife.home.robot.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ilife.home.robot.R;
import com.ilife.home.robot.adapter.TextImageSelectorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomSheetSelectDialog extends BottomSheetDialogFragment {
    private Builder builder;
    private Context context;
    private List<String> data;
    private TextImageSelectorAdapter adapter;
    private OnTextSelect onTextSelect;
    public static String KEY_TEXT_VALUE = "key_text_value";
    public static String KEY_CURRENT_VALUE = "key_current_value";
    public static String KEY_TITLE = "key_title";
    public static String KEY_SUPPORT_CHECK = "key_support_check";
    private String currentValue;
    private String title;
    private boolean supportCheck;//是否支持多选
    private SparseIntArray selectPosition;

    public BottomSheetSelectDialog(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_bottom_sheet_text_select, container, false);
        initData();
        initView(view);
        return view;
    }

    private void initData() {
        supportCheck = getArguments().getBoolean(KEY_SUPPORT_CHECK);
        String[] text = getArguments().getStringArray(KEY_TEXT_VALUE);
        if (text == null) {
            data = new ArrayList<>();
        } else {
            data = Arrays.asList(text);
        }
        currentValue = getArguments().getString(KEY_CURRENT_VALUE);
        if (currentValue == null) {
            currentValue = "";
        }
        title = getArguments().getString(KEY_TITLE);
        if (title == null) {
            title = "";
        }
    }

    private void initView(View view) {
        View ll_container = view.findViewById(R.id.ll_text_selector_action_container);
        ll_container.setVisibility(supportCheck ? View.VISIBLE : View.GONE);
        TextView tv_title = view.findViewById(R.id.tv_text_selector_title);
        tv_title.setText(title);
        RecyclerView recyclerView = view.findViewById(R.id.rv_text_selector);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        selectPosition = new SparseIntArray();
        for (int i = 0; i < data.size(); i++) {
            if (currentValue.contains(data.get(i))) {
                selectPosition.put(i, i);
            }
        }
        adapter = new TextImageSelectorAdapter(R.layout.item_text_selector, data, selectPosition);
        recyclerView.setAdapter(adapter);
        this.onTextSelect = builder.onTextSelect;
        adapter.setOnItemClickListener((adapter, view1, position) -> {
            if (supportCheck) {
               adapter.notifyDataSetChanged();
            } else {
                if (onTextSelect != null) {
                    onTextSelect.onSelect(new int[]{position}, new String[]{data.get(position)});
                }
                dismissAllowingStateLoss();
            }
        });
        view.findViewById(R.id.btn_dismiss_text_selector).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_save_text).setOnClickListener(v -> {
            String[] valus = new String[selectPosition.size()];
            int[] positions = new int[selectPosition.size()];
            for (int i = 0; i < selectPosition.size(); i++) {
                valus[i] = data.get(selectPosition.valueAt(i));
                positions[i] = selectPosition.valueAt(i);
            }
            onTextSelect.onSelect(positions, valus);
            dismiss();
        });
    }


    public static class Builder {
        boolean cancelOutSide;
        boolean supportCheck;//多选
        String[] array;
        BottomSheetSelectDialog.OnTextSelect onTextSelect;

        public Builder() {
        }

        public BottomSheetSelectDialog.Builder setOnTextSelect(BottomSheetSelectDialog.OnTextSelect onTextSelect) {
            this.onTextSelect = onTextSelect;
            return this;
        }

        public BottomSheetSelectDialog.Builder setArray(String[] array) {
            this.array = array;
            return this;
        }

        public BottomSheetSelectDialog.Builder setCancelOutSide(boolean cancelOutSide) {
            this.cancelOutSide = cancelOutSide;
            return this;
        }

        public BottomSheetSelectDialog.Builder setSupportCheck(boolean supportCheck) {
            this.supportCheck = supportCheck;
            return this;
        }

        public BottomSheetSelectDialog build() {
            return new BottomSheetSelectDialog(this);
        }

    }

    public interface OnTextSelect {
        void onSelect(int[] position, String[] text);

    }
}
