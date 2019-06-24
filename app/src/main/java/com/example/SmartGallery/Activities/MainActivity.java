package com.example.SmartGallery.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.SmartGallery.Adapters.AlbumAdapter;
import com.example.SmartGallery.Adapters.SearchAdapter;
import com.example.SmartGallery.Album;
import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.Database.DBAdapter;
import com.example.SmartGallery.Image;
import com.example.SmartGallery.R;
import com.example.SmartGallery.ServerConnectionService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final String endpoint = "https://api.androidhive.info/json/glide.json";
    private ArrayList<Album> albumList;
    private ProgressDialog pDialog;
    private AlbumAdapter mAdapter;
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    private Uri uri;
    private Toolbar toolbar;
    static final int REQUEST_PERMISSION_KEY = 1;
    final int RequestPermissionCode=2;
    DBAdapter DB;
    private Button StartService;


    RecyclerView.OnItemTouchListener touchListener;
    private Intent ServerSercvice ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle(R.string.albums);
        albumList = new ArrayList<>();


        openDB();
        recyclerView = findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(this);
        mAdapter = new AlbumAdapter(getApplicationContext(), albumList);

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
                Intent intent = new Intent(MainActivity.this, AlbumView.class);
                intent.putExtra(CONSTANTS.ALBUM_NAME, albumList.get(position).getName());
                intent.putExtra(CONSTANTS.ALBUM_PATH, albumList.get(position).getPath());
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });

        recyclerView.addOnItemTouchListener(touchListener);

        String API = loadSharedPref();
        if(API.equals(""))
        {
            saveSharedPref("http://192.168.1.6:5000/api");
        }
        else
        {
            CONSTANTS.SERVER_URI= API;
        }
        ServerSercvice = new Intent(MainActivity.this, ServerConnectionService.class);
        StartService = findViewById(R.id.testService);
        StartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(ServerSercvice);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.cancel();
        }
    }

    private void openDB() {
        DB = new DBAdapter(this);
        DB.open();
    }
    private void closeDB() {
        DB.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting,menu);
        MenuItem search = menu.findItem(R.id.search_menu);
        SearchView  searchView = (SearchView) search.getActionView();
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
                if(s.equals("")) return false;

                SharedPreferences sharedPreferences = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF);
                String SearchBy = sharedPreferences.getString(CONSTANTS.SEARCH_BY,CONSTANTS.SEARCH_BY_DEFAULT);
                Cursor data = null;
                if(SearchBy.equals(CONSTANTS.SEARCH_BY_CAPTIONS))
                {
                    data = DB.getRowByCaption(s);
                }
                else if(SearchBy.equals(CONSTANTS.SEARCH_BY_TAGS))
                {
                    data = DB.getRowByTag(s);
                }

                HashSet<Image> searcedImages = new HashSet<>();
                if (data != null) {
                    data.moveToFirst();
                    for (int i = 0; i < data.getCount(); i++) {


                        String path = data.getString(DBAdapter.COL_PATH);
                        File f = new File(path);
                        String name = f.getName();
                        String caption = data.getString(DBAdapter.COL_CAPTION);
                        String album = data.getString(DBAdapter.COL_ALBUM);
                        String tags = data.getString(DBAdapter.COL_TAGS);
                        String time = data.getString(DBAdapter.COL_DATE);
                        searcedImages.add(new Image(album, name, time, path, caption, tags));
                        data.moveToNext();


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
//                        Toast.makeText(MainActivity.this, "long click share "+ , Toast.LENGTH_LONG).show();
                            Uri fileUri = Uri.parse("file://" + searchedlist.get(position).getPath());

                            //No need to do mimeType work or ext

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            intent.setType("image/*");
                            startActivity(Intent.createChooser(intent, "Share Image:"));
                        }
                    });
                    recyclerView.addOnItemTouchListener(touchListener);

                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.setting)
        {
            startActivity(new Intent(MainActivity.this,Setting.class));
            return true;
        }
        if(item.getItemId() == R.id.camera_menu)
        {
            openCamAndCrop();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchAlbums() {

        pDialog.setMessage("Loading Albums");
        pDialog.show();

        Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
        Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");
        Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

        boolean first = true;
        albumList.clear();
        while (cursor.moveToNext()) {


            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

            Album album = new Album(this, path,name,timestamp);
            albumList.add(album);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    fetchAlbums();
                    addAllPathstoDB();
                } else
                {
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
            break;
            case RequestPermissionCode:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    Toast.makeText(this,getString(R.string.perm_granted),Toast.LENGTH_SHORT).show();
                    openCamAndCrop();
//                else
//                    Toast.makeText(this,getString(R.string.perm_canceled),Toast.LENGTH_SHORT).show();
            }
            break;
        }

    }
    boolean twice;

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

            toolbar.setSubtitle(R.string.albums);

            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            recyclerView.removeOnItemTouchListener(touchListener);
            touchListener = new AlbumAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new AlbumAdapter.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    Intent intent = new Intent(MainActivity.this, AlbumView.class);
                    intent.putExtra(CONSTANTS.ALBUM_NAME, albumList.get(position).getName());
                    intent.putExtra(CONSTANTS.ALBUM_PATH, albumList.get(position).getPath());
                    startActivity(intent);

                }

                @Override
                public void onLongClick(View view, int position) {

                }
            });
            recyclerView.addOnItemTouchListener(touchListener);
        }
        else
        {
            toolbar.setSubtitle(R.string.albums);
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!CONSTANTS.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }else{
            if(albumList.size()==0)
                fetchAlbums();
            addAllPathstoDB();
        }



    }

    public void checkPermissions()
    {

    }


    private void saveSharedPref(String API)
    {
        SharedPreferences.Editor editor = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).edit();
        editor.putString(CONSTANTS.APP_SERVER_PREF_API,API);
        editor.apply();
    }

    private String loadSharedPref()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF);
        String API = sharedPreferences.getString(CONSTANTS.APP_SERVER_PREF_API,"");
        return API;
    }

    private void openCamAndCrop()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
            CameraOpen();
        else
            RequestRuntimePermission();

    }

    private void RequestRuntimePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA))
        {
//            Toast.makeText(this,getString(R.string.camera_permission),Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestPermissionCode);
        }
    }
    private void CameraOpen() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();

                try {
                    getTextFromImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap) {

        new ServerConnection().execute( bitmap);
    }

    private class ServerConnection extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, "Async task Finished\n"+s, Toast.LENGTH_LONG).show();

        }

        @Override
        protected String doInBackground(Bitmap... bitmap) {

            String encodedImage;
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();

            bitmap[0].compress(Bitmap.CompressFormat.PNG, CONSTANTS.COMPRESSION_QUALITY, byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            JSONObject postData = new JSONObject();
            try{
                postData.put(CONSTANTS.IMAGE_POST_SERVER, encodedImage );
            }
            catch (Exception e){
                e.printStackTrace();
            }

            String data = "";
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(CONSTANTS.SERVER_URI).openConnection();
                httpURLConnection.setRequestProperty(CONSTANTS.CONTENT_TYPE_STRING, CONSTANTS.CONTENT_TYPE);
                httpURLConnection.setRequestMethod(CONSTANTS.REQUEST_TYPE);

                httpURLConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(postData.toString());
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                data = "error connecting to server\n"+ e.getMessage();
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }
    }

    public void addAllPathstoDB() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                String[] projection = {MediaStore.MediaColumns.DATA,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.TITLE};
                Cursor cursorExternal = getContentResolver().query(uriExternal, projection, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
                Cursor cursorInternal = getContentResolver().query(uriInternal, projection, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");
                Cursor URIcursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});
                //check if there is data in DB
                if (URIcursor.getCount() != 0) {
                    Log.d(TAG, "found photos");
                    long rowID;
                    //add photos to database
                    URIcursor.moveToNext();
                    do {
                        //ask about rowID (internal and external merge)
                        String path = URIcursor.getString(URIcursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                        String albumName = URIcursor.getString(URIcursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        String timestamp = URIcursor.getString(URIcursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                        rowID = DB.insertRow(path, null, null, timestamp,albumName);
                        Log.d(TAG, "rowID: "+rowID);
                    } while (URIcursor.moveToNext());
                    URIcursor.close();
                    Log.d(TAG, "run: service about to be started");
                    startService(ServerSercvice);
                } else {
                    Log.d(TAG, "there is no photos on the phone");
                }
            }
        };
        thread.start();
    }

}
