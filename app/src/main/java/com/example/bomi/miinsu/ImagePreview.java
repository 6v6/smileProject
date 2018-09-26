package com.example.bomi.miinsu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class ImagePreview extends Activity{
    private Context mContext = null;
    private final int imgWidth = 320;
    private final int imgHeight = 372;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_preview);
        mContext = this;


        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String imgPath = extras.getString("filename");
        String happy;

        Log.d("getimgPath:",imgPath);

        makePicture(imgPath);
        ImageView iv = (ImageView)findViewById(R.id.imageView);
        TextView txt = (TextView)findViewById(R.id.aboutPhoto);
        happy = imgPath.substring(31, imgPath.indexOf("-"));


        /*BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inSampleSize = 4;
        Bitmap bm = BitmapFactory.decodeFile(imgPath, bfo);
        Bitmap resized = Bitmap.createScaledBitmap(bm, imgWidth, imgHeight, true);*/
        txt.setHint(happy+"% 웃음");
        iv.setImageBitmap(makePicture(imgPath));
    }

    //사진의 회전값 가져오기
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //사진 돌리기
    private Bitmap rotate(Bitmap bmp, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.setScale(-1,1);
        matrix.setScale(1,-1);
        matrix.postRotate(degree);
        //좌우반전

        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                bmp.getHeight(), matrix, true);
    }

    private Bitmap makePicture(String imagePath) {

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);
        Log.d("getexifDegree:", String.valueOf(exifOrientation));
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환

        Bitmap resized=rotate(bitmap,270);

        return resized;


    }
}
