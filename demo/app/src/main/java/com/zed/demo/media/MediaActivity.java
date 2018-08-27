package com.zed.demo.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.tutk.libTUTKMedia.CameraDecodePreview;
import com.tutk.libTUTKMedia.CameraEncodePreview;
import com.tutk.libTUTKMedia.CameraEncodePreview2;
import com.tutk.libTUTKMedia.TUTKMedia;
import com.tutk.libTUTKMedia.inner.OnMediaListener;
import com.tutk.libTUTKMedia.utils.MediaCodecUtils;
import com.zed.demo.R;
import com.zed.demo.ToastUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * @author zed
 * @date 2018/7/26 下午2:23
 * @desc
 */

public class MediaActivity extends Activity {

	public static final float[] BITRATE = new float[]{
			MediaCodecUtils.PREVIEW_BITRATE_LOWEST,
			MediaCodecUtils.PREVIEW_BITRATE_LOW,
			MediaCodecUtils.PREVIEW_BITRATE_MIDDLE,
			MediaCodecUtils.PREVIEW_BITRATE_HIGH,
			MediaCodecUtils.PREVIEW_BITRATE_HIGHEST,
	};

	public static final int[][] RESOLUTION = new int[][]{
			MediaCodecUtils.PREVIEW_RESOLUTION_LOW,
			MediaCodecUtils.PREVIEW_RESOLUTION_MIDDLE,
			MediaCodecUtils.PREVIEW_RESOLUTION_HIGH,

	};

	public static final int[] FPS = new int[]{
			MediaCodecUtils.PREVIEW_FPS_LOWEST,
			MediaCodecUtils.PREVIEW_FPS_LOW,
			MediaCodecUtils.PREVIEW_FPS_MIDDLE,
			MediaCodecUtils.PREVIEW_FPS_HIGH,
			MediaCodecUtils.PREVIEW_FPS_HIGHEST,
	};

	public static final int[] GOP = new int[]{
			MediaCodecUtils.PREVIEW_GOP_LOW,
			MediaCodecUtils.PREVIEW_GOP_MIDDLE,
			MediaCodecUtils.PREVIEW_GOP_HIGH,
	};

	//控件唯一标识
	private static final int MEDIA_TAG = 1;

	private boolean isSoftEncode = false;
	private boolean isSoftDecode = false;

