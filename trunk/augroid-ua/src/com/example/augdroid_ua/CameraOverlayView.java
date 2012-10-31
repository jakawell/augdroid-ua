package com.example.augdroid_ua;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.View;

public class CameraOverlayView extends View {

	public static final int OVERLAY_TYPE_NONE = 0;
	public static final int OVERLAY_TYPE_COMPASS = 1;
	public static final int OVERLAY_TYPE_TAG = 2;
	
	private Paint mPaint = new Paint();
	private int mOverlayType;
	
	private float[] mOrientation = null;
	private Location mLocation;
	private float[] mCameraViewAngles;
	private LinkedList<Location> mTagLocations = new LinkedList<Location>();
	
	/**
	 * Creates a new camera overlay object.
	 * 
	 * @param context	the context of this overlay
	 */
	public CameraOverlayView(Context context) {
		super(context);
		mOverlayType = OVERLAY_TYPE_NONE;
		
	}
	
	/**
	 * 
	 * @param cameraViewAngles	the angles that the camera can see (in degrees), with horizontal first, then vertical
	 */
	public void setupCamera(float[] cameraViewAngles) {
		this.mCameraViewAngles = cameraViewAngles;
	}
	
	public void refresh(float[] newOrientation) {
		mOrientation = newOrientation;
		invalidate();
	}
	
	public void updateLocation(Location newLocation) {
		mLocation = newLocation;
	}
	
	public void addTag(Location tag) {
		mTagLocations.add(tag);
	}
	
	public void setOverlayType(int newType) {
		mOverlayType = newType;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();
		int width = getWidth();
		float horizontalPixelsPerDegree = (float)width / mCameraViewAngles[0];
//		float verticalPixelsPerDegree = (float)height / mCameraViewAngles[0];
		
		if (mOrientation != null) {
//			String testBox = "azimuth:\t " + mOrientation[0] + "\n   pitch:\t " + mOrientation[1] + "\n   roll:\t " + mOrientation[2];
//			float azimuth = ((float)Math.round(Math.toDegrees(mOrientation[0]) * 2)) / 2.0f;
			float azimuth = (float)Math.toDegrees(mOrientation[0]);
			String testBox = "azimuth: " + azimuth;
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(24);
			canvas.drawText(testBox, 30, 30, mPaint);
			
			switch (mOverlayType) {
			case OVERLAY_TYPE_COMPASS:
				if (azimuth < 40 && azimuth > -40) {
					mPaint.setStyle(Paint.Style.FILL);
					canvas.drawCircle(((float)width / 2.0f) - (((azimuth * 2.5f)/100.0f)*((float)width/2.0f)), height / 2, 50, mPaint);
				}
				break;
			case OVERLAY_TYPE_TAG:
				if (mLocation != null && !mTagLocations.isEmpty()) {
					for (int i = 0; i < mTagLocations.size(); i++) {
						Location location = mTagLocations.get(i);
						float bearing = mLocation.bearingTo(location);
						float bearingDifference = bearing - azimuth; // compute the difference between the bearing of the tag and the current bearing of the phone
						// if a tag is located at (i.e., "has its bearing at") the azimuth, it is centered at pixel width/2
						// if a tag is lcoated 2 degree east of the azimuth, it is centered at pixel (width/2)+(2 * pixelsPerDegree)
						float displayPixel = ((float)width / 2.0f) + bearingDifference * horizontalPixelsPerDegree;
						int radius = 50;
						if (displayPixel > 0 - radius && displayPixel < width + radius) { // if any part of the tag would be visible
							canvas.drawCircle(displayPixel, height / 2, radius, mPaint);
							canvas.drawText("Bearing " + i + ": " + bearing + " (VISIBLE)", 30, 62 + (30 * i), mPaint);
						}
						else
							canvas.drawText("Bearing " + i + ": " + bearing + " (NOT VISIBLE)", 30, 62 + (30 * i), mPaint);
					}
				}
				break;
			case OVERLAY_TYPE_NONE:
			default:
				break;
			}
		}
	}
}
