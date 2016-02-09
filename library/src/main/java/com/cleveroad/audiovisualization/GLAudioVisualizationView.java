package com.cleveroad.audiovisualization;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Audio visualization view implementation for OpenGL.
 */
public class GLAudioVisualizationView extends GLSurfaceView implements AudioVisualization, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnCompletionListener, VisualizerWrapper.OnFftDataCaptureListener {

	private static final int EGL_VERSION = 2;
	private final GLRenderer renderer;
	private VisualizerWrapper visualizerWrapper;
	private final Configuration configuration;
	private MediaPlayer.OnPreparedListener innerOnPreparedListener;
	private MediaPlayer.OnCompletionListener innerOnCompletionListener;

	public GLAudioVisualizationView(Context context) {
		this(context, null);
	}

	public GLAudioVisualizationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		configuration = new Configuration(context, attrs, isInEditMode());
		renderer = new GLRenderer(getContext(), configuration);
		setEGLContextClientVersion(EGL_VERSION);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (visualizerWrapper != null) {
			visualizerWrapper.setEnabled(true);
		}
	}

	@Override
	public void onPause() {
		if (visualizerWrapper != null) {
			visualizerWrapper.setEnabled(false);
		}
		super.onPause();
	}

	@Override
	public void linkTo(int audioSessionId) {
		visualizerWrapper = new VisualizerWrapper(audioSessionId, this);
	}

	@Override
	public void linkTo(MediaPlayer mediaPlayer) {
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		linkTo(mediaPlayer.getAudioSessionId());
	}

	@Override
	public void setInnerOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
		this.innerOnPreparedListener = onPreparedListener;
	}

	@Override
	public void setInnerOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
		this.innerOnCompletionListener = onCompletionListener;
	}

	@Override
	public void release() {
		if (visualizerWrapper != null) {
			visualizerWrapper.release();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (innerOnCompletionListener != null) {
			innerOnCompletionListener.onCompletion(mp);
		}
		if (visualizerWrapper != null) {
			visualizerWrapper.setEnabled(false);
		}
		onFftDataCapture(new byte[Visualizer.getCaptureSizeRange()[1]]);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (innerOnPreparedListener != null) {
			innerOnPreparedListener.onPrepared(mp);
		}
		if (visualizerWrapper != null) {
			visualizerWrapper.setEnabled(true);
		}
	}

	@Override
	public void onFftDataCapture(byte[] fft) {
		renderer.updateData(fft);
	}

	/**
	 * Configuration holder class.
	 */
	static class Configuration {

		int wavesCount;
		int layersCount;
		float bubbleSize;
		float waveHeight;
		float footerHeight;
		boolean randomizeBubbleSize;
		float[] backgroundColor;
		float[][] layerColors;

		public Configuration(Context context, AttributeSet attrs, boolean isInEditMode) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GLAudioVisualizationView);
			int[] colors;
			int bgColor;
			try {
				layersCount = array.getInt(R.styleable.GLAudioVisualizationView_layers_count, Constants.DEFAULT_LAYERS_COUNT);
				layersCount = Utils.between(layersCount, Constants.MIN_LAYERS_COUNT, Constants.MAX_LAYERS_COUNT);
				wavesCount = array.getInt(R.styleable.GLAudioVisualizationView_waves_count, Constants.DEFAULT_WAVES_COUNT);
				wavesCount = Utils.between(wavesCount, Constants.MIN_WAVES_COUNT, Constants.MAX_WAVES_COUNT);
				waveHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_wave_height, (int) Constants.DEFAULT_WAVE_HEIGHT);
				waveHeight = Utils.between(waveHeight, Constants.MIN_WAVE_HEIGHT, Constants.MAX_WAVE_HEIGHT);
				bubbleSize = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_bubble_size, Constants.DEFAULT_BUBBLE_SIZE);
				bubbleSize = Utils.between(bubbleSize, Constants.MIN_BUBBLE_SIZE, Constants.MAX_BUBBLE_SIZE);
				randomizeBubbleSize = array.getBoolean(R.styleable.GLAudioVisualizationView_randomize_bubble_size, false);
				footerHeight = array.getDimensionPixelSize(R.styleable.GLAudioVisualizationView_footer_height, (int) Constants.DEFAULT_FOOTER_HEIGHT);
				footerHeight = Utils.between(footerHeight, Constants.MIN_FOOTER_HEIGHT, Constants.MAX_FOOTER_HEIGHT);
				bgColor = array.getColor(R.styleable.GLAudioVisualizationView_background_color, Color.TRANSPARENT);
				if (bgColor == Color.TRANSPARENT) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						bgColor = context.getResources().getColor(R.color.color_bg, context.getTheme());
					} else {
						//noinspection deprecation
						bgColor = context.getResources().getColor(R.color.color_bg);
					}
				}
				int arrayId = array.getResourceId(R.styleable.GLAudioVisualizationView_waves_colors, 0);
				if (arrayId == 0 && !isInEditMode) {
					colors = array.getResources().getIntArray(R.array.colors);
				} else {
					if (isInEditMode && arrayId == 0)
						arrayId = R.array.colors;
					String[] colorsArray = array.getResources().getStringArray(arrayId);
					colors = new int[colorsArray.length];
					for (int i = 0; i < colors.length; i++) {
						colors[i] = Color.parseColor(colorsArray[i]);
					}
				}
			} finally {
				array.recycle();
			}
			if (colors.length < layersCount) {
				throw new IllegalArgumentException("You specified more layers than colors.");
			}

			layerColors = new float[colors.length][];
			for (int i =0; i<colors.length; i++) {
				layerColors[i] = Utils.convertColor(colors[i]);
			}
			backgroundColor = Utils.convertColor(bgColor);
			bubbleSize /= context.getResources().getDisplayMetrics().widthPixels;
		}
	}
}
