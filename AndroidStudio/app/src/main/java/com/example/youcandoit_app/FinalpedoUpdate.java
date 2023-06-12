package com.example.youcandoit_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FinalpedoUpdate extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("FinalpedoUpdate.java", "FinalpedoUpdate.java로 진입");

//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            // Set the alarm here.
//
//        }

        //최종 집계는 1:00에 이루어짐. 전날 날짜의 값으로 가져와야함.

        //년월일 포맷
        SimpleDateFormat FormatDate = new SimpleDateFormat("yyyy-MM-dd");

        //오늘 날짜
        String today = FormatDate.format(System.currentTimeMillis());

        //어제 날짜
        Date yesterday_b;
        yesterday_b = new Date();
        yesterday_b.setTime(System.currentTimeMillis() - (long)(1000*60*60*24));

        String yesterday_a = FormatDate.format(yesterday_b);
        Log.i("FinalpedoUpdate.java", "오늘 날짜 : " + today);
        Log.i("FinalpedoUpdate.java", "어제 날짜 : " + yesterday_a);

        ((Pedometer) Pedometer.PedoContext).pedoSend(yesterday_a); //만보기 db전송하는 함수 실행시키기.
        Log.i("FinalpedoUpdate.java", "어제날짜 인자값 전달하여 pedoSend()실행. ");


    }
}