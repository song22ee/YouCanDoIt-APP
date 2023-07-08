package com.example.youcandoit_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class PedometerService extends Service implements SensorEventListener {

    NotificationCompat.Builder builder;
    //만보기
    Sensor sensor_accelerometer;
    SensorManager sm;
    public int walkingCount = 0; //만보기 변수

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Intent pedometerIntent;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.v("Pedometer.java", "onSensorChanged()");
            // 앱을 처음 사용할 때
            if (preferences.getInt("step", -1) == -1) {
                Log.v("Pedometer.java:", "초기화됨");
                editor.putInt("step", 0);
                editor.putInt("prevStep", (int) event.values[0]);
                editor.commit();
            }

            // 전 만보기 값
            walkingCount = preferences.getInt("step", 0);
            // 전 이벤트 값
            int prevWalkingCount = preferences.getInt("prevStep", 0);

            if (event.values[0] < prevWalkingCount) {
                // 재부팅 했을 경우 이벤트 값이 초기화 되기 때문에 처음부터 다시 쌓아준다.
                Log.v("Pedometer.java:", "이벤트 값이 적음");
                editor.putInt("prevStep", 0);
                editor.putInt("step", walkingCount + (int) event.values[0]);
            } else if (event.values[0] - prevWalkingCount > 1) {
                // 센서가 다시 반응 했을 때 다시 반응할 동안의 누적값을 쌓아준다.
                Log.v("Pedometer.java:", "센서 반응");
                editor.putInt("step", walkingCount + ((int) event.values[0] - prevWalkingCount));
            } else if (event.values[0] > prevWalkingCount) {
                // 센서가 활성화 되었을 때 1씩 증가
                Log.v("Pedometer.java:", "카운트 증가");
                editor.putInt("step", ++walkingCount);
            }

            editor.putInt("prevStep", (int) event.values[0]);
            editor.commit();

            // notification 갱신
            builder.setContentText(preferences.getInt("step", 0) + "");
            startForeground(1, builder.build());

            // Activity에 갱신 요청
            pedometerIntent.setAction("pedometer");
            pedometerIntent.putExtra("step", preferences.getInt("step", 0));
            sendBroadcast(pedometerIntent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        pedometerIntent = new Intent();

        // SharedPreferences : 단순한 데이터를 저장(키-값). 앱을 종료해도 유지
        preferences = getSharedPreferences("pedometer", MODE_PRIVATE);
        editor = preferences.edit();

        // 안드로이드 8.0 이상 버전에서 알림을 사용하기 위한 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("test_notification", "테스트 알림", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        // notification을 눌렀을 때 띄울 Activity
        Intent notificationIntent = new Intent(this, Pedometer.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // notification 선언
        builder = new NotificationCompat.Builder(this, "test_notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("유캔두잇")
                .setContentText(preferences.getInt("step", 0) + "")
                .setContentIntent(pendingIntent);

        // notification Foreground로 실행
        startForeground(1, builder.build());

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (sensor_accelerometer != null) {
            sm.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}