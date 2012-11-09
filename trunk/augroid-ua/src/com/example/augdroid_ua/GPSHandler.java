package com.example.augdroid_ua;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSHandler extends Activity {

	private Location myLocation;	
	private LocationManager imManager;		// imManager and listener handle listening for
	private ImListener imListener;			// and finding the phones location
	
	public Location GetLocation() {
		imManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        imListener = new ImListener();
        startListening();		// start listening for current location
        myLocation = imManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);		// gets the last known location of the phone
//        if (myLocation != null){
//        	int lat = (int) (myLocation.getLatitude() * 1E6);
//			int lng = (int) (myLocation.getLongitude() * 1E6);
//			// don't know if lat and long will be needed
//        }
    	stopListening();		// stop listening for current location
    	return myLocation;
	}
	
	private void startListening(){
    	imManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, imListener);
    }
    
    private void stopListening(){
    	if (imManager != null){imManager.removeUpdates(imListener);}
    }
	
	//This class handles listening for the phone's location
	public class ImListener implements LocationListener {

		// These were overridden in our project, but got an error when overridden here. 
		// I'm thinking they do need to override LocationListener's functions though... -Carter
		//@Override
		public void onLocationChanged(Location location) {
			myLocation = location;
			
		}

		//@Override
		public void onProviderDisabled(String provider) {
			
		}

		//@Override
		public void onProviderEnabled(String provider) {
			
		}

		//@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}

	}
	
}


