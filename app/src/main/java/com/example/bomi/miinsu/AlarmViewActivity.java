package com.example.bomi.miinsu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.bomi.miinsu.R;
import com.example.bomi.miinsu.activity.FaceDetectGrayActivity;

public class AlarmViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_view);
    }

    public void onAlarmStart(View view) {
        Intent intent = new Intent(getApplicationContext(), FaceDetectGrayActivity.class);
        startActivity(intent);
    }
}
