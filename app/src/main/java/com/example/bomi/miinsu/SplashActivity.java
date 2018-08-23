package com.example.bomi.miinsu;

import android.content.Intent;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.bomi.miinsu.activity.FaceDetectGrayActivity;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FaceDetectGrayActivity fd = new FaceDetectGrayActivity();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, loginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);


    }
}


