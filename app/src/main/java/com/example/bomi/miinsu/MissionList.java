package com.example.bomi.miinsu;


import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.bomi.miinsu.activity.FaceDetectGrayActivity;
import com.example.bomi.miinsu.activity.FaceDetectMissionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MissionList extends AppCompatActivity {
    ListView listView;
    ListAdapter adapter;
    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference chdb = Database.getReference("challenge");
    Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_list);

        listView = (ListView) findViewById(R.id.list);

        adapter = new ListAdapter();

        getChallenge();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ListItem item = (ListItem) adapter.getItem(position);
                Toast.makeText(getApplicationContext(), "미션 선택 : " + item.getMission(), Toast.LENGTH_LONG).show();
                Intent intent=new Intent(getApplicationContext(),FaceDetectMissionActivity.class);
                //intent.putExtra("Total", item.getMission().substring(0,1));
                intent.putExtra("Mission", item.getMission());
                startActivity(intent);
                finish();
            }
        });


    }


    //챌린지 가져오기
    public void getChallenge(){
        String day = calendar.get(Calendar.DATE)+"";
        int iday = Integer.parseInt(day);
        iday = iday%3+1;
        chdb.child(iday+"").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot datas : dataSnapshot.getChildren()) {
                    String get = datas.getValue().toString();
                    Log.e("getChallenge", get);
                    adapter.addItem(new ListItem(get));
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    class ListAdapter extends BaseAdapter {
        ArrayList<ListItem> items = new ArrayList<ListItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(ListItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ListItemView view = new ListItemView(getApplicationContext());

            ListItem item = items.get(position);
            view.setMission(item.getMission());

            return view;
        }
    }

}
