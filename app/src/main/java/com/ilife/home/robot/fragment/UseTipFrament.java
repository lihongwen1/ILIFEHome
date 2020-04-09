package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class UseTipFrament extends Fragment {
    @BindView(R.id.tv_use_tip_title)
    TextView tv_tip_title;
    @BindView(R.id.tv_use_tip1)
    TextView tv_tip;
    @BindView(R.id.tv_i_know)
    TextView tv_i_know;
    @BindView(R.id.iv_use_tip)
    ImageView iv_use_tip;
    @BindView(R.id.tv_use_tip2)
    TextView tv_use_tip2;
    @BindView(R.id.ll_use_tip2)
    LinearLayout ll_use_tip2;
    private Unbinder unbinder;
    public static final String KEY_PAGE_NUMBER = "page_number";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_use_tip, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            int pageNumber = bundle.getInt(KEY_PAGE_NUMBER);
            String title, tip1, tip2;
            int tipImage;
            if (pageNumber == 1) {
                title = "整理清扫环境";
                tip1 = "清扫前请清理好地面上的玩具、电线、抹布、鞋子等小物品，避免被扫地机卷入。";
                tipImage = R.drawable.pic_arrange;
                tip2 = "";
            } else {
                title = "建立完整家居地图";
                tip1 = "请从充电座启动自动清扫，开始建图。";
                tip2 = "扫地机清扫完成后，自动返回充电座，将会生成完整家居地图。(手动搬回充电座、未完成清扫手动触发回充等无法生成完整家居地图)";
                tipImage = R.drawable.pic_build_map;
            }
            tv_tip_title.setText(title);
            if (tip2.isEmpty()) {
                ll_use_tip2.setVisibility(View.GONE);
            } else {
                tv_use_tip2.setText(tip2);
            }
            tv_tip.setText(tip1);
            iv_use_tip.setImageResource(tipImage);
            tv_i_know.setVisibility(pageNumber == 2 ? View.VISIBLE : View.GONE);
        }
        tv_i_know.setOnClickListener(v -> LiveEventBus.get(UseTipDialogFragment.KEY_HIDE_DIALOG, Boolean.class).post(true));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
