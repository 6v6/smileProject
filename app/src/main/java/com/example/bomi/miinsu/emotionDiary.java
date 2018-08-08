package com.example.bomi.miinsu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

public class emotionDiary extends AppCompatActivity {
    private static final String TAG ="emotion:";
    private GridView gridView;
    emotionAdapter emotionAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_diary);
        GridView gv = (GridView)findViewById(R.id.gridView);
        emotionAdapter=new emotionAdapter(getApplicationContext());
        gv.setAdapter(emotionAdapter);

    }

}

