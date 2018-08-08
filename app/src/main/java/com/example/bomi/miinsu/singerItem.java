package com.example.bomi.miinsu;
import android.graphics.Bitmap;

import java.util.Date;

public class singerItem {
    private Bitmap image;
    private String date;

    public singerItem(Bitmap image,String date) {
        super();
        this.image = image;
        this.date=date;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

}
