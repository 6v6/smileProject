package com.example.bomi.miinsu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;


public class ListItemView extends LinearLayout {

    TextView textView;


    public ListItemView(Context context) {
        super(context);
        init(context);
    }
    public ListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_list_item, this, true);
        textView=(TextView)findViewById(R.id.mission);

    }
    public void setMission(String Mission) { textView.setText(Mission);}
}

