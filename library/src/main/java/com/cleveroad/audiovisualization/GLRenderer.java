package com.cleveroad.audiovisualization;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL renderer implementation.
 */
class GLRenderer implements GLSurfaceView.Renderer {

	/**
	 * Maximum value of dB. Used for controlling wave height percentage.
	 */
	private static final float MAX_DB_VALUE = 76;
	private static final long ANIMATION_TIME = 400;
	private static final float D_ANGLE = (float) (2 * Math.PI / ANIMATION_TIME);

	private final GLAudioVisualizationView.Configuration configuration;
	private GLWaveLayer[] layers;
	private long startTime;
	private final float height;
	private final Random random;
	private float ratioY = 1;

	public GLRenderer(@NonNull Context context, GLAudioVisualizationView.Configuration configuration) {
		this.configuration = configuration;
		this.random = new Random();
		startTime = System.currentTimeMillis();
		height = context.getResources().getDisplayMetrics().heightPixels;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		float[] backgroundColor = configuration.backgroundColor;
		GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
		layers = new GLWaveLayer[configuration.layersCount];
		float layerHeightPerc = (configuration.footerHeight + configuration.waveHeight) / height;
		float waveHeightPerc = configuration.waveHeight / height * 2;
		for (int i = 0; i < layers.length; i++) {
			int reverseI = layers.length - i - 1;
			float fromY = -1 + reverseI * waveHeightPerc * 2;
			float toY = fromY + layerHeightPerc * 2;
			layers[i] = new GLWaveLayer(configuration, configuration.layerColors[reverseI], fromY, toY, random);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		ratioY = (float) width / height;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		long endTime = System.currentTimeMillis();
		long dt = endTime - startTime;
		startTime = endTime;
		int i = 0;
		for (GLWaveLayer layer : layers) {
			// slow down speed of wave from top to bottom of screen
			float speedCoef = (1 - 1f * i / (layers.length) * 0.8f);
			layer.update(dt, D_ANGLE * speedCoef, ratioY);
			i++;
		}
		for (GLWaveLayer layer : layers) {
			layer.draw();
		}
	}

	/**
	 * Update waves data using latest FFT snapshot
	 * @param fft array of bytes containing the frequency representation
	 */
	public final void updateData(byte[] fft) {
		if (layers == null)
			return;
		// calculate dBs and amplitudes
		int dataSize = fft.length / 2 - 1;
		float[] dbs = new float[dataSize];
		float[] allAmps = new float[dataSize];
		for (int i = 0; i < dataSize; i++) {
			float re = fft[i];
			float im = fft[i + 1];
			float sqMag = re * re + im * im;
			dbs[i] = Utils.magnitudeToDb(sqMag);
			float k = 1;
			if (i == 0 || i == dataSize - 1) {
				k = 2;
			}
			allAmps[i] = (float) (k * Math.sqrt(sqMag) / dataSize);
		}
		int layersCount = layers.length;
		int size = dbs.length / layersCount;
		for (int i = 0; i < layersCount; i++) {
			int index = (int) ((i + 0.5f) * size);
			float db = dbs[index];
			float amp = allAmps[index];
			if (layers[i] == null)
				return;
			layers[i].updateData(db / MAX_DB_VALUE, amp);
		}
	}

	/**
	 * Utility method for compiling a OpenGL shader.
	 *
	 * @param type       - Vertex or fragment shader type.
	 * @param shaderCode - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}
}