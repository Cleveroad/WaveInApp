package com.cleveroad.audiovisualization;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

/**
 * DbmHandler implementation for visualizer.
 */
public class VisualizerDbmHandler extends DbmHandler<byte[]> implements VisualizerWrapper.OnFftDataCaptureListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    /**
     * Maximum value of dB. Used for controlling wave height percentage.
     */
    private static final float MAX_DB_VALUE = 76;
    private final float[] mCoefficients = new float[]{
            80 / 44100f,
            350 / 44100f,
            2500 / 44100f,
            10000 / 44100f,
    };
    private final VisualizerWrapper mVisualizerWrapper;
    private float[] mAllAmps;
    private float[] mDbs;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;

    VisualizerDbmHandler(@NonNull Context context, int audioSession) {
        mVisualizerWrapper = new VisualizerWrapper(context, audioSession, this);
    }

    VisualizerDbmHandler(@NonNull Context context, @NonNull MediaPlayer mediaPlayer) {
        this(context, mediaPlayer.getAudioSessionId());
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        calmDownAndStopRendering();
        mVisualizerWrapper.setEnabled(false);
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public void onFftDataCapture(byte[] fft) {
        onDataReceived(fft);
    }

    @Override
    public void onPause() {
        mVisualizerWrapper.setEnabled(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVisualizerWrapper.setEnabled(true);
    }

    @Override
    public void release() {
        super.release();
        mVisualizerWrapper.release();
    }

    @Override
    protected void onDataReceivedImpl(byte[] fft, int layersCount, @NonNull float[] dBmArray, @NonNull float[] ampArray) {
        // calculate dBs and amplitudes
        int dataSize = fft.length / 2 - 1;
        if (mDbs == null || mDbs.length != dataSize) {
            mDbs = new float[dataSize];
        }
        if (mAllAmps == null || mAllAmps.length != dataSize) {
            mAllAmps = new float[dataSize];
        }
        for (int i = 0; i < dataSize; i++) {
            float re = fft[2 * i];
            float im = fft[2 * i + 1];
            float sqMag = re * re + im * im;
            mDbs[i] = Utils.magnitudeToDb(sqMag);
            float k = 1;
            if (i == 0 || i == dataSize - 1) {
                k = 2;
            }
            mAllAmps[i] = (float) (k * Math.sqrt(sqMag) / dataSize);
        }
        for (int i = 0; i < layersCount; i++) {
            int index = (int) (mCoefficients[i] * fft.length);
            float db = mDbs[index];
            float amp = mAllAmps[index];
            dBmArray[i] = db / MAX_DB_VALUE;
            ampArray[i] = amp;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startRendering();
        mVisualizerWrapper.setEnabled(true);
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mp);
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        mOnPreparedListener = onPreparedListener;
    }
}
