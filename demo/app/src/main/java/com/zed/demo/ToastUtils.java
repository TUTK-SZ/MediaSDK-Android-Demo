package com.zed.demo;

import android.app.Activity;
import android.widget.Toast;

/**
 * @author zed
 * @date 2018/7/26 上午10:50
 * @desc
 */

public class ToastUtils {

	public static void showToast(final Activity activity, final String msg) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
