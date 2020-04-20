package com.ilife.home.robot.base;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.aliyun.iot.aep.sdk.contant.IlifeAli;
import com.aliyun.iot.aep.sdk.framework.AActivity;
import com.ilife.home.robot.R;
import com.ilife.home.robot.activity.BaseMapActivity;
import com.ilife.home.robot.activity.PersonalActivity;
import com.ilife.home.robot.app.MyApplication;
import com.ilife.home.robot.fragment.DialogFragmentUtil;
import com.ilife.home.robot.utils.MyLogger;
import com.ilife.home.robot.utils.StatusBarUtil;
import com.ilife.home.robot.utils.ToastUtils;
import com.ilife.home.robot.view.GrayFrameLayout;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by chenjiaping on 2017/11/9.
 */

public abstract class BaseActivity<T extends BasePresenter> extends AActivity implements BaseView {
    protected T mPresenter;
    protected long exitTime;
    private Unbinder mUnBinder;
    private DialogFragmentUtil loadingDialog;
    private MyApplication application;
    protected BaseActivity context;
    protected boolean isActivityInteraction;
    private boolean isGrayTheme = false;//特殊日子黑白模式

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (isGrayTheme && "FrameLayout".equals(name)) {
            int count = attrs.getAttributeCount();
            for (int i = 0; i < count; i++) {
                String attributeName = attrs.getAttributeName(i);
                String attributeValue = attrs.getAttributeValue(i);
                if (attributeName.equals("id")) {
                    int id = Integer.parseInt(attributeValue.substring(1));
                    String idVal = getResources().getResourceName(id);
                    if ("android:id/content".equals(idVal)) {
                        GrayFrameLayout grayFrameLayout = new GrayFrameLayout(context, attrs);
                        return grayFrameLayout;
                    }
                }
            }
        }
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(getLayoutId());
        mUnBinder = ButterKnife.bind(this);
        attachPresenter();
        initData();
        initView();
        if (this instanceof PersonalActivity || this instanceof BaseMapActivity) {
            StatusBarUtil.setTransparentForWindow(this);
        }
        setAndroidNativeLightStatusBar();
        if (application == null) {
            // 得到Application对象
            application = (MyApplication) getApplication();
        }
        addActivity();
        MyLogger.i("LIFE_CYCLE", this.getClass().getName() + "onCreate");
    }


    /**
     * SYSTEM_UI_FLAG_LAYOUT_STABLE 白色图标
     * SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 黑色图标
     */
    private void setAndroidNativeLightStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            StatusBarUtil.setColor(this, getResources().getColor(R.color.color_00));
        }
    }

    protected void setNavigationBarColor(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(colorId));
        }
    }

    @Override
    public void attachPresenter() {

    }

    protected boolean isChildPage() {
        return false;
    }

    protected boolean canGoBack() {
        return true;
    }

    protected void beforeFinish() {

    }


    @Override
    public void onBackPressed() {
        if (!canGoBack()) {//拦截返回事件
            return;
        }
        if (!isChildPage() && System.currentTimeMillis() - exitTime >= 2000) {
            ToastUtils.showToast(this, getString(R.string.main_aty_press_exit));
            exitTime = System.currentTimeMillis();
        } else {
            beforeFinish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IlifeAli.getInstance().checkAndReconnection();
        MyLogger.i("LIFE_CYCLE", this.getClass().getName() + "onResume");
    }

    @Override
    protected void onDestroy() {
        if (mUnBinder != null) {
            mUnBinder.unbind();
        }
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        MyLogger.i("LIFE_CYCLE", this.getClass().getName() + "onDestroy");
        super.onDestroy();
    }

    /**
     * 隐藏虚拟按键，并且设置成全屏
     */
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void showLoadingDialog() {
        if (loadingDialog == null) {
            DialogFragmentUtil.Builder builder = new DialogFragmentUtil.Builder();
            loadingDialog = builder.setLayoutId(R.layout.layout_loading_dialog).setCancelOutSide(false).build();
        }
        loadingDialog.show(getSupportFragmentManager(), "refresh");

    }

    protected void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isAdded()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 设置布局
     *
     * @return
     */
    public abstract int getLayoutId();

    /**
     * 初始化视图
     */
    public abstract void initView();

    /**
     * 初始化传递过来的变量，序列化到本地的变量
     */
    public void initVariables() {

    }

    /**
     * 初始化，处理耗时数据，网络数据
     */
    public void initData() {
    }


    // 添加Activity方法
    public void addActivity() {
        application.addActivity_(context);// 调用myApplication的添加Activity方法
    }

    //销毁单个Activity方法
    public void removeActivity() {
        application.removeActivity_(context);// 调用myApplication的销毁单个Activity方法
    }

    //销毁所有Activity方法
    public void removeALLActivity() {
        application.removeALLActivity_();// 调用myApplication的销毁所有Activity方法
    }

    /**
     * 销毁所有页面，只保留当前页面
     */
    public void removeAllActivityExclude() {
        application.removeALLActivityExclude(context);
    }

    /**
     * 键盘顶输入框方法
     * 1、获取main在窗体的可视区域
     * 2、获取main在窗体的不可视区域高度
     * 3、判断不可视区域高度，之前根据经验值，在有些手机上有点不大准，现改成屏幕整体高度的1/3
     * 1、大于屏幕整体高度的1/3：键盘显示  获取Scroll的窗体坐标
     * 算出main需要滚动的高度，使scroll显示   
     * 小于屏幕整体高度的1/3：键盘隐藏
     * * @param main 根布局 
     * * @param scroll 需要显示的最下方View
     */

    public static void addLayoutListener(final View main, final View scroll) {
        main.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            main.getWindowVisibleDisplayFrame(rect);
            int screenHeight = main.getRootView().getHeight();
            int mainInvisibleHeight = main.getRootView().getHeight() - rect.bottom;
            if (mainInvisibleHeight > screenHeight / 4) {
                int[] location = new int[2];
                scroll.getLocationInWindow(location);
                int scrollHeight = (location[1] + scroll.getHeight()) - rect.bottom;
                main.scrollTo(0, scrollHeight);
            } else {
                main.scrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityInteraction = true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        isActivityInteraction = false;
    }

}
