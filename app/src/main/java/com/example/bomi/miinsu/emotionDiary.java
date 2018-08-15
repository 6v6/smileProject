package com.example.bomi.miinsu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){

                emotionAdapter.ImageViewer(position);
            }
        });
    }
}

