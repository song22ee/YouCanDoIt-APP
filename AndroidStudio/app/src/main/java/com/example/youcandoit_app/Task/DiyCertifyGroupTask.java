package com.example.youcandoit_app.Task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.youcandoit_app.dto.GroupDto;
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

public class DiyCertifyGroupTask extends AsyncTask<String, Void, List<GroupDto>> {
    String sendMsg;
    JSONArray jArray;
    String receiveMsg;
    List<GroupDto> groupList = new ArrayList();

    @Override
    protected List<GroupDto> doInBackground(String... strings) {
        try {
            Log.i("DiyCertifyGroupTask", "인증할 DIY 그룹 조회 실행");

            String str;
            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/diyGroupSelect");
//            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/diyGroupSelect");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            //전송할 데이터. GET방식으로 작성
            sendMsg = "id=" + strings[0];

            receiveMsg = new TaskSupport().httpConnection(url, sendMsg);

            jArray = new JSONArray(receiveMsg);
            Log.i("JsonArray : ", jArray.toString());

            for(int i = 0; i < jArray.length(); i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                URL imageUrl = new URL("https://ycdi.cafe24.com" + jObject.getString("groupImage"));
                HttpURLConnection imageConn = (HttpURLConnection)imageUrl.openConnection();
                imageConn.setDoInput(true);
                imageConn.connect();

                InputStream is = imageConn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);

                groupList.add(new GroupDto(jObject.getInt("groupNumber"),
                        jObject.getString("groupName"),
                        jObject.getString("groupSubject"),
                        bitmap));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("DiyCertifyGroup", "JSON에러");
            throw new RuntimeException(e);
        }

        //jsp로부터 받은 리턴 값
        return groupList;
    }
}