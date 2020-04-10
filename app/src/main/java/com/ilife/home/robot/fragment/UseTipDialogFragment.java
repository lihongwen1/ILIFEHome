package com.ilife.home.robot.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import com.ilife.home.livebus.LiveEventBus;
import com.ilife.home.robot.R;
import com.rd.PageIndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class UseTipDialogFragment extends DialogFragment {
    public static final String KEY_HIDE_DIALOG="key_hide_dialog";
    @BindView(R.id.vp_use_tip)
    ViewPager mVp;
    @BindView(R.id.page_indicator)
    PageIndicatorView mPageIndicator;
    private Unbinder unbinder;
    private List<UseTipFrament> pages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.universal_dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        getDialog().setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) getResources().getDimension(R.dimen.dp_315);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_use_tip, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        pages = new ArrayList<>();
        UseTipFrament fmPage1 = new UseTipFrament();
        Bundle bundlePage1 = new Bundle();
        bundlePage1.putInt(UseTipFrament.KEY_PAGE_NUMBER, 1);
        fmPage1.setArguments(bundlePage1);
        UseTipFrament fmPage2 = new UseTipFrament();
        Bundle bundlePage2 = new Bundle();
        bundlePage2.putInt(UseTipFrament.KEY_PAGE_NUMBER, 2);
        fmPage2.setArguments(bundlePage2);
        pages.add(fmPage1);
        pages.add(fmPage2);
        mVp.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), pages));
        mPageIndicator.setCount(pages.size()); // specify total count of indicators
        mPageIndicator.setSelection(0);
        LiveEventBus.get(UseTipDialogFragment.KEY_HIDE_DIALOG,Boolean.class).observe(this, aBoolean -> {
            if (true){
                mVp.setCurrentItem(0);
                dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<UseTipFrament> list;

        public ViewPagerAdapter(FragmentManager fm, List<UseTipFrament> list) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.list = list;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
