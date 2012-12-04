package com.example.augdroid_ua;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;


public class CameraFeedActivity extends Activity implements SensorEventListener {
	
	public static final String EXTRA_OVERLAY_TYPE = "overlay_type_extra";
	private static final String TAG = "CameraFeedActivity";
	
	private Camera mCamera;
	private CameraFeedView mCameraFeedView;
	private CameraOverlayView mCameraOverlayView;
	private LinearLayout mTutorialView;
	private FrameLayout mFrame;
	private int mOverlayType;
	private boolean mShowingTutorial;
	private Location mLocation;
	
	private SensorManager mSensorManger;
	private float[] mAccelerometerData = new float[3];
	private float[] mMagneticData = new float[3];
	private float[] mRawRotationMatrix = new float[9];
	private float[] mRemappedRotationMatrix = new float[9];
	private float[] mOrientation = new float[3];
	
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	
	private Tag mDraggedTag;
	private GestureDetector mDragDetector;
	private OnTouchListener mTouchListener; // this is only to handle when a finger leaves the screen for the end of dragging motions
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_feed_layout);
		
		mFrame = (FrameLayout)findViewById(R.id.camera_feed_preview);
		mTutorialView = (LinearLayout)getLayoutInflater().inflate(R.layout.tutorial_overlay, null);
		mSensorManger = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		mDraggedTag = null;
		mDragDetector = new GestureDetector(this, new DragListener());
		mTouchListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (mDragDetector.onTouchEvent(event)) // let the drag detector try to handle it first
					return true;
				if (event.getAction() == MotionEvent.ACTION_UP) // if it wasn't handled by the drag detector above, check if it's and UP and that we were dragging something
					if (mDraggedTag != null) {
						finishDrag(event);
					}
				return false;
			}
		};
		mFrame.setOnTouchListener(mTouchListener);
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
			
			mCameraOverlayView = new CameraOverlayView(this, mCamera.getParameters().getHorizontalViewAngle(), mCamera.getParameters().getVerticalViewAngle());
			mCameraOverlayView.setOverlayType(mOverlayType);
			mFrame.addView(mTutorialView);
			mShowingTutorial = true;
			mFrame.addView(mCameraOverlayView);
		} catch (Exception e) {
			// camera not available (in use)
			e.printStackTrace();
			Toast.makeText(this, "Camera not available", Toast.LENGTH_LONG).show();
			this.finish();
		}
	}
	
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
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
				mOrientation[2] = tempOrientation[2];
				mCameraOverlayView.refresh(mOrientation);
				mCameraOverlayView.updateDistance(getDistance(Math.toDegrees(mOrientation[1])));
			}
		}
		else {
			Log.d(TAG, "Roatation matrix calculation failed.");
		}
	}
	
	public double getDistance(double angle) {
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
	public Location calculateLocation(double lat1, double lng1, double bearing, double distance) {
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
		if (mDraggedTag == null)
			mDraggedTag = mCameraOverlayView.getTagAtPoint((int)event.getX(), (int)event.getY());
		if (mDraggedTag != null)
			mDraggedTag.forceLocation(mDraggedTag.screenLocationX - (int)distanceX, mDraggedTag.screenLocationY - (int)distanceY, mLocation.distanceTo(mDraggedTag.location));
	}
	
	private void finishDrag(MotionEvent event) {
		float azimuth = (float)Math.toDegrees(mOrientation[0]);
		float pitch = (float)Math.toDegrees(mOrientation[1]);
		float[] screenInfo = mCameraOverlayView.getScreenInfo();
		float width = screenInfo[0];
		float height = screenInfo[1];
		float horizontalPixelsPerDegree = screenInfo[2];
		float verticalPixelsPerDegree = screenInfo[3];
		float lastX = event.getX();
		float lastY = event.getY();
		float oldLocation = mDraggedTag.screenOldDistance * 3.28084f;		
		
		float rawNewLocationHorizontalInDegrees = azimuth - (((width / 2.0f) - lastX) * (1.0f / horizontalPixelsPerDegree));
		float rawNewLocationVerticalInDegrees = pitch - (((height / 2) - lastY) * (1.0f / verticalPixelsPerDegree));
		if (rawNewLocationHorizontalInDegrees > 180)
			rawNewLocationHorizontalInDegrees = rawNewLocationHorizontalInDegrees % -180f;
		if (rawNewLocationHorizontalInDegrees < 180)
			rawNewLocationHorizontalInDegrees = rawNewLocationHorizontalInDegrees % 180f;
		if (rawNewLocationVerticalInDegrees > 180)
			rawNewLocationVerticalInDegrees = rawNewLocationVerticalInDegrees % -180f;
		if (rawNewLocationVerticalInDegrees < 180)
			rawNewLocationVerticalInDegrees = rawNewLocationVerticalInDegrees % 180f;
		Location newLocation = calculateLocation(mLocation.getLatitude(), mLocation.getLongitude(), rawNewLocationHorizontalInDegrees, oldLocation);
		mDraggedTag.location = newLocation;
		mDraggedTag.releaseForceLocation();
		mDraggedTag = null;
	}
	
	private void deleteTag(float x, float y) {
		final Tag deleteCandidate = mCameraOverlayView.getTagAtPoint((int)x, (int)y);
		if (deleteCandidate != null) {
			deleteCandidate.highlight(true);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Delete the tag?");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mCameraOverlayView.removeTag(deleteCandidate);
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					deleteCandidate.highlight(false);
				}
			});
			builder.show();
		}
	}
	
	private class DragListener extends GestureDetector.SimpleOnGestureListener {
		public static final String TAG = "DragListener";
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			onDrag(e1, distanceX, distanceY);
			return true;
		}
		
		@Override
		public boolean onDown(MotionEvent me) {
			return true; // this doesn't make sense, but it's necessary. Go figure.
		}
		
		@Override
		public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent me) {
			deleteTag(me.getX(), me.getY());
		}
		
		@Override
		public void onShowPress(MotionEvent me) {

		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent me) {
			if (mShowingTutorial) {
				mFrame.removeView(mTutorialView);
				mShowingTutorial = false;
				return true;
			}
			try {
        		
        		float azimuth = (float)Math.toDegrees(mOrientation[0]);
        		float pitch = (float)Math.toDegrees(mOrientation[1]);
			
        		double distance = getDistance(pitch);
        		Location newLoc = calculateLocation(mLocation.getLatitude(), mLocation.getLongitude(), azimuth, distance);
        		
        		Tag newTag = new Tag(1, "newTag", newLoc, 2.0f);
        		mCameraOverlayView.addTag(newTag);
        	}
        	catch (Exception ex) {
        		Log.d(TAG, "No GPS lock yet.");
        		// If this triggers, GPS location is probably null
        	}
			mDraggedTag = null;
			return true;
		}
	}
	
}
