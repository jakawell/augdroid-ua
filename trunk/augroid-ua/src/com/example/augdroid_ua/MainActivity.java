package com.example.augdroid_ua;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button startCameraCompassButton = (Button)findViewById(R.id.view_feed_compass_button);
        Button startCameraTagsButton = (Button)findViewById(R.id.view_feed_tags_button);
        startCameraCompassButton.setOnClickListener(new View.OnClickListener(	) {
			public void onClick(View v) {
				startCameraActivity(CameraOverlayView.OVERLAY_TYPE_COMPASS);
			}
		});
        startCameraTagsButton.setOnClickListener(new View.OnClickListener(	) {
			public void onClick(View v) {
				startCameraActivity(CameraOverlayView.OVERLAY_TYPE_TAG);
			}
		});
    }
    
    private void startCameraActivity(int overlayType) {
    	Intent i = new Intent(this, CameraFeedActivity.class);
    	i.putExtra(CameraFeedActivity.EXTRA_OVERLAY_TYPE, overlayType);
    	startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
