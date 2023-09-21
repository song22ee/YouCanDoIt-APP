package com.example.youcandoit_app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.youcandoit_app.Fragment.MainFragment;
import com.example.youcandoit_app.Fragment.SchedulerFragment;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Service.PedometerService;
import com.example.youcandoit_app.support.onBackPressedSupport;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TabLayout tabs;
    Fragment main_fragment, scheduler_fragment;
    FragmentManager fm;

    Sensor sensor_accelerometer;
    SensorManager sm;

    TextView nickname;
    SharedPreferences user_preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("MainActivity.java:", "로그인 완료");
        Log.i("MainActivity.java:", "MainActivity.java에 들어옴.");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //화면 세로로 설정
        setContentView(R.layout.main_page);

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

        nickname = findViewById(R.id.text);
        tabs = findViewById(R.id.tabs);

        // 사용자 닉네임 보여주기.
        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        nickname.setText(user_preferences.getString("nickname", null) + " 님");

        // 탭 추가
        tabs.addTab(tabs.newTab().setText("챌린지"));
        tabs.addTab(tabs.newTab().setText("스케줄러"));
        tabs.addTab(tabs.newTab().setText("친구"));

        main_fragment = new MainFragment();
        scheduler_fragment = new SchedulerFragment();

        fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        // 프레임 레이아웃에 fragment 띄우기
        ft.replace(R.id.tabContent, main_fragment);
        ft.add(R.id.tabContent, scheduler_fragment).hide(scheduler_fragment).commit();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                FragmentTransaction tabFt = fm.beginTransaction();
                switch (position) {
                    case 0:
                        tabFt.show(main_fragment);
                        tabFt.hide(scheduler_fragment).commit();
                        break;
                    case 1:
                        tabFt.show(scheduler_fragment);
                        tabFt.hide(main_fragment).commit();
                        break;
                    case 2:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
    
    @Override
    public void onBackPressed() {
        // fragment 종료 코드. 실행중인 호스트 프래그먼트들을 불러온다.
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        for(Fragment fragment : fragmentList){
            // 자식 프래그먼트를 호출한다.
            List<Fragment> childList = fragment.getChildFragmentManager().getFragments();
            for(Fragment child : childList) { // 자식 프래그먼트가 있다면.
                if(!child.getChildFragmentManager().getFragments().isEmpty()) // 그 아래 프래그먼트 확인해서 있다면 자식 프래그먼트 교체
                    child = child.getChildFragmentManager().getFragments().get(0);
                // 자식 프래그먼트 종료
                ((onBackPressedSupport)child).onBackPressed();
                return;
            }
        }
        super.onBackPressed();
    }
}
