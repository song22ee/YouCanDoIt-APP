package com.example.youcandoit_app.Task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.youcandoit_app.dto.GroupDto;
import com.example.youcandoit_app.dto.ScheduleDto;
import com.example.youcandoit_app.support.TaskSupport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TodayScheduleTask extends AsyncTask<String, Void, List<ScheduleDto>> {
    String sendMsg;
    JSONArray jArray;
    String receiveMsg;
    List<ScheduleDto> scheduleList = new ArrayList();

    @Override
    protected List<ScheduleDto> doInBackground(String... strings) {
        try {
            Log.i("TodayScheduleTask", "오늘의 일정 조회 실행");

            String str;
            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/todayScheduleSelect");
//            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/todayScheduleSelect");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            //전송할 데이터. GET방식으로 작성
            sendMsg = "id=" + strings[0];

            receiveMsg = new TaskSupport().httpConnection(url, sendMsg);

            jArray = new JSONArray(receiveMsg);
            Log.i("JsonArray : ", jArray.toString());

            for(int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);

                scheduleList.add(new ScheduleDto(jObject.getInt("scheduleNumber"),
                        jObject.getString("scheduleTitle"),
                        jObject.getString("scheduleStartDate"),
                        jObject.getString("scheduleEndDate"),
                        jObject.getString("scheduleSuccess")));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("TodayScheduleTask", "JSON에러");
            throw new RuntimeException(e);
        }

        //jsp로부터 받은 리턴 값
        return scheduleList;
    }
}
