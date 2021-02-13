package com.bitbybit.qrscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class IPActivity extends AppCompatActivity {

    EditText url;
    Button okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_p);
        url = findViewById(R.id.url);
        okay = findViewById(R.id.okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = url.getText().toString().trim();
                Shared_prefs.getInstance(IPActivity.this).set_ip(text);
                if(checkPermission()){
                    startActivity(new Intent(IPActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(IPActivity.this, "Please grant camera permission", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkPermission(){
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},001);

        } else {
            return true;
        }
        return false;
    }
}