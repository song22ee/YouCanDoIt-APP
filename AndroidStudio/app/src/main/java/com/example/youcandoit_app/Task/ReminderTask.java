package com.example.youcandoit_app.Task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.youcandoit_app.support.TaskSupport;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReminderTask  extends AsyncTask<String, Void, List<String>> {
    String sendMsg;
    JSONArray jArray;
    String receiveMsg;
    List<String> reminderList = new ArrayList();

    @Override
    protected List<String> doInBackground(String... strings) {
        try {
            Log.i("ReminderTask", "리마인더 조회 실행");

            String str;
            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
//            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/reminderSelect");
            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/reminderSelect");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            //전송할 데이터. GET방식으로 작성
            sendMsg = "id=" + strings[0];

            receiveMsg = new TaskSupport().httpConnection(url, sendMsg);

            jArray = new JSONArray(receiveMsg);
            Log.i("JsonArray : ", jArray.toString());

            for(int i = 0; i < jArray.length(); i++) {
                reminderList.add(jArray.getString(i));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("TodayScheduleTask", "JSON에러");
            throw new RuntimeException(e);
        }

        //jsp로부터 받은 리턴 값
        return reminderList;
    }
}