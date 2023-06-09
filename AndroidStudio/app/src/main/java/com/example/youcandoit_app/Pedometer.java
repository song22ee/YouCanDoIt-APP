package com.example.youcandoit_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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

public class Pedometer extends AppCompatActivity implements SensorEventListener {

    public static Context PedoContext; // context 변수 선언 - PedoSend.java에서 사용하기 위해 선언.
    Intent i;
    String id;
    String nickname;
    TextView text;

   //현재 날짜 가져오기.
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat mFormatHour = new SimpleDateFormat("HH");

    private String getDate(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        return mFormatHour.format(mNow);
    }

    //현재 날짜
    String pedometer_date = getDate();

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

    long startTime;

    long currentTime;

    public int walkingCount=0; //만보기 변수

    TextView tv_step;

    ValueHandler handler = new ValueHandler();

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
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
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
                Log.i("Pedometer.java:", "PedoUpThread 실행중");
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
                currentTime = System.currentTimeMillis();
                SystemClock.sleep(1000);//1초 간격
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Pedometer.java:","Pedometer.java에 들어옴.");
        Log.v("Pedometer.java","현재 시간 : " + current_hour +"시");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

        tv_step = findViewById(R.id.tv_step);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        text = (TextView) findViewById(R.id.text);

        PedoContext = this; //onCreate에서 this 할당 -> PedoSend.java에서 사용.

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        startTime = System.currentTimeMillis();

        //사용자 닉네임 보여주기.
        i = getIntent();
        nickname=i.getStringExtra("nickname");
        text.setText(nickname+" 님");

        //만보기능 스레드 시작.
        PedoUpThread pedoUpThread = new PedoUpThread();
        pedoUpThread.start();

        //1시간마다 만보기 결과 db로 전송하기.
        pedoSendAlarm();
//        resetAlarm();
    }

    //1시간마다 만보기 결과 db로 전송하기
    public void pedoSendAlarm(){
        AlarmManager pedoSendAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent pedoSendIntent = new Intent(this, PedoSend.class);
        PendingIntent pedoSender = PendingIntent.getBroadcast(this.getApplicationContext(), 0, pedoSendIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int hour = Integer.parseInt(current_hour)+1; //현재시간에 한시간 더한 시간.

        // 현재 시각의 1시간 이후 정각으로 알람 시간 세팅.
        Calendar pedoSendCal = Calendar.getInstance();
        pedoSendCal.setTimeInMillis(System.currentTimeMillis());
        pedoSendCal.set(Calendar.HOUR_OF_DAY, hour);
        pedoSendCal.set(Calendar.MINUTE,0);
        pedoSendCal.set(Calendar.SECOND, 0);

        // 설정한 시간에 1시간 간격으로 pedoSender객체가 실행됨.
            //AlarmManager.RTC_WAKEUP 는 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 인텐트를 실행.
            //AlarmManager.INTERVAL_HOUR = 1시간을 의미.
        pedoSendAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, pedoSendCal.getTimeInMillis()
                , AlarmManager.INTERVAL_HOUR, pedoSender);

        // 설정한 시간 보기
        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setPedoSendTime = format.format(new Date(pedoSendCal.getTimeInMillis()));
        Log.d("pedoSendAlarm", "pedoSendHour : " + setPedoSendTime);

    }

    //만보기 결과 db 전송 - pedoSendAlarm()로 인해 1시간 간격으로 PedoSend.java에서 이 함수가 실행 되게 됨.
    public void pedoSend(){
        try {
            Log.d("Pedometer.java:", "pedoSend() 진입");
            id = i.getStringExtra("id");
            String pedometer_result = String.valueOf(tv_step.getText());
            DBsendActivity task = new DBsendActivity(); //DBsendActivity.java 객체 생성.
            task.execute(pedometer_date,id,pedometer_result).get(); //DBsendActivity.java로 현재날짜, 아이디, 만보기 결과값 전송.
            Log.i("Pedometer.java:", "pedoSend() : 만보기결과값 전송됨.");
        } catch (Exception e) {
            Log.i("Pedometer.java:", "pedoSend() : .....ERROR.....!");
        }
    }

    //만보기 리셋 - pedoSendAlarm()로 인해 1시간 간격으로 PedoSend.java에서 이 함수가 실행 되게 됨.
    public void countReset(){
        walkingCount=0;
    }


//    //매일 자정시간에 만보기 0으로 초기화.

//    public void resetAlarm(){
//        AlarmManager resetAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//        Intent resetIntent = new Intent(this, StepReset.class);
//        PendingIntent resetSender = PendingIntent.getBroadcast(this.getApplicationContext(), 1, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        // 자정 시간
//        Calendar resetCal = Calendar.getInstance();
//        resetCal.setTimeInMillis(System.currentTimeMillis());
//        resetCal.set(Calendar.HOUR_OF_DAY, 0);
//        resetCal.set(Calendar.MINUTE,0);
//        resetCal.set(Calendar.SECOND, 0);
//
//        //다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
//        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis()+AlarmManager.INTERVAL_DAY
//                , AlarmManager.INTERVAL_DAY, resetSender);
//
//        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
//        String setResetTime = format.format(new Date(resetCal.getTimeInMillis()+AlarmManager.INTERVAL_DAY));
//
//        Log.d("resetAlarm", "ResetHour : " + setResetTime);
//
//    }



}