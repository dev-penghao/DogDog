package im.penghao.sdk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private String createTable;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        createTable="create table if not exists android_metadata"+" ("
                +"id integer primary key autoincrement, "
                +"type integer, "
                +"msg_when long, "
                +"msg_from text, "// from,to,when这三个都是SQL的保留字，所以这里才加前缀
                +"msg_to text, "
                +"content text)";
    }

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName) {
        super(context, name, factory, version);
        createTable="create table if not exists "+tableName+" ("
                +"id integer primary key autoincrement, "
                +"type integer, "
                +"msg_when long, "
                +"msg_from text, "// from,to,when这三个都是SQL的保留字，所以这里才加前缀
                +"msg_to text, "
                +"content text)";
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
