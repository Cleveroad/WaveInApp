package com.cleveroad.audiovisualization;

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

    private final VisualizerWrapper visualizerWrapper;
    private float[] dbs;
    private float[] allAmps;
    private MediaPlayer.OnPreparedListener innerOnPreparedListener;
    private MediaPlayer.OnCompletionListener innerOnCompletionListener;

    VisualizerDbmHandler(int audioSession) {
        visualizerWrapper = new VisualizerWrapper(audioSession, this);
    }

    VisualizerDbmHandler(@NonNull MediaPlayer mediaPlayer) {
        this(mediaPlayer.getAudioSessionId());
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onDataReceivedImpl(byte[] fft, int layersCount, float[] dBmArray, float[] ampArray) {
        // calculate dBs and amplitudes
        int dataSize = fft.length / 2 - 1;
        if (dbs == null || dbs.length != dataSize) {
            dbs = new float[dataSize];
        }
        if (allAmps == null || allAmps.length != dataSize) {
            allAmps = new float[dataSize];
        }
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
        int size = dbs.length / layersCount;
        for (int i = 0; i < layersCount; i++) {
            int index = (int) ((i + 0.5f) * size);
            float db = dbs[index];
            float amp = allAmps[index];
            dBmArray[i] = db / MAX_DB_VALUE;
            ampArray[i] = amp;
        }
    }

    @Override
    public void onFftDataCapture(byte[] fft) {
        onDataReceived(fft);
    }

    @Override
    public void onResume() {
        super.onResume();
        visualizerWrapper.setEnabled(true);
    }

    @Override
    public void onPause() {
        visualizerWrapper.setEnabled(false);
        super.onPause();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        calmDownAndStopRendering();
        visualizerWrapper.setEnabled(false);
        if (innerOnCompletionListener != null) {
            innerOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startRendering();
        visualizerWrapper.setEnabled(true);
        if (innerOnPreparedListener != null) {
            innerOnPreparedListener.onPrepared(mp);
        }
    }

    public void setInnerOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
        this.innerOnPreparedListener = onPreparedListener;
    }

    public void setInnerOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
        this.innerOnCompletionListener = onCompletionListener;
    }
}
