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
	private int mWidth;
	private int mHeight;
	private float mHorizontalPixelsPerDegree;
	private float mVerticalPixelsPerDegree;
	
	private float[] mOrientation = null;
	private double mDistance = 0;
	private Location mLocation;
	private float[] mCameraViewAngles;
	private LinkedList<Tag> mTags = new LinkedList<Tag>();
	
	/**
	 * Creates a new camera overlay object.
	 * 
	 * @param context	the context of this overlay
	 */
	public CameraOverlayView(Context context, float horizontalCameraViewAngle, float verticalCameraViewAngle) {
		super(context);
		mOverlayType = OVERLAY_TYPE_NONE;
		mCameraViewAngles = new float[] { horizontalCameraViewAngle, verticalCameraViewAngle };
		mCameraViewAngles[0] = horizontalCameraViewAngle;
		mCameraViewAngles[1] = verticalCameraViewAngle;
		mWidth = getWidth();
		mHeight = getHeight();
		mHorizontalPixelsPerDegree = (float)mWidth / mCameraViewAngles[0];
		mVerticalPixelsPerDegree = (float)mHeight / mCameraViewAngles[1];
	}
	
	public void refresh(float[] newOrientation) {
		mOrientation = newOrientation;
		invalidate();
	}
	
	public void updateLocation(Location newLocation) {
		mLocation = newLocation;
	}
	
	public void updateDistance(double newDistance) {
		int roundedDistance = (int)(newDistance * 10);
		mDistance = roundedDistance / 10.0d;
	}
	
	public void addTag(Tag tag) {
		mTags.add(tag);
	}
	
	public void setOverlayType(int newType) {
		mOverlayType = newType;
	}
	
	public Tag getTagAtPoint(int x, int y) {
		for (Tag tag : mTags) {
			if (tag.screenVisible)
				if (((x - tag.screenLocationX) ^ 2 + (y - tag.screenLocationY) ^ 2) <= (tag.screenRadius ^ 2))
					return tag;
		}
		return null;
	}
	
	public float[] getScreenInfo() {
		return new float[]{(float)mWidth, (float)mHeight, mHorizontalPixelsPerDegree, mVerticalPixelsPerDegree};
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mWidth = getWidth();
		mHeight = getHeight();
		mHorizontalPixelsPerDegree = (float)mWidth / mCameraViewAngles[0];
		mVerticalPixelsPerDegree = (float)mHeight / mCameraViewAngles[1];
		
		if (mLocation == null) {
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(32);
			canvas.drawText("Aquiring GPS...", 15, 30, mPaint);
		}
		else if (mOrientation != null) {
			mPaint.setColor(Color.DKGRAY); // draw cross hairs
			canvas.drawLine((mWidth / 2), (mHeight / 2) + 15, (mWidth / 2), (mHeight / 2) + 35, mPaint);
			canvas.drawLine((mWidth / 2), (mHeight / 2) - 15, (mWidth / 2), (mHeight / 2) - 35, mPaint);
			canvas.drawLine((mWidth / 2) + 15, (mHeight / 2), (mWidth / 2) + 35, (mHeight / 2), mPaint);
			canvas.drawLine((mWidth / 2) - 15, (mHeight / 2), (mWidth / 2) - 35, (mHeight / 2), mPaint);
			
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(32);
			canvas.drawText("Distance: " + mDistance + "feet", 15, 30, mPaint);
			
			float azimuth = (float)Math.toDegrees(mOrientation[0]);
			float pitch = (float)Math.toDegrees(mOrientation[1]);
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(24);
			
			switch (mOverlayType) {
			case OVERLAY_TYPE_COMPASS:
				if (azimuth < 40 && azimuth > -40) {
					mPaint.setStyle(Paint.Style.FILL);
					canvas.drawCircle(((float)mWidth / 2.0f) - (((azimuth * 2.5f)/100.0f)*((float)mWidth/2.0f)), mHeight / 2, 50, mPaint);
				}
				break;
			case OVERLAY_TYPE_TAG:
				if (mLocation != null && !mTags.isEmpty()) {
					for (int i = 0; i < mTags.size(); i++) {
						Tag tag =  mTags.get(i);
						Location location = tag.location;
						float tagHeight = tag.height;

						float horizontalDisplayPixel, verticalDisplayPixel, radiusDisplaySize;
						if (tag.forceScreenLocation) { // if we're forcing the location (user is controlling the location by dragging), don't calculate
							horizontalDisplayPixel = (float)tag.screenLocationX;
							verticalDisplayPixel = (float)tag.screenLocationY;
							radiusDisplaySize = (float)tag.screenRadius;
						}
						else {
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
							radiusDisplaySize = (angularSize / mCameraViewAngles[1]) * (float)mHeight;
							
							// HORIZONTAL HANDLING (AZIMUTH)
							float bearing = mLocation.bearingTo(location);
							float bearingDifference = bearing - azimuth; // compute the difference between the bearing of the tag and the current bearing of the phone
							// if a tag is located at (i.e., "has its bearing at") the azimuth, it is centered at pixel width/2
							// if a tag is located 2 degree east of the azimuth, it is centered at pixel (width/2)+(2 * pixelsPerDegree)
							horizontalDisplayPixel = ((float)mWidth / 2.0f) + (bearingDifference * mHorizontalPixelsPerDegree);
							
							// VERTICAL HANDLING (PITCH)
							// the raw position (without the tag's height factored in) is found exactly as above
							verticalDisplayPixel = ((float)mHeight / 2.0f) - (pitch * mVerticalPixelsPerDegree);
							// ...then we calculate the tag's height (using the angular size, as in "size handling"), and add it to the position
							float heightAngularSize = 2.0f * (float)Math.atan(tagHeight / (2.0f * distance));
							heightAngularSize = (float)Math.toDegrees(heightAngularSize);
							float heightAddedPixels = (heightAngularSize / mCameraViewAngles[1]) * (float)mHeight;
							verticalDisplayPixel += heightAddedPixels;
						}
						
						if (radiusDisplaySize > 2 && horizontalDisplayPixel > 0 - radiusDisplaySize && verticalDisplayPixel > 0 - radiusDisplaySize && horizontalDisplayPixel < mWidth + radiusDisplaySize && verticalDisplayPixel < mHeight + radiusDisplaySize) { // if any part of the tag would be visible
							if (tag.forceScreenLocation) {
								int oldColor = mPaint.getColor();
								mPaint.setColor(Color.YELLOW); // add yellow border to show that tag is selected
								canvas.drawCircle(horizontalDisplayPixel, verticalDisplayPixel, radiusDisplaySize + 3, mPaint);
								mPaint.setColor(oldColor);
							}
							tag.setOnScreen((int)horizontalDisplayPixel, (int)verticalDisplayPixel, (int)radiusDisplaySize);
							canvas.drawCircle(horizontalDisplayPixel, verticalDisplayPixel, radiusDisplaySize, mPaint);
						}
						else {
							tag.setOffScreen();
						}
						
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
