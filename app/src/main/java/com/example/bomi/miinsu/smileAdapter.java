package com.example.bomi.miinsu;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class smileAdapter extends BaseAdapter {
    private String imgData;
    private String geoData;
    private String imageName;
    private ArrayList<String> thumbsDataList;
    private ArrayList<String> thumbsIDList;
    private ArrayList<singerItem> items = new ArrayList<singerItem>();
    private Context mContext;

    public smileAdapter(Context c){
        mContext = c;
        thumbsDataList = new ArrayList<String>();
        thumbsIDList = new ArrayList<String>();
        getThumbInfo(thumbsIDList, thumbsDataList);

    }

    public final void ImageViewer(int selectedIndex){
        Intent intent = new Intent(mContext, ImagePreview.class);
        String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
        intent.putExtra("filename", imgPath);
        mContext.startActivity(intent);
    }

    public boolean deleteSelected(int sIndex){
        return true;
    }

    public int getCount() {
        return thumbsIDList.size();
    }

    public Object getItem(int position) {
        return thumbsIDList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    public void addItem(singerItem item) {
        items.add(item);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        ExifInterface exif;

        String happy = "";
        String day = "";
        Date beforeDate;
        String newDate = null;

        SingerItemView view = new SingerItemView(mContext);
       /* BitmapFactory.Options bo = new BitmapFactory.Options();
        bo.inSampleSize = 8;

        Bitmap bmp = BitmapFactory.decodeFile(thumbsDataList.get(position), bo);
        Bitmap resized = Bitmap.createScaledBitmap(bmp, 95, 95, true);*/
        BitmapFactory.Options bo = new BitmapFactory.Options();
        bo.inSampleSize = 6;
        ContentResolver cr = mContext.getApplicationContext().getContentResolver();
        int id = Integer.parseInt(thumbsIDList.get(position));
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, bo);

        String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(position));

        imageName = getImageName(imgData, geoData, thumbsIDList.get(position));
        happy = imageName.substring(0, imageName.indexOf("-"));
        day = imageName.substring(imageName.indexOf("-") + 1, imageName.indexOf(".") - 1);
        Log.d("beforedate:", "|" + day + "|");


        // String subDate=newDate.substring(2,10);

        view.setDate(day + "|웃음 " + happy + "%");
        view.setImage(bitmap);
        return view;
    }

    private String getTagString(String tag, ExifInterface exif) {
        return exif.getAttribute(tag);
    }

    private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas){
        String[] proj = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};

         Cursor imageCursor = mContext.getApplicationContext().getContentResolver().query( MediaStore.Files.getContentUri("external"),
                null,
                MediaStore.Images.Media.DATA + " like ? ",
                new String[] {"%smileDiary%"},
                null);

        Log.d("Images:",MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
       Log.d("image:",imageCursor.toString());

        if (imageCursor != null && imageCursor.moveToFirst()){
            String thumbsID;
            String thumbsImageID;
            String thumbsData;
            String imgSize;

            int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
            int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
            int num = 0;
            do {
                thumbsID = imageCursor.getString(thumbsIDCol);
                thumbsData = imageCursor.getString(thumbsDataCol);
                thumbsImageID = imageCursor.getString(thumbsImageIDCol); //이름
                imgSize = imageCursor.getString(thumbsSizeCol);
                num++;
                if (thumbsImageID != null){
                    thumbsIDs.add(thumbsID);
                    thumbsDatas.add(thumbsData);
                }
            }while (imageCursor.moveToNext());
        }
        imageCursor.close();
        return;
    }

    private String getImageInfo(String ImageData, String Location, String thumbID){
        String imageDataPath = null;
        String[] proj = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor imageCursor = mContext.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                proj, "_ID='"+ thumbID +"'", null, null);

        if (imageCursor != null && imageCursor.moveToFirst()){
            if (imageCursor.getCount() > 0){
                int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imageDataPath = imageCursor.getString(imgData);
            }
        }
        imageCursor.close();
        return imageDataPath;
    }

    private String getImageName(String ImageData, String Location, String thumbID){
        String imageName = null;
        String[] proj = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor imageCursor = mContext.getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                proj, "_ID='"+ thumbID +"'", null, null);

        if (imageCursor != null && imageCursor.moveToFirst()){
            if (imageCursor.getCount() > 0){
                int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                imageName = imageCursor.getString(imgData);
            }
        }
        imageCursor.close();
        return imageName;
    }


}




