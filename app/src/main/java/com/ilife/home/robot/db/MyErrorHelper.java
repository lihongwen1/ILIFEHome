package com.ilife.home.robot.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chengjiaping on 2017/8/26.
 */

public class MyErrorHelper extends SQLiteOpenHelper {
    private final String TAG = MyErrorHelper.class.getSimpleName();
    private static final String DB_NAME = "error.db";
    public static final int DB_VERSION = 1;
    private final String SQL_CREATE = "create table errorInfo(_id integer primary key autoincrement," +
            "errorStr text)";

    /*
     * DbHelper的构造方法，当其被初始化的时候，如果数据库不存在，则以
     * DB_NAME创建数据库；数据库如果存在则打开
     * */
    public MyErrorHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //帮助类初始化之后，会调用onCreate方法，在onCreate()中执行建表的操作
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    //数据库升级的时候调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
