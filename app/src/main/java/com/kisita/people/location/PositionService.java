package com.kisita.people.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class PositionService extends Service {

    private static String TAG = "### FixHandler Service";

    private AddressResultReceiver mResultReceiver;

    private LocationManager locationMgr = null;

    public PositionService() {
    }

    private LocationListener onLocationChange = new LocationListener()
    {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onLocationChanged(Location location)
        {

            startIntentService(location);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG,"FixHandler service created");
        mResultReceiver = new AddressResultReceiver(null);
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,
                0, onLocationChange);
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0,
            onLocationChange);


        super.onCreate();
    }

    public DatabaseReference getDb() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        locationMgr.removeUpdates(onLocationChange);
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void sendLocationToFirebase(Address address){

        if(address == null){
            Log.e(TAG,"Address is null");
            return;
        }


        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/positions/" + getUid() + "/Thoroughfare",address.getThoroughfare());
        childUpdates.put("/positions/" + getUid() + "/PostalCode"  ,address.getPostalCode());
        childUpdates.put("/positions/" + getUid() + "/AdminArea"   ,address.getAdminArea());

        childUpdates.put("/positions/" + getUid() + "/country"     ,address.getCountryCode());
        childUpdates.put("/positions/" + getUid() + "/latitude"    , address.getLatitude());
        childUpdates.put("/positions/" + getUid() + "/longitude"   ,address.getLongitude());

         getDb().updateChildren(childUpdates);
    }


    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            Address address = resultData.getParcelable(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            sendLocationToFirebase(address);
            //printAddress(mAddressOutput,TAG);
        }
    }
}
