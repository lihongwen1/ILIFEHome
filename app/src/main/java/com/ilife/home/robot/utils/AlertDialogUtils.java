package com.ilife.home.robot.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ilife.home.robot.R;

/**
 * Created by chengjiaping on 2018/8/14.
 */

public class AlertDialogUtils {
    static final String TAG = AlertDialogUtils.class.getSimpleName();

    public static AlertDialog showDialogNoCancel(Context context, View contentView, int width, int height) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog_Style);
        builder.setView(contentView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setLayout(width, height);
        return dialog;
    }
 public static AlertDialog showDialog(Context context, View contentView, int width, int height) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog_Style);
        builder.setView(contentView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(width, height);
        return dialog;
    }

    public static void hidden(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static AlertDialog showDialogBottom(Context context, View contentView, int width, int height, int yOffset) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog_Style);
        builder.setView(contentView);
        AlertDialog dialog = builder.create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(width, height);
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = yOffset;
            window.setAttributes(params);
        }
        return dialog;
    }
}
