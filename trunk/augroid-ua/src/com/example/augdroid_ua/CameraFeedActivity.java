package com.example.augdroid_ua;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


public class CameraFeedActivity extends Activity implements SensorEventListener {
	
	public static final String EXTRA_OVERLAY_TYPE = "overlay_type_extra";
	private static final String TAG = "CameraFeedActivity";
	
	private Camera mCamera;
	private CameraFeedView mCameraFeedView;
	private CameraOverlayView mCameraOverlayView;
	private FrameLayout mFrame;
	private int mOverlayType;
	private Location mLocation;
	
	private SensorManager mSensorManger;
	private float[] mAccelerometerData = new float[3];
	private float[] mMagneticData = new float[3];
	private float[] mRawRotationMatrix = new float[9];
	private float[] mRemappedRotationMatrix = new float[9];
	private float[] mOrientation = new float[3];
	
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	
	private boolean mDrawNewTag;
	private Tag mDraggedTag;
	private GestureDetector mDragDetector;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_feed_layout);
		
		mFrame = (FrameLayout)findViewById(R.id.camera_feed_preview);
		mSensorManger = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		mDrawNewTag = true;
		mDraggedTag = null;
		mDragDetector = new GestureDetector(new DragListener());
		mLocationListener = new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			public void onProviderEnabled(String provider) { }
			public void onProviderDisabled(String provider) { }
			public void onLocationChanged(Location location) {
				if (mCameraOverlayView != null)
					mCameraOverlayView.updateLocation(location);
					mLocation = location;
			}
		};
		// camera and sensors are set up in onResume()
		Bundle extras = getIntent().getExtras();
		mOverlayType = extras.getInt(EXTRA_OVERLAY_TYPE);
	}
	
	

	private void setupCamera() {
		try {
			mCamera = Camera.open();
			Camera.Parameters cameraParameters = mCamera.getParameters();
			if (cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				mCamera.setParameters(cameraParameters);
			}
			mCameraFeedView = new CameraFeedView(this, mCamera);
			mFrame.addView(mCameraFeedView);
			
			mCameraOverlayView = new CameraOverlayView(this);
			mCameraOverlayView.setupCamera(new float[] {mCamera.getParameters().getHorizontalViewAngle(), mCamera.getParameters().getVerticalViewAngle()});
			
//			mCameraOverlayView.setOnTouchListener(new View.OnTouchListener() {
//				public boolean onTouch(View v, MotionEvent event) {
//					int action = event.getAction();
//					if (action == MotionEvent.ACTION_DOWN) {
//						mDraggedTag = getTag(event.getX(), event.getY());
//						if (mDraggedTag != null) {
//							mDrawNewTag = false;
//						}
//					}
//					else if (action == MotionEvent.ACTION_UP && mDrawNewTag) {
//						try {
//		            		Location testLoc = new Location("augdroid-ua.testLocProvider");
//		            		testLoc.setLatitude(33.195721);
//		            		testLoc.setLongitude(-87.535137);
//		            		
//		            		mCameraOverlayView.updateLocation(testLoc);
//		            		mLocation = testLoc; // Test stuff for debugging*/
//		            		
//		            		float azimuth = (float)Math.toDegrees(mOrientation[0]);
//		            		float pitch = (float)Math.toDegrees(mOrientation[1]);
//		    			
//		            		double distance = GetDistance(pitch);
//		            		Location newLoc = CalculateLocation(mLocation.getLatitude(), mLocation.getLongitude(), azimuth, distance);
//		    			
//		            		Tag newTag = new Tag(1, "newTag", newLoc, 2.0f);
//		            		mCameraOverlayView.addTag(newTag);
//		            		mCameraOverlayView.setOverlayType(mOverlayType);
//		            	}
//		            	catch (Exception ex) {
//		            		Log.e(TAG, ex.getMessage());
//		            		// If this triggers, GPS location is probably null
//		            	}
//						mDrawNewTag = true;
//						mDraggedTag = null;
//					}
//					return true;
//				}
//			});
			
			mFrame.addView(mCameraOverlayView);
			
			setupTests();
		} catch (Exception e) {
			// camera not available (in use)
			Toast.makeText(this, "Camera not available", Toast.LENGTH_LONG).show();
			this.finish();
		}
	}
	
	private void setupTests() {
		Location tag1Loc = new Location("augdroid-ua.testLocProvider");
		tag1Loc.setLatitude(33.165615);
		tag1Loc.setLongitude(-87.509218);

		Location tag2Loc = new Location("augdroid-ua.testLocProvider");
		tag2Loc.setLatitude(33.166198);
		tag2Loc.setLongitude(-87.508601);
		
		Location tagShelbyCenterLoc = new Location("augdroid-ua.testLocProvider");
		tagShelbyCenterLoc.setLatitude(33.215156);
		tagShelbyCenterLoc.setLongitude(-87.542064);
		
		Location tagPathToFergLoc = new Location("augdroid-ua.testLocProvider");
		tagPathToFergLoc.setLatitude(33.215546);
		tagPathToFergLoc.setLongitude(-87.542792);
		
		Location forestLake = new Location("augdroid-ua.testLocProvider");
		forestLake.setLatitude(33.195698);
		forestLake.setLongitude(-87.534305);
		
		Tag tag1 = new Tag(1, "Hello!", tag1Loc, 2.0f);
		Tag tag2 = new Tag(2, "World!", tag2Loc, 2.0f);
		Tag tag3 = new Tag(3, "Shelby", tagShelbyCenterLoc, 2.0f);
		Tag tag4 = new Tag(4, "Path", tagPathToFergLoc, 2.0f);
		Tag tag5 = new Tag(5, "Lake", forestLake, 2.0f);
		
		mCameraOverlayView.addTag(tag1);
		mCameraOverlayView.addTag(tag2);
		mCameraOverlayView.addTag(tag3);
		mCameraOverlayView.addTag(tag4);
		mCameraOverlayView.addTag(tag5);
		mCameraOverlayView.setOverlayType(mOverlayType);
	}
	
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
		return this.mDragDetector.onTouchEvent(event);
		}
		catch (Exception e) {return false;}
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		setupCamera();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		mSensorManger.registerListener(this, mSensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
		mSensorManger.registerListener(this, mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
		mLocationManager.removeUpdates(mLocationListener);
		mSensorManger.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(TAG, "Accuracy Changed");
	}

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, mMagneticData, 0, 3);
			break;
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, mAccelerometerData, 0, 3);
			break;
		}
		
		float[] tempOrientation = new float[3];
		if (SensorManager.getRotationMatrix(mRawRotationMatrix, null, mAccelerometerData, mMagneticData)) {
			SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemappedRotationMatrix);
			SensorManager.getOrientation(mRemappedRotationMatrix, tempOrientation);
			if (mCameraOverlayView != null) {	// interpolate magnetic orientation data
				for (int i = 0; i < tempOrientation.length - 1; i++) {
					mOrientation[i] = (mOrientation[i] * 9 + tempOrientation[i]) / 10.0f;
				}
				mCameraOverlayView.refresh(mOrientation);
			}
		}
		else {
			Log.d(TAG, "Roatation matrix calculation failed.");
		}
	}
	
	public double GetDistance(double angle) {
		// angle is in degrees
		// need to figure out what to do if object is not in middle of screen? maybe?
		
		double height = 5.5; // default to 5'6" - can be changed 
		double max = 100; // set maximum distance - can be changed
				
		angle = 90.0 - angle;
		double radians = Math.toRadians(angle);
		double distance = Math.tan(radians) * height;
		if (angle > 90.0) {
			distance = 100;
		}
		return Math.min(max, distance);
	}
	
	// Assume bearing is input as degrees, and distance input as feet
	// bearing should be clockwise from North
	public Location CalculateLocation(double lat1, double lng1, double bearing, double distance) {
		if (bearing < 0.0) {
			bearing = bearing + 360.0;
		}
		double radius = 6371.00; // Radius of Earth in km
		double kmDistance = distance * 0.0003048; // convert to km
		double radiansBearing = Math.toRadians(bearing);	// convert to radians
		double angDistance = kmDistance / radius; // angular distance
		lat1 = Math.toRadians(lat1);
		lng1 = Math.toRadians(lng1);
		
		double lat2 = Math.asin( (Math.sin(lat1) * Math.cos(angDistance)) +
				(Math.cos(lat1)*Math.sin(angDistance)*Math.cos(radiansBearing)) );	// new lat
		double lng2 = lng1 + Math.atan2(Math.sin(radiansBearing)*Math.sin(angDistance)*Math.cos(lat1), 
				Math.cos(angDistance)-Math.sin(lat1)*Math.sin(lat2));				// new long
		
		lng2 = (lng2 + 3 * Math.PI) % (2*Math.PI) - Math.PI;	// normalize to -180...180 degrees
		
		lat2 = Math.toDegrees(lat2);
		lng2 = Math.toDegrees(lng2);
		
		Location newLoc = new Location("augdroid-ua.testLocProvider");
		newLoc.setLatitude(lat2);
		newLoc.setLongitude(lng2);
		
		return newLoc;
		
	}
	
	private void onDrag(MotionEvent event, float distanceX, float distanceY) {
		mDrawNewTag = false;
		if (mDraggedTag != null) {
			// update mDraggedTag
		}
	}
	
	/**
	 * Get the tag under the pixels (x, y). Return null if no tag is under those points.
	 * @param x
	 * @param y
	 * @return
	 */
	private Tag getTag(float x, float y) {
		Tag result = null;
		
		return result;
	}
	
	private class DragListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			onDrag(e1, distanceX, distanceY);
			return true;
		}
		
		@Override
		public boolean onDown(MotionEvent me) {
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent me) {

		}
		
		@Override
		public void onShowPress(MotionEvent me) {

		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent me) {
				try {
            		Location testLoc = new Location("augdroid-ua.testLocProvider");
            		testLoc.setLatitude(33.195721);
            		testLoc.setLongitude(-87.535137);
            		
            		mCameraOverlayView.updateLocation(testLoc);
            		mLocation = testLoc; // Test stuff for debugging*/
            		
            		float azimuth = (float)Math.toDegrees(mOrientation[0]);
            		float pitch = (float)Math.toDegrees(mOrientation[1]);
    			
            		double distance = GetDistance(pitch);
            		Location newLoc = CalculateLocation(mLocation.getLatitude(), mLocation.getLongitude(), azimuth, distance);
    			
            		Tag newTag = new Tag(1, "newTag", newLoc, 2.0f);
            		mCameraOverlayView.addTag(newTag);
            		mCameraOverlayView.setOverlayType(mOverlayType);
            	}
            	catch (Exception ex) {
            		Log.e(TAG, ex.getMessage());
            		// If this triggers, GPS location is probably null
            	}
				mDrawNewTag = true;
				mDraggedTag = null;
			return true;
		}
	}
	
}
