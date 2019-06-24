package com.example.SmartGallery.Activities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.Database.DBAdapter;
import com.example.SmartGallery.Image;
import com.example.SmartGallery.ManualQueueSingleton;
import com.example.SmartGallery.R;
import com.example.SmartGallery.ServiceQueueSingleton;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;



public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<Image> images;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0;
    private Button GetCaptionBtn;
    private TextView captionTxt;
    private TextView tagsTxt;
    private String curposition;
    DBAdapter DB;
    public static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }
    public Context mainthis;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_slider, container, false);
        openDB();
        viewPager = v.findViewById(R.id.viewpager);
        lblCount = v.findViewById(R.id.lbl_count);
        lblTitle =  v.findViewById(R.id.title);
        lblDate =  v.findViewById(R.id.date);
        GetCaptionBtn = v.findViewById(R.id.caption_btn);
        mainthis = getActivity();
        captionTxt = v.findViewById(R.id.caption_txt);
        tagsTxt = v.findViewById(R.id.tags_txt);
        GetCaptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFromServer(CONSTANTS.CAPTION_DTECTION);
            }
        });

        images = (ArrayList<Image>) getArguments().getSerializable(CONSTANTS.IMAGES);
        selectedPosition = getArguments().getInt(CONSTANTS.POSITION);
        curposition = getArguments().getInt(CONSTANTS.POSITION)+"";
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }
    private void openDB() {
        DB = new DBAdapter(this.getContext());
        DB.open();
    }
    private void closeDB() {
        DB.close();
    }

    @Override
    public void onDestroy() {
        closeDB();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //  page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + images.size());
        curposition = position+"";
        Image image = images.get(position);
        String path = image.getPath();
        Cursor c = DB.getRow(path);
        c.moveToFirst();
        File f = new File(path);
        String name = f.getName();
        lblTitle.setText(name);
        lblDate.setText(CONSTANTS.converToTime(c.getString(DBAdapter.COL_DATE)));
        String Caption = c.getString(DBAdapter.COL_CAPTION);
        String Tags = c.getString(DBAdapter.COL_TAGS);
        if(Caption==null || Tags== null)
        {
            GetCaptionBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            GetCaptionBtn.setVisibility(View.GONE);
            captionTxt.setText(Caption);
            tagsTxt.setText(Tags);
        }

    }



    //  adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter()  {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.zoomable_image, container, false);

            ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);
            Image image = images.get(position);

            Glide.with(getActivity()).load(new File(image.getPath()))
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPreview);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public void getFromServer(final String Service) {

        String Path= images.get(Integer.parseInt(curposition)).getPath();
        JSONObject postData = CONSTANTS.CreateJsonObject(Path);
        String url = getContext().getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).getString(CONSTANTS.APP_SERVER_PREF_API,CONSTANTS.APP_SERVER_PREF_API)+Service;
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(Object tag , JSONObject response) {
                try {
                    String path = (String) tag;
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
                            String Caption = response.getString(CONSTANTS.RECEIVED_CAPTION_JSON);
                            String Tags = response.getString(CONSTANTS.RECEIVED_TAGS_JSON);
                            DB.updateRow("\""+path+"\"",Caption,Tags);
                            if(images.get(Integer.parseInt(curposition)).getPath().equals(tag))
                            {
                                captionTxt.setText(Caption);
                                tagsTxt.setText(Tags);
                            }
                            break;
                    }
                    ServiceQueueSingleton.getInstance(getContext()).startRequestQueue();

                } catch (Exception ex) {
                    ServiceQueueSingleton.getInstance(getContext()).startRequestQueue();

                    Log.d(TAG, "onResponse: error"+ ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("eeeeeee", "onErrorResponse: "+ error.toString());
                ServiceQueueSingleton.getInstance(getContext()).startRequestQueue();

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
        Context context = getContext();
        ServiceQueueSingleton.getInstance(context).stopRequestQueue();
        ServiceQueueSingleton.getInstance(context).cancelRequestByTag(Path);
        ManualQueueSingleton.getInstance(context).addToRequestQueue(request);

    }










}