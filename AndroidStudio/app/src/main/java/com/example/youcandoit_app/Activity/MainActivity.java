package com.example.youcandoit_app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youcandoit_app.Adapter.DiyCertifyAdapter;
import com.example.youcandoit_app.Service.PedometerService;
import com.example.youcandoit_app.Task.DiyCertifyGroupTask;
import com.example.youcandoit_app.Task.PedometerTask;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.dto.GroupDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String id;
    TextView text;
    RecyclerView recyclerView;
    Button btn;
    View.OnClickListener cl;

    // 만보기 사용을 위해 선언
    Sensor sensor_accelerometer;
    SensorManager sm;
    SharedPreferences pedometer_preferences, user_preferences;

    // 인증이 필요한 그룹 리스트
    List<GroupDto> diyGroupList;


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
                Log.v("MainActivity.java:", "PedoUpThread 실행중");

                String current_Time = getTime(); // 현재 시간, 분, 초
                String RTC = getRTC(); // 현재 년월일 시분초

                if (current_Time.equals("00:00:00")) {//자정일때 - 만보기값 초기화, 최종 집계
                    Log.i("MainActivity.java", "ValueHandler : 현재 RTC = " + RTC);

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
                    Log.i("MainActivity.java", "ValueHandler : 현재 RTC = " + RTC);

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

        Log.i("MainActivity.java:", "로그인 완료");
        Log.i("MainActivity.java:", "MainActivity.java에 들어옴.");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

        tv_step = findViewById(R.id.tv_step);
        text = findViewById(R.id.text);
        recyclerView = findViewById(R.id.diyCertifyList);
        btn = findViewById(R.id.camera);
        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.camera) {

                }
            }

        };
        btn.setOnClickListener(cl);

        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);
        // 사용자 닉네임 보여주기.
        text.setText(user_preferences.getString("nickname", null) + " 님");


        // ==================================== 만보기 관련 ====================================
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (sensor_accelerometer == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        } else {
            // 센서가 있다면 Service 실행
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, PedometerService.class));
            }
        }

        // Service에서 보내는 값을 받기 위한 선언
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("pedometer");
        this.registerReceiver(br, filter);

        pedometer_preferences = getSharedPreferences("pedometer", MODE_PRIVATE);
        tv_step.setText(pedometer_preferences.getInt("step", 0) + "");
        // ===================================================================================


        // ==================================== diy 인증 관련 ==================================
        certifyGroup();
        // 아이템을 가로로 배치하기 위해 LinearLayout 사용
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        // 아이템 간격 설정
        RecyclerViewDecoration decoration = new RecyclerViewDecoration(30);
        recyclerView.addItemDecoration(decoration);

        DiyCertifyAdapter diyCertifyAdapter = new DiyCertifyAdapter(diyGroupList);
        // 커스텀 ClickListener 바디 구현
        // 실제로는 recyclerView 영역을 클릭하면 어댑터에 구현해놓은 clickListener가 실행
        // clickListener에서 포지션 값을 구해 다시 커스텀 ClickListener를 호출한다.
        diyCertifyAdapter.setOnItemClickListener(new DiyCertifyAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int groupNumber) {
                Log.i("MainActivity.java", "그룹번호 받음 : " + groupNumber);
                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                i.putExtra("number", String.valueOf(groupNumber));
                startActivityForResult(i, 100);
            }
        });
        recyclerView.setAdapter(diyCertifyAdapter);
        // ===================================================================================

        // 정각, 자정 전송
//        PedoUpThread pedoUpThread = new PedoUpThread();
//        pedoUpThread.start();

    }

    /** DIY 인증을 완료했다면 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 100) {
            // 액티비티 새로고침
            Intent intent = getIntent();
            finish(); 
            overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
            startActivity(intent); 
            overridePendingTransition(0, 0); 
        }
    }

    /** 인증이 필요한 그룹 불러오기 */
    public void certifyGroup() {
        try {
            DiyCertifyGroupTask task = new DiyCertifyGroupTask();
            diyGroupList = task.execute(id).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 만보기 결과 db 전송 */
    public void pedoSend(String date) {
        try {
            Log.i("MainActivity.java:", "pedoSend() : 진입");
            PedometerTask task = new PedometerTask(); //DBsendActivity.java 객체 생성.
            task.execute(date, id, String.valueOf(pedometer_preferences.getInt("step", 0))); //DBsendActivity.java로 날짜, 아이디, 만보기 결과값 전송.
            Log.i("MainActivity.java:", "pedoSend() : 만보기결과값 서버 DB로 전송됨.");
            Toast.makeText(MainActivity.this, "만보기 값이 서버 DB로 전송되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i("MainActivity.java:", "pedoSend() : .....ERROR.....!");
            e.printStackTrace();
        }
    }

    public void todayFinal() {
        Log.i("MainActivity.java", "todayFinal() : 진입");

        // 00시가 되면 다음 날짜가 되므로 어제 날짜로 전송해야함.

        Date date;
        date = new Date();
        //어제 날짜
        date.setTime(System.currentTimeMillis() - (long) (1000 * 60 * 60 * 24));
        String yesterday = FormatDate.format(date);
        Log.i("MainActivity.java", "todayFinal() : 어제 날짜 : " + yesterday);

        pedoSend(yesterday); //만보기 db전송하는 함수 실행시키기.
        Log.i("MainActivity.java", "todayFinal() : pedoSend(yesterday)실행. ");

    }

    // Service에서 요청을 보냈을 때 할 작업 (만보기 증가시 요청을 보낸다)
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 보낸 액션이 pedometer라면
            if(intent.getAction() == "pedometer") {
                tv_step.setText(pedometer_preferences.getInt("step", 0) + "");
            }
        }
    }

    /** RecyclerView 간격 조절 클래스 */
    public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {
        private final int divWidth;

        public RecyclerViewDecoration(int divWidth)
        {
            this.divWidth = divWidth;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.right = divWidth;
        }
    }
}