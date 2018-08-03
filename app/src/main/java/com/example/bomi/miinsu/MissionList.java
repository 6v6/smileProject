package com.example.bomi.miinsu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MissionList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_list);
        ListView listview= (ListView) findViewById(android.R.id.list);


       /* String[] mission={"하루에 1번 웃기","하루에 2번 웃기","하루에 3번 웃기","하루에 4번 웃기","하루에 5번 웃기"};
        final ArrayList<String> list = new ArrayList<String>(Arrays.asList(mission));

        ArrayAdapter adapter=new ArrayAdapter<String>(this,android.R.layout.activity_list_item,mission);


        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getApplicationContext(),list.get(position),Toast.LENGTH_LONG).show();
            }
        });*/

    }
}
