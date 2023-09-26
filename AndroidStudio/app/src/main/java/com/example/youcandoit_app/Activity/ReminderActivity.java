package com.example.youcandoit_app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.youcandoit_app.Adapter.ReminderAdapter;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.ReminderTask;
import com.example.youcandoit_app.Task.TodayScheduleTask;

import java.util.ArrayList;
import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    ImageButton back;
    View.OnClickListener cl;
    RecyclerView reminder_list_view;

    TextView nickname;
    SharedPreferences user_preferences;

    String id;
    List<String> reminder_list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_page);

        back = findViewById(R.id.back);
        nickname = findViewById(R.id.text);
        reminder_list_view = findViewById(R.id.reminderList);

        // 사용자 닉네임 보여주기.
        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        nickname.setText(user_preferences.getString("nickname", null) + " 님");

        id = user_preferences.getString("id", null);

        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        };
        back.setOnClickListener(cl);

        getServerData();
        // ==================================== 리마인더 목록 ==================================
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        reminder_list_view.setLayoutManager(linearLayoutManager);

        ReminderAdapter reminderAdapter = new ReminderAdapter(reminder_list);
        reminder_list_view.setAdapter(reminderAdapter);
    }

    /** 스케줄러페이지에 필요한 데이터 받아오기 */
    public void getServerData() {
        try {
            ReminderTask task = new ReminderTask();
            reminder_list = task.execute(id).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }
}
