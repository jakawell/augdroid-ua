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
	
	private SensorManager mSensorManger;
	private float[] mAccelerometerData = new float[3];
	private float[] mMagneticData = new float[3];
	private float[] mRawRotationMatrix = new float[9];
	private float[] mRemappedRotationMatrix = new float[9];
	private float[] mOrientation = new float[3];
	
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_feed_layout);
		
		mFrame = (FrameLayout)findViewById(R.id.camera_feed_preview);
		mSensorManger = (SensorManager)getSystemService(SENSOR_SERVICE);
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		mLocationListener = new LocationListener() {
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			public void onProviderEnabled(String provider) { }
			public void onProviderDisabled(String provider) { }
			public void onLocationChanged(Location location) {
				if (mCameraOverlayView != null)
					mCameraOverlayView.updateLocation(location);
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
		
		Tag tag1 = new Tag(1, "Hello!", tag1Loc, 2.0f);
		Tag tag2 = new Tag(2, "World!", tag2Loc, 2.0f);
		Tag tag3 = new Tag(3, "Shelby", tagShelbyCenterLoc, 2.0f);
		Tag tag4 = new Tag(4, "Path", tagPathToFergLoc, 2.0f);
		
		mCameraOverlayView.addTag(tag1);
		mCameraOverlayView.addTag(tag2);
		mCameraOverlayView.addTag(tag3);
		mCameraOverlayView.addTag(tag4);
		mCameraOverlayView.setOverlayType(mOverlayType);
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
		
		if (SensorManager.getRotationMatrix(mRawRotationMatrix, null, mAccelerometerData, mMagneticData)) {
			SensorManager.remapCoordinateSystem(mRawRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRemappedRotationMatrix);
			SensorManager.getOrientation(mRemappedRotationMatrix, mOrientation);
			if (mCameraOverlayView != null) {
				mCameraOverlayView.refresh(mOrientation);
			}
		}
		else {
			Log.d(TAG, "Roatation matrix calculation failed.");
		}
	}
	
}
