package com.kisita.people.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PeopleReceiver extends BroadcastReceiver {

    private static String TAG = "### People receiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Boot completed. Starting location services");
        context.startService(new Intent(context, PositionService.class));
        context.startService(new Intent(context, PeoplePositionService.class));
    }
}
