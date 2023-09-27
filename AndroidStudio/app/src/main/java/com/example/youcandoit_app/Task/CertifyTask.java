package com.example.youcandoit_app.Task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class CertifyTask extends AsyncTask<String, Void, String> {
    String receiveMsg;

    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.i("CertifyTask", "DIY 인증사진 전송 실행");

            String str;

            String[] dataName = {"id", "groupNumber", "certifyImage"}; // 보낼 데이터명

            File certifyFile = new File(new URI(strings[2]));

            final String twoHyphens = "--";
            String lineEnd = "\r\n";
            String boundary = "==" + System.currentTimeMillis() + "==";
            String delimiter = twoHyphens + boundary + lineEnd;

            byte[] buf;
            int maxBufferSize = 5 * 1024 * 1024;

            //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
            URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/diyCertify");
//            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/diyCertify");
            // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestMethod("POST");


            StringBuilder postDataBuilder = new StringBuilder();
            // 일반 문자열은 아이디, 그룹번호 2개를 넘겨야 하기 때문에 두번 반복
            for (int i = 0; i < 2; i++) {
                postDataBuilder.append(delimiter)
                        .append("Content-Disposition: form-data; name=\"")
                        .append(dataName[i]).append("\"")
                        .append(lineEnd)
                        .append(lineEnd)
                        .append(strings[i])
                        .append(lineEnd);
            }

            // 파일 전송을 위해 따로 작성
            postDataBuilder.append(delimiter)
                    .append("Content-Disposition: form-data; name=\"")
                    .append(dataName[2])
                    .append("\";filename=\"")
                    .append(certifyFile.getName()).append("\"")
                    .append(lineEnd)
                    .append(lineEnd);

            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            dataOutputStream.write(postDataBuilder.toString().getBytes());


            FileInputStream fStream = new FileInputStream(certifyFile);
            buf = new byte[maxBufferSize];
            int length = -1;
            while ((length = fStream.read(buf)) != -1) {
                dataOutputStream.write(buf, 0, length);
            }
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(lineEnd);
            dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd); // requestbody end
            fStream.close();

            Log.i("CertifyTask.java", "values : " + dataOutputStream.size());
            dataOutputStream.flush();


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
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("CertifyTask.java", e.getMessage());
        }

        //jsp로부터 받은 리턴 값
        return receiveMsg;
    }
}
