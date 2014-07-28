/* GPS Module - Created by Nitin Rao
 * This class checks if wifi or gps is enabled on the phone and chooses accordingly
 */
package com.vimd.p1t;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
 
public class GPS extends Service implements LocationListener {
 
    private final Context context;
    Location location;
    double latitude; 
    double longitude; 
    boolean isGPSEnabled = false;

    protected LocationManager locationManager;
 
    public GPS(Context context) {
        this.context = context;
        getLocation();
    }
 
    public Location getLocation() {
        try {
        	
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                              //location.distanceBetween(latitude, longitude, 35.8278, -78.6421, results);
                            }
                        }
                    }
                
            }
           try {
            	if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                          //location.distanceBetween(latitude, longitude, 35.8278, -78.6421, results);
                        }
                    }
                }
           }
           catch(Exception ex) {
        	   ex.printStackTrace();
           }
            
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
 
   
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
 
        return latitude;
    }
 

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }
 
      
    public void onLocationChanged(Location location) {
    }
 
 
    public void onProviderDisabled(String provider) {
    }
 
   
    public void onProviderEnabled(String provider) {
    }
 
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
 
}
