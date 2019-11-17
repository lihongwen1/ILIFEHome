package com.ilife.home.robot.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by 卖火柴的小女孩 - Jc on 2018/7/31.
 */

public class JcUtilScreen {

    public static Context mContext;
    /**
     * 屏幕密度
     */
    public static float mDensity;
    /**
     * 屏幕宽度
     */
    public static float mScreenWidth;
    /**
     * 屏幕高度
     */
    public static float mScreenHeight;

    public static void init(Context context) {
        mContext = context; //通过构造方法来传入上下文，这个上下文是Aplication的，在Aplication创建的时候，创建了
        initScreenSize();//获取屏幕尺寸的方法
    }

    /**
     * 屏幕的尺寸
     */
    private static void initScreenSize() {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();//上下文，通过资源文件获取到getDisplayMetrics方法
        mDensity = dm.density; //获取到屏幕的密度
        mScreenHeight = dm.heightPixels;//获取到屏幕的高度
        mScreenWidth = dm.widthPixels;//获取到屏幕的宽度
    }

    //延伸Layout至StatusBar - StatusBar透明
    public static void extendStatusBar(Activity activity) {
        //4.4及以上可以使用沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }

    //延伸布局至NavigationBar - NavigationBar透明 - 不可单独使用
    public static void extendNavigationBar(Activity activity) {
        //4.4及以上可以使用沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        //这里有个坑 单单 当Windows设置FLAG_TRANSLUCENT_NAVIGATION
        //屏幕的显示内容区域就会延伸到状态栏上,但状态栏区域依旧被状态栏掩盖 -- 颜色是主题颜色 colorPrimary
        //解决方案1：布局文件 android:layout_marginTop="" getStatusBarHeight()
        //解决方案2：extendStatusBar() + 解决方案1
    }

    //获取StatusBar的高度
    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //检查设备是否具有NavigationBar - (全面屏返回 Ture)
    public static boolean checkDeviceHasNavigationBar(Activity activity) {
        boolean hasNavigationBar = false;
        Resources rs = activity.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }

    public static int getNavigationBar(Context context) {
        return getScreenHeight(context) - getScreenShowHeight(context);
    }

    //屏幕的全高
    public static int getScreenHeight(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    //屏幕显示内容的高
    public static int getScreenShowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }


    public static void moveViewDown(View view) {
        //sdk 4.4: KITKAT
        int distance = getStatusBarHeight(view.getContext());
        // 父控件
        ViewParent parent = view.getParent();
        if (parent instanceof LinearLayout) {
            // 通过marginTop属性让控件往下偏移一段距离
            // 相当于：android:layout_marginTop="24dp"
            ((LinearLayout.LayoutParams) view.getLayoutParams()).topMargin = distance;
        } else if (parent instanceof RelativeLayout) {
            ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin = distance;
        } else if (parent instanceof FrameLayout) {
            ((FrameLayout.LayoutParams) view.getLayoutParams()).topMargin = distance;
        }
        return;
    }

    public static void moveViewDown(View view, int downHeight) {
        //sdk 4.4: KITKAT
        // 父控件
        ViewParent parent = view.getParent();
        if (parent instanceof LinearLayout) {
            // 通过marginTop属性让控件往下偏移一段距离
            // 相当于：android:layout_marginTop="24dp"
            ((LinearLayout.LayoutParams) view.getLayoutParams()).topMargin = downHeight;
        } else if (parent instanceof RelativeLayout) {
            ((RelativeLayout.LayoutParams) view.getLayoutParams()).topMargin = downHeight;
        } else if (parent instanceof FrameLayout) {
            ((FrameLayout.LayoutParams) view.getLayoutParams()).topMargin = downHeight;
        }
        return;
    }

    //-- Color 设置

    //设置 NavigationBar 颜色 5.0 一下机型不支持
    public static void setNavigationBarColor(Activity activity, int navigationBarColor) {
        //API21 以上才能设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            activity.getWindow().setNavigationBarColor(navigationBarColor);
    }

    public static void setStatusBarColor(Activity activity, int statusBarColor) {
        //设置的extendStatusBar将无效
        //  [5.0 以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColor5_0(activity, statusBarColor);
        }

        // [4.4 - 5.0}
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setStatusBarColor4_4_5_0(activity, statusBarColor);
        }
    }

    private static void setStatusBarColor5_0(Activity activity, int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消状态栏透明
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(statusBarColor);
            //设置系统状态栏处于可见状态
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            //让view不根据系统窗口来调整自己的布局
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows(mChildView, false);
                ViewCompat.requestApplyInsets(mChildView);
            }
        }
    }

    private static View mStatusBarView;

    private static void setStatusBarColor4_4_5_0(Activity activity, int statusBarColor) {
        //4.4 - 5.0 不支持直接设置statusBarHeight
        //思路：全屏 - 添加一个View置顶 高为getStatusBarHeight
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //获取状态栏的高度
        int statusBarHeight = getStatusBarHeight(activity);
        //将顶部空间的top padding设置为和状态栏一样的高度，以此达到预期的效果
        ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
        if (mStatusBarView == null) {
            mStatusBarView = new View(activity);
            mStatusBarView.setBackgroundColor(statusBarColor);
        } else {
            // 先解除父子控件关系，否则重复把一个控件多次
            // 添加到其它父控件中会出错
            ViewParent parent = mStatusBarView.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                if (viewGroup != null)
                    viewGroup.removeView(mStatusBarView);
            }
        }
        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        root.addView(mStatusBarView, param);
        return;
    }

    //-- 单位 转换

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }
}