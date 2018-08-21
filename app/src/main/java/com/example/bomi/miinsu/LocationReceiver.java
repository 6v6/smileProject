package com.example.bomi.miinsu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING,false);
        if(isEntering) {
            Toast.makeText(context,"접근중", Toast.LENGTH_LONG).show();
            Log.d("Receiveer","Enteringggg");
            Intent intent1 = new Intent(context, AlarmViewActivity.class);
            context.startActivity(intent1);
        } else {
            Toast.makeText(context,"벗어나는중", Toast.LENGTH_LONG).show();
            Log.d("Receiver","Exitinggggg");
        }
    }
}
