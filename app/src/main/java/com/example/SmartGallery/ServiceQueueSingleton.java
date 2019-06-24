package com.example.SmartGallery;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ServiceQueueSingleton {
    private static ServiceQueueSingleton serviceQueue;
    private RequestQueue requestQueue;
    private static Context ctx;

    private ServiceQueueSingleton(Context ctx)
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

    public static synchronized ServiceQueueSingleton getInstance(Context ctx)
    {
        if(serviceQueue == null)
        {
            serviceQueue = new ServiceQueueSingleton(ctx);
        }
        return serviceQueue;
    }

    public void addToRequestQueue(Request request)
    {
        Log.d("eeeeeeee", "addToRequestQueue: adding new one");
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
    public void cancelRequestByTag(String Tag)
    {
        requestQueue.cancelAll(Tag);
    }

}
