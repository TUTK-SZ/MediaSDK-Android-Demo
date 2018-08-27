package com.zed.demo.link;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.St_AvStatus;
import com.tutk.IOTC.St_SInfo;
import com.tutk.libTUTKMedia.CameraDecodePreview;
import com.tutk.libTUTKMedia.CameraEncodePreview;
import com.tutk.libTUTKMedia.CameraEncodePreview2;
import com.tutk.libTUTKMedia.TUTKMedia;
import com.tutk.libTUTKMedia.inner.OnMediaListener;
import com.tutk.libTUTKMedia.utils.MediaCodecUtils;
import com.tutk.p2p.TUTKP2P;
import com.tutk.p2p.inner.OnP2PDeviceListener;
import com.tutk.p2p.utils.P2PUtils;
import com.zed.demo.R;
import com.zed.demo.ToastUtils;

import java.io.File;

/**
 * @author zed
 * @date 2018/7/26 上午10:40
 * @desc
 */

public class DeviceActivity extends Activity {

	private static final String TAG = "ClientActivity";

	//控件唯一标识
	private static final int MEDIA_TAG = 1;

	private int mSID = -1;

	private String mUID = "CDKA8H4CU7R4GGPGUHC1";//tutk
	private String mPassword = TUTKP2P.DEFAULT_PASSWORD;

