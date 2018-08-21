package com.example.bomi.miinsu;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class GpsService extends Service {

    LocationReceiver locationR;
    IntentFilter filter;
    PendingIntent pending;
    LocationManager locationM;
    LocationListener locationL;
    String before; String latest;
    double latitude;
    double longitude;

//    public GpsService(double lat, double lon) {
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service","서비스 실행");

        locationM = (LocationManager)getSystemService(LOCATION_SERVICE);

        ///////////////////////<
        locationR = new LocationReceiver();
        filter = new IntentFilter("com.example.bomi.miinsu");
        registerReceiver(locationR, filter);
        ///////////////////////>

        locationL = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(latest == null) {
                    before = location.getLatitude()+""+location.getLongitude();
                } else {
                    before = latest;
                }
                latest = location.getLatitude()+""+location.getLongitude();
                Log.d("좌표", "위도: " + location.getLatitude() + " 경도: " + location.getLongitude());
                //Toast.makeText(getApplicationContext(),"위도: " + location.getLatitude() + " 경도: " + location.getLongitude(),Toast.LENGTH_LONG).show();

                if(!before.equals(latest)) {
                    Log.d("!=","좌표 변경됨");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","onStartCommand");
        latitude = intent.getDoubleExtra("latitude",0.0);
        longitude = intent.getDoubleExtra("longitude",0.0);
        Toast.makeText(this,"위치알람등록됨:"+latitude+" / "+longitude,Toast.LENGTH_LONG).show();

        ////////////////////
        Intent intent1 = new Intent("com.example.bomi.miinsu");
        pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // for ActivityCompat#requestPermissions for more details.
            // to handle the case where the user grants the permission. See the documentation
            //return;
        }
        Log.d("addProximityAlert",latitude+" / "+longitude);
        locationM.addProximityAlert(latitude, longitude, 50, -1, pending);
        /////////////////////////////////

        ////////////////////////<
        locationM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                500, 0, locationL);
        /////////////////////////>

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
            Log.d("Service"," 서비스 종료");
            unregisterReceiver(locationR);
            locationM.removeUpdates(locationL);
            super.onDestroy();
    }
}
