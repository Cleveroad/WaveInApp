package com.cleveroad.audiovisualization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Implementation of {@link DbmHandler} for {@link SpeechRecognizer}.
 * <b>Note:</b> make sure you're calling {@link AudioVisualization#onPause()} when
 * {@link RecognitionListener#onEndOfSpeech()} event called to stop visualizing.
 */
public class SpeechRecognizerDbmHandler extends DbmHandler<Float> implements RecognitionListener {

    private static final float MIN_RMS_DB_VALUE = -2.12f;
    private static final float MAX_RMS_DB_VALUE = 10.0f;
    private final SpeechRecognizer speechRecognizer;
    private final float minRmsDbValue;
    private final float maxRmsDbValue;
    private RecognitionListener innerRecognitionListener;

    /**
     * Create new instance of DbmHandler with default RMS dB values.
     * @param context instance of context.
     */
    SpeechRecognizerDbmHandler(@NonNull Context context) {
        this(context, MIN_RMS_DB_VALUE, MAX_RMS_DB_VALUE);
    }

    /**
     * Create new instance of DbmHandler and set {@code maxRmsDbValue}.
     * @param context instance of context
     * @param maxRmsDbValue maximum RMS dB value
     */
    SpeechRecognizerDbmHandler(@NonNull Context context, float minRmsDbValue, float maxRmsDbValue) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);
        this.minRmsDbValue = minRmsDbValue;
        this.maxRmsDbValue = maxRmsDbValue;
    }

    /**
     * Start listening for speech.
     * @param recognitionIntent contains parameters for the recognition to be performed
     * @see SpeechRecognizer#startListening(Intent)
     */
    public void startListening(Intent recognitionIntent) {
        speechRecognizer.startListening(recognitionIntent);
    }

    /**
     * Stop listening for speech.
     * @see SpeechRecognizer#stopListening()
     */
    public void stopListening() {
        speechRecognizer.stopListening();
    }

    /**
     * Set inner recognition listener.
     * @param innerRecognitionListener inner recognition listener
     */
    public SpeechRecognizerDbmHandler innerRecognitionListener(@Nullable RecognitionListener innerRecognitionListener) {
        this.innerRecognitionListener = innerRecognitionListener;
        return this;
    }

    /**
     * Get inner recognition listener.
     * @return inner recognition listener
     */
    public RecognitionListener innerRecognitionListener() {
        return innerRecognitionListener;
    }

    @Override
    protected void onDataReceivedImpl(Float rmsdB, int layersCount, float[] dBmArray, float[] ampsArray) {
        for (int i = 0; i < layersCount; i++) {
            dBmArray[i] = Utils.normalize(rmsdB, minRmsDbValue, maxRmsDbValue);
            ampsArray[i] = 1;
        }
    }

    @Override
    public void release() {
        super.release();
        speechRecognizer.destroy();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onReadyForSpeech(params);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onBeginningOfSpeech();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        onDataReceived(rmsdB);
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onRmsChanged(rmsdB);
        }
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onBufferReceived(buffer);
        }
    }

    @Override
    public void onEndOfSpeech() {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onEndOfSpeech();
        }
    }

    @Override
    public void onError(int error) {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onError(error);
        }
    }

    @Override
    public void onResults(Bundle results) {
        speechRecognizer.cancel();
        onDataReceived(minRmsDbValue);
        calmDownAndStopRendering();
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onResults(results);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onPartialResults(partialResults);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        if (innerRecognitionListener != null) {
            innerRecognitionListener.onEvent(eventType, params);
        }
    }
}
