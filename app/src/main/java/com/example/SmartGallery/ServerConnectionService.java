package com.example.SmartGallery;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.SmartGallery.Database.DBAdapter;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class ServerConnectionService extends Service {
    NotificationCompat.Builder notification;
    private NotificationManagerCompat notificationManager;
    int Progress;
    DBAdapter DB;
    private static final String TAG = "ServerConnectionService";
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: Started");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Started");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//             //contact server and save in DB
//
//
//            }
//        }).run();
        return START_REDELIVER_INTENT; // to continue from where it is stopped and auto restart

//        return super.onStartCommand(intent, flags, startId);
    }
    private void openDB() {
        DB = new DBAdapter(this);
        DB.open();
    }
    private void closeDB() {
        DB.close();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: started");
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        openDB();
        trustEveryone();
        final Cursor images = DB.getAllRowsNullCaptionAndTags();
        showNotification(images.getCount());
        getCaptionAndTag(CONSTANTS.CAPTION_DTECTION,images);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Started");
        closeDB();
        super.onDestroy();
    }

    public void showNotification(int ProgressMax)
    {
        if(ProgressMax ==0)
        {
            closeDB();
            stopSelf();
            return;
        }
        Intent broadcastPause = new Intent(this,NotificationReceiver.class);
        broadcastPause.putExtra(CONSTANTS.BROADCAST_MSG,CONSTANTS.PAUSE_QUEUE);
        PendingIntent pauseAction = PendingIntent.getBroadcast(this,0,broadcastPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcastResume = new Intent(this,NotificationReceiver.class);
        broadcastResume.putExtra(CONSTANTS.BROADCAST_MSG,CONSTANTS.RESUME_QUEUE);
        PendingIntent resumeAction = PendingIntent.getBroadcast(this,1,broadcastResume, PendingIntent.FLAG_UPDATE_CURRENT);



        notification = new NotificationCompat.Builder(this,CONSTANTS.CHANNEL_ID)
                .setContentTitle(CONSTANTS.NOTIFICATION_TITLE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentText("0/"+ProgressMax)
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .setOngoing(true)
                .addAction(R.drawable.ic_launcher_background,"Pause",pauseAction)
                .addAction(R.drawable.ic_launcher_background,"Resume",resumeAction)
                .setOnlyAlertOnce(true)
                .setProgress(ProgressMax,0,false);


        notificationManager.notify(CONSTANTS.NOTIFICATION_ID,notification.build());
    }

    @SuppressLint("RestrictedApi")
    public void updateNotification(int Progress, int ProgressMax)
    {
        if(Progress>=ProgressMax) {
            notification.setProgress(0, 0, false);
            notification.setContentText("Done");
            notification.setOngoing(false);
            notification.mActions.clear();
            notificationManager.notify(CONSTANTS.NOTIFICATION_ID, notification.build());
        }
        else
        {
            notification.setProgress(ProgressMax, Progress, false);
            notification.setContentText(Progress + "/" + ProgressMax);
            notificationManager.notify(CONSTANTS.NOTIFICATION_ID, notification.build());
        }
    }



    public void getCaptionAndTag(final String Service,Cursor images) {
        String url = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).getString(CONSTANTS.APP_SERVER_PREF_API,CONSTANTS.APP_SERVER_PREF_API)+Service;
        if(images==null || images.getCount()==0)
        {
            stopSelf();
            return;
        }
        final int ProgressMax = images.getCount();
        Progress = 0;
        do {
            String Path = images.getString(DBAdapter.COL_PATH);
            JSONObject postData = CONSTANTS.CreateJsonObject(Path);
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(Object tag , JSONObject response) {
                    try {
                        String path = (String) tag;
                        Progress++;
                        updateNotification(Progress,ProgressMax);
                        if(!DB.isOpen())
                        {
                            DB.open();
                        }
                        switch (Service)
                        {

                            case CONSTANTS.CAPTION:
                                DB.updateRowCaption("\""+path+"\"",response.getString(CONSTANTS.RECEIVED_CAPTION_JSON));
                                break;
                            case CONSTANTS.DETECTION:
                                DB.updateRowTags("\""+path+"\"",response.getString(CONSTANTS.RECEIVED_TAGS_JSON));
                                break;
                            case CONSTANTS.CAPTION_DTECTION:
                                DB.updateRow("\""+path+"\"",response.getString(CONSTANTS.RECEIVED_CAPTION_JSON),response.getString(CONSTANTS.RECEIVED_TAGS_JSON));
                                break;
                        }
//                    Toast.makeText(MainActivity.this, response.getString("caption"), Toast.LENGTH_LONG).show();
//                    Log.d("eeeeeeeee", "onResponse: "+response.getString("caption"));


                    } catch (Exception ex) {
                        Log.d(TAG, "onResponse: error"+ ex.getMessage());
// Toast.makeText(ServerConnectionService.this, "error", Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, "error ya 3abeeet\n"+ error.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("eeeeeee", "onErrorResponse: "+ error.toString());
                }
            });
            request.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 300000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });
            request.setTag(Path);
            ServiceQueueSingleton.getInstance(this).addToRequestQueue(request);
        }while (images.moveToNext());
        Progress=0;
        closeDB();
    }


    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
