package com.example.youcandoit_app.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import com.example.youcandoit_app.Service.PedometerService;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor_accelerometer = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            // 디바이스에 걸음 센서의 존재 여부 체크
            if (sensor_accelerometer != null) {
                // 센서가 있다면 Service 실행
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, PedometerService.class));
                }
            }
        }
    }
}
