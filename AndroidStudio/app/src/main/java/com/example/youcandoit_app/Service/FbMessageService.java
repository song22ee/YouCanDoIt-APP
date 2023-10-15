package com.example.youcandoit_app.Service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.youcandoit_app.Activity.LoginActivity;
import com.example.youcandoit_app.R;
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        pedometer_preferences = getSharedPreferences("pedometer", MODE_PRIVATE);
        user_editor = user_preferences.edit();
        pedometer_editor = pedometer_preferences.edit();

        Map data = message.getData();
        String title = data.get("title").toString();

        if(title.equals("pedometerUpdate")) {
            Log.i("FCM", "만보기 값을 서버로 전송합니다. " + title);

            pedometer_preferences = getSharedPreferences("pedometer", MODE_PRIVATE);

            try {
                //접속할 서버 주소 (이클립스에서 android.jsp실행시 웹브라우저 주소)
                URL url = new URL("http://ycdi.cafe24.com:8080/YouCanDoIt/pedometerUpdate");
//                URL url = new URL("http://192.168.45.94:8080/YouCanDoIt/pedometerUpdate");
                // http://ip주소:포트번호/이클립스프로젝트명/WebContent아래폴더/androidDB.jsp

                String date = data.get("date").toString();
                String isLast = data.get("isLast").toString();
                String id = user_preferences.getString("id", null);
                int step = pedometer_preferences.getInt("step", 0);

                //전송할 데이터. GET방식으로 작성
                sendMsg = "date=" + date + "&id=" + id + "&pedometer_result=" + step + "&last=" + isLast;

                receiveMsg = new TaskSupport().httpConnection(url, sendMsg);
                Log.i("FCM", "서버 전송 결과 : " + receiveMsg);

                if(isLast.equals("1")) {
                    Log.i("FCM", "자정이므로 만보기를 초기화합니다.");

                    pedometer_editor.putInt("step", 0);
                    pedometer_editor.commit();

                    // 갱신 요청
                    Intent i = new Intent();
                    i.setAction("isLast");
                    sendBroadcast(i);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            String content = data.get("content").toString();

            Log.i("FCM", "알림 수신 : " + title);
            Log.i("FCM", "알림 내용 : " + content);

            // 안드로이드 8.0 이상 버전에서 알림을 사용하기 위한 채널 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("ycdi_notification", "유캔두잇 알림", NotificationManager.IMPORTANCE_HIGH);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            // notification을 눌렀을 때 띄울 Activity
            Intent notificationIntent = new Intent(this, LoginActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            // notification 그룹 선언
            String group = "reminder";
            NotificationCompat.Builder groupNotification = new NotificationCompat.Builder(this, "ycdi_notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setGroup(group)
                    .setGroupSummary(true);

            // notification 선언
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ycdi_notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setGroup(group)
                    .setContentIntent(pendingIntent);

            // notification id 가져오기
            int notifyId = user_preferences.getInt("notifyId", 3);

            // 화면 깨우기
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            wakeLock.acquire(5000);

            // notification 생성
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.notify(notifyId, builder.build());
            manager.notify(2, groupNotification.build());

            // notification id 증가
            user_editor.putInt("notifyId", ++notifyId);
            user_editor.commit();
        }


    }
}
