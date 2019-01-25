package com.example.SmartGallery.Activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.SmartGallery.CONSTANTS;
import com.example.SmartGallery.R;

import java.net.CacheRequest;

public class Setting extends AppCompatActivity {

    private EditText URI;
    private Button Save;
    private Button Cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        URI = findViewById(R.id.server_uri_txt);
        Save = findViewById(R.id.save_setting_btn);
        Cancel = findViewById(R.id.cancel_setting_btn);
        URI.setText(CONSTANTS.SERVER_URI);


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
}
