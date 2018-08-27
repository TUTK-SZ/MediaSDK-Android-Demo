package com.zed.demo.link;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

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
import com.tutk.p2p.inner.OnP2PClientListener;
import com.zed.demo.R;
import com.zed.demo.ToastUtils;

import java.io.File;

/**
 * @author zed
 * @date 2018/7/26 上午10:40
 * @desc
 */

public class ClientActivity extends Activity {

	private static final String TAG = "ClientActivity";

	//控件唯一标识
	private static final int MEDIA_TAG = 1;

	private CameraDecodePreview decode_preview;

	private CameraEncodePreview encode_preview;

	private String mUID = "CDKA8H4CU7R4GGPGUHC1";//tutk
	private String mPassword = TUTKP2P.DEFAULT_PASSWORD;

	private TextView tv_rtt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_client);

		tv_rtt = findViewById(R.id.tv_rtt);

		decode_preview = findViewById(R.id.decode_preview);
		encode_preview = findViewById(R.id.encode_preview);

		EditText edit_uid = findViewById(R.id.edit_uid);
		edit_uid.setText(mUID);

		//注册监听
		TUTKP2P.TK_getInstance().TK_registerClientListener(mClientListener);
		TUTKMedia.TK_getInstance().TK_registerListener(mOnMediaListener);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* p2p */
		TUTKP2P.TK_getInstance().TK_unRegisterClientListener(mClientListener);
		TUTKP2P.TK_getInstance().TK_client_disConnectAll();

		/* media */
		TUTKMedia.TK_getInstance().TK_unRegisterListener(mOnMediaListener);
		TUTKMedia.TK_getInstance().TK_encode_unBindView();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllVideo();
		TUTKMedia.TK_getInstance().TK_audio_stopCapture();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllAudio();
	}

	private OnP2PClientListener mClientListener = new OnP2PClientListener() {
		@Override
		public void receiveConnectInfo(String uid, int sid, int state) {
//			Log.i(TAG, " IOTC_Connect_ByUID_Parallel = " + i);
		}

		@Override
		public void receiveClientStartInfo(String uid, int avIndex, int state) {
			if (state == TUTKP2P.CONNECTION_STATE_CONNECTED) {
				ToastUtils.showToast(ClientActivity.this, "连接成功 avNewClientStart = " + avIndex);

				TUTKP2P.TK_getInstance().TK_client_sendIOCtrl(mUID, 0, 0x719,
						new byte[]{0, 0, 0, 0, 0, 0, 0, 0});

			} else if (state == TUTKP2P.CONNECTION_STATE_CONNECT_FAILED) {
				ToastUtils.showToast(ClientActivity.this, "连接失败 avNewClientStart = " + avIndex);
			}
		}

		@Override
		public void receiveSessionCheckInfo(String uid, St_SInfo info, int result) {

			if (result == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE
					|| result == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT) {
				ToastUtils.showToast(ClientActivity.this, "对方掉线");
				finish();
			}
		}

		@Override
		public void receiveStatusCheckInfo(String uid, final St_AvStatus status, int result) {
//			if (result == P2PUtils.STATUS_CHECK_BLOCK) {
//				ToastUtils.showToast(ClientActivity.this, "网络阻塞 延时严重");
//			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tv_rtt.setText(String.valueOf(status.RoundTripTime));
				}
			});
		}

		@Override
		public void receiveIOCtrlDataInfo(String uid, int avChannel, int avIOCtrlMsgType, byte[] data) {
			//收到设备回传的command
			ToastUtils.showToast(ClientActivity.this, "收到command " + Integer.toHexString(avIOCtrlMsgType));
		}

		@Override
		public void sendIOCtrlDataInfo(String uid, int avChannel, int avIOCtrlMsgType, int result, byte[] data) {

		}

		@Override
		public void receiveVideoInfo(String uid, int avChannel, byte[] videoData, long timeStamp, int number, int onlineNum, boolean isIFrame) {
			//将P2P模块收到的视频数据发送给Media模块
			TUTKMedia.TK_getInstance().TK_decode_onReceiveVideoData(MEDIA_TAG, videoData, timeStamp, number, isIFrame);

		}

		@Override
		public void receiveAudioInfo(String uid, int avChannel, byte[] audioData, long timeStamp, int number) {
			//将P2P模块收到的音频数据发送给Media模块
			TUTKMedia.TK_getInstance().TK_decode_onReceiveAndPlayAudio(MEDIA_TAG, audioData, audioData.length, timeStamp);
		}
	};

	private OnMediaListener mOnMediaListener = new OnMediaListener() {
		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview cameraEncodePreview) {

		}

		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview2 cameraEncodePreview2) {

		}

		@Override
		public void onVideoEncodeCallback(byte[] data, int width, int height, boolean isIFrame, long timeStamp) {
			//将视频编码的数据发送给P2P模块
			TUTKP2P.TK_getInstance().TK_client_onSendVideoData(mUID, TUTKP2P.DEFAULT_CHANNEL,
					data, isIFrame, timeStamp);
		}

		@Override
		public void onVideoEncodeSnapshot(boolean success) {

		}

		@Override
		public void onVideoDecodePreviewInit(CameraDecodePreview cameraDecodePreview) {

		}

		@Override
		public void onVideoDecodeCallback(Object videoTag, byte[] data, int width, int height) {

		}

		@Override
		public void onVideoDecodeSnapshot(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(ClientActivity.this, "拍照成功");
			} else {
				ToastUtils.showToast(ClientActivity.this, "没有图像 拍照失败");
			}
		}

		@Override
		public void onVideoDecodeRecord(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(ClientActivity.this, "开始录像");
			} else {
				TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
				ToastUtils.showToast(ClientActivity.this, "没有图像,录像失败");
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
			TUTKP2P.TK_getInstance().TK_client_onSendAudioData(mUID, TUTKP2P.DEFAULT_CHANNEL, data, encodeLength, timeStamp);
		}

		@Override
		public void onAudioDecodeCallback(Object audioTag, byte[] data, int encodeLength, int audioID, int simpleRate, int bit) {

		}
	};

	public void connect(View view) {
		//连接uid
		EditText edit_uid = findViewById(R.id.edit_uid);
		mUID = edit_uid.getText().toString().trim();
		TUTKP2P.TK_getInstance().TK_client_connect(mUID, mPassword, TUTKP2P.DEFAULT_CHANNEL);

	}

	public void disconnect(View view) {
		//断线uid
		TUTKP2P.TK_getInstance().TK_client_disConnect(mUID);
	}

	public void startSendVideo(View view) {
		//开始发送视频数据
		TUTKP2P.TK_getInstance().TK_client_startSendVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//开始视频采集编码
		TUTKMedia.TK_getInstance().TK_encode_bindView(encode_preview);
		TUTKMedia.TK_getInstance().TK_preview_setScaleType(MediaCodecUtils.SCALE_ASPECT_FILL);
		TUTKMedia.TK_getInstance().TK_preview_startCapture(MediaCodecUtils.MEDIA_CODEC_H264, false);
	}

	public void stopSendVideo(View view) {
		//停止发送视频数据
		TUTKP2P.TK_getInstance().TK_client_stopSendVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//停止视频采集编码
		TUTKMedia.TK_getInstance().TK_preview_stopCapture();
	}

	public void startReceiveVideo(View view) {
		//开始接收视频数据
		TUTKP2P.TK_getInstance().TK_client_startReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL, false);

		//开始视频数据的解码播放
		TUTKMedia.TK_getInstance().TK_decode_startDecodeVideo(decode_preview, MEDIA_TAG, MediaCodecUtils.MEDIA_CODEC_H264, false);

	}

	public void stopReceiveVideo(View view) {
		//停止接收视频数据
		TUTKP2P.TK_getInstance().TK_client_stopReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//停止视频数据的解码播放
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeVideo(MEDIA_TAG);
	}

	public void startSpeak(View view) {
		//开始发送音频数据
		TUTKP2P.TK_getInstance().TK_client_startSpeaking(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//开始音频采集编码
		TUTKMedia.TK_getInstance().TK_audio_startCapture(
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);
	}

	public void stopSpeak(View view) {
		//停止发送音频数据
		TUTKP2P.TK_getInstance().TK_client_stopSpeaking(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//停止音频采集编码
		TUTKMedia.TK_getInstance().TK_audio_stopCapture();
	}

	public void startListener(View view) {
		//开始接收音频数据
		TUTKP2P.TK_getInstance().TK_client_startListener(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//开始音频解码播放
		TUTKMedia.TK_getInstance().TK_decode_startDecodeAudio(
				MEDIA_TAG,
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);
	}

	public void stopListener(View view) {
		//停止接收音频数据
		TUTKP2P.TK_getInstance().TK_client_stopReceiveVideo(mUID, TUTKP2P.DEFAULT_CHANNEL);

		//停止音频解码播放
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAudio(MEDIA_TAG);
	}

	byte xxx = 1;

	public void sendIOCtrl(View view) {
//		if(xxx == 3){
//			xxx = 1;
//		}else if(xxx == 5){
//			xxx = 1;
//		}else if(xxx == 1){
//			xxx = 3;
//		}
		//开始发送自定义command
		TUTKP2P.TK_getInstance().TK_client_sendIOCtrl(
				mUID,
				TUTKP2P.DEFAULT_CHANNEL,
				0x0320,
				new byte[]{0, 0, 0, 0, xxx, 0, 0, 0});
	}

	public void snapshot(View view) {
		//拍照
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_snapShot(MEDIA_TAG, path + "/client_snapshot.png");
	}

	public void startRecord(View view) {
		//开始录像
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_startRecord(MEDIA_TAG, path + "/client_record.mp4", false);
	}

	public void stopRecord(View view) {
		//停止录像
		TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
	}

}
