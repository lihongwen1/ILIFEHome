package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.ilife.home.robot._interface.OnDialogClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TimeSelectDialog extends DialogFragment {
    public static String KEY_TIME_HOUR = "key_time_hour";
    @BindView(R.id.timePicker)
    TimePicker timePicker;
    private OnDialogClick<int[]> onDialogClick;
    private Unbinder unbinder;

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
        View view = inflater.inflate(R.layout.layout_timepick_dialog, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        if (DateFormat.is24HourFormat(getActivity())) {
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            int[] hm = bundle.getIntArray(KEY_TIME_HOUR);
            if (hm != null) {
                timePicker.setCurrentHour(hm[0]);
                timePicker.setCurrentMinute(hm[1]);
            }
        }
    }

    public void setOnDialogClick(OnDialogClick<int[]> onDialogClick) {
        this.onDialogClick = onDialogClick;
    }


    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                if (onDialogClick != null) {
                    onDialogClick.onConfirmClick(new int[]{hour, minute});
                }
                dismiss();
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
