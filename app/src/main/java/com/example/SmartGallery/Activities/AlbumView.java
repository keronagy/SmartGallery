package com.example.SmartGallery.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.SmartGallery.Adapters.ImagesAdapter;
import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.Adapters.GalleryAdapter;
import com.example.SmartGallery.Image;
import com.example.SmartGallery.R;

import java.util.ArrayList;

public class AlbumView extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "https://api.androidhive.info/json/glide.json";
    private ArrayList<Image> images;
    private ProgressDialog pDialog;
    private ImagesAdapter mAdapter;
    private RecyclerView recyclerView;
    static final int REQUEST_PERMISSION_KEY = 1;
    private String album_name="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);
        images = new ArrayList<>();
        mAdapter = new ImagesAdapter(getApplicationContext(), images);
        album_name = getIntent().getStringExtra(CONSTANTS.ALBUM_NAME);
        toolbar.setTitle(album_name);
        toolbar.setSubtitle(album_name);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!CONSTANTS.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }


        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(CONSTANTS.IMAGES, images);
                bundle.putInt(CONSTANTS.POSITION, position);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        fetchImages();
    }

    private void fetchImages() {
        pDialog.setMessage("Loading Photos");
        pDialog.show();

        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED,MediaStore.MediaColumns.TITLE};
        Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");
        Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + album_name + "\"", null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});


        boolean first = true;
        while (cursor.moveToNext()) {


            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE));


            Image image = new Image(albumName,name,timestamp,path,"");
            images .add(image);
            mAdapter.notifyDataSetChanged();
            if(first)
            {
                first = false;
                pDialog.dismiss();
            }
        }


        cursor.close();

    }
}
