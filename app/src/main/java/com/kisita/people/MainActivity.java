package com.kisita.people;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kisita.people.location.PeoplePositionService;
import com.kisita.people.location.PositionService;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final int REQUEST_COARSE_LOCATION = 100;
    private static String TAG = "### Main activity";
    PeoplePositionService mService;
    private TextView mTextMessage;
    private FirebaseAuth mAuth;
    private boolean mBound;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_people);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_chat);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_settings);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        authentication();
    }


    private void authentication() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("kisita2002@yahoo.fr","kisita")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "You have signed up successfully",
                                    Toast.LENGTH_SHORT).show(); // Then start localisation service
                            requestPermissionsForLocalisation();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void requestPermissionsForLocalisation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }else{
            // Start position service if it is not running
            if(!isMyServiceRunning(PositionService.class)){
                Log.i(TAG, "Position service is not running. Start it now");
                Intent i = new Intent(MainActivity.this, PositionService.class);
                startService(i);
                //bindService(i, this, Context.BIND_AUTO_CREATE);
            }

            // Start people position service if it is not running
            if (!isMyServiceRunning(PeoplePositionService.class)) {
                Log.i(TAG, "People position service is not running. Start it now");
                Intent i = new Intent(MainActivity.this, PeoplePositionService.class);
                startService(i);
                bindService(i, this, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Start position service if it is not running
                    if(!isMyServiceRunning(PositionService.class)){
                        Log.i(TAG, "Position service is not running. Start it now");
                        Intent i = new Intent(MainActivity.this, PositionService.class);
                        startService(i);
                        //bindService(i, this, Context.BIND_AUTO_CREATE);
                    }

                    // Start people position service if it is not running
                    if (!isMyServiceRunning(PeoplePositionService.class)) {
                        Log.i(TAG, "People position service is not running. Start it now");
                        Intent i = new Intent(MainActivity.this, PeoplePositionService.class);
                        startService(i);
                        bindService(i, this, Context.BIND_AUTO_CREATE);
                    }

                } else {
                    //TODO
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        Log.i(TAG,"isMyServiceRunning ?");
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //Log.i(TAG,"Service : "+service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(TAG,"Service found !!! ");
                return true;
            }
        }
        Log.i(TAG,"Research finished !!!");
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.i(TAG, "People service connected");
        PeoplePositionService.LocalBinder binder = (PeoplePositionService.LocalBinder) iBinder;
        mService = binder.getService();

        Log.i(TAG, "Hashmap size is : " + mService.getHashMap().size());

        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i(TAG, "People service disconnected");
        mBound = false;
    }
}
