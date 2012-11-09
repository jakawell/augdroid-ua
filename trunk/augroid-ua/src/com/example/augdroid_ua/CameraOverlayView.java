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
	private LinkedList<Tag> mTags = new LinkedList<Tag>();
	
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
	
	public void addTag(Tag tag) {
		mTags.add(tag);
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
		float verticalPixelsPerDegree = (float)height / mCameraViewAngles[1];
		
		// TODO: REMOVE. This tracks the next y coordinate for text to be displayed on screen 
		int testTextLine = 1;
		int testTextSpacing = 30;
		
		if (mLocation == null) {
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(32);
			canvas.drawText("Aquiring GPS...", 30, 30, mPaint);
		}
		else if (mOrientation != null) {
//			String testBox = "azimuth:\t " + mOrientation[0] + "\n   pitch:\t " + mOrientation[1] + "\n   roll:\t " + mOrientation[2];
//			float azimuth = ((float)Math.round(Math.toDegrees(mOrientation[0]) * 2)) / 2.0f;
			float azimuth = (float)Math.toDegrees(mOrientation[0]);
			float pitch = (float)Math.toDegrees(mOrientation[1]);
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(24);
			
			switch (mOverlayType) {
			case OVERLAY_TYPE_COMPASS:
				if (azimuth < 40 && azimuth > -40) {
					mPaint.setStyle(Paint.Style.FILL);
					canvas.drawCircle(((float)width / 2.0f) - (((azimuth * 2.5f)/100.0f)*((float)width/2.0f)), height / 2, 50, mPaint);
				}
				break;
			case OVERLAY_TYPE_TAG:
				if (mLocation != null && !mTags.isEmpty()) {
					for (int i = 0; i < mTags.size(); i++) {
						Tag tag =  mTags.get(i);
						Location location = tag.location;
						float tagHeight = tag.height;
						
						// SIZE HANDLING (DISTANCE)
						float radiusTrueSize = 1.5f; // in meters
						float distance = mLocation.distanceTo(location);
						// the "angular size" is how large an object appears, measured in degrees of visual field, based on the true size of
						// the object and the distance to the object. The equation is: angularSize = 2 * arctan(size / 2*distance)
						float angularSize = 2.0f * (float)Math.atan(radiusTrueSize / (2.0f * distance));
						angularSize = (float)Math.toDegrees(angularSize); // convert from radians to degrees
						// to get the size in pixels, we take the percentage of the camera's field of view that the angular size takes up, 
						// and multiply that by the size of the screen. NOTE: I am using the vertical field of view and height, but I could 
						// use the horizontal field of view and width, and maybe get a different number (maybe).
						float radiusDisplaySize = (angularSize / mCameraViewAngles[1]) * (float)height;

						// HORIZONTAL HANDLING (AZIMUTH)
						float bearing = mLocation.bearingTo(location);
						float bearingDifference = bearing - azimuth; // compute the difference between the bearing of the tag and the current bearing of the phone
						// if a tag is located at (i.e., "has its bearing at") the azimuth, it is centered at pixel width/2
						// if a tag is lcoated 2 degree east of the azimuth, it is centered at pixel (width/2)+(2 * pixelsPerDegree)
						float horizontalDisplayPixel = ((float)width / 2.0f) + (bearingDifference * horizontalPixelsPerDegree);
						
						// VERTICAL HANDLING (PITCH)
						// the raw position (with out the tag's height factored in) is found exactly as above
						float verticalDisplayPixel = ((float)height / 2.0f) - (pitch * verticalPixelsPerDegree);
						// ...then we calculate the tag's height (using the angular size, as in "size handling"), and add it to the position
						float heightAngularSize = 2.0f * (float)Math.atan(tagHeight / (2.0f * distance));
						heightAngularSize = (float)Math.toDegrees(heightAngularSize);
						float heightAddedPixels = (heightAngularSize / mCameraViewAngles[1]) * (float)height;
						verticalDisplayPixel += heightAddedPixels;
						
						String tagText = "Tag " + i + ": " + bearing + "deg., " + distance + " m";
						if (radiusDisplaySize > 2 && horizontalDisplayPixel > 0 - radiusDisplaySize && verticalDisplayPixel > 0 - radiusDisplaySize && horizontalDisplayPixel < width + radiusDisplaySize && verticalDisplayPixel < height + radiusDisplaySize) { // if any part of the tag would be visible
							canvas.drawCircle(horizontalDisplayPixel, verticalDisplayPixel, radiusDisplaySize, mPaint);
							canvas.drawText(tagText + " (VISIBLE)", 30, testTextSpacing * testTextLine++, mPaint);
						}
						else
							canvas.drawText(tagText + " (NOT VISIBLE)", 30, testTextSpacing * testTextLine++, mPaint);
						
						
					}
				}
				break;
			case OVERLAY_TYPE_NONE:
			default:
				break;
			}
			mPaint.setColor(Color.GREEN);
			String testBoxAzimuth = "azimuth: " + azimuth;
			String testBoxPitch =   "pitch:   " + pitch;
			canvas.drawText(testBoxAzimuth, 30, testTextSpacing * testTextLine++, mPaint);
			canvas.drawText(testBoxPitch, 30, testTextSpacing * testTextLine++, mPaint);
			
			canvas.drawText("Hor. View Angle: " + mCameraViewAngles[0], 30, testTextSpacing * testTextLine++, mPaint);
			canvas.drawText("Ver. View Angle: " + mCameraViewAngles[1], 30, testTextSpacing * testTextLine++, mPaint);
			canvas.drawText("Hor. pix. per deg.: " + horizontalPixelsPerDegree, 30, testTextSpacing * testTextLine++, mPaint);
			canvas.drawText("Ver. pix. per deg.: " + verticalPixelsPerDegree, 30, testTextSpacing * testTextLine++, mPaint);
		}
	}
}
