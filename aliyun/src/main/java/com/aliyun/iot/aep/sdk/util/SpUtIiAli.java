package com.aliyun.iot.aep.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.aliyun.iot.aep.sdk.framework.AApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SpUtIiAli {
    private static SharedPreferences a;

    public SpUtIiAli() {
    }

    private static SharedPreferences a(Context context) {
        if (a == null) {
            if (context == null) {
                context = AApplication.getInstance();
            }

            a = ((Context)context).getSharedPreferences("GlobalConfigFW", 0);
        }

        return a;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = a(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences preferences = a(context);
        return preferences.getString(key, "");
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = a(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = a(context);
        return sp.getBoolean(key, defValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp = a(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = a(context);
        return sp.getLong(key, defValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = a(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = a(context);
        return sp.getInt(key, defValue);
    }


    public static void putList(Context context, String key, List<? extends Serializable> list) {
        try {
            a(context, key, list);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public static <E extends Serializable> List<E> getList(Context context, String key) {
        try {
            return (List)a(context, key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static <K extends String, V extends String> void putMap(Context context, String key, Map<K, V> map) {
        try {
            a(context, key, map);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public static <K extends String, V extends String> Map<K, V> getMap(Context context, String key) {
        try {
            return (Map)a(context, key);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private static void a(Context context, String key, Object obj) throws IOException {
        if (obj != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            String objectStr = new String(Base64.encode(baos.toByteArray(), 0));
            baos.close();
            oos.close();
            putString(context, key, objectStr);
        }
    }

    private static Object a(Context context, String key) throws IOException, ClassNotFoundException {
        String wordBase64 = getString(context, key);
        if (TextUtils.isEmpty(wordBase64)) {
            return null;
        } else {
            byte[] objBytes = Base64.decode(wordBase64.getBytes(), 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            bais.close();
            ois.close();
            return obj;
        }
    }

    public static void remove(Context context, String tag, Object object) {
        try {
            if (object instanceof Boolean) {
                putBoolean(context, tag, false);
            } else if (!(object instanceof Integer) && !(object instanceof Float)) {
                a(context, tag, (Object)null);
            } else {
                putInt(context, tag, -1);
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public static void remove(Context context, String key) {
        SharedPreferences sp = a(context);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.contains(key)) {
            editor.remove(key);
        }

    }

    public static void clean(Context context) {
        SharedPreferences sp = a(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
    }
}
