package com.example.youcandoit_app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LoginActivity extends AsyncTask<String, Void, String> {
    String sendMsg, receiveMsg;
    String loginresult,nickname;

    @Override
    protected String doInBackground(String... strings) {
        try {
            String str;
            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
//            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/Android/AndroidDB.jsp");
            URL url = new URL("http://172.30.1.94:8080/YouCanDoIt/Android/Login.jsp");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");

            //전송할 데이터. GET방식으로 작성
            sendMsg = "id=" + strings[0] + "&pw=" + strings[1];

            osw.write(sendMsg);
            osw.flush();
            //logcat에 찍어볼 수 있음.
            Log.i("sendMsg : " , sendMsg);


            //jsp와 통신 성공 시 수행
            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();

                // jsp에서 보낸 값을 받는 부분
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ",receiveMsg);

            } else {
                //통신 실패
                Log.i("통신실패!!!!","통신실패!!!");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("LoginActivity.java", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("LoginActivity.java", e.getMessage());
        }

        //jsp로부터 받은 리턴 값
        return receiveMsg;
    }

}