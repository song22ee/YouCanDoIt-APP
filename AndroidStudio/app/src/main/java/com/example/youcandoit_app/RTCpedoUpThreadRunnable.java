package com.example.youcandoit_app;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class RTCpedoUpThreadRunnable implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(false);

    public RTCpedoUpThreadRunnable() {
    }

    public void stop() {
        Log.i("Pedometer.java","Stop RTCpedoUpThreadRunnable");
        running.set(false);
    }

    //스레드 클래스 생성 - 실시간 내부 db업데이트
//    class RTCpedoUPThread extends Thread {
//        boolean running = false;
//        public void run() {
//            running = true;
//            while (running) {
//                handler.sendEmptyMessage(1);
//                currentTime = System.currentTimeMillis();
//                SystemClock.sleep(1000);//1초 간격
//            }
//        }
//    }

    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                ((Pedometer) Pedometer.PedoContext).handler.sendEmptyMessage(1);
//                currentTime = System.currentTimeMillis();
                Log.i("RTCpedoUpThreadRunnable","RTCpedoUpThreadRunnable is running");
                Thread.sleep(1000);//1초 간격
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("RTCpedoUpThreadRunnable was interrupted");
            }
        }
    }
}