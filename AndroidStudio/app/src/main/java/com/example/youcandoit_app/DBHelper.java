package com.example.youcandoit_app;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//db 생성
public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //mytable이라는 table 생성.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE if not exists pedometer ("
                + "dateid text primary key,"
                + "date text,"
                + "id text,"
                + "step int);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists pedometer";

        db.execSQL(sql);
        onCreate(db);
    }
}