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

	private static final long ANIMATION_TIME = 400;
	private static final float D_ANGLE = (float) (2 * Math.PI / ANIMATION_TIME);

	private final GLAudioVisualizationView.Configuration configuration;
	private GLWaveLayer[] layers;
	private long startTime;
	private final float height;
	private final Random random;
	private float ratioY = 1;
    private InnerAudioVisualization.CalmDownListener calmDownListener;

	public GLRenderer(@NonNull Context context, GLAudioVisualizationView.Configuration configuration) {
		this.configuration = configuration;
		this.random = new Random();
		startTime = System.currentTimeMillis();
		height = context.getResources().getDisplayMetrics().heightPixels;
	}

    public GLRenderer calmDownListener(InnerAudioVisualization.CalmDownListener calmDownListener) {
        this.calmDownListener = calmDownListener;
        return this;
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
        boolean isCalmedDown = true;
		for (GLWaveLayer layer : layers) {
			// slow down speed of wave from top to bottom of screen
			float speedCoef = (1 - 1f * i / (layers.length) * 0.8f);
			layer.update(dt, D_ANGLE * speedCoef, ratioY);
            isCalmedDown &= layer.isCalmedDown();
			i++;
		}
		for (GLWaveLayer layer : layers) {
			layer.draw();
		}
        if (isCalmedDown && calmDownListener != null) {
            calmDownListener.onCalmedDown();
        }
	}

    public final void onDataReceived(float[] dBmArray, float[] ampsArray) {
        if (layers == null)
            return;
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] == null)
                return;
            layers[i].updateData(dBmArray[i], ampsArray[i]);
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