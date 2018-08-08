package com.example.bomi.miinsu;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;


public class SingerItemView extends LinearLayout {

    ImageView imageView;
    TextView textView;


    public SingerItemView(Context context) {
        super(context);
        init(context);
    }

    public SingerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_singer_item, this, true);
        imageView = (ImageView) findViewById(R.id.img);
        textView=(TextView)findViewById(R.id.imageDate);

    }

    public void setImage(Bitmap bitmap) { imageView.setImageBitmap(bitmap);}
    public void setDate(String date) { textView.setText(date);}
}
