package com.example.SmartGallery.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.Database.DBAdapter;
import com.example.SmartGallery.R;

import java.util.ArrayList;
import java.util.Arrays;

public class Setting extends AppCompatActivity {

    private EditText URI;
    private Button Save;
    private Button Cancel;
    private TextView searchByTxt;
    private CheckBox StartCheckBox;
    private TextView AlbumsSelected;
    private ArrayList<String> Selected;
    SharedPreferences sharedPreferences;
    DBAdapter DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        searchByTxt = findViewById(R.id.search_by);
        URI = findViewById(R.id.server_uri_txt);
        StartCheckBox = findViewById(R.id.start_sync_with_app);
        Save = findViewById(R.id.save_setting_btn);
        Cancel = findViewById(R.id.cancel_setting_btn);
        AlbumsSelected = findViewById(R.id.select_albums);
        openDB();
        sharedPreferences = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF);
        String SearchBy = sharedPreferences.getString(CONSTANTS.SEARCH_BY,CONSTANTS.SEARCH_BY_DEFAULT);
        searchByTxt.setText(SearchBy);
        StartCheckBox.setChecked(sharedPreferences.getBoolean(CONSTANTS.START_WITH_APP,CONSTANTS.START_WITH_APP_DEFAULT));
        AlbumsSelected.setText(sharedPreferences.getString(CONSTANTS.ALBUMS_SELECTION,CONSTANTS.ALBUMS_SELECTION_DEFAULT).trim().replaceAll(" ",", "));

        String URIPREF = sharedPreferences.getString(CONSTANTS.APP_SERVER_PREF_API,CONSTANTS.SERVER_URI);
        URI.setText(URIPREF);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = URI.getText().toString();
                if(!uri.isEmpty()) {
                    CONSTANTS.SERVER_URI = URI.getText().toString();
                    saveSharedPref(uri);
                    finish();
                }
            }

        });
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        StartCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    sharedPreferences.edit().putBoolean(CONSTANTS.START_WITH_APP,CONSTANTS.SHARED_TRUE).apply();
                }
                else
                {
                    sharedPreferences.edit().putBoolean(CONSTANTS.START_WITH_APP,CONSTANTS.SHARED_FALSE).apply();

                }
            }
        });
    }
    private void openDB() {
        DB = new DBAdapter(this);
        DB.open();
    }
    private void closeDB() {
        DB.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void saveSharedPref(String API)
    {
        SharedPreferences.Editor editor = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).edit();
        editor.putString(CONSTANTS.APP_SERVER_PREF_API,API);
        editor.apply();
    }


    public void chooseSearchType(View view) {
        final SharedPreferences.Editor editor = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder
                .setTitle("Search by")
                .setPositiveButton("Objects", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editor.putString(CONSTANTS.SEARCH_BY, CONSTANTS.SEARCH_BY_TAGS);
                        editor.apply();
                        searchByTxt.setText(CONSTANTS.SEARCH_BY_TAGS);
                    }
                })
                .setNegativeButton("Captions", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putString(CONSTANTS.SEARCH_BY, CONSTANTS.SEARCH_BY_CAPTIONS);
                        editor.apply();
                        searchByTxt.setText(CONSTANTS.SEARCH_BY_CAPTIONS);
                    }
                }).show();
    }

    public void chooseAlbums(View view) {

        // where we will store or remove selected items
        final ArrayList<String> Albums = getAlbumsNames();

        final String[] AlbumsArr = Albums.toArray(new String[Albums.size()]);
        CharSequence AlbumsArrSeq[] = (CharSequence[]) AlbumsArr;
        final String selectedAlbums = sharedPreferences.getString(CONSTANTS.ALBUMS_SELECTION,CONSTANTS.ALBUMS_SELECTION_DEFAULT);
        final String[] splited = selectedAlbums.split("\\s+");
        Selected = new ArrayList<>(Arrays.asList(splited));
        boolean[] selectedIndecies = new boolean[Albums.size()];

        for (int i = 0; i < Albums.size(); i++) {
            if(Selected.contains(Albums.get(i)))
            {
                 selectedIndecies[i] = true;
            }
            else
            {
                selectedIndecies[i] = false;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);

        // set the dialog title
        builder.setTitle("Choose One or More")

                // specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive call backs when items are selected
                // R.array.choices were set in the resources res/values/strings.xml
                .setMultiChoiceItems(AlbumsArrSeq ,  selectedIndecies , new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            // if the user checked the item, add it to the selected items
                            Selected.add(Albums.get(which));
                        }

                        else if (Selected.contains(Albums.get(which))) {
                            // else if the item is already in the array, remove it
                            Selected.remove(Albums.get(which));
                        }

                        // you can also add other codes here,
                        // for example a tool tip that gives user an idea of what he is selecting
                        // showToast("Just an example description.");
                    }

                })

                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // user clicked OK, so save the mSelectedItems results somewhere
                        // here we are trying to retrieve the selected items indices
                        String selectedIndex = "";
                        for(String i : Selected){
                            selectedIndex += i + " ";
                        }
                        sharedPreferences.edit().putString(CONSTANTS.ALBUMS_SELECTION,selectedIndex).apply();
                        AlbumsSelected.setText(selectedIndex.trim().replaceAll(" ",", "));
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // removes the AlertDialog in the screen
                    }
                })

                .show();
    }

    private ArrayList<String> getAlbumsNames()
    {
        ArrayList<String> AlbumsName = new ArrayList<String>();
        Cursor mCursor = DB.getAllAlbumsNames();
        do{
            AlbumsName.add(mCursor.getString(0));
        }while (mCursor.moveToNext());
        return AlbumsName;

    }

}
