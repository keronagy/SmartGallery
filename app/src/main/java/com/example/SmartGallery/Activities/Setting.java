package com.example.SmartGallery.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.R;

public class Setting extends AppCompatActivity {

    private EditText URI;
    private Button Save;
    private Button Cancel;
    private TextView searchByTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        searchByTxt = findViewById(R.id.search_by);
        URI = findViewById(R.id.server_uri_txt);
        Save = findViewById(R.id.save_setting_btn);
        Cancel = findViewById(R.id.cancel_setting_btn);
        SharedPreferences sharedPreferences = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF);
        String SearchBy = sharedPreferences.getString(CONSTANTS.SEARCH_BY,CONSTANTS.SEARCH_BY_DEFAULT);
        searchByTxt.setText(SearchBy);
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
    }

    private void saveSharedPref(String API)
    {
        SharedPreferences.Editor editor = getSharedPreferences(CONSTANTS.APP_SERVER_PREF,CONSTANTS.PRIVATE_SHARED_PREF).edit();
        editor.putString(CONSTANTS.APP_SERVER_PREF_API,API);
        editor.apply();
    }


    public void showRadioButtonDialog(View view) {
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
}
