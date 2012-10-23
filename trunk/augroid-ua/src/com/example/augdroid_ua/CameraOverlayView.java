package com.example.augdroid_ua;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CameraOverlayView extends View {

	private Paint mPaint = new Paint();
	
	public CameraOverlayView(Context context) {
		super(context);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();
		int width = getWidth();
		
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.GREEN);
		canvas.drawCircle(width / 2, height / 2, 50, mPaint);
	}
}
