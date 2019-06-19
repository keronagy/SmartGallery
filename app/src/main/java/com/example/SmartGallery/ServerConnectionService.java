package com.example.SmartGallery;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.SmartGallery.Database.DBAdapter;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class ServerConnectionService extends Service {
    private NotificationManagerCompat notificationManager;
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
        notificationManager = NotificationManagerCompat.from(this);
        openDB();

        new Thread(new Runnable() {
            @Override
            public void run() {
             //contact server and save in DB
                showNotification();

            }
        });
//        stopSelf();
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
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Started");
        closeDB();
        super.onDestroy();
    }

    public void showNotification()
    {
        Notification notification = new NotificationCompat.Builder(this,CONSTANTS.CHANNEL_ID)
                .setContentTitle(CONSTANTS.NOTIFICATION_TITLE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setSmallIcon(R.drawable.ic_search_black_24dp)
                .build();

        notificationManager.notify(CONSTANTS.NOTIFICATION_ID,notification);
    }



    public void getCaptionAndTag(final String Path, final String Service) {
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        Bitmap bmp =  getBitmap(Path);
        bmp.compress(Bitmap.CompressFormat.PNG, CONSTANTS.COMPRESSION_QUALITY, byteArrayBitmapStream);
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
//                R.drawable.test);
        bmp.compress(Bitmap.CompressFormat.PNG, CONSTANTS.COMPRESSION_QUALITY, byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        JSONObject postData = new JSONObject();
        try {
            postData.put(CONSTANTS.IMAGE_POST_SERVER, encodedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = CONSTANTS.SERVER_URI+Service;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    switch (Service)
                    {
                        case CONSTANTS.CAPTION:
                            DB.updateRowCaption(Path,response.getString(CONSTANTS.RECEIVED_CAPTION_JSON));
                            break;
                        case CONSTANTS.DETECTION:
                            DB.updateRowTags(Path,response.getString(CONSTANTS.RECEIVED_TAGS_JSON));
                            break;
                        case "both":
                            DB.updateRow(Path,response.getString(CONSTANTS.RECEIVED_CAPTION_JSON),response.getString(CONSTANTS.RECEIVED_TAGS_JSON));
                            break;
                    }
//                    Toast.makeText(MainActivity.this, response.getString("caption"), Toast.LENGTH_LONG).show();
//                    Log.d("eeeeeeeee", "onResponse: "+response.getString("caption"));


                } catch (Exception ex) {
//                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
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
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue.add(request);

    }

    public Bitmap getBitmap(String path) {
        try {
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
