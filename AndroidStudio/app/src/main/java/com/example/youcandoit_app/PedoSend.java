package com.example.youcandoit_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PedoSend extends BroadcastReceiver {

    // 현재 시간 가져오기.
    long mNow;
    SimpleDateFormat mFormatHour = new SimpleDateFormat("HH");

    private String getTime(){
        mNow = System.currentTimeMillis();
        return mFormatHour.format(mNow);
    }

    //현재 시간
    String current_hour = getTime();

    //업데이트 횟수
    int update_count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            // Set the alarm here.
//
//        }

        Log.d("pedoSend.java", "pedoSend.java 진입 - PedoSend 알람 실행. ");

        ((Pedometer) Pedometer.PedoContext).pedoSend(); //만보기 db전송하는 함수 실행시키기.
        Log.d("pedoSend.java", "만보기 결과값이 전송 되었습니다.");
        Log.d("pedoSend.java", "현재 시간 : " + current_hour + "시");
        Log.d("pedoSend.java", "오늘 업데이트 횟수 : " + ++update_count);

        //자정일때
        if(current_hour.startsWith("00")){
            Log.d("pedoSend.java", "자정이므로 만보기를 초기화 합니다. ");
            ((Pedometer) Pedometer.PedoContext).countReset(); //만보기 초기화 하는 함수 실행 시키기.
            update_count=0;
        }

        Toast.makeText(context, "PedoSend", Toast.LENGTH_LONG).show();

    }
}