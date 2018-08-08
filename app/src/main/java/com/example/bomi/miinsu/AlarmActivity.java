package com.example.bomi.miinsu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;

import java.util.Calendar;

public class AlarmActivity extends AppCompatActivity {
    Button btn1;
    Button btn2;
    TimePickerDialog dialog;
    Switch tSwitch;
    Switch pSwitch;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    AlarmManager alarmM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        btn1 = (Button) findViewById(R.id.button4);
        btn2 = (Button) findViewById(R.id.button3);
        tSwitch = (Switch) findViewById(R.id.switch1);
        pSwitch = (Switch) findViewById(R.id.switch2);

        tSwitch.setChecked(pref.getBoolean("tSwitch", false));
        pSwitch.setChecked(pref.getBoolean("pSwitch", false));

        String setPlace = pref.getString("setPlace", null);
        if (setPlace != null) {
            btn2.setText(setPlace);
        } else {
            btn2.setText("위치를 지정하세요");
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sethour = pref.getInt("setHour", 00);
        int setMin = pref.getInt("setMin", 00);
        String setTime = sethour + " : " + setMin;
        if (setTime != null) {
            btn1.setText(setTime);
        } else {
            if (min < 10) {
                btn1.setText(hour + " : 0" + min);
            } else {
                btn1.setText(hour + " : " + min);
            }
        }

        dialog = new TimePickerDialog(this, listener, sethour, setMin, true);

        tSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("tSwitch", isChecked); //true이면 On, false이면 off
                editor.commit();
                if (isChecked) {
                    setAlarm();
                } else {
                    cancelAlarm();
                }
            }
        });
        pSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("pSwitch", isChecked); //true이면 On, false이면 off
                editor.commit();
            }
        });
    }

    private TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            btn1.setText(hourOfDay + " : "+minute);
            editor = pref.edit();
            editor.putInt("setHour", hourOfDay);
            editor.putInt("setMin", minute);
            editor.commit();
            if(pref.getBoolean("tSwitch",false)){
                setAlarm();
            }
        }
    };

    public void onClockClicked(View view) {
        dialog.show();
    }

    public void onMapClicked(View view) {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivityForResult(intent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101) {
            String place = data.getExtras().getString("place");
            double latitude = data.getExtras().getDouble("latitude");
            double longitude = data.getExtras().getDouble("longitude");

            if (place != null) {
                btn2.setText(place);
                editor = pref.edit();
                editor.putString("setPlace",place);
                editor.putString("latitude",Double.toString(latitude));
                editor.putString("longitude",Double.toString(longitude));
                editor.commit();
            } else {
                btn2.setText("위치를 지정하세요");
            }
        }
    }

    private void setAlarm() {
        //cancelAlarm();
        Intent intent = new Intent(this, AlarmViewActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar triggerTime;
        long intervalTime = 24 * 60 * 60 * 1000; //24시간
        triggerTime = setTriggerTime();
        alarmM.setRepeating(AlarmManager.RTC_WAKEUP,triggerTime.getTimeInMillis(),intervalTime,pending);
    }

    private Calendar setTriggerTime() {
        Calendar now = Calendar.getInstance(); //현재 시간
        Calendar curTime = Calendar.getInstance(); //알람 설정 시간
        int setHour = pref.getInt("setHour",00);
        Log.d("setHourrrrrrr",setHour+"");
        if(setHour>12) {
            curTime.set(Calendar.AM_PM,1);
            curTime.set(Calendar.HOUR,setHour-12);
        } else {
            curTime.set(Calendar.AM_PM,0);
            curTime.set(Calendar.HOUR,setHour);
        }
        curTime.set(Calendar.HOUR_OF_DAY,setHour);
        curTime.set(Calendar.MINUTE,pref.getInt("setMin",00));
        curTime.set(Calendar.SECOND,0);
        int day = curTime.get(Calendar.DAY_OF_YEAR);
        long triggerTime = curTime.getTimeInMillis();
        if(now.getTimeInMillis() > triggerTime)
            curTime.set(Calendar.DAY_OF_YEAR,day+1);
        Log.d("currentTimeeeeeeeeee",now.toString());
        Log.d("setTimeeeeeeeeeeeeee",curTime.toString());
        return curTime;
    }

    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmViewActivity.class);
        PendingIntent pending = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        this.alarmM.cancel(pending);
    }
}