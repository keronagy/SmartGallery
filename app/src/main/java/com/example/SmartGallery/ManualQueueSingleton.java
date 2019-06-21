package com.example.SmartGallery;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ManualQueueSingleton {
    private static ManualQueueSingleton manualQueue;
    private RequestQueue requestQueue;
    private static Context ctx;

    private ManualQueueSingleton(Context ctx)
    {
        this.ctx = ctx;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue()
    {
        if(requestQueue==null)
        {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized ManualQueueSingleton getInstance(Context ctx)
    {
        if(manualQueue == null)
        {
            manualQueue = new ManualQueueSingleton(ctx);
        }
        return manualQueue;
    }

    public void addToRequestQueue(Request request)
    {
        requestQueue.add(request);
    }
    public void stopRequestQueue()
    {
        requestQueue.stop();
    }
    public void startRequestQueue()
    {
        requestQueue.start();
    }

}
