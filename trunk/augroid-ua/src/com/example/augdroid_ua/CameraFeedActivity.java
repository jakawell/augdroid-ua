package com.example.augdroid_ua;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraFeedActivity extends Activity implements SensorEventListener {
	
	private static final String TAG = "CameraFeedActivity";
	
	private Camera mCamera;
	private CameraFeedView mCameraFeedView;
	private CameraOverlayView mCameraOverlayView;
	private FrameLayout mFrame;
	
	private SensorManager mSensorManger;
	private float[] mAccelerometerData = new float[3];
	private float[] mMagneticData = new float[3];
	private float[] mRawRotationMatrix = new float[9];
	private float[] mRemappedRotationMatrix = new float[9];
	private float[] mOrientation = new float[3];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_feed_layout);
		
		mFrame = (FrameLayout)findViewById(R.id.camera_feed_preview);
		mSensorManger = (SensorManager)getSystemService(SENSOR_SERVICE);
		// camera and sensors are set up in onResume()
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
			mFrame.addView(mCameraOverlayView);
		} catch (Exception e) {
			// camera not available (in use)
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
		mSensorManger.registerListener(this, mSensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
		mSensorManger.registerListener(this, mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
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
