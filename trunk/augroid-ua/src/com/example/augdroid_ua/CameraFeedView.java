package com.example.augdroid_ua;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraFeedView extends SurfaceView implements SurfaceHolder.Callback {

	private final static String TAG = "CameraFeedView";
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera;
	
	@SuppressWarnings("deprecation")
	public CameraFeedView(Context context, Camera camera) {
		super(context);
		
		mCamera = camera;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // required for Gingerbread (even though deprecated)
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Error previewing camera:\n" + ex.getMessage());
		} 
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mSurfaceHolder.getSurface() == null) // check that surface exists first
			return;
		
		try {
			mCamera.stopPreview();
		} catch (Exception ex) { // preview didn't exist yet 
		}
		
		// TODO: Add edits
		
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
		} catch (Exception ex) {
			Log.e(TAG, "Error restarting camera preview:\n" + ex.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Don't forget to release camera!
	}
}
