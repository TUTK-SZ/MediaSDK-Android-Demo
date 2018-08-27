package com.zed.demo.p2p;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.St_AvStatus;
import com.tutk.IOTC.St_SInfo;
import com.tutk.p2p.TUTKP2P;
import com.tutk.p2p.inner.OnP2PDeviceListener;
import com.tutk.p2p.utils.AVIOCTRLDEFs;
import com.tutk.p2p.utils.P2PUtils;
import com.zed.demo.R;
import com.zed.demo.ToastUtils;

/**
 * @author zed
 * @date 2018/7/26 上午10:40
 * @desc
 */

public class P2PDeviceActivity extends Activity {

	private static final String TAG = "ClientActivity";

	private int mSID = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_p2p_device);

		//注册监听
		TUTKP2P.TK_getInstance().TK_registerDeviceListener(mDeviceListener);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* p2p */
		TUTKP2P.TK_getInstance().TK_unRegisterDeviceListener(mDeviceListener);
		TUTKP2P.TK_getInstance().TK_device_disConnectAll();
		TUTKP2P.TK_getInstance().TK_device_logout();
	}

	private OnP2PDeviceListener mDeviceListener = new OnP2PDeviceListener() {
		@Override
		public void receiveOnLineInfo(String checkUID, int result) {

		}

		@Override
		public void receiveIOTCListenInfo(int sid) {
			mSID = sid;
			ToastUtils.showToast(P2PDeviceActivity.this, "收到client连线");
		}

		@Override
		public void receiveAvServerStart(int sid, int avIndex, int state) {
			if (avIndex >= 0) {
				ToastUtils.showToast(P2PDeviceActivity.this, "连接成功 avNewServStart = " + avIndex);
			} else {
				ToastUtils.showToast(P2PDeviceActivity.this, "连接失败 avNewServStart = " + avIndex);
			}
		}

		@Override
		public void receiveSessionCheckInfo(int sid, St_SInfo info, int result) {
			if (result == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE
					|| result == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT) {
				ToastUtils.showToast(P2PDeviceActivity.this, "对方掉线");
				finish();
			}
		}

		@Override
		public void receiveStatusCheckInfo(int sid, St_AvStatus status, int result) {
			if (result == P2PUtils.STATUS_CHECK_BLOCK) {
				ToastUtils.showToast(P2PDeviceActivity.this, "网络阻塞 延时严重");
			}
		}

		@Override
		public void receiveIOCtrlDataInfo(int sid, int avIOCtrlMsgType, byte[] data) {
			//收到client回传的command
			switch (avIOCtrlMsgType) {
				case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART: {
					//收到client请求device发送音频数据 底层已经自动开启发送音频的线程

					//模拟发送数据
					TUTKP2P.TK_getInstance().TK_device_onSendAudioData(mSID, new byte[]{0, 0, 0, 0}, 4, System.currentTimeMillis());
				}
				break;
				case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART:{
					//收到client请求device接收音频数据 底层已经自动开启接收音频的线程

				}
				break;
				case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START: {
					//收到client请求device发送视频数据 底层已经自动开启发送视频的线程

					//模拟发送数据
					TUTKP2P.TK_getInstance().TK_device_onSendVideoData(mSID, new byte[]{0, 0, 0, 0}, true, System.currentTimeMillis());
				}
				break;
				case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START_CLIENT:{
					//收到client请求device接收视频数据 底层已经自动开启接收视频的线程

				}
				break;
				default:{
					ToastUtils.showToast(P2PDeviceActivity.this, "收到command " + Integer.toHexString(avIOCtrlMsgType));
				}
			}

		}

		@Override
		public void sendIOCtrlDataInfo(int sid, int avIOCtrlMsgType, int result, byte[] data) {

		}

		@Override
		public void receiveVideoInfo(int sid, byte[] videoData, long timeStamp, int number, int onlineNum, boolean isIFrame) {
			ToastUtils.showToast(P2PDeviceActivity.this,"收到视频数据");
		}

		@Override
		public void receiveAudioInfo(int sid, byte[] audioData, long timeStamp, int number) {
			ToastUtils.showToast(P2PDeviceActivity.this,"收到音频数据");
		}
	};


	public void login(View view) {
		//设备登陆
		EditText edit_uid = findViewById(R.id.edit_uid);
		String UID = edit_uid.getText().toString().trim();
		int login = TUTKP2P.TK_getInstance().TK_device_login(UID, TUTKP2P.DEFAULT_PASSWORD);
		if (login >= 0) {
			ToastUtils.showToast(P2PDeviceActivity.this, "登陆成功");
		}
	}

	public void logout(View view) {
		//设备登出 调用后无法收到client的连线
		TUTKP2P.TK_getInstance().TK_device_logout();
	}

	public void disconnect(View view) {
		//断线uid
		TUTKP2P.TK_getInstance().TK_device_disConnect(mSID);
	}

	public void sendIOCtrl(View view) {
		//开始发送自定义command
		TUTKP2P.TK_getInstance().TK_device_sendIOCtrl(
				mSID,
				0x0901,
				new byte[]{0, 0, 0, 0});
	}


}
