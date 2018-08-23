package com.example.bomi.miinsu;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView userEmail;
    private TextView userName;
    private TextView day;

    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mPostReference = Database.getReference("users");

    String email;
    String ruser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

       email = user.getEmail();
        ruser = email.substring(0, email.indexOf("."));

        userEmail = (TextView) headerView.findViewById(R.id.email);
        userName = (TextView) headerView.findViewById(R.id.name);


        day = (TextView) findViewById(R.id.textView);

        userEmail.setText(email);
        userName.setText(mAuth.getCurrentUser().getDisplayName().toString());


        mPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(ruser)) {
                        day.setText("Day" + snapshot.child("challenge").getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
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

    @Override
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

    public void onMissonActivity(View view) {
        Intent intent=new Intent(getApplicationContext(),MissionActivity.class);
        startActivity(intent);
    }

}
