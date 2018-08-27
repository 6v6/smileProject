package com.example.bomi.miinsu;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class AlarmActivity extends AppCompatActivity {
    Button btn1;
    Button btn2;
    ImageButton question;
    TimePickerDialog dialog;
    Switch tSwitch;
    Switch pSwitch;
    Switch rSwitch;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    AlarmManager alarmM;
    double latitude;
    double longitude;
    PendingIntent pending;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    Intent service;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    Handler handler;
    long interv;  //다시 runnable할 간격
    int flag;
    Runnable r;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        alarmM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        service = new Intent(getApplicationContext(),GpsService.class);
        callPermission();  // 권한 요청을 해야 함
        notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        handler = new Handler();
        interv = 0;

        btn1 = (Button) findViewById(R.id.button4);
        btn2 = (Button) findViewById(R.id.button3);
        tSwitch = (Switch) findViewById(R.id.switch1);
        pSwitch = (Switch) findViewById(R.id.switch2);
        rSwitch = (Switch) findViewById(R.id.switch3);

        tSwitch.setChecked(pref.getBoolean("tSwitch", false));
        pSwitch.setChecked(pref.getBoolean("pSwitch", false));
        rSwitch.setChecked(pref.getBoolean("rSwitch",false));

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

        //시간 알람 스위치 이벤트
        tSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("tSwitch", isChecked); //true이면 On, false이면 off
                editor.commit();
                if (isChecked) { // On이면 setAlarm
                    setAlarm();
                } else {  //Off이면 cancelAlarm
                    Toast.makeText(getApplicationContext(),"꺼짐",Toast.LENGTH_SHORT).show();
                    cancelAlarm();
                }
            }
        });
        //위치 알람 스위치 이벤트
        pSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("pSwitch", isChecked); //true이면 On, false이면 off
                editor.commit();
                if(isChecked) {
                    if(pref.getString("setPlace",null)!=null) {
                        double latitude = Double.parseDouble(pref.getString("latitude",null));
                        double longitude = Double.parseDouble(pref.getString("longitude",null));
                        service.putExtra("latitude",latitude);
                        service.putExtra("longitude",longitude);
                        startService(service);
                    } else {
                        Toast.makeText(getApplicationContext(),"위치를 지정해주세요",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopService(service);
                }
            }
        });
        //랜덤 알람 스위치 이벤트
        rSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("rSwitch", isChecked); //true이면 On, false이면 off
                editor.commit();
                if (isChecked) { // On이면 setAlarm
                    flag = 0;
                    setRunnable();
                } else {  //Off이면 cancelAlarm
                    Toast.makeText(getApplicationContext(),"꺼짐",Toast.LENGTH_SHORT).show();
                    handler.removeCallbacksAndMessages(null);
                    interv = 0;
                }
            }
        });
    }

    //지정한 시간 SharedPreference로 저장
    private TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            btn1.setText(hourOfDay + " : " + minute);
            editor = pref.edit();
            editor.putInt("setHour", hourOfDay);
            editor.putInt("setMin", minute);
            editor.commit();
            if (pref.getBoolean("tSwitch", false)==true) {
                setAlarm();
            }
        }
    };

    //TimePickerDialog 띄우기
    public void onClockClicked(View view) {
        dialog.show();
    }

    //위치 지정하기
    public void onMapClicked(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, 101);
    }

    // MapsActivity에서 지정한 위치 정보 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            String place = data.getExtras().getString("place");  //위치 이름

            if (place != null) {

                latitude = data.getExtras().getDouble("latitude");
                longitude = data.getExtras().getDouble("longitude");

                btn2.setText(place);
                editor = pref.edit();
                editor.putString("setPlace", place);
                editor.putString("latitude", Double.toString(latitude));
                editor.putString("longitude", Double.toString(longitude));
                editor.commit();
                if(latitude != 0.0) {
                    if (pref.getBoolean("pSwitch", false)) {
                        service.putExtra("latitude", latitude);
                        service.putExtra("longitude", longitude);
                        startService(service);
                    }
                }
            } else {
                btn2.setText("위치를 지정하세요");
            }
        }
    }

    //알람 설정
    private void setAlarm() {
        Intent intent = new Intent(this, AlarmViewActivity.class);
        pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar triggerTime;
            long intervalTime = 24 * 60 * 60 * 1000; //24시간
            triggerTime = setTriggerTime();
            alarmM.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), intervalTime, pending);
    }

    //알람 설정 시간 계산
    private Calendar setTriggerTime() {
        Calendar now = Calendar.getInstance(); //현재 시간
        Calendar curTime = Calendar.getInstance(); //알람 설정 시간
        int setHour = pref.getInt("setHour", 00);
        Log.d("setHourrrrrrr", setHour + "");
        if (setHour > 12) {
            curTime.set(Calendar.AM_PM, 1);
            curTime.set(Calendar.HOUR, setHour - 12);
        } else {
            curTime.set(Calendar.AM_PM, 0);
            curTime.set(Calendar.HOUR, setHour);
        }
        curTime.set(Calendar.HOUR_OF_DAY, setHour);
        curTime.set(Calendar.MINUTE, pref.getInt("setMin", 00));
        curTime.set(Calendar.SECOND, 0);
        int day = curTime.get(Calendar.DAY_OF_YEAR);
        long triggerTime = curTime.getTimeInMillis();
        if (now.getTimeInMillis() > triggerTime)
            curTime.set(Calendar.DAY_OF_YEAR, day + 1);
        Log.d("currentTimeeeeeeeeee", now.toString());
        Log.d("setTimeeeeeeeeeeeeee", curTime.toString());
        return curTime;
    }

    //알람 해제
    private void cancelAlarm() {
        if(pending != null) {
            alarmM.cancel(pending);
            pending.cancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    //전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    public void onQuestionClicked(View view) {
        AlarmDialog dialog = new AlarmDialog(this);
        dialog.show();
    }

    public int getRandomTime(int curHour) {  //알람 울릴 시간(Hour) 반환
        ///////////random이 24시간보다 작아야 함.
        ///////////알람 울릴 시간이 10~22 사이여야함
        Random random = new Random();
        int randtime = random.nextInt(23)+1;  //1~23
        int resulttime = randtime + curHour;
        if(resulttime < 24) {
            if(resulttime<10 || resulttime>=22) {  //10시 이전, 22시 이후이면
                return getRandomTime(curHour);  //다시 random
            } else {
                interv = randtime*60*60*1000;  //interval 설정
                return resulttime;
            }
        } else {
            if(resulttime<34 || resulttime>=46) {
                return getRandomTime(curHour);
            } else {
                interv = randtime*60*60*1000;
                return  resulttime-24;
            }
        }
    }

    public void setPushAlarm() {
        Calendar curtime = Calendar.getInstance(); //알람 지정했을 때의 시각
        Calendar setTime = curtime; //랜덤 시각(알람이 울릴 시각)
        Log.d("Curtime", "curHour" + curtime.get(Calendar.HOUR_OF_DAY));
        int setHour = getRandomTime(curtime.get(Calendar.HOUR_OF_DAY)); //알람 울릴 랜덤 hour
        if (setHour < curtime.get(Calendar.HOUR_OF_DAY)) {  //setHour가 현재시간보다 이른 시간이면 내일 알람
            setTime.set(Calendar.DAY_OF_YEAR, setTime.get(Calendar.DAY_OF_YEAR) + 1);
        }
        setTime.set(Calendar.HOUR_OF_DAY, setHour);  //setHour의 hour 랜덤hour로 지정
        //Log.d("SetRandomAlarm", "setHour" + setTime.get(Calendar.HOUR_OF_DAY));
        SimpleDateFormat form = new SimpleDateFormat("MM-dd HH:mm:ss");
        Log.d("랜덤알람 울릴시간",form.format(setTime.getTime()));
        Log.d("interv",interv/1000/60/60+"시간");
//        curtime.set(Calendar.SECOND,setHour+curtime.get(Calendar.SECOND));
        if(flag != 0)
            pushAlarm(setTime);
        flag++;
    }

    public void setRunnable() {
        handler = new Handler();
        r = new Runnable(){
            @Override
            public void run() {
                setPushAlarm();
                setRunnable();
            }
        };
        handler.postDelayed(r,
                interv
        );
    }

    public void pushAlarm(Calendar setTime) {
        String channelId = "channel";
        String channelName = "Channel Name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        builder = new NotificationCompat.Builder(this, channelId);
        Intent intent = new Intent(this, AlarmViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent p = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setWhen(setTime.getTimeInMillis()).setContentTitle("SmileProject")
                .setContentText("웃을 시간이에요!").setContentIntent(p)
                .setAutoCancel(true).setSmallIcon(R.drawable.sun_smile);
        notificationManager.notify(0, builder.build());
    }
}