	private TextView tv_rtt;

	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device);

		final EditText edit_uid = findViewById(R.id.edit_uid);
		edit_uid.setText(mUID);

		tv_rtt = findViewById(R.id.tv_rtt);

		final SeekBar seek_bar = findViewById(R.id.seek_bar);
		seek_bar.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP: {
						int action = event.getAction();
						Log.w("zed_test", " action = " + action);
						int progress = seek_bar.getProgress();
						TUTKMedia.TK_getInstance().TK_decode_setAecGain(progress,6);
						if (toast == null) {
							toast = Toast.makeText(DeviceActivity.this, progress + "", Toast.LENGTH_SHORT);
							toast.show();
						} else {
							toast.setText(progress + "");
							toast.show();
						}
					}
					break;
				}
				return false;
			}
		});

		TUTKP2P.TK_getInstance().TK_registerDeviceListener(mDeviceListener);
		TUTKMedia.TK_getInstance().TK_registerListener(mOnMediaListener);

		//开始音频采集编码
		TUTKMedia.TK_getInstance().TK_audio_startCapture(
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);

		//开始音频解码播放
		TUTKMedia.TK_getInstance().TK_decode_startDecodeAudio(
				MEDIA_TAG,
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* p2p */
		TUTKP2P.TK_getInstance().TK_unRegisterDeviceListener(mDeviceListener);
		TUTKP2P.TK_getInstance().TK_device_disConnectAll();
		TUTKP2P.TK_getInstance().TK_device_logout();

		/* media */
		TUTKMedia.TK_getInstance().TK_unRegisterListener(mOnMediaListener);
		TUTKMedia.TK_getInstance().TK_encode_unBindView();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllVideo();
		TUTKMedia.TK_getInstance().TK_audio_stopCapture();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllAudio();
	}

	private OnP2PDeviceListener mDeviceListener = new OnP2PDeviceListener() {
		@Override
		public void receiveOnLineInfo(String checkUID, int result) {

		}

		@Override
		public void receiveIOTCListenInfo(int sid) {
			mSID = sid;
			ToastUtils.showToast(DeviceActivity.this, "收到client连线");
		}

		@Override
		public void receiveAvServerStart(int sid, int avIndex, int state) {
			if (avIndex >= 0) {
				ToastUtils.showToast(DeviceActivity.this, "连接成功 avNewServStart = " + avIndex);
			} else {
				ToastUtils.showToast(DeviceActivity.this, "连接失败 avNewServStart = " + avIndex);
			}
		}

		@Override
		public void receiveSessionCheckInfo(int sid, St_SInfo info, int result) {
			if (result == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE
					|| result == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT) {
				ToastUtils.showToast(DeviceActivity.this, "对方掉线");
				finish();
			}
		}

		@Override
		public void receiveStatusCheckInfo(final int sid, final St_AvStatus status, int result) {
			if (result == P2PUtils.STATUS_CHECK_BLOCK) {
//				ToastUtils.showToast(DeviceActivity.this, "网络阻塞 延时严重");
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tv_rtt.setText(String.valueOf(status.RoundTripTime));
				}
			});
		}

		@Override
		public void receiveIOCtrlDataInfo(int sid, int avIOCtrlMsgType, byte[] data) {
			//收到client回传的command
			ToastUtils.showToast(DeviceActivity.this, "收到command " + Integer.toHexString(avIOCtrlMsgType));
		}

		@Override
		public void sendIOCtrlDataInfo(int sid, int avIOCtrlMsgType, int result, byte[] data) {

		}

		@Override
		public void receiveVideoInfo(int sid, byte[] videoData, long timeStamp, int number, int onlineNum, boolean isIFrame) {
			//将P2P模块收到的视频数据发送给Media模块
			TUTKMedia.TK_getInstance().TK_decode_onReceiveVideoData(MEDIA_TAG, videoData, timeStamp, number, isIFrame);

		}

		@Override
		public void receiveAudioInfo(int sid, byte[] audioData, long timeStamp, int number) {
			//将P2P模块收到的音频数据发送给Media模块
			TUTKMedia.TK_getInstance().TK_decode_onReceiveAndPlayAudio(MEDIA_TAG, audioData, audioData.length, timeStamp);
		}
	};

	private OnMediaListener mOnMediaListener = new OnMediaListener() {
		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview cameraEncodePreview) {
			//视频采集编码
			TUTKMedia.TK_getInstance().TK_encode_bindView(cameraEncodePreview);
			TUTKMedia.TK_getInstance().TK_preview_setScaleType(MediaCodecUtils.SCALE_ASPECT_FILL);
			TUTKMedia.TK_getInstance().TK_preview_startCapture(MediaCodecUtils.MEDIA_CODEC_H264, false);
		}

		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview2 cameraEncodePreview2) {

		}

		@Override
		public void onVideoEncodeCallback(byte[] data, int width, int height, boolean isIFrame, long timeStamp) {
			//将视频编码的数据发送给P2P模块
			TUTKP2P.TK_getInstance().TK_device_onSendVideoData(mSID, data, isIFrame, timeStamp);
		}

		@Override
		public void onVideoEncodeSnapshot(boolean success) {

		}

		@Override
		public void onVideoDecodePreviewInit(CameraDecodePreview cameraDecodePreview) {
			//开始视频数据的解码播放
			TUTKMedia.TK_getInstance().TK_decode_startDecodeVideo(cameraDecodePreview, MEDIA_TAG,
					MediaCodecUtils.MEDIA_CODEC_H264, false);
		}

		@Override
		public void onVideoDecodeCallback(Object videoTag, byte[] data, int width, int height) {

		}

		@Override
		public void onVideoDecodeSnapshot(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(DeviceActivity.this, "拍照成功");
			} else {
				ToastUtils.showToast(DeviceActivity.this, "没有图像 拍照失败");
			}
		}

		@Override
		public void onVideoDecodeRecord(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(DeviceActivity.this, "开始录像");
			} else {
				TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
				ToastUtils.showToast(DeviceActivity.this, "没有图像,录像失败");
			}
		}

		@Override
		public void onVideoDecodeUnSupport(final CameraDecodePreview cameraDecodePreview) {
			//硬解码不支持 切到软解码
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TUTKMedia.TK_getInstance().TK_decode_stopDecodeVideo(cameraDecodePreview.getTag());
					TUTKMedia.TK_getInstance().TK_decode_startDecodeVideo(cameraDecodePreview, cameraDecodePreview.getTag(),
							MediaCodecUtils.MEDIA_CODEC_H264, true);
				}
			});
		}

		@Override
		public void onVideoEncodeUnSupport() {
			//硬编码不支持 切到软编码
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TUTKMedia.TK_getInstance().TK_preview_stopCapture();
					TUTKMedia.TK_getInstance().TK_preview_startCapture(MediaCodecUtils.MEDIA_CODEC_H264, true);
				}
			});
		}

		@Override
		public void onAudioEncodeCallback(byte[] data, int encodeLength, long timeStamp) {
			//将音频编码的数据发送给P2P模块
			TUTKP2P.TK_getInstance().TK_device_onSendAudioData(mSID, data, encodeLength, timeStamp);
		}

		@Override
		public void onAudioDecodeCallback(Object audioTag, byte[] data, int encodeLength, int audioID, int simpleRate, int bit) {

		}
	};

	public void login(View view) {
		//设备登陆
		EditText edit_uid = findViewById(R.id.edit_uid);
		String UID = edit_uid.getText().toString().trim();
		int login = TUTKP2P.TK_getInstance().TK_device_login(UID, mPassword);
		if (login >= 0) {
			ToastUtils.showToast(DeviceActivity.this, "登陆成功");
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

	public void snapshot(View view) {
		//拍照
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_snapShot(MEDIA_TAG, path + "/device_snapshot.png");
	}


	public void startRecord(View view) {
		//开始录像
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_startRecord(MEDIA_TAG, path + "/device_record.mp4", false);
	}

	public void stopRecord(View view) {
		//停止录像
		TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
	}

}
