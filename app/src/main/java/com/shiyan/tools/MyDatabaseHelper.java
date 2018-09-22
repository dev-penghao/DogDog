package com.shiyan.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private final String createTable;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName) {
        super(context, name, factory, version);
//        createTable = "create table "+table+" ("
//                + "id integer primary key autoincrement, "
//                + "from_num text, "
//                + "to_num text, "
//                + "when long, "
//                + "msgSize long, "
//                + "type integer, "
//                + "textContent text)";
//
        createTable="create table if not exists "+tableName+" ("
            +"id integer primary key autoincrement, "
            +"msg_from text, "
            +"msg_to text, "
            +"msg_when long, "
            +"msgSize long, "
            +"type integer, "
            +"textContent text)";
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL(createTable);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
