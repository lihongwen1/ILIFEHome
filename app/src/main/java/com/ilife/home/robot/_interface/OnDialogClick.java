package com.ilife.home.robot._interface;

public interface OnDialogClick<T> {
    void onCancelClick();

    void onConfirmClick(T t);

}
