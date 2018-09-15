package com.shiyan.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private String createTable;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String table) {
        super(context, name, factory, version);
        createTable="create table "+table+"("
            +"id integer primary key autoincrement,"
            +"from text,"
            +"to text,"
            +"when long,"
            +"msgSize long,"
            +"type integer,"
            +"textContent text)";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
