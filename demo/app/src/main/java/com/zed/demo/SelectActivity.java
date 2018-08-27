package com.zed.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.tutk.p2p.TUTKP2P;
import com.zed.demo.link.ClientActivity;
import com.zed.demo.link.DeviceActivity;
import com.zed.demo.p2p.P2PClientActivity;
import com.zed.demo.p2p.P2PDeviceActivity;

/**
 * @author zed
 * @date 2018/7/26 上午10:39
 * @desc
 */

public class SelectActivity extends Activity {

	private boolean mBoolean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			mBoolean = bundle.getBoolean(MainActivity.INTENT_LINK, false);
		}

		//p2p初始化
		TUTKP2P.TK_getInstance().TK_setDebug(true);
		TUTKP2P.TK_getInstance().TK_init();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("选择设备")
				.setNegativeButton("作为client", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if(mBoolean) {
							Intent intent = new Intent(SelectActivity.this, ClientActivity.class);
							startActivity(intent);
						}else {
							Intent intent = new Intent(SelectActivity.this, P2PClientActivity.class);
							startActivity(intent);
						}
					}
				})
				.setPositiveButton("作为device", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if(mBoolean) {
							Intent intent = new Intent(SelectActivity.this, DeviceActivity.class);
							startActivity(intent);
						}else {
							Intent intent = new Intent(SelectActivity.this, P2PDeviceActivity.class);
							startActivity(intent);
						}
					}
				});

		builder.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//p2p反初始化
		TUTKP2P.TK_getInstance().TK_unInit();
	}
}
