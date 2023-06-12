package com.example.youcandoit_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    DBHelper helper;
    SQLiteDatabase db;

    public boolean isFirst = true; // 오늘 pedometer SQLite DB에 insert를 최초로 했는지.

    // 현재 RTC
    SimpleDateFormat FormatRTC = new SimpleDateFormat("yyyy-MM-dd");
    private String getRTC(){
        Date mDate;
        mDate = new Date(System.currentTimeMillis());
        return FormatRTC.format(mDate);
    }

    //현재 년월일
    SimpleDateFormat FormatDate = new SimpleDateFormat("yyyy-MM-dd");
    private String getTodayDate(){
        Date mDate;
        mDate = new Date(System.currentTimeMillis());
        return FormatDate.format(mDate);
    }

    //현재 시각
    SimpleDateFormat FormatHour = new SimpleDateFormat("HH");
    private String getHour(){
        return FormatHour.format(System.currentTimeMillis());
    }

    //현재 시분초
    SimpleDateFormat FormatTime = new SimpleDateFormat("HH:mm:ss");
    private String getTime(){
        return FormatTime.format(System.currentTimeMillis());
    }

    //만보기
    SensorManager sm;
    Sensor sensor_accelerometer;
    long myTime1, myTime2;

    float x, y, z;
    float lastX, lastY, lastZ;

    final int walkThreshold = 455; //걷기 인식 임계 값
    double acceleration = 0;


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
                        String today = getTodayDate();
                        id = i.getStringExtra("id");

                        ContentValues values = new ContentValues();

                        values.put("dateid", today+id);
                        values.put("date", today);
                        values.put("id", id);
                        values.put("step", walkingCount);

                        Log.i("pedoSend.java", "onSensorChanged() : values=" + values);

                        String thereis= selectOne(today,id);
                        Log.i("pedoSend.java",thereis);
                        //만들어진 레코드가 없다면 insert
                        if(thereis.equals("null")){
                            db.insert("pedometer", null, values);
                            Log.i("pedoSend.java", "onSensorChanged() : pedometer db insert 완료됨.");
                            Log.i("pedoSend.java", "onSensorChanged() : selectOne()실행. 오늘날짜, 해당 아이디의 pedometer data값.");

                            selectOne(today,id);
                        }else{
                            Log.i("pedoSend.java", "onSensorChanged() : 이미 레코드가 있으므로 insert 안함.");
                        }

                        isFirst=false; //insert를 했으므로 false로 변경.
                        selectAll();

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

                String current_Time = getTime(); // 현재 시간, 분, 초
                String RTC = getRTC(); // 현재 년월일 시분초

                if(current_Time.equals("00:00:00")){//자정일때 - 만보기값 초기화, 최종 집계
                    Log.i("Pedometer.java", "ValueHandler : 현재 RTC = " + RTC);

                    //만보기 초기화
                    Log.i("pedoSend.java", "ValueHandler : 만보기를 초기화 합니다. ");
                    walkingCount=0;

                    //00:00:00 최종집계
                    Log.i("pedoSend.java", "ValueHandler :  어제의 최종 만보기 결과값이 전송 되었습니다.");
                    todayFinal();

                }else if(current_Time.endsWith("00:00")){//정각마다, 서버 DB 전송
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Pedometer.java:","로그인 완료");
        Log.i("Pedometer.java:","Pedometer.java에 들어옴.");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

        tv_step = findViewById(R.id.tv_step);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        text = (TextView) findViewById(R.id.text);


        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

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


    }


    //SQLite db
    @SuppressLint("Range")
    public String selectOne(String date, String id) {
        String step = "null";
        String sql = String.format("select * from pedometer where date='%s' and id='%s';",date,id);
        Cursor c = db.rawQuery(sql, null);
        Log.i("Pedometer.java:", "selectOne() : 진입");
        while(c.moveToNext()){
            step= c.getString(c.getColumnIndex("step"));
        }
        return step;
    }

    @SuppressLint("Range")
    public void selectAll() {
        String dateid, date, id;
        String step = "null";
        String sql = "select * from pedometer;";
        Cursor c = db.rawQuery(sql, null);
        Log.i("Pedometer.java:", "selectAll() : 진입");
        System.out.println("--------------현재 pedometer 테이블------------------");
        while(c.moveToNext()){
            dateid= c.getString(c.getColumnIndex("dateid"));
            date= c.getString(c.getColumnIndex("date"));
            id= c.getString(c.getColumnIndex("id"));
            step= c.getString(c.getColumnIndex("step"));

            Log.i("Pedometer.java: ", "| dateid=" +dateid+ " | date="+date+" | id="+id+" | step="+step + " |");
        }
        System.out.println("---------------------------------------------------");
    }

    public void update(){
        ContentValues updateVal = new ContentValues();
        String today = getTodayDate();
        id = i.getStringExtra("id");
        updateVal.put("step",walkingCount);
        db.update("pedometer",updateVal,"date=? AND id=?", new String[]{today,id});
        selectAll();
    }

    //만보기 결과 db 전송
    public void pedoSend(String date){
        try {
            Log.i("Pedometer.java:", "pedoSend() : 진입");
            id = i.getStringExtra("id");
            String step = selectOne(date,id);
            Log.i("Pedometer.java:", "pedoSend() : date = " + date
                    +" id=" + id +" pedometer_result="+ step);
            DBsendActivity task = new DBsendActivity(); //DBsendActivity.java 객체 생성.
            task.execute(date,id,step); //DBsendActivity.java로 날짜, 아이디, 만보기 결과값 전송.
            Log.i("Pedometer.java:", "pedoSend() : 만보기결과값 서버 DB로 전송됨.");
            Toast.makeText(Pedometer.this, "만보기 값이 서버 DB로 전송되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i("Pedometer.java:", "pedoSend() : .....ERROR.....!");
            e.printStackTrace();
        }
    }

    public void todayFinal(){
        Log.i("Pedometer.java", "todayFinal() : 진입");

        // 00시가 되면 다음 날짜가 되므로 어제 날짜로 전송해야함.

        Date date;
        date = new Date();
        //어제 날짜
        date.setTime(System.currentTimeMillis() - (long)(1000*60*60*24));
        String yesterday = FormatDate.format(date);
        Log.i("Pedometer.java", "todayFinal() : 어제 날짜 : " + yesterday);

        pedoSend(yesterday); //만보기 db전송하는 함수 실행시키기.
        Log.i("Pedometer.java", "todayFinal() : pedoSend(yesterday)실행. ");

    }





}