package com.cleveroad.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;

/**
 * Fragment with visualization of audio recording.
 */
public class AudioRecordingFragment extends Fragment {

    public static AudioRecordingFragment newInstance() {
        return new AudioRecordingFragment();
    }

    private AudioVisualization audioVisualization;
    private Button btnRecord;
    private AudioRecordingDbmHandler handler;
    private AudioRecorder audioRecorder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_recording, container, false);
        audioVisualization = (AudioVisualization) view.findViewById(R.id.visualizer_view);
        btnRecord = (Button) view.findViewById(R.id.btn_record);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioRecorder = new AudioRecorder();
        handler = new AudioRecordingDbmHandler();
        audioRecorder.recordingCallback(handler);
        audioVisualization.linkTo(handler);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioRecorder.isRecording()) {
                    audioRecorder.finishRecord();
                    btnRecord.setText(R.string.start_recording);
                    handler.stop();
                } else {
                    btnRecord.setText(R.string.stop_recording);
                    audioRecorder.startRecord();
                }
            }
        });
    }

    private static class AudioRecordingDbmHandler extends DbmHandler<byte[]> implements AudioRecorder.RecordingCallback {

        private static final float MAX_DB_VALUE = 170;

        private float[] dbs;
        private float[] allAmps;

        @Override
        protected void onDataReceivedImpl(byte[] bytes, int layersCount, float[] dBmArray, float[] ampsArray) {

            final int bytesPerSample = 2; // As it is 16bit PCM
            final double amplification = 100.0; // choose a number as you like
            Complex[] fft = new Complex[bytes.length / bytesPerSample];
            for (int index = 0, floatIndex = 0; index < bytes.length - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
                double sample = 0;
                for (int b = 0; b < bytesPerSample; b++) {
                    int v = bytes[index + b];
                    if (b < bytesPerSample - 1) {
                        v &= 0xFF;
                    }
                    sample += v << (b * 8);
                }
                double sample32 = amplification * (sample / 32768.0);
                fft[floatIndex] = new Complex(sample32, 0);
            }
            fft = FFT.fft(fft);
            // calculate dBs and amplitudes
            int dataSize = fft.length / 2 - 1;
            if (dbs == null || dbs.length != dataSize) {
                dbs = new float[dataSize];
            }
            if (allAmps == null || allAmps.length != dataSize) {
                allAmps = new float[dataSize];
            }

            for (int i = 0; i < dataSize; i++) {
                dbs[i] = (float) fft[i].abs();
                float k = 1;
                if (i == 0 || i == dataSize - 1) {
                    k = 2;
                }
                float re = (float) fft[2 * i].re();
                float im = (float) fft[2 * i + 1].im();
                float sqMag = re * re + im * im;
                allAmps[i] = (float) (k * Math.sqrt(sqMag) / dataSize);
            }
            int size = dbs.length / layersCount;
            for (int i = 0; i < layersCount; i++) {
                int index = (int) ((i + 0.5f) * size);
                float db = dbs[index];
                float amp = allAmps[index];
                dBmArray[i] = db > MAX_DB_VALUE ? 1 : db / MAX_DB_VALUE;
                ampsArray[i] = amp;
            }
        }

        public void stop() {
            calmDownAndStopRendering();
        }

        @Override
        public void onDataReady(byte[] data) {
            onDataReceived(data);
        }
    }
}
