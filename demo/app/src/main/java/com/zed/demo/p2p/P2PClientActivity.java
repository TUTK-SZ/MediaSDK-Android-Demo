package com.zed.demo.p2p;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.St_AvStatus;
import com.tutk.IOTC.St_SInfo;
import com.tutk.p2p.TUTKP2P;
import com.tutk.p2p.inner.OnP2PClientListener;
import com.tutk.p2p.utils.P2PUtils;
import com.zed.demo.R;
import com.zed.demo.ToastUtils;

/**
 * @author zed
 * @date 2018/7/26 上午10:40
 * @desc
 */

public class P2PClientActivity extends Activity {

	private static final String TAG = "P2PClientActivity";

	private String mUID = "CDKA8H4CU7R4GGPGUHC1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_client);

		//注册监听
		TUTKP2P.TK_getInstance().TK_registerClientListener(mClientListener);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* p2p */
		TUTKP2P.TK_getInstance().TK_unRegisterClientListener(mClientListener);
		TUTKP2P.TK_getInstance().TK_client_disConnectAll();

	}

	private OnP2PClientListener mClientListener = new OnP2PClientListener() {
		@Override
		public void receiveConnectInfo(String uid, int sid, int state) {
//			Log.i(TAG, " IOTC_Connect_ByUID_Parallel = " + i);
		}

		@Override
		public void receiveClientStartInfo(String uid, int avIndex, int state) {
			if (state == TUTKP2P.CONNECTION_STATE_CONNECTED) {
				ToastUtils.showToast(P2PClientActivity.this, "连接成功 avNewClientStart = " + avIndex);
			} else if (state == TUTKP2P.CONNECTION_STATE_CONNECT_FAILED) {
				ToastUtils.showToast(P2PClientActivity.this, "连接失败 avNewClientStart = " + avIndex);
			}
		}

		@Override
		public void receiveSessionCheckInfo(String uid, St_SInfo info, int result) {
			if (result == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE
					|| result == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT) {
				ToastUtils.showToast(P2PClientActivity.this, "对方掉线");
				finish();
			}
		}

		@Override
		public void receiveStatusCheckInfo(String uid, St_AvStatus status, int result) {
			if (result == P2PUtils.STATUS_CHECK_BLOCK) {
				ToastUtils.showToast(P2PClientActivity.this, "网络阻塞 延时严重");
			}
		}

		@Override
		public void receiveIOCtrlDataInfo(String uid, int avChannel, int avIOCtrlMsgType, byte[] data) {
			//收到设备回传的command
			ToastUtils.showToast(P2PClientActivity.this, "收到command " + Integer.toHexString(avIOCtrlMsgType));
		}

		@Override
		public void sendIOCtrlDataInfo(String uid, int avChannel, int avIOCtrlMsgType, int result, byte[] data) {

		}

		@Override
		public void receiveVideoInfo(String uid, int avChannel, byte[] videoData, long timeStamp, int number, int onlineNum, boolean isIFrame) {
			ToastUtils.showToast(P2PClientActivity.this,"收到视频数据");
		}

		@Override
		public void receiveAudioInfo(String uid, int avChannel, byte[] audioData, long timeStamp, int number) {
			ToastUtils.showToast(P2PClientActivity.this,"收到音频数据");
		}
	};


	public void connect(View view) {
		//连接uid
		EditText edit_uid = findViewById(R.id.edit_uid);
		mUID = edit_uid.getText().toString().trim();
		TUTKP2P.TK_getInstance().TK_client_connect(mUID, TUTKP2P.DEFAULT_PASSWORD, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void disconnect(View view) {
		//断线uid
		TUTKP2P.TK_getInstance().TK_client_disConnect(mUID);
	}

	public void startSendVideo(View view) {
		//开始发送视频数据
		TUTKP2P.TK_getInstance().TK_client_startSendVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);
		//模拟发送数据
		TUTKP2P.TK_getInstance().TK_client_onSendVideoData(mUID, TUTKP2P.DEFAULT_CHANNEL, new byte[]{0, 0, 0, 0}, true, System.currentTimeMillis());
	}

	public void stopSendVideo(View view) {
		//停止发送视频数据
		TUTKP2P.TK_getInstance().TK_client_stopSendVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void startReceiveVideo(View view) {
		//开始接收视频数据
		TUTKP2P.TK_getInstance().TK_client_startReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL, false);
	}

	public void stopReceiveVideo(View view) {
		//停止接收视频数据
		TUTKP2P.TK_getInstance().TK_client_stopReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void startSpeak(View view) {
		//开始发送音频数据
		TUTKP2P.TK_getInstance().TK_client_startSpeaking(mUID, TUTKP2P.DEFAULT_CHANNEL);
		//模拟发送数据
		TUTKP2P.TK_getInstance().TK_client_onSendAudioData(mUID, TUTKP2P.DEFAULT_CHANNEL, new byte[]{0, 0, 0, 0}, 4, System.currentTimeMillis());
	}

	public void stopSpeak(View view) {
		//停止发送音频数据
		TUTKP2P.TK_getInstance().TK_client_stopSpeaking(mUID, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void startListener(View view) {
		//开始接收音频数据
		TUTKP2P.TK_getInstance().TK_client_startListener(mUID, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void stopListener(View view) {
		//停止接收音频数据
		TUTKP2P.TK_getInstance().TK_client_stopReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);
	}

	public void sendIOCtrl(View view) {
		//开始发送自定义command
		TUTKP2P.TK_getInstance().TK_client_sendIOCtrl(
				mUID,
				TUTKP2P.DEFAULT_CHANNEL,
				0x0900,
				new byte[]{0, 0, 0, 0});
	}
}
