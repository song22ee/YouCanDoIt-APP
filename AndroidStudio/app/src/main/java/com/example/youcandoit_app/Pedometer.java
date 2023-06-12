package com.example.youcandoit_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Pedometer extends AppCompatActivity implements SensorEventListener {

    public static Context PedoContext; // context 변수 선언 - PedoSend.java에서 사용하기 위해 선언.
    Intent i;
    String id;
    String nickname;
    TextView text;
    DBHelper helper;
    SQLiteDatabase db;

    public boolean isFirst = true; // 오늘 pedometer SQLite DB에 insert를 최초로 했는지.


    //현재 년월일
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String getDate(){
        Date mDate;
        mDate = new Date(System.currentTimeMillis());
        return mFormat.format(mDate);
    }

    //현재 시각
    SimpleDateFormat mFormatHour = new SimpleDateFormat("HH");
    private String getTime(){
        return mFormatHour.format(System.currentTimeMillis());
    }

    //현재 시분초
    SimpleDateFormat mFormatRTC = new SimpleDateFormat("HH:mm:ss");
    private String getRTC(){
        return mFormatRTC.format(System.currentTimeMillis());
    }


    //현재 시간
    String current_hour = getTime();


    //만보기
    SensorManager sm;
    Sensor sensor_accelerometer;
    long myTime1, myTime2;

    float x, y, z;
    float lastX, lastY, lastZ;

    final int walkThreshold = 455; //걷기 인식 임계 값
    double acceleration = 0;

//    long startTime;

//    long currentTime;

    public int walkingCount=0; //만보기 변수

    TextView tv_step;

    ValueHandler handler = new ValueHandler();

    //알람 매니저
    public static AlarmManager pedoSendAlarmManager = null;
    public static PendingIntent pedoSender =null;
    public static AlarmManager RTC_pedoUpdateManager = null;
    public static PendingIntent RTC_pedoSender =null;
    public static AlarmManager F_pedoUpdateManager = null;
    public static PendingIntent F_pedoSender =null;

    // 오늘 업데이트 해야하는 횟수
    int do_update_count =0 ;
    //오늘 업데이트 한 횟수
    int update_count = 0;

    //실시간 업데이트 스레드 선언.
    RTCpedoUpThreadRunnable runnable = new RTCpedoUpThreadRunnable();
    Thread RTCpedoUPThread = new Thread(runnable);

    protected void onResume() {
        super.onResume();
        if (sensor_accelerometer != null) {
            sm.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.v("Pedometer.java", "onSensorChanged()");
            myTime2 = System.currentTimeMillis();
            long gab = myTime2 - myTime1;//시간차

            if(gab > 90) {
                myTime1 = myTime2;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                acceleration = Math.abs(x + y + z - lastX - lastY - lastZ) / gab * 9000; //이동 속도 공식

                if (acceleration > walkThreshold) {
                    walkingCount += 1.0;
                    Log.i("Pedometer.java","onSensorChanged(): 값이 올라감");
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }

            //만보가 최초로 올라갈때 insert 후 update alarmManager 세팅.
            if(walkingCount == 1){
                if(isFirst){// isFirst = 오늘 최초로 insert를 하는 건지.
                    try{
                        Log.i("pedoSend.java", "onSensorChanged() : 만보기가 최초로 올라감. if(walkingCount == 1)문 실행.");
                        Log.i("pedoSend.java", "onSensorChanged() : walkingCount="+walkingCount);
                        String today = getDate();
                        id = i.getStringExtra("id");

                        Log.i("pedoSend.java", "onSensorChanged() : id="+id);
                        Log.i("pedoSend.java", "onSensorChanged() : pedometer_date="+today);

                        ContentValues values = new ContentValues();

                        values.put("dateid", today+id);
                        values.put("date", today);
                        values.put("id", id);
                        values.put("step", walkingCount);

                        Log.i("pedoSend.java", "onSensorChanged() : values=" + values);
                        db.insert("pedometer", null, values);
                        isFirst=false; //insert를 했으므로 false로 변경.
                        Log.i("pedoSend.java", "onSensorChanged() : pedometer db insert 완료됨.");
                        Log.i("pedoSend.java", "onSensorChanged() : select()실행. 오늘날짜, 해당 아이디의 pedometer data값.");

                        select(today,id);

                        //오늘 업데이트 해야 하는 횟수.
                            /* 구하는 방식
                                예시 1. 22:10에 로그인을 처음한 사용자
                                -> 23:00, 24:00 업데이트가 되어야 함. -> do_update_count = 24-22 = 2

                                예시 2. 21:30에
                                -> 22:00,23:00,24:00 -> do_update_count = 24-21=3

                                예시 3. 00:00에 로그인을 처음한 사용자
                                ->do_update_count = 24-0=24

                                예시 4. 23:30
                                -> do_update_count = 24-23 =1

                                => update_count == do_update_count 될때까지 업데이트 해야함.
                            */
                        Log.i("Pedometer.java", "현재 시간 : " + current_hour + "시");
                        do_update_count = 24 - Integer.parseInt(current_hour);
                        Log.i("Pedometer.java", "오늘 업데이트 해야하는 횟수 : " + do_update_count);

                        //1시간마다 만보기 결과 내부,서버 db 업데이트하는 알람매니저 등록.
                        setPedoSendAlarm();
                        Log.i("Pedometer.java", "onSensorChanged() : setPedoSendAlarm() 실행.");

                    }catch (Exception e){
                        Log.e("Pedometer.java", "onSensorChanged() :  if(walkingCount == 1)오류 발생.");
                        e.printStackTrace();
                    }
                }
            }else if(walkingCount >= 2){ // 2보다 클때
                if(!isFirst){
                    isFirst = true; //내일 pedometer SQLite db에 최초로 insert를 위해 미리 true로 세팅.
                }
                if (acceleration > walkThreshold) {
                    walkingCount += 1.0;
                    Log.i("Pedometer.java","onSensorChanged(): 값이 올라감");
                    Log.i("Pedometer.java","onSensorChanged(): update()");
                    update();
                }
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    class ValueHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                tv_step.setText(walkingCount+"");
                Log.v("Pedometer.java:", "PedoUpThread 실행중");
            }else if(msg.what == 1){
                //23:30~23:59에 실시간으로 내부 db 업데이트
                String RTC = getRTC(); // 현재 시간, 분, 초
                Log.i("Pedometer.java", "ValueHandler : 현재시간 = " + RTC);
//                update();
                //자정일때
                if(RTC.equals("00:00:00")){
                    Log.i("pedoSend.java", "ValueHandler : 자정이므로 만보기를 초기화 합니다. ");
                    walkingCount=0; //만보기 초기화
                    //알람매니저 해제.
                    cancelRTCAlarm();

                    //실시간 update 스레드 멈추기
                    Log.i("Pedometer.java","runnable.stop() 실행.");
                    runnable.stop();

                    //1:00 최종집계 알람매니저 등록.
                    finalUpdateAlarm();

                }
            }
        }
    }

    //스레드 클래스 생성 - 만보기 기능.
    class PedoUpThread extends Thread {
        boolean running = false;
        public void run() {
            running = true;
            while (running) {
                handler.sendEmptyMessage(0);
//                currentTime = System.currentTimeMillis();
                SystemClock.sleep(1000);//1초 간격
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Pedometer.java:","로그인 완료");
        Log.i("Pedometer.java:","Pedometer.java에 들어옴.");
        Log.i("Pedometer.java","현재 시간 : " + current_hour +"시");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);



        tv_step = findViewById(R.id.tv_step);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        text = (TextView) findViewById(R.id.text);

        PedoContext = this; //onCreate에서 this 할당 -> 다른 activity에서 Pedometer.java 사용하기 위해.



        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

//        startTime = System.currentTimeMillis();

        //사용자 닉네임 보여주기.
        i = getIntent();
        nickname=i.getStringExtra("nickname");
        text.setText(nickname+" 님");

        //만보기능 스레드 시작.
        PedoUpThread pedoUpThread = new PedoUpThread();
        pedoUpThread.start();






        //내부 DB SQLite - 안드로이드는 SQLite를 지원한다.
        helper = new DBHelper(Pedometer.this, "ycdiApp.db", null, 1);
        db = helper.getWritableDatabase(); //수정가능하게 db를 불러옴.
        helper.onCreate(db);


        //23:30분에 실시간 내부 DB update 알람매니저 등록.
//        RTC_pedoUpdate();



    }

    //알람매니저 생성 - 1시간마다 만보기 결과 내부, 서버 db 업데이트
    public void setPedoSendAlarm(){
        Log.i("Pedometer.java", "setPedoSendAlarm() 진입 - 1시간 마다 만보기 결과 내부,서버 db update ");
        pedoSendAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent pedoSendIntent = new Intent(this, PedoSend.class);
        pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 0, pedoSendIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 사용자가 로그인 한 현재 시간이 15:30이면 16:00에 업데이트가 되어야함. -> 현재 시간 +1 시간에 업데이트해야함.
        int hour = Integer.parseInt(current_hour)+1; //현재시간에 한시간 더한 시간.

        // 현재 시각 + 1 정각으로 알람 시간 세팅.
        Calendar pedoSendCal = Calendar.getInstance();
        pedoSendCal.setTimeInMillis(System.currentTimeMillis());
        pedoSendCal.set(Calendar.HOUR_OF_DAY, hour);
        pedoSendCal.set(Calendar.MINUTE,0);
        pedoSendCal.set(Calendar.SECOND, 0);

        //기존 alarmManager삭제
        pedoSendAlarmManager.cancel(pedoSender);

        // 설정한 시간에 1시간 간격으로 pedoSender객체가 실행됨.
            //AlarmManager.RTC_WAKEUP 는 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행.
            //AlarmManager.INTERVAL_HOUR = 1시간을 의미.
        pedoSendAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, pedoSendCal.getTimeInMillis()
                , AlarmManager.INTERVAL_HOUR, pedoSender);

        // 설정한 시간 보기
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setPedoSendTime = format.format(new Date(pedoSendCal.getTimeInMillis()));
        Log.i("Pedometer.java", "setPedoSendAlarm() : 알람매니저 시작 시간 : " + setPedoSendTime);
        Log.i("Pedometer.java", "setPedoSendAlarm() : 1시간 마다 만보기 결과 내부,서버 db update가 이루어집니다. ");

    }

    //알람매니저 해제
    public void cancelPedoSendAlarm() {
        Log.i("Pedometer.java", "cancelPedoSendAlarm()로 진입. ");

        if (pedoSender != null) {
            pedoSendAlarmManager = (AlarmManager) this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this.getApplicationContext(), PedoSend.class);
            pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            pedoSendAlarmManager.cancel(pedoSender);
            pedoSender.cancel();
            pedoSendAlarmManager = null;
            pedoSender = null;
        }
        Log.i("Pedometer.java", "cancelPedoSendAlarm() : setPedoSendAlarm 알람매니저 삭제 ");

    }

    //알람매니저 생성 - 23:30~23:59 실시간 내부 db pedometer 업데이트
    public void RTC_pedoUpdate(){
        Log.i("Pedometer.java", "RTC_pedoUpdate() 진입 - 23:30~23:59 실시간 내부 db pedometer 업데이트 ");
        RTC_pedoUpdateManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent RTC_pedoUdate_i = new Intent(this, RTCpedoUpdate.class);
        RTC_pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 1, RTC_pedoUdate_i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar RTC_pedoUdateCal = Calendar.getInstance();
        RTC_pedoUdateCal.setTimeInMillis(System.currentTimeMillis());
        RTC_pedoUdateCal.set(Calendar.HOUR_OF_DAY, 23);
        RTC_pedoUdateCal.set(Calendar.MINUTE,30);
        RTC_pedoUdateCal.set(Calendar.SECOND, 0);

        // 설정한 시간에 일회성으로 실행.
        //AlarmManager.RTC_WAKEUP 는 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행.
        RTC_pedoUpdateManager.set(AlarmManager.RTC_WAKEUP, RTC_pedoUdateCal.getTimeInMillis(), RTC_pedoSender);

        // 설정한 시간 보기
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setPedoSendTime = format.format(new Date(RTC_pedoUdateCal.getTimeInMillis()));
        Log.i("Pedometer.java", "RTC_pedoUpdate() : 알람매니저 시작 시간 : " + setPedoSendTime);
        Log.i("Pedometer.java", "RTC_pedoUpdate() : 실시간 내부 db pedometer 업데이트가 23:30~23:59 동안 이루어집니다.");

    }

    //알람매니저 해제
    public void cancelRTCAlarm() {
        Log.i("Pedometer.java", "cancelRTCAlarm()로 진입. ");
        if (RTC_pedoSender != null) {
            RTC_pedoUpdateManager = (AlarmManager) this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent RTC_pedoUdate_i = new Intent(this.getApplicationContext(), RTCpedoUpdate.class);
            RTC_pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 1, RTC_pedoUdate_i, PendingIntent.FLAG_CANCEL_CURRENT);
            RTC_pedoUpdateManager.cancel(RTC_pedoSender);
            RTC_pedoSender.cancel();
            RTC_pedoUpdateManager = null;
            RTC_pedoSender = null;
            Log.i("Pedometer.java", "cancelRTCAlarm() : RTC_pedoUpdate 알람매니저 삭제 ");

        }
    }

    //1:00 최종 집계 알람매니저 등록
    public void finalUpdateAlarm(){
        Log.i("Pedometer.java", "finalUpdateAlarm() 진입 - 1:00 최종 집계 알람매니저 ");

        F_pedoUpdateManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent F_pedoUdate_i = new Intent(this, FinalpedoUpdate.class);
        F_pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 2, F_pedoUdate_i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar F_pedoUdateCal = Calendar.getInstance();
        F_pedoUdateCal.setTimeInMillis(System.currentTimeMillis());
        F_pedoUdateCal.set(Calendar.HOUR_OF_DAY, 1);
        F_pedoUdateCal.set(Calendar.MINUTE, 0);
        F_pedoUdateCal.set(Calendar.SECOND, 0);

        // 설정한 시간에 일회성으로 실행.
        //AlarmManager.RTC_WAKEUP 는 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행.
        F_pedoUpdateManager.set(AlarmManager.RTC_WAKEUP, F_pedoUdateCal.getTimeInMillis(), F_pedoSender);

        // 설정한 시간 보기
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setPedoSendTime = format.format(new Date(F_pedoUdateCal.getTimeInMillis()));
        Log.i("Pedometer.java", "finalUpdateAlarm() : 알람매니저 시작 시간 : " + setPedoSendTime);
        Log.i("Pedometer.java", "finalUpdateAlarm() : 1:00에 전날 만보기 최종 집계가 이루어집니다.");


    }



    //만보기 결과 db 전송 - pedoSendAlarm()로 인해 1시간 간격으로 PedoSend.java에서 이 함수가 실행 되게 됨.
    public void pedoSend(String pedometer_date){
        try {
            Log.i("Pedometer.java:", "pedoSend() 진입");
            //pedometer_date는 PedoSend.java에서 인자로 받아옴.
            id = i.getStringExtra("id");
            String pedometer_result = select(pedometer_date,id);
            Log.i("Pedometer.java:", "pedoSend() : pedometer_date = " + pedometer_date);
            Log.i("Pedometer.java:", "pedoSend() : id = " + id);
            Log.i("Pedometer.java:", "pedoSend() : pedometer_result = " + pedometer_result);
            DBsendActivity task = new DBsendActivity(); //DBsendActivity.java 객체 생성.
            task.execute(pedometer_date,id,pedometer_result).get(); //DBsendActivity.java로 현재날짜, 아이디, 만보기 결과값 전송.
            Log.i("Pedometer.java:", "pedoSend() : 만보기결과값 전송됨.");
        } catch (Exception e) {
            Log.i("Pedometer.java:", "pedoSend() : .....ERROR.....!");
        }
    }


    //SQLite db
    @SuppressLint("Range")
    public String select(String date, String id) {
        String step = "";
//        String sql = String.format("select * from pedometer where date='%s' and id='%s';",date,id);
        String sql = "select * from pedometer;";
        Cursor c = db.rawQuery(sql, null);
        System.out.println("------------------pedometer db data----------------");
        while(c.moveToNext()){
            System.out.println("-------------------");
            System.out.println("dateid : "+c.getString(c.getColumnIndex("dateid")));
            System.out.println("date : "+c.getString(c.getColumnIndex("date")));
            System.out.println("id : "+c.getString(c.getColumnIndex("id")));
            System.out.println("step : "+c.getString(c.getColumnIndex("step")));
            step = c.getString(c.getColumnIndex("step"));
        }
        System.out.println("---------------------------------------------------");
        return step;
    }

    public void update(){
        ContentValues updateVal = new ContentValues();
        String today = getDate();
        id = i.getStringExtra("id");
        updateVal.put("step",walkingCount);
        db.update("pedometer",updateVal,"date=? AND id=?", new String[]{today,id});
        select(today,id);
    }


    //RTCpedoUPThread 쓰레드 실행 함수.
    public void exe_RTCpedoUPThread(){
        Log.i("Pedometer.java", "exe_RTCpedoUPThread() : RTCpedoUPThread 실행");
        //실시간 스레드 시작.
        RTCpedoUPThread.start();
    }


}