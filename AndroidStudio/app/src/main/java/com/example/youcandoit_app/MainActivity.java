package com.example.youcandoit_app;


import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText id ,pw;
    Button loginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = (EditText) findViewById(R.id.id);
        pw= (EditText) findViewById(R.id.pw);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String nickname;
                    String mem_id = id.getText().toString();
                    String password = pw.getText().toString();

                    LoginActivity task = new LoginActivity();
                    nickname = task.execute(mem_id, password).get();
                    switch (nickname){
                        case "로그인 실패": //로그인 실패
                            Toast.makeText(MainActivity.this, "아이디 또는 비번이 틀립니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default://로그인 성공
                            Log.i("MainActivity.java:","로그인 성공");
                            Log.i("MainActivity.java:","아이디 : " + mem_id);
                            Log.i("MainActivity.java:","닉네임 : " + nickname);
                            Intent i = new Intent(getApplicationContext(),Pedometer.class);
                            i.putExtra("nickname",nickname);
                            i.putExtra("id",mem_id);
                            startActivityForResult(i,10);
                            break;
                    }
                    Log.i("MainActivity.java:", "LoginActivity.java와 통신 성공.");
                } catch (Exception e) {
                    Log.i("MainActivity.java:", "LoginActivity.java와 통신 실패.");
                    Log.i("MainActivity.java:", e.getMessage());
                }
            }
        });





    }
}