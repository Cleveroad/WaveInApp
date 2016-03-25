package com.cleveroad.audiovisualization;

import android.media.audiofx.Visualizer;
import android.support.annotation.NonNull;

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
        visualizer.setEnabled(true);
	}

	public void release() {
		visualizer.release();
		visualizer = null;
	}

	public void setEnabled(boolean enabled) {
		visualizer.setEnabled(enabled);
	}

	public interface OnFftDataCaptureListener {
		void onFftDataCapture(byte[] fft);
	}
}
