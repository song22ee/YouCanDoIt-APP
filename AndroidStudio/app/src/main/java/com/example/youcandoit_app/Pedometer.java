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

    public static Context PedoContext; // context 변수 선언
    Intent i;
    String id;
    String nickname;
    TextView text;

//    //현재 날짜 가져오기.
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Object PedoRankInsertActivity;

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

    String pedometer_date = getTime();

//    //만보기
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
//
    TextView tv_step;
//
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    class ValueHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                tv_step.setText(walkingCount+"");
                Log.i("Pedometer.java:", "만보올라감");
            }else if(msg.what == 1) {
                //만보기 결과 db로 전송하기.
                try {
                    id = i.getStringExtra("id");
                    int group_num=1;// 일단 그룹 넘버가 1이라고 가정.
                    String pedometer_result = String.valueOf(tv_step.getText());
                    DBsendActivity task = new DBsendActivity();
                    task.execute(pedometer_date,"1",id,pedometer_result).get();// 일단 그룹 넘버가 1이라고 가정.
                    Log.i("Pedometer.java:", "만보기결과값 전송.");
                } catch (Exception e) {
                    Log.i("Pedometer.java:", ".....ERROR.....!");
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
                currentTime = System.currentTimeMillis();
                SystemClock.sleep(1000);//1초 간격
            }
        }
    }


    //스레드 생성 - 만보기 결과값 1시간 마다 db전송
    class PedoSendThread extends Thread {
        boolean running = false;
        public void run() {
            running = true;
            while (running) {
                handler.sendEmptyMessage(1);
//                SystemClock.sleep(1000 * 60 * 60); //60분
                SystemClock.sleep(1000*5); //5초
            }
        }
    }


    //    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Pedometer.java:","들어옴.");

        resetAlarm();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

        PedoContext = this; //onCreate에서 this 할당

        tv_step = findViewById(R.id.tv_step);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        startTime = System.currentTimeMillis();

        //사용자 닉네임
        i = getIntent();
        nickname=i.getStringExtra("nickname");

        text = (TextView) findViewById(R.id.text);

        text.setText(nickname+" 님");

        //만보기 레코드 없으면 insert
        AddRecord();

        //만보기능 스레드 시작.
        PedoUpThread pedoUpThread = new PedoUpThread();
        pedoUpThread.start();


        //만보기 결과 db로 전송하기 스레드 시작.
        PedoSendThread pedoSendThread = new PedoSendThread();
        pedoSendThread.start();


    }

//    //매일 자정시간에 만보기 0으로 초기화.

    public void resetAlarm(){
        AlarmManager resetAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent resetIntent = new Intent(this, StepReset.class);
        PendingIntent resetSender = PendingIntent.getBroadcast(this.getApplicationContext(), 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE,0);
        resetCal.set(Calendar.SECOND, 0);

        //다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis()+AlarmManager.INTERVAL_DAY
               , AlarmManager.INTERVAL_DAY, resetSender);

        SimpleDateFormat format = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setResetTime = format.format(new Date(resetCal.getTimeInMillis()+AlarmManager.INTERVAL_DAY));

        Log.d("resetAlarm", "ResetHour : " + setResetTime);
    }

    //만보기 리셋.
    public void countReset(){
        walkingCount=0;
    }

    //pedometer_ranking에 새 레코드 추가
    public void AddRecord(){
        try {
            id = i.getStringExtra("id");
            PedoRankInsertActivity task = new PedoRankInsertActivity();
            task.execute(pedometer_date,"1",id);// 일단 그룹 넘버가 1이라고 가정.
            Log.i("Pedometer.java", "AddRecord() sucess");
        } catch (Exception e) {
            Log.i("Pedometer.java", "AddRecord() fail : "+e.getMessage());
        }
    }


}