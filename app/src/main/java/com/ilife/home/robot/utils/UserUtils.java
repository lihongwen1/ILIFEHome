package com.ilife.home.robot.utils;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.ilife.home.robot.R;
import com.ilife.home.robot.app.MyApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenjiaping on 2017/7/6.
 */

public class UserUtils {

    public static boolean isPhone(String phone) {
//        if (phone.length() == 11) {
//            Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$");//新加所有18段 17段
//            Matcher m = p.matcher(phone);
//            return m.matches();
//        } else {
//            return false;
//        }
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$");//新加所有18段 17段
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    public static boolean isResetPw(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String mode = bundle.getString("mode");
                if (!TextUtils.isEmpty(mode)) {
//                    if (mode.equals(LoginActivity.MODE)){
//                        return true;
//                    }
                }
            }
        }
        return false;
    }

    public static boolean rexCheckPassword(String pass) {
//        String str= "^([A-Z]|[a-z]|[0-9]|[~!@#$%^&*()_+|<>,.?/:;'\\\\[\\\\]{}\\\"]){6,20}$";
        //亲测这个是可以的
//        String str= "^([A-Z0-9a-z]|[~!@#$%^&*()_+|=<>,.?/:\\\\;'\\[\\]\\{}\"])*$";
        String str = "[a-z0-9A-Z`~!@#$%^&*()+=_|\\-{\\\\}'\":;,\\[\\].<>/?¥…]+";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(pass);
        return m.matches();
    }

    public static boolean checkPassword(String pw) {
//        String str = "^[A-Z0-9a-z]{6,18}$";
        String str = "[a-z0-9A-Z`~!@#$%^&*()+=_|\\-{\\\\}'\":;,\\[\\].<>/?¥…]{6,18}";
//        String str = "^{6,18}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(pw);
        return m.matches();
    }


    public static void setInputFilter(EditText editText, int maxLength) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                CharSequence value = super.filter(source, start, end, dest, dstart, dend);
                if (value == "") {
                    ToastUtils.showToast(MyApplication.getInstance().getString(R.string.name_max_length,maxLength+""));
                }
                return value;
            }
        }, emojiFillter()});
    }

    /**
     * emoji表情限制输入
     * @return
     */
    public static InputFilter emojiFillter(){
        InputFilter inputFilter= new InputFilter() {
            Pattern emoji = Pattern.compile("[\ud83e\udc00-\ud83e\udfff]|[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                MyLogger.d("EMOJI",source.toString());
                Matcher emojiMatcher = emoji.matcher(source);
                if (emojiMatcher.find()) {
                    return "";
                }
                return null;
            }
        };
        return inputFilter;
    }
    /**
     * 判断是否包含特殊字符
     * @return  false:未包含 true：包含
     */
    public static boolean isContainsSpecialCharacter(String value) {
        String speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(speChat);
        Log.d("inputJudge", "pattern: "+ pattern);
        Matcher matcher = pattern.matcher(value);
        Log.d("inputJudge", "matcher: "+ matcher);
        return matcher.find();
    }

    public static boolean isMatch(String str) {
        String regex = "[\\u4E00-\\u9FA5a-z0-9A-Z`~!@#$%^&*()+=-|{\\\\}':;,[\\\\].<>/?¥…\\s]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static boolean checkPhysicalId(String id) {
        String str = "^[A-Z0-9a-z]{12}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(id);
        return m.matches();
    }

    public static String exChange(String str) {
        StringBuffer sb = new StringBuffer();
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (Character.isLowerCase(c)) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
