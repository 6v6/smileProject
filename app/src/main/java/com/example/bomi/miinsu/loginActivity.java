package com.example.bomi.miinsu;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class loginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int PERMISSIONS_REQUEST_CODE = 101;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;

    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mPostReference = Database.getReference();
    private DatabaseReference userdb = Database.getReference("users");
    private GoogleSignInClient mGoogleSignInClient;

    String email, ruser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SignInButton button = (SignInButton) findViewById(R.id.loginBtn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int cp = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
            int wp = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int rp = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if ( cp == PackageManager.PERMISSION_GRANTED
                    && rp==PackageManager.PERMISSION_GRANTED){
                ;//이미 퍼미션을 가지고 있음
                //GoMain();
            }
            else {
                //퍼미션 요청
                ActivityCompat.requestPermissions( this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        }
        else{ }

        //로그인 버튼
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
/*
        //로그인 됐는지, 로그아웃 됐는지 확인하는 리스너
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //로그인
                   GoMain();
                } else {
                    //로그아웃 상태
                }
            }
        };*/
    }
private void GoMain(){
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
}
    //로그인 됐는지, 로그아웃 됐는지 확인하는 리스너할때 필요한 것 onstart onstopp
    @Override
    protected void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mAuth.removeAuthStateListener(mAuthStateListener);
    }
@TargetApi(Build.VERSION_CODES.M)
  private void checkPermissions() {
    int cp = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.CAMERA);
    int wp =
            ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    int rp =
            ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

    boolean cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            android.Manifest.permission.CAMERA);
    boolean writeExternalStorageRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);


    if ( (cp == PackageManager.PERMISSION_DENIED && cameraRationale)
            || (wp== PackageManager.PERMISSION_DENIED
            && writeExternalStorageRationale))
        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

    else if ( (cp == PackageManager.PERMISSION_DENIED && !cameraRationale)
            || (wp== PackageManager.PERMISSION_DENIED
            && !writeExternalStorageRationale))
        showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");

    else if ( cp == PackageManager.PERMISSION_GRANTED
            || wp== PackageManager.PERMISSION_GRANTED ) {
    }
}
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length > 0) {

            int cp = ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA);
            int wp =
                    ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int rp =
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
            if ( cp == PackageManager.PERMISSION_GRANTED
                    && rp == PackageManager.PERMISSION_GRANTED ){
                //이미 퍼미션을 가지고 있음
               //GoMain();
                }
            else{
                checkPermissions();
            }
        }

    }
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//퍼미션 요청
                ActivityCompat.requestPermissions( loginActivity.this,
                        new String[]{android.Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getApplicationContext().getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        //mAuth=FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            postFirebaseDatabase(true);
                            Toast.makeText(loginActivity.this,"환영합니다~!",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            startActivity(intent);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                    }
                });
    }
    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void postFirebaseDatabase(boolean add) {
        //db에서 users읽기
        FirebaseUser user = mAuth.getCurrentUser();
        email = user.getEmail();
        ruser = email.substring(0,email.indexOf("."));
        userdb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean check = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if((snapshot.getKey()).equals(ruser)){
                        check = true;
                    }
                }
                if(check == false)
                    mPostReference.child("users").child(ruser).child("challenge").setValue("1");
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
