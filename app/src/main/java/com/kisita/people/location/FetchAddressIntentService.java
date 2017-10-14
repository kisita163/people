package com.kisita.people.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
 * Created by HuguesKi on 01-10-17.
 */

public class FetchAddressIntentService extends IntentService {

    public static final String TAG = "### Address Service";

    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        mReceiver = intent.getParcelableExtra(
                Constants.RECEIVER);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid longitude or latitude";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, null);
        } else {
            Address address = addresses.get(0);
            printAddress(address,TAG);
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                            address);
        }
    }

    private void deliverResultToReceiver(int resultCode,Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY,address);
        mReceiver.send(resultCode, bundle);
    }

    final class Constants {
        static final int SUCCESS_RESULT = 0;
        static final int FAILURE_RESULT = 1;
        static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }

    public static void printAddress(Address address , String tag){
        if(address == null){
            Log.i(tag,"Address is null");
            return;
        }
        Log.i(tag,"Admin   Area  : " +address.getAdminArea());
        Log.i(tag,"Country Code  : " +address.getCountryCode());
        Log.i(tag,"Country Name  : " +address.getCountryName());
        Log.i(tag,"No            : " +address.getFeatureName());
        Log.i(tag,"Locality      : " +address.getLocality());
        Log.i(tag,"Sub Admin Area: " +address.getSubAdminArea());
        Log.i(tag,"Sub Locality  : " +address.getSubLocality());
        Log.i(tag,"Postal Code   : " +address.getPostalCode());
        Log.i(tag,"Premises      : " +address.getPremises());
        Log.i(tag,"Thoroughfare  : " +address.getThoroughfare());
        Log.i(tag,"Phone         : " +address.getPhone());
    }
}
