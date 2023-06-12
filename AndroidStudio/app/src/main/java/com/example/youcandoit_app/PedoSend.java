package com.example.youcandoit_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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



    @Override
    public void onReceive(Context context, Intent intent) {

        /**************************1시간마다 내부 db 업데이트 & 서버 db로 전송 **********************************/


        Log.i("pedoSend.java", "pedoSend.java 진입 - PedoSend 알람 실행. ");
        Toast.makeText(context, "PedoSend.java 실행", Toast.LENGTH_LONG).show();
        Log.i("pedoSend.java", "현재 시간 : " + current_hour + "시");


        //오늘 업데이트 해야하는 횟수
        int do_update_count =((Pedometer) Pedometer.PedoContext).do_update_count;

        Log.i("pedoSend.java", "오늘 업데이트 해야하는 횟수 -1 : " + (do_update_count-1));
        // -> 1을 빼는 이유
        /**
         하루의 마지막 업데이트가 00시 이후에 실행이 된다면(하루의 시작 00시 업데이트 말고)-> 만보기 결과값이 다음날에 저장될것임.
         -> 업데이트 해야하는 횟수를 하나를 줄이자 -> 23시가 마지막 업데이트이고,
         23:30에 실시간 업데이트 알람매니저가 실행됨.
         */

        if((do_update_count-1) != 0){
            //내부 db 업데이트 (SQLite db update)
            ((Pedometer) Pedometer.PedoContext).update();
            Log.i("pedoSend.java", "내부 db 업데이트 완료");
            Log.i("pedoSend.java", "오늘 업데이트 한 횟수 : " + ++((Pedometer) Pedometer.PedoContext).update_count);

            //서버 db 업데이트
            SimpleDateFormat FormatDate = new SimpleDateFormat("yyyy-MM-dd"); //년월일 포맷
            String today = FormatDate.format(System.currentTimeMillis()); //오늘 날짜
            Log.i("pedoSend.java", "오늘 날짜 : " + today);

            ((Pedometer) Pedometer.PedoContext).pedoSend(today); //만보기 db전송하는 함수 실행시키기.
            Log.i("pedoSend.java", "서버 db 업데이트 완료");
        }


        //오늘 업데이트를 다했다면(or 23시 이후에 실행되었다면) 알람매니저 해제.
        if(((Pedometer) Pedometer.PedoContext).update_count == (do_update_count-1)){
            ((Pedometer) Pedometer.PedoContext).cancelPedoSendAlarm();
            ((Pedometer) Pedometer.PedoContext).update_count=0; //오늘 업데이트 한 횟수 초기화.
        }

        //자정일때
        // 자정 이후에 실행 되었고 자정과의 시간차가 7분 안일때(00시59분 작동 방지) or
        // 자정 전에 실행 되었고 다음날 자정과의 시간차 7분 안일 때(86,400,000ms(1일)에서 오늘 자정과의 차이를 빼 다음날 자정과의 차이를 만들어준다.)
//        if((current_hour.startsWith("00") && mDifference <= (1000 * 60 * 7)) ||
//                (current_hour.startsWith("23") && (86400000 - mDifference) <= (1000 * 60 * 7))) {
//            Log.i("pedoSend.java", "자정이므로 만보기를 초기화 합니다. ");
//            ((Pedometer) Pedometer.PedoContext).countReset(); //만보기 초기화 하는 함수 실행 시키기.
//        }

    }
}