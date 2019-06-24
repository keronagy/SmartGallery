package com.example.SmartGallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        String Msg = intent.getStringExtra(CONSTANTS.BROADCAST_MSG);
            Log.d("eeeeeeeeeeeeeee", "onReceive: "+Msg);
        switch(Msg)
        {
            case CONSTANTS.PAUSE_QUEUE:
                StopQueue();
                break;
            case CONSTANTS.RESUME_QUEUE:
                StartQueue();
                break;
        }
    }

    public void StopQueue() {
        ServiceQueueSingleton.getInstance(ctx).stopRequestQueue();
    }

    public void StartQueue() {
        ServiceQueueSingleton.getInstance(ctx).startRequestQueue();
    }
}
