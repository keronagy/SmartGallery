package com.example.SmartGallery;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServerConnectionService extends Service {
    private static final String TAG = "ServerConnectionService";
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Started");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Started");
        new Thread(new Runnable() {
            @Override
            public void run() {
             //contact server and save in DB
            }
        });
        stopSelf();
        return START_REDELIVER_INTENT; // to continue from where it is stopped and auto restart

//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Started");
        super.onDestroy();
    }
}
