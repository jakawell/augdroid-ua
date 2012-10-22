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
        
        Button startCameraButton = (Button)findViewById(R.id.view_feed_button);
        startCameraButton.setOnClickListener(new View.OnClickListener(	) {
			public void onClick(View v) {
				startCameraActivity();
			}
		});
    }
    
    private void startCameraActivity() {
    	Intent i = new Intent(this, CameraFeedActivity.class);
    	startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
