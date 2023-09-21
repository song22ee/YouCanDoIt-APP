package com.example.youcandoit_app.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youcandoit_app.Task.LoginTask;
import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.TokenTask;
import com.example.youcandoit_app.support.PermissionSupport;

public class LoginActivity extends AppCompatActivity {

    private PermissionSupport permission;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    LoginTask task;
    String mem_id, password, nickname;
    EditText id ,pw;
    Button loginBtn;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 퍼미션 체크
        permission = new PermissionSupport(this, this);
        if(permission.checkPermission()) {
            permission.requestPermission();
        }

        preferences = getSharedPreferences("login", MODE_PRIVATE);
        mem_id = preferences.getString("id", null);
        password = preferences.getString("pw", null);
        editor = preferences.edit();

        if(mem_id != null && password != null) {
            Log.i("LoginActivity.java:", "자동로그인");
            try {
                task = new LoginTask();
                nickname = task.execute(mem_id, password).get();
                if(nickname != "로그인 실패") {
                    // 토큰이 없으면 토큰 서버로 전송
                    if(!preferences.getBoolean("isToken", true)) {
                        new TokenTask().execute(mem_id, preferences.getString("token", null));
                        editor.putBoolean("isToken", true);
                        editor.commit();
                    }
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } catch (Exception e) {
                Log.i("LoginActivity.java:", "LoginActivity.java와 통신 실패.");
                Log.i("LoginActivity.java:", e.getMessage());
            }
        }

        setContentView(R.layout.login_page);

        id = (EditText) findViewById(R.id.id);
        pw= (EditText) findViewById(R.id.pw);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mem_id = id.getText().toString();
                    password = pw.getText().toString();

                    task = new LoginTask();
                    nickname = task.execute(mem_id, password).get();
                    switch (nickname){
                        case "로그인 실패": //로그인 실패
                            Toast.makeText(LoginActivity.this, "아이디 또는 비번이 틀립니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default://로그인 성공
                            Log.i("LoginActivity.java:","로그인 성공");
                            Log.i("LoginActivity.java:","아이디 : " + mem_id);
                            Log.i("LoginActivity.java:","닉네임 : " + nickname);
                            editor.putString("id", mem_id);
                            editor.putString("pw", password);
                            editor.putString("nickname", nickname);
                            // 토큰이 없으면 토큰 서버로 전송
                            if(!preferences.getBoolean("isToken", true)) {
                                new TokenTask().execute(mem_id, preferences.getString("token", null));
                                editor.putBoolean("isToken", true);
                            }
                            editor.commit();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                            break;
                    }
                    Log.i("LoginActivity.java:", "LoginActivity.java와 통신 성공.");
                } catch (Exception e) {
                    Log.i("LoginActivity.java:", "LoginActivity.java와 통신 실패.");
                    Log.i("LoginActivity.java:", e.getMessage());
                }
            }
        });
    }
}