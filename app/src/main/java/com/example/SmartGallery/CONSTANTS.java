package com.example.SmartGallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CONSTANTS {
    public static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_PATH = "album_path";
    public static final String IMAGES = "images";
    public static final String POSITION = "position";
    public static final int COMPRESSION_QUALITY = 100;




    //Server Data
    public static String SERVER_URI = "http://192.168.1.6:5000/api";
    public static final String IMAGE_POST_SERVER = "image";
    public static final String CONTENT_TYPE_STRING = "Content-Type";
    public static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    public static final String REQUEST_TYPE = "POST";
    public static final String APP_SERVER_PREF = "server_pref";
    public static final String APP_SERVER_PREF_API = "server_api";
    public static final int PRIVATE_SHARED_PREF = 0;





    public static String converToTime(String timestamp)
    {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        return formatter.format(date);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}
