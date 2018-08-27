package com.zed.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.zed.demo.media.MediaActivity;


public class MainActivity extends Activity {

	public static final String INTENT_P2P   = "p2p";
	public static final String INTENT_LINK  = "link";
	public static final String INTENT_MEDIA = "media";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		String[] strings = new String[]{Manifest.permission.RECORD_AUDIO,
				Manifest.permission.CAMERA,
				Manifest.permission.WRITE_EXTERNAL_STORAGE};

		ActivityCompat.requestPermissions(this, strings, 0);
	}


	public void link(View view) {
		Intent intent = new Intent(MainActivity.this, SelectActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean(INTENT_LINK, true);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void p2p(View view) {
		Intent intent = new Intent(MainActivity.this, SelectActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean(INTENT_P2P, true);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void media(View view) {
		Intent intent = new Intent(MainActivity.this, MediaActivity.class);
		startActivity(intent);
	}
}
