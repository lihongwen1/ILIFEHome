package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ilife.home.robot.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UniversalEditDialog extends DialogFragment {
    private EditText et_input_value;
    private String hint;
    private String title;
    private OnRightButtonClick onRightButtonClick;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) getResources().getDimension(R.dimen.dp_315);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_edit_universal, container, false);
        et_input_value = v.findViewById(R.id.et_input_value);
        TextView tv_title = v.findViewById(R.id.tv_et_input_title);
        tv_title.setText(title);
        et_input_value.setHint(hint);
        v.findViewById(R.id.tv_dialog_right).setOnClickListener(v1 -> {
            dismiss();
            onRightButtonClick.onClick(et_input_value.getText().toString().trim());
        });
        v.findViewById(R.id.tv_dialog_left).setOnClickListener(v12 -> dismiss());
        return v;
    }


    public interface OnRightButtonClick {
        void onClick(String value);
    }

    public UniversalEditDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public UniversalEditDialog setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public UniversalEditDialog setOnRightButtonClick(OnRightButtonClick onRightButtonClick) {
        this.onRightButtonClick = onRightButtonClick;
        return this;
    }
}
