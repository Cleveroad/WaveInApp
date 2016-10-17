package com.cleveroad.audiovisualization;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.NonNull;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL renderer implementation.
 */
class GLRenderer implements GLAudioVisualizationView.AudioVisualizationRenderer {

    private static final long ANIMATION_TIME = 400;
    private static final float D_ANGLE = (float) (2 * Math.PI / ANIMATION_TIME);

    private final GLAudioVisualizationView.Configuration mConfiguration;
    private final float mHeight;
    private final Random mRandom;
    private boolean mBgUpdated;
    private InnerAudioVisualization.CalmDownListener mCalmDownListener;
    private GLWaveLayer[] mGlWaveLayers;
    private float mRatioY = 1;
    private long mStartTime;

    GLRenderer(@NonNull Context context, GLAudioVisualizationView.Configuration configuration) {
        mConfiguration = configuration;
        mRandom = new Random();
        mStartTime = System.currentTimeMillis();
        mHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    GLRenderer calmDownListener(InnerAudioVisualization.CalmDownListener calmDownListener) {
        mCalmDownListener = calmDownListener;
        return this;
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * @param type       - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    final void onDataReceived(float[] dBmArray, float[] ampsArray) {
        if (mGlWaveLayers == null)
            return;
        for (int i = 0; i < mGlWaveLayers.length; i++) {
            if (mGlWaveLayers[i] == null)
                return;
            mGlWaveLayers[i].updateData(dBmArray[i], ampsArray[i]);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        float[] backgroundColor = mConfiguration.mBackgroundColor;
        GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        mGlWaveLayers = new GLWaveLayer[mConfiguration.mLayersCount];
        float layerHeightPerc = (mConfiguration.mFooterHeight + mConfiguration.mWaveHeight) / mHeight;
        float waveHeightPerc = mConfiguration.mWaveHeight / mHeight * 2;
        for (int i = 0; i < mGlWaveLayers.length; i++) {
            int reverseI = mGlWaveLayers.length - i - 1;
            float fromY = -1 + reverseI * waveHeightPerc * 2;
            float toY = fromY + layerHeightPerc * 2;
            mGlWaveLayers[i] = new GLWaveLayer(mConfiguration, mConfiguration.mLayerColors[i], fromY, toY, mRandom);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mRatioY = (float) width / height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBgUpdated) {
            float[] backgroundColor = mConfiguration.mBackgroundColor;
            GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
            mBgUpdated = false;
        } else {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }
        long endTime = System.currentTimeMillis();
        long dt = endTime - mStartTime;
        mStartTime = endTime;
        int i = 0;
        boolean isCalmedDown = true;
        for (GLWaveLayer layer : mGlWaveLayers) {
            // slow down speed of wave from top to bottom of screen
            float speedCoef = (1 - 1f * i / (mGlWaveLayers.length) * 0.8f);
            layer.update(dt, D_ANGLE * speedCoef, mRatioY);
            isCalmedDown &= layer.isCalmedDown();
            i++;
        }
        for (GLWaveLayer layer : mGlWaveLayers) {
            layer.draw();
        }
        if (isCalmedDown && mCalmDownListener != null) {
            mCalmDownListener.onCalmedDown();
        }
    }

    @Override
    public void updateConfiguration(@NonNull GLAudioVisualizationView.ColorsBuilder builder) {
        float[] bgColor = mConfiguration.mBackgroundColor;
        float[] backgroundColor = builder.getBackgroundColor();
        mBgUpdated = false;
        for (int i = 0; i < 4; i++) {
            mBgUpdated |= Float.compare(bgColor[i], backgroundColor[i]) != 0;
        }
        if (mBgUpdated) {
            mConfiguration.mBackgroundColor = builder.getBackgroundColor();
        }
        if (mGlWaveLayers == null)
            return;
        float[][] colors = builder.getLayerColors();
        for (int i = 0; i < mGlWaveLayers.length; i++) {
            mGlWaveLayers[i].setColor(colors[i]);
        }
    }
}