package com.example.bomi.miinsu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.bomi.miinsu.activity.FaceDetectDiaryActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.example.bomi.miinsu.activity.FaceDetectGrayActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MissionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int RC_HANDLE_CAMERA_PERM_GRAY = 2;
    public static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_emotion) {
            Intent intent = new Intent(getApplicationContext(), emotionDiary.class);
            startActivity(intent);


        } else if (id == R.id.nav_smile) {
            Intent intent = new Intent(getApplicationContext(), smileDiary.class);
            startActivity(intent);
        }
        else if (id==R.id.nav_logout){
            onLogOut();
        }
        else if(id==R.id.nav_alarm) {
            Intent intent = new Intent(getApplicationContext(),AlarmActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onLogOut() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        finish();
        Intent intent=new Intent(getApplicationContext(),loginActivity.class);
        startActivity(intent);
    }

    public void onMissonStart(View view) {
        Intent intent = new Intent(getApplication(),FaceDetectDiaryActivity.class);
        startActivity(intent);
    }
}
