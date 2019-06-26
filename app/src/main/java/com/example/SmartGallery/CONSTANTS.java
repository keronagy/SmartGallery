package com.example.SmartGallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CONSTANTS {

    public static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_PATH = "album_path";
    public static final String IMAGES = "images";
    public static final String POSITION = "position";
    public static final int COMPRESSION_QUALITY = 100;


    //Notification Constants
    public static final String CHANNEL_ID = "Service_Channel";
    public static final String CHANNEL_NAME = "Server Connection";
    public static final String CHANNEL_DESC = "Server Connection Details";
    public static final String NOTIFICATION_TITLE = "Getting Caption and Tags";
    public static final int NOTIFICATION_ID = 1;


    //Server Data
    public static String SERVER_URI = "http://192.168.43.214:5000";
    public static final String CAPTION = "/api/caption";
    public static final String DETECTION = "/api/detection";
    public static final String CAPTION_DTECTION = "/api/image";
    public static final String RECEIVED_CAPTION_JSON = "caption";
    public static final String RECEIVED_TAGS_JSON = "tags";
    public static final String IMAGE_POST_SERVER = "image";
    public static final String CONTENT_TYPE_STRING = "Content-Type";
    public static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    public static final String REQUEST_TYPE = "POST";


    //SharedPreference Constants
    public static final String APP_SERVER_PREF = "server_pref";
    public static final String APP_SERVER_PREF_API = "server_api";
    public static final int PRIVATE_SHARED_PREF = 0;
    public static final String SEARCH_BY = "SearchBy";
    public static final String SEARCH_BY_DEFAULT= "Objects";
    public static final String SEARCH_BY_TAGS= "Objects";
    public static final String SEARCH_BY_CAPTIONS= "Captions";
    public static final String ALBUMS_SELECTION= "albums";
    public static final String ALBUMS_SELECTION_DEFAULT= "Camera";
    public static final String START_WITH_APP= "start_with_app";
    public static final Boolean START_WITH_APP_DEFAULT= false;
    public static final Boolean SHARED_FALSE= false;
    public static final Boolean SHARED_TRUE= false;





    //BroadCast Receiver Constants
    public static final String BROADCAST_MSG = "queue_status";
    public static final String PAUSE_QUEUE= "PAUSE_queue";
    public static final String RESUME_QUEUE= "resume_queue";


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
    public static JSONObject CreateJsonObject(String path)
    {
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        Bitmap bmp =  getBitmap(path);
        bmp.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        JSONObject postData = new JSONObject();
        try {
            postData.put(IMAGE_POST_SERVER, encodedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return postData;
    }

    public static Bitmap getBitmap(String path) {
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
