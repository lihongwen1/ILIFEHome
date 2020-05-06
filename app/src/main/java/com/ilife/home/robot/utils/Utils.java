package com.ilife.home.robot.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;

import com.aliyun.iot.aep.sdk.contant.EnvConfigure;
import com.ilife.home.robot.BuildConfig;
import com.ilife.home.robot.R;
import com.ilife.home.robot.able.Constants;
import com.ilife.home.robot.app.MyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by chengjiaping on 2018/8/4.
 */

public class Utils {
    public static Typeface getTypeFace(Context context) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCNRegular.ttf");
        return tf;
    }

    public static void setTransformationMethod(EditText editText, boolean isSelected) {
        editText.setTransformationMethod(isSelected ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }

    public static String getString(int id) {
        return MyApplication.getInstance().getString(id);
    }

    /**
     * 是否是ILIFE品牌
     *
     * @return
     */
    public static boolean isIlife() {
        return BuildConfig.BRAND.equals(Constants.BRAND_ILIFE);
    }

    public static boolean isSupportPhone() {
        return true;
    }

    public static boolean isChineseLanguage() {
        String lan = LanguageUtils.getDefaultLanguage();
        Log.d("LANGUAGE", "语言：   " + lan);
        return lan.equals("zh");
    }


    public static int getInputMaxLength() {
        int maxLength;
        if (Utils.isChinaEnvironment()) {
            maxLength = 12;
        } else {
            maxLength = 30;
        }
        return maxLength;
    }

    /**
     * Determine if the account is useful base on whether the brand is supports that the mobile phone number as an account!
     * @param account
     * @return
     */
    public static boolean checkAccountUseful(String account) {
        boolean isAccountUseful = false;
        if (account.isEmpty()) {
            if (Utils.isSupportPhone()) {
                ToastUtils.showToast(Utils.getString(R.string.login_aty_input_email_phone));
            } else {
                ToastUtils.showToast(Utils.getString(R.string.login_aty_input_email));
            }
        } else if (Utils.isSupportPhone()) {//ILIFE China supports phone to receive a verification code
            if (UserUtils.isPhone(account) || UserUtils.isEmail(account)) {
                isAccountUseful = true;
            } else {
                ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_account));
            }
        } else {
            if (UserUtils.isEmail(account)) {
                isAccountUseful = true;
            } else {
                ToastUtils.showToast(MyApplication.getInstance(), Utils.getString(R.string.regist_wrong_email));
            }
        }
        return isAccountUseful;
    }

    /**
     * 是否是国内环境
     */
    public static boolean isChinaEnvironment() {
        return BuildConfig.Area == EnvConfigure.CHINA;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static byte[] concat_(byte[] a, byte[] b, byte type) {
        int offset = 0;
        switch (type) {
            case 1:
                offset = 7;
                break;
            case 2:
                offset = 2;
                break;
        }
        byte[] c = new byte[a.length + b.length - offset];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, offset, c, a.length, b.length - offset);
        return c;
    }

    /**
     * 读取assets本地json
     * @param fileName
     * @param context
     * @return
     */
    public static String getJson(String fileName,Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