	private final String ENCODE_TYPE = MediaCodecUtils.MEDIA_CODEC_H264;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_media);

		findViewById(R.id.btn_resolution).setTag(0);
		findViewById(R.id.btn_bitrate).setTag(2);
		findViewById(R.id.btn_fps).setTag(0);
		findViewById(R.id.btn_gop).setTag(1);


		TUTKMedia.TK_getInstance().TK_registerListener(mOnMediaListener);

		//开始音频解码播放
		TUTKMedia.TK_getInstance().TK_decode_startDecodeAudio(
				MEDIA_TAG,
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);

		//开始音频采集编码
		TUTKMedia.TK_getInstance().TK_audio_startCapture(
				MediaCodecUtils.TUTK_AUDIO_SAMPLE_RATE_8K,
				MediaCodecUtils.TUTK_AUDIO_BIT_16,
				MediaCodecUtils.TUTK_AUDIO_AAC_ADTS);


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		TUTKMedia.TK_getInstance().TK_unRegisterListener(mOnMediaListener);
		TUTKMedia.TK_getInstance().TK_encode_unBindView();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllVideo();
		TUTKMedia.TK_getInstance().TK_audio_stopCapture();
		TUTKMedia.TK_getInstance().TK_decode_stopDecodeAllAudio();
	}

	private OnMediaListener mOnMediaListener = new OnMediaListener() {

		private int number;

		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview cameraEncodePreview) {
			//视频采集编码
			TUTKMedia.TK_getInstance().TK_encode_bindView(cameraEncodePreview);
			TUTKMedia.TK_getInstance().TK_preview_setScaleType(MediaCodecUtils.SCALE_ASPECT_FILL);
			TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);
		}

		@Override
		public void onVideoEncodePreviewInit(CameraEncodePreview2 cameraEncodePreview2) {

		}

		@Override
		public void onVideoEncodeCallback(byte[] data, int width, int height, boolean isIFrame, long timeStamp) {
			//将视频编码的数据进行解码
			TUTKMedia.TK_getInstance().TK_decode_onReceiveVideoData(MEDIA_TAG, data, timeStamp, ++number, isIFrame);
		}

		@Override
		public void onVideoEncodeSnapshot(boolean success) {

		}

		@Override
		public void onVideoDecodePreviewInit(final CameraDecodePreview cameraDecodePreview) {
			//开始视频数据的解码播放
			TUTKMedia.TK_getInstance().TK_decode_startDecodeVideo(cameraDecodePreview, MEDIA_TAG,
					ENCODE_TYPE, isSoftDecode);
		}

		@Override
		public void onVideoDecodeCallback(Object videoTag, byte[] data, int width, int height) {
		}

		@Override
		public void onVideoDecodeSnapshot(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(MediaActivity.this, "拍照成功");
			} else {
				ToastUtils.showToast(MediaActivity.this, "没有图像 拍照失败");
			}
		}

		@Override
		public void onVideoDecodeRecord(Object videoTag, boolean success) {
			if (success) {
				ToastUtils.showToast(MediaActivity.this, "开始录像");
			} else {
				TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
				ToastUtils.showToast(MediaActivity.this, "没有图像,录像失败");
			}
		}

		@Override
		public void onVideoDecodeUnSupport(final CameraDecodePreview cameraDecodePreview) {
			//硬解码不支持 切到软解码
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					isSoftDecode = true;
					TUTKMedia.TK_getInstance().TK_decode_stopDecodeVideo(cameraDecodePreview.getTag());
					TUTKMedia.TK_getInstance().TK_decode_startDecodeVideo(cameraDecodePreview, cameraDecodePreview.getTag(),
							ENCODE_TYPE, isSoftDecode);
					TextView tv_decode = findViewById(R.id.tv_decode);
					tv_decode.setText("解码:Soft");
				}
			});
		}

		@Override
		public void onVideoEncodeUnSupport() {
			//硬编码不支持 切到软编码
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					isSoftEncode = true;
					TUTKMedia.TK_getInstance().TK_preview_stopCapture();
					TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);
					TextView tv_encode = findViewById(R.id.tv_encode);
					tv_encode.setText("编码:Soft");
				}
			});
		}

		@Override
		public void onAudioEncodeCallback(byte[] data, int encodeLength, long timeStamp) {
			//将音频编码的数据进行解码
			byte[] audioData = new byte[encodeLength];
			System.arraycopy(data, 0, audioData, 0, encodeLength);
			TUTKMedia.TK_getInstance().TK_decode_onReceiveAndPlayAudio(MEDIA_TAG, audioData, encodeLength, timeStamp);
		}

		@Override
		public void onAudioDecodeCallback(Object audioTag, byte[] data, int encodeLength, int audioID, int simpleRate, int bit) {

		}
	};

	public void switchCamera(View view) {
		int cameraCount = Camera.getNumberOfCameras();
		if (cameraCount == 1) {
			return;
		}
		int position = TUTKMedia.TK_getInstance().TK_preview_getCamera();
		if (position == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			TUTKMedia.TK_getInstance().TK_preview_switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
		} else {
			TUTKMedia.TK_getInstance().TK_preview_switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
		}
	}


	int audio = 0;

	public void audioUp(View view) {
		if (audio == 10) {
			return;
		}
		TUTKMedia.TK_getInstance().TK_decode_setPlayVolume(++audio);
		TextView tv_audio = findViewById(R.id.tv_audio);
		tv_audio.setText("音频增益:" + audio);
	}

	public void audioDown(View view) {
		if (audio == 0) {
			return;
		}
		TUTKMedia.TK_getInstance().TK_decode_setPlayVolume(--audio);
		TextView tv_audio = findViewById(R.id.tv_audio);
		tv_audio.setText("音频增益:" + audio);
	}


	public void snapshot(View view) {
		//拍照
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_snapShot(MEDIA_TAG, path + "/snapshot.png");
	}


	public void startRecord(View view) {
		//开始录像
		String path = Environment.getExternalStorageDirectory().getPath() + "/MediaSDK";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		TUTKMedia.TK_getInstance().TK_video_startRecord(MEDIA_TAG, path + "/record.mp4", false);
	}

	public void stopRecord(View view) {
		//停止录像
		TUTKMedia.TK_getInstance().TK_video_stopRecord(MEDIA_TAG);
	}

	private boolean speakerphoneOn = true;

	public void changeSpeakerphone(View view) {
		TUTKMedia.TK_getInstance().TK_audio_setSpeakerphoneOn(!speakerphoneOn);
		speakerphoneOn = !speakerphoneOn;
	}

	public void changeGOP(View view) {

		final ArrayList<String> mStrings = new ArrayList<>();

		for (int gop : GOP) {
			mStrings.add(gop + "");
		}

		final Button button = findViewById(view.getId());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("gop")
				.setSingleChoiceItems(mStrings.toArray(new String[mStrings.size()]),
						(int) button.getTag(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								button.setText("gop:" + mStrings.get(which));
								button.setTag(which);

								TUTKMedia.TK_getInstance().TK_preview_changeQuality(
										TUTKMedia.TK_getInstance().TK_preview_getResolution(),
										TUTKMedia.TK_getInstance().TK_preview_getBitrate(),
										TUTKMedia.TK_getInstance().TK_preview_getFPS(),
										GOP[which]);

								TUTKMedia.TK_getInstance().TK_preview_stopCapture();
								TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);

							}
						});

		builder.show();
	}

	public void changeFPS(View view) {
		final ArrayList<String> mStrings = new ArrayList<>();

		for (int fps : FPS) {
			mStrings.add(fps + "");
		}

		final Button button = findViewById(view.getId());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("fps")
				.setSingleChoiceItems(mStrings.toArray(new String[mStrings.size()]),
						(int) button.getTag(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								button.setText("fps:" + mStrings.get(which));
								button.setTag(which);

								TUTKMedia.TK_getInstance().TK_preview_changeQuality(
										TUTKMedia.TK_getInstance().TK_preview_getResolution(),
										TUTKMedia.TK_getInstance().TK_preview_getBitrate(),
										FPS[which],
										TUTKMedia.TK_getInstance().TK_preview_getGOP());

								TUTKMedia.TK_getInstance().TK_preview_stopCapture();
								TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);

							}
						});

		builder.show();
	}

	public void changeBitrate(View view) {
		final ArrayList<String> mStrings = new ArrayList<>();

		for (Float bitrate : BITRATE) {
			mStrings.add(bitrate + "");
		}

		final Button button = findViewById(view.getId());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("bitrate")
				.setSingleChoiceItems(mStrings.toArray(new String[mStrings.size()]),
						(int) button.getTag(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								button.setText("bitrate:" + mStrings.get(which));
								button.setTag(which);

								TUTKMedia.TK_getInstance().TK_preview_changeQuality(
										TUTKMedia.TK_getInstance().TK_preview_getResolution(),
										BITRATE[which],
										TUTKMedia.TK_getInstance().TK_preview_getFPS(),
										TUTKMedia.TK_getInstance().TK_preview_getGOP());

								TUTKMedia.TK_getInstance().TK_preview_stopCapture();
								TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);

							}
						});

		builder.show();
	}

	public void changeResolution(View view) {
		final ArrayList<String> mStrings = new ArrayList<>();

		for (int[] resolution : RESOLUTION) {
			mStrings.add(resolution[0] + "x" + resolution[1]);
		}

		final Button button = findViewById(view.getId());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("resolution")
				.setSingleChoiceItems(mStrings.toArray(new String[mStrings.size()]),
						(int) button.getTag(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								button.setText(mStrings.get(which));
								button.setTag(which);

								TUTKMedia.TK_getInstance().TK_preview_changeQuality(
										RESOLUTION[which],
										TUTKMedia.TK_getInstance().TK_preview_getBitrate(),
										TUTKMedia.TK_getInstance().TK_preview_getFPS(),
										TUTKMedia.TK_getInstance().TK_preview_getGOP());

								TUTKMedia.TK_getInstance().TK_preview_stopCapture();
								TUTKMedia.TK_getInstance().TK_preview_startCapture(ENCODE_TYPE, isSoftEncode);

							}
						});

		builder.show();
	}
}
