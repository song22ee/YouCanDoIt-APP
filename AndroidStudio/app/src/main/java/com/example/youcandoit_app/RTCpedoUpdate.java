package com.example.youcandoit_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RTCpedoUpdate extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            // Set the alarm here.
//
//        }
        ((Pedometer) Pedometer.PedoContext).exe_RTCpedoUPThread();
    }
}