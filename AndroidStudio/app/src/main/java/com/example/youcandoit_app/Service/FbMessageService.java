package com.example.youcandoit_app.Service;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.youcandoit_app.support.TaskSupport;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class FbMessageService extends FirebaseMessagingService {

    SharedPreferences pedometer_preferences, user_preferences;
    SharedPreferences.Editor pedometer_editor, user_editor;
    String sendMsg, receiveMsg;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.i("FCM", "토큰확인 : " + token);

        FirebaseMessaging.getInstance().subscribeToTopic("pedometerUpdate")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "주제 구독 완료";
                        if (!task.isSuccessful()) {
                            msg = "주제 구독 실패";
                        }
                        Log.d("FCM", msg);
                    }
                });

        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        user_editor = user_preferences.edit();
        user_editor.putString("token", token);
        user_editor.putBoolean("isToken", false);
        user_editor.commit();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        user_preferences = getSharedPreferences("login", MODE_PRIVATE);

        Map data = message.getData();
        String msg = data.get("title").toString();

        if(msg.equals("pedometerUpdate")) {
            Log.i("FCM", "만보기 값을 서버로 전송합니다. " + msg);

            pedometer_preferences = getSharedPreferences("pedometer", MODE_PRIVATE);

            try {
                //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
//                URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/Android/PedometerUpdate.jsp");
            URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/Android/PedometerUpdate.jsp");
                // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

                String date = data.get("date").toString();
                String isLast = data.get("isLast").toString();
                String id = user_preferences.getString("id", null);
                int step = pedometer_preferences.getInt("step", 0);

                //전송할 데이터. GET방식으로 작성
                sendMsg = "date=" + date + "&mem_id=" + id + "&pedometer_result=" + step + "&is_last=" + isLast;

                receiveMsg = new TaskSupport().httpConnection(url, sendMsg);
                Log.i("FCM", "서버 전송 결과 : " + receiveMsg);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("FCM", "개인 호출 " + msg);
            // 리마인더 코드
        }


    }
}

/*


//    ValueHandler handler = new ValueHandler();

class ValueHandler extends Handler {
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (msg.what == 0) {
            Log.v("MainActivity.java:", "PedoUpThread 실행중");

            String current_Time = getTime(); // 현재 시간, 분, 초
            String RTC = getRTC(); // 현재 년월일 시분초

            if (current_Time.equals("00:00:00")) {//자정일때 - 만보기값 초기화, 최종 집계
                Log.i("MainActivity.java", "ValueHandler : 현재 RTC = " + RTC);

//                    //만보기 초기화
//                    Log.i("pedoSend.java", "ValueHandler : 만보기를 초기화 합니다. ");
//                    editor.putInt("step", 0);
//                    editor.commit();
//                    tv_step.setText(0 + "");

                //00:00:00 최종집계
                Log.i("pedoSend.java", "ValueHandler :  어제의 최종 만보기 결과값이 전송 되었습니다.");
                todayFinal();

            } else if (current_Time.endsWith("00:00")) {//정각마다, 서버 DB 전송
                //1:00~23:00에만 실행가능
                Log.i("MainActivity.java", "ValueHandler : 현재 RTC = " + RTC);

                String today;
                today = getTodayDate();
                pedoSend(today);

            }

        }
    }
}



public void pedoSend(String date) {

}

    public void todayFinal() {
        Log.i("MainActivity.java", "todayFinal() : 진입");

        // 00시가 되면 다음 날짜가 되므로 어제 날짜로 전송해야함.

        Date date;
        date = new Date();
        //어제 날짜
        date.setTime(System.currentTimeMillis() - (long) (1000 * 60 * 60 * 24));
        String yesterday = FormatDate.format(date);
        Log.i("MainActivity.java", "todayFinal() : 어제 날짜 : " + yesterday);

        pedoSend(yesterday); //만보기 db전송하는 함수 실행시키기.
        Log.i("MainActivity.java", "todayFinal() : pedoSend(yesterday)실행. ");

    }
*/
