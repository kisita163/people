package com.kisita.people.location;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PeoplePositionService extends Service implements ChildEventListener {

    static final String TAG = "PeoplePositionService";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private DatabaseReference mDatabase;
    private Map<String, People> mPeople = new HashMap<>();

    public PeoplePositionService() {
    }

    @Override
    public void onCreate() {
        /*
         * Get people location when starting the service
         */
        Log.i(TAG, "Starting service ...");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        Query itemsQuery = getPositionsQuery(mDatabase);
        itemsQuery.addChildEventListener(this);

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Query getPositionsQuery(DatabaseReference databaseReference) {
        return databaseReference.child("positions");
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "Position added " + dataSnapshot.getKey());
        // Add to people hash map
        addNewPeople(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "Position changed " + dataSnapshot.getKey());
        // Update to people hash map
        updateExistingPeople(dataSnapshot);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.i(TAG, "Position removed ");
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "Position moved " + s);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.i(TAG, "Position cancelled");
    }

    /**
     * Add new people to the database
     *
     * @param dataSnapshot
     */
    void addNewPeople(DataSnapshot dataSnapshot) {
        String pseudo = "Unknown";
        String sex = "M"; // M = male, F = female , N = Not specified

        // get pseudo
        if (dataSnapshot.hasChild("pseudo"))
            pseudo = dataSnapshot.child("pseudo").getValue().toString();

        // get the sex
        if (dataSnapshot.hasChild("sex"))
            sex = dataSnapshot.child("sex").getValue().toString();

        People people = new People(pseudo, sex);
        setPeopleData(dataSnapshot, people);


        mPeople.put(dataSnapshot.getKey(), people); // The key is the user id
    }

    private void setPeopleData(DataSnapshot dataSnapshot, People people) {

        String thoroughfare = null;
        String postalCode = null;
        String adminArea = null;
        String country = null;
        String latitude = null;
        String longitude = null;

        // Check people before continuing
        if (people == null)
            return;

        // get Thoroughfare
        if (dataSnapshot.hasChild("thoroughfare"))
            thoroughfare = dataSnapshot.child("thoroughfare").getValue().toString();

        // get PostalCode
        if (dataSnapshot.hasChild("postalCode"))
            postalCode = dataSnapshot.child("postalCode").getValue().toString();

        // get AdminArea
        if (dataSnapshot.hasChild("adminArea"))
            adminArea = dataSnapshot.child("adminArea").getValue().toString();

        // get Country
        if (dataSnapshot.hasChild("country"))
            country = dataSnapshot.child("country").getValue().toString();

        // get latitude
        if (dataSnapshot.hasChild("latitude"))
            latitude = dataSnapshot.child("latitude").getValue().toString();

        // get longitude
        if (dataSnapshot.hasChild("longitude"))
            longitude = dataSnapshot.child("longitude").getValue().toString();

        // Setting the new people


        people.setThoroughfare(thoroughfare);
        people.setPostalCode(postalCode);
        people.setAdminArea(adminArea);
        people.setCountryCode(country);

        try {
            people.setLatitude(Double.valueOf(latitude));
            people.setLongitude(Double.valueOf(longitude));
        } catch (NumberFormatException e) {
            // Latitude or longitude is corrupted
            people.setLatitude(0.0);
            people.setLongitude(0.0);
        }
    }

    /**
     * Update an existing location into the database
     *
     * @param dataSnapshot
     */
    void updateExistingPeople(DataSnapshot dataSnapshot) {

        // Get the existing people from database
        People people = mPeople.get(dataSnapshot.getKey());
        setPeopleData(dataSnapshot, people);

        printHashMapValues();
    }

    void printHashMapValues() {
        Set keys = mPeople.keySet();

        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            People value = mPeople.get(key);
            Log.i(TAG, "Pseudo : " + value.getPseudo());
            Log.i(TAG, value.getLatitude() + " " + value.getLongitude());
            Log.i(TAG, "");
        }
    }

    /**
     * method for clients
     */
    public Map<String, People> getHashMap() {
        return mPeople;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public PeoplePositionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PeoplePositionService.this;
        }
    }
}
