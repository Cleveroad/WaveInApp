package com.cleveroad.audiovisualization;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.annotation.NonNull;

/**
 * Wrapper for visualizer.
 */
class VisualizerWrapper {

    private static final long WAIT_UNTIL_HACK = 500;
    private Visualizer.OnDataCaptureListener mOnDataCaptureListener;
    private int mCaptureRate;
    private long mLastZeroArrayTimestamp;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;

    VisualizerWrapper(@NonNull Context context, int audioSessionId, @NonNull final OnFftDataCaptureListener onFftDataCaptureListener) {
        mMediaPlayer = MediaPlayer.create(context, R.raw.av_workaround_1min);
        mVisualizer = new Visualizer(audioSessionId);
        mVisualizer.setEnabled(false);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mCaptureRate = Visualizer.getMaxCaptureRate();
        mOnDataCaptureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                boolean allZero = Utils.allElementsAreZero(fft);
                if (mLastZeroArrayTimestamp == 0) {
                    if (allZero) {
                        mLastZeroArrayTimestamp = System.currentTimeMillis();
                    }
                } else {
                    if (!allZero) {
                        mLastZeroArrayTimestamp = 0;
                    } else if (System.currentTimeMillis() - mLastZeroArrayTimestamp >= WAIT_UNTIL_HACK) {
                        setEnabled(true);
                        mLastZeroArrayTimestamp = 0;
                    }
                }
                onFftDataCaptureListener.onFftDataCapture(fft);
            }
        };
        mVisualizer.setEnabled(true);
    }

    public void release() {
        mVisualizer.setEnabled(false);
        mVisualizer.release();
        mVisualizer = null;
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void setEnabled(final boolean enabled) {
        if (mVisualizer == null) return;
        mVisualizer.setEnabled(false);
        if (enabled) {
            mVisualizer.setDataCaptureListener(mOnDataCaptureListener, mCaptureRate, false, true);
        } else {
            mVisualizer.setDataCaptureListener(null, mCaptureRate, false, false);
        }
        mVisualizer.setEnabled(true);
    }

    interface OnFftDataCaptureListener {
        void onFftDataCapture(byte[] fft);
    }
}
