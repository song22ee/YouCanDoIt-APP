package com.example.youcandoit_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PedoSend extends BroadcastReceiver {

    // 현재 시간 가져오기.
    long mNow;
    long mDifference;
    SimpleDateFormat mFormatHour = new SimpleDateFormat("HH");

    private String getTime(){
        mNow = System.currentTimeMillis();

        Calendar midnightCal = Calendar.getInstance();
        midnightCal.setTimeInMillis(mNow);
        midnightCal.set(Calendar.HOUR_OF_DAY, 0);
        midnightCal.set(Calendar.MINUTE,0);
        midnightCal.set(Calendar.SECOND, 0);

        mDifference = mNow - midnightCal.getTimeInMillis(); // 오늘 자정과의 차이
        Log.d("pedoSend.java", "현재 밀리세컨드 : " + mNow);
        Log.d("pedoSend.java", "자정 밀리세컨드 : " + midnightCal.getTimeInMillis());
        Log.d("pedoSend.java", "차이 밀리세컨드 : " + (mNow - midnightCal.getTimeInMillis()));

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
//        if(current_hour.startsWith("00")){
        // 자정 이후에 실행 되었고 자정과의 시간차가 7분 안일때(00시59분 작동 방지) or
        // 자정 전에 실행 되었고 다음날 자정과의 시간차 7분 안일 때(86,400,000ms(1일)에서 오늘 자정과의 차이를 빼 다음날 자정과의 차이를 만들어준다.)
        if((current_hour.startsWith("00") && mDifference <= (1000 * 60 * 7)) ||
                (current_hour.startsWith("23") && (86400000 - mDifference) <= (1000 * 60 * 7))) {
            Log.d("pedoSend.java", "자정이므로 만보기를 초기화 합니다. ");
            ((Pedometer) Pedometer.PedoContext).countReset(); //만보기 초기화 하는 함수 실행 시키기.
            update_count=0;
        }

        Toast.makeText(context, "PedoSend", Toast.LENGTH_LONG).show();

    }
}