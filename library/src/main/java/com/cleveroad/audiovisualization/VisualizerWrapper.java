package com.cleveroad.audiovisualization;

import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Wrapper for visualizer.
 */
class VisualizerWrapper {

	private Visualizer visualizer;

	public VisualizerWrapper(int audioSessionId, @NonNull final OnFftDataCaptureListener onFftDataCaptureListener) {
		visualizer = new Visualizer(audioSessionId);
        visualizer.setEnabled(false);
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
			@Override
			public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

			}

			@Override
			public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
				onFftDataCaptureListener.onFftDataCapture(fft);
			}
		}, Visualizer.getMaxCaptureRate(), false, true);
	}

	public void release() {
		visualizer.release();
		visualizer = null;
	}

	public void setEnabled(final boolean enabled) {
        if (visualizer.getEnabled() != enabled) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int res = visualizer.setEnabled(enabled);
                    Log.d("TEST", "result: " + res);
                }
            }, 500);
        }
	}

	public interface OnFftDataCaptureListener {
		void onFftDataCapture(byte[] fft);
	}
}
