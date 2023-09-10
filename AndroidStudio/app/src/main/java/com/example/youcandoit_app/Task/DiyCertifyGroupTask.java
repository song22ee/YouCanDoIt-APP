package com.example.youcandoit_app.Task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.youcandoit_app.dto.GroupDto;

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
            String str;
            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
//            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/Android/DiyGroupSelect.jsp");
            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/Android/DiyGroupSelect.jsp");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");

            //전송할 데이터. GET방식으로 작성
            sendMsg = "mem_id=" + strings[0];

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
            } else {
                //통신 실패
                Log.i("통신실패!!!!","통신실패!!!");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("DiyCertifyGroup", "JSON에러");
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //jsp로부터 받은 리턴 값
        return groupList;
    }
}
