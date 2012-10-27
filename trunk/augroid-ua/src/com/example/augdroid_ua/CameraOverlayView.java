package com.example.augdroid_ua;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CameraOverlayView extends View {

	private Paint mPaint = new Paint();
	
	private float[] mOrientation = null;
	
	public CameraOverlayView(Context context) {
		super(context);
	}
	
	public void refresh(float[] newOrientation) {
		mOrientation = newOrientation;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();
		int width = getWidth();
		
		if (mOrientation != null) {
//			String testBox = "azimuth:\t " + mOrientation[0] + "\n   pitch:\t " + mOrientation[1] + "\n   roll:\t " + mOrientation[2];
//			float azimuth = ((float)Math.round(Math.toDegrees(mOrientation[0]) * 2)) / 2.0f;
			float azimuth = (float)Math.toDegrees(mOrientation[0]);
			String testBox = "azimuth: " + azimuth;
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(24);
			canvas.drawText(testBox, 30, 30, mPaint);
			
			if (azimuth < 40 && azimuth > -40) {
				mPaint.setStyle(Paint.Style.FILL);
				mPaint.setColor(Color.GREEN); 
				canvas.drawCircle(((float)width / 2.0f) - (((azimuth * 2.5f)/100.0f)*((float)width/2.0f)), height / 2, 50, mPaint);
			}
		}
	}
}
