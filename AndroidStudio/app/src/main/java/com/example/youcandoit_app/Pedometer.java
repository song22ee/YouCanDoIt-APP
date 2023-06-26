package com.example.youcandoit_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pedometer extends AppCompatActivity implements SensorEventListener {

    Intent i;
    String id;
    String nickname;
    TextView text;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public boolean isFirst = true; // 오늘 pedometer SQLite DB에 insert를 최초로 했는지.

    // 현재 RTC
    SimpleDateFormat FormatRTC = new SimpleDateFormat("yyyy-MM-dd");

    private String getRTC() {
        Date mDate;
        mDate = new Date(System.currentTimeMillis());
        return FormatRTC.format(mDate);
    }

    //현재 년월일
    SimpleDateFormat FormatDate = new SimpleDateFormat("yyyy-MM-dd");

    private String getTodayDate() {
        Date mDate;
        mDate = new Date(System.currentTimeMillis());
        return FormatDate.format(mDate);
    }

    //현재 시각
    SimpleDateFormat FormatHour = new SimpleDateFormat("HH");

    private String getHour() {
        return FormatHour.format(System.currentTimeMillis());
    }

    //현재 시분초
    SimpleDateFormat FormatTime = new SimpleDateFormat("HH:mm:ss");

    private String getTime() {
        return FormatTime.format(System.currentTimeMillis());
    }

    //만보기
    SensorManager sm;
    Sensor sensor_accelerometer;

    public int walkingCount = 0; //만보기 변수

    TextView tv_step;

    ValueHandler handler = new ValueHandler();


    protected void onResume() {
        super.onResume();
        if (sensor_accelerometer != null) {
            sm.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.v("Pedometer.java", "onSensorChanged()");
            // 앱을 처음 사용할 때
            if (preferences.getInt("step", -1) == -1) {
                Log.v("Pedometer.java:", "초기화됨");
                editor.putInt("step", 0);
                editor.putInt("prevStep", (int) event.values[0]);
                editor.commit();
            }

            // 전 만보기 값
            walkingCount = preferences.getInt("step", 0);
            // 전 이벤트 값
            int prevWalkingCount = preferences.getInt("prevStep", 0);

            if (event.values[0] < prevWalkingCount) {
                // 재부팅 했을 경우 이벤트 값이 초기화 되기 때문에 처음부터 다시 쌓아준다.
                Log.v("Pedometer.java:", "이벤트 값이 적음");
                editor.putInt("prevStep", 0);
                editor.putInt("step", walkingCount + (int) event.values[0]);
            } else if (event.values[0] - prevWalkingCount > 1) {
                // 센서가 다시 반응 했을 때 다시 반응할 동안의 누적값을 쌓아준다.
                Log.v("Pedometer.java:", "센서 반응");
                editor.putInt("step", walkingCount + ((int) event.values[0] - prevWalkingCount));
            } else if (event.values[0] > prevWalkingCount) {
                // 센서가 활성화 되었을 때 1씩 증가
                Log.v("Pedometer.java:", "카운트 증가");
                editor.putInt("step", ++walkingCount);
            }

            editor.putInt("prevStep", (int) event.values[0]);
            editor.commit();

            tv_step.setText(preferences.getInt("step", 0) + "");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    class ValueHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Log.v("Pedometer.java:", "PedoUpThread 실행중");

                String current_Time = getTime(); // 현재 시간, 분, 초
                String RTC = getRTC(); // 현재 년월일 시분초

                if (current_Time.equals("00:00:00")) {//자정일때 - 만보기값 초기화, 최종 집계
                    Log.i("Pedometer.java", "ValueHandler : 현재 RTC = " + RTC);

                    //만보기 초기화
                    Log.i("pedoSend.java", "ValueHandler : 만보기를 초기화 합니다. ");
                    editor.putInt("step", 0);
                    editor.commit();
                    tv_step.setText(0 + "");

                    //00:00:00 최종집계
                    Log.i("pedoSend.java", "ValueHandler :  어제의 최종 만보기 결과값이 전송 되었습니다.");
                    todayFinal();

                } else if (current_Time.endsWith("00:00")) {//정각마다, 서버 DB 전송
                    //1:00~23:00에만 실행가능
                    Log.i("Pedometer.java", "ValueHandler : 현재 RTC = " + RTC);

                    String today;
                    today = getTodayDate();
                    pedoSend(today);

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


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Pedometer.java:", "로그인 완료");
        Log.i("Pedometer.java:", "Pedometer.java에 들어옴.");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

        tv_step = findViewById(R.id.tv_step);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        text = (TextView) findViewById(R.id.text);

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        // SharedPreferences : 단순한 데이터를 저장(키-값). 앱을 종료해도 유지
        preferences = getSharedPreferences("pedometer", MODE_PRIVATE);
        editor = preferences.edit();

        //사용자 닉네임 보여주기.
        i = getIntent();
        nickname = i.getStringExtra("nickname");
        text.setText(nickname + " 님");

        // 정각, 자정 전송
        PedoUpThread pedoUpThread = new PedoUpThread();
        pedoUpThread.start();

    }

    //만보기 결과 db 전송
    public void pedoSend(String date) {
        try {
            Log.i("Pedometer.java:", "pedoSend() : 진입");
            id = i.getStringExtra("id");
            DBsendActivity task = new DBsendActivity(); //DBsendActivity.java 객체 생성.
            task.execute(date, id, String.valueOf(preferences.getInt("step", 0))); //DBsendActivity.java로 날짜, 아이디, 만보기 결과값 전송.
            Log.i("Pedometer.java:", "pedoSend() : 만보기결과값 서버 DB로 전송됨.");
            Toast.makeText(Pedometer.this, "만보기 값이 서버 DB로 전송되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i("Pedometer.java:", "pedoSend() : .....ERROR.....!");
            e.printStackTrace();
        }
    }

    public void todayFinal() {
        Log.i("Pedometer.java", "todayFinal() : 진입");

        // 00시가 되면 다음 날짜가 되므로 어제 날짜로 전송해야함.

        Date date;
        date = new Date();
        //어제 날짜
        date.setTime(System.currentTimeMillis() - (long) (1000 * 60 * 60 * 24));
        String yesterday = FormatDate.format(date);
        Log.i("Pedometer.java", "todayFinal() : 어제 날짜 : " + yesterday);

        pedoSend(yesterday); //만보기 db전송하는 함수 실행시키기.
        Log.i("Pedometer.java", "todayFinal() : pedoSend(yesterday)실행. ");

    }
}