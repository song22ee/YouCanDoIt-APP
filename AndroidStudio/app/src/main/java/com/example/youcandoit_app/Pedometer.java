package com.example.youcandoit_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class Pedometer extends AppCompatActivity {

    Intent i;
    String id;
    String nickname;
    TextView text;

    public boolean isFirst = true; // 오늘 pedometer SQLite DB에 insert를 최초로 했는지.

    Sensor sensor_accelerometer;
    SensorManager sm;
    SharedPreferences preferences;

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

    TextView tv_step;

    ValueHandler handler = new ValueHandler();


//    protected void onResume() {
//        super.onResume();
////        if (sensor_accelerometer != null) {
////            sm.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
////        }
//    }

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

//                    //만보기 초기화
//                    Log.i("pedoSend.java", "ValueHandler : 만보기를 초기화 합니다. ");
//                    editor.putInt("step", 0);
//                    editor.commit();
//                    tv_step.setText(0 + "");

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
        text = (TextView) findViewById(R.id.text);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        } else {
            // 센서가 있다면 Service 실행
            Intent pedometerIntent = new Intent(this, PedometerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(pedometerIntent);
            }
        }

        preferences = getSharedPreferences("pedometer", MODE_PRIVATE);

        //사용자 닉네임 보여주기.
        i = getIntent();
        nickname = i.getStringExtra("nickname");
        text.setText(nickname + " 님");

        // 정각, 자정 전송
        PedoUpThread pedoUpThread = new PedoUpThread();
//        pedoUpThread.start();

        // Service에서 보내는 값을 받기 위한 선언
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("pedometer");
        this.registerReceiver(br, filter);

        tv_step.setText(preferences.getInt("step", 0) + "");
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

    // Service에서 요청을 보냈을 때 할 작업
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 보낸 액션이 pedometer라면
            if(intent.getAction() == "pedometer") {
                tv_step.setText(intent.getIntExtra("step", 0) + "");
            }
        }
    }
}