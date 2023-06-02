package com.example.youcandoit_app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class StepReset extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        ((Pedometer)Pedometer.PedoContext).countReset(); //만보기 리셋하는 함수 실행시키기.
        ((Pedometer)Pedometer.PedoContext).AddRecord(); //만보기 리셋하는 함수 실행시키기.

        Log.d("resetAlarm", "StepRest.java 실행. ");

        Toast.makeText(context, "StepReset", Toast.LENGTH_LONG).show();

    }
}
