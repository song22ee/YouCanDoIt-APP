package com.example.youcandoit_app.Task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.youcandoit_app.support.TaskSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginTask extends AsyncTask<String, Void, String> {
    String sendMsg, receiveMsg;

    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.i("LoginTask", "로그인 실행");

            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/login");
//            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/login");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            //전송할 데이터. GET방식으로 작성
            sendMsg = "id=" + strings[0] + "&pw=" + strings[1];

            receiveMsg = new TaskSupport().httpConnection(url, sendMsg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("LoginTask.java", e.getMessage());
        }
        return receiveMsg;
    }
}