package com.example.SmartGallery.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.SmartGallery.Adapters.AlbumAdapter;
import com.example.SmartGallery.Adapters.ImagesAdapter;
import com.example.SmartGallery.Adapters.SearchAdapter;
import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.Database.DBAdapter;
import com.example.SmartGallery.Image;
import com.example.SmartGallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;


public class AlbumView extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "https://api.androidhive.info/json/glide.json";
    private ArrayList<Image> images;
    private ProgressDialog pDialog;
    private ImagesAdapter mAdapter;
    private RecyclerView recyclerView;
    static final int REQUEST_PERMISSION_KEY = 1;
    private String album_name="";
    DBAdapter DB;

    private SearchAdapter searchAdapter;
    private Toolbar toolbar;

    RecyclerView.OnItemTouchListener touchListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        openDB();


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        pDialog = new ProgressDialog(this);
        images = new ArrayList<>();
        mAdapter = new ImagesAdapter(getApplicationContext(), images);
        album_name = getIntent().getStringExtra(CONSTANTS.ALBUM_NAME);
        toolbar.setSubtitle(album_name);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!CONSTANTS.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }



        touchListener = new AlbumAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new AlbumAdapter.ClickListener() {
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
                Uri fileUri  = Uri.parse("file://"+images.get(position).getPath());

                //No need to do mimeType work or ext

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.setType("image/*");
                startActivity(Intent.createChooser(intent, "Share Image:"));
            }
        });
        recyclerView.addOnItemTouchListener(touchListener);

        fetchImages();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting,menu);MenuItem search = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) search.getActionView();
        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do whatever you need
                return true; // KEEP IT TO TRUE OR IT DOESN'T OPEN !!
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                toolbar.setSubtitle(R.string.search_results);

                return true; // OR FALSE IF YOU DIDN'T WANT IT TO CLOSE!
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                Cursor data = DB.getAllRowsSorted();
                HashSet<Image> searcedImages = new HashSet<>();

                for (int i = 0; i < 10; i++) {

                    data.moveToNext();
                    String path = data.getString(DBAdapter.COL_PATH);
                    File f = new File(path);
                    String name = f.getName();
                    String caption = data.getString(DBAdapter.COL_CAPTION);
                    String album = data.getString(DBAdapter.COL_ALBUM);
                    String tags = data.getString(DBAdapter.COL_TAGS);
                    String time = data.getString(DBAdapter.COL_DATE);
                    searcedImages.add(new Image(album, name, time, path, caption, tags));


                }
                final ArrayList<Image> searchedlist = new ArrayList<Image>(searcedImages);

                searchAdapter = new SearchAdapter(getApplicationContext(), searchedlist);
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(searchAdapter);
                recyclerView.removeOnItemTouchListener(touchListener);
                touchListener = new AlbumAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new AlbumAdapter.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CONSTANTS.IMAGES, searchedlist);
                        bundle.putInt(CONSTANTS.POSITION, position);

                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                        newFragment.setArguments(bundle);
                        newFragment.show(ft, "slideshow");
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        Uri fileUri  = Uri.parse("file://"+searchedlist.get(position).getPath());

                        //No need to do mimeType work or ext

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        intent.setType("image/*");
                        startActivity(Intent.createChooser(intent, "Share Image:"));
                    }
                });
                recyclerView.addOnItemTouchListener(touchListener);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.setting)
        {
            startActivity(new Intent(AlbumView.this,Setting.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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


            Image image = new Image(albumName,name,timestamp,path,"","");

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

    @Override
    public void onBackPressed() {
        if(recyclerView.getAdapter()== mAdapter)
        {
            super.onBackPressed();
            toolbar.setSubtitle(R.string.search_results);
//            Intent i = new Intent(Intent.ACTION_MAIN);
//            i.addCategory(Intent.CATEGORY_HOME);
//            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);
//            finish();
//            System.exit(0);
        }
        else if (recyclerView.getAdapter()== searchAdapter)
        {

            toolbar.setSubtitle(album_name);

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            recyclerView.removeOnItemTouchListener(touchListener);

            touchListener = new AlbumAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new AlbumAdapter.ClickListener() {
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
                    Uri fileUri  = Uri.parse("file://"+images.get(position).getPath());

                    //No need to do mimeType work or ext

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    intent.setType("image/*");
                    startActivity(Intent.createChooser(intent, "Share Image:"));
                }
            });
            recyclerView.addOnItemTouchListener(touchListener);
        }
        else
        {
            toolbar.setSubtitle(album_name);
            super.onBackPressed();
        }
    }

    private void openDB() {
        DB = new DBAdapter(this);
        DB.open();
    }
    private void closeDB() {
        DB.close();
    }


}
