package com.example.youcandoit_app;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class PedometerDAO extends AppCompatActivity {
    public static Context context;


    DBHelper helper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //내부 DB SQLite - 안드로이드는 SQLite를 지원한다.
        helper = new DBHelper(PedometerDAO.this, "ycdiApp.db", null, 1);
        db = helper.getWritableDatabase(); //수정가능하게 db를 불러옴.
        helper.onCreate(db);
    }

    public void insertOne(PedometerDTO dto) {
        Log.d("PedometerDAO.java", "PedometerDAO.java insertOne()로 진입.");
        ContentValues values = new ContentValues();
        values.put("date", dto.getDate());
        values.put("id", dto.getId());
        values.put("step", dto.getStep());
        Log.d("PedometerDAO.java", "insertOne() : values=" + values);
        db.insert("pedometer", null, values);
        Log.d("PedometerDAO.java", "insertOne() : pedometer db insert 완료됨.");
        Log.d("PedometerDAO.java", "insertOne() : select()실행. 오늘날짜, 해당 아이디의 pedometer data값.");

        select(dto);

    }

    @SuppressLint("Range")
    public void select(PedometerDTO dto) {
        String sql = String.format("select * from pedometer where date='%s' and id='%s';",dto.getDate(),dto.getId());
        Cursor c = db.rawQuery(sql, null);
        while(c.moveToNext()){
            System.out.println("date : "+c.getString(c.getColumnIndex("date")));
            System.out.println("id : "+c.getString(c.getColumnIndex("id")));
            System.out.println("step : "+c.getString(c.getColumnIndex("step")));
        }
    }


}
