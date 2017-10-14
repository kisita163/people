package com.kisita.people.location;

/**
 * Created by HuguesKi on 01-10-17.
 */

public class FixHandler {
    private String name;
    private double oldLatitude = 0;
    private double oldLongitude = 0;
    private double newLatitude = 0;
    private double newLongitude = 0;

    public FixHandler(String name, double oldLatitude, double oldLongitude) {
        this.name = name;
        this.oldLatitude = oldLatitude;
        this.oldLongitude = oldLongitude;
    }

    public double getDistanceTraveled(){

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(this.newLatitude - this.oldLatitude);
        double lonDistance = Math.toRadians(this.newLongitude - this.oldLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.newLatitude)) * Math.cos(Math.toRadians(this.oldLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters


        distance = Math.pow(distance, 2) ;

        return Math.sqrt(distance);
    }

    public void setNewLatLon(double lat,double lng){
        if(this.newLongitude != 0 && this.newLatitude != 0) {
            this.oldLongitude = this.newLongitude;
            this.oldLatitude = this.newLatitude;
        }

        this.newLongitude = lng;
        this.newLatitude = lat;
    }

    public String toString(){
        return ("Old latitude  : "+this.oldLatitude + "\n"+
                "Old longitude : "+this.oldLongitude + "\n"+
                "New latitude  : "+this.newLatitude + "\n"+
                "New longitude : "+this.newLongitude + "\n"+
                "Distance      : "+getDistanceTraveled() + "\n"+
                "Name          : "+this.name);
    }
}
