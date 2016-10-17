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

    private static final float MAX_RMS_DB_VALUE = 10.0f;
    private static final float MIN_RMS_DB_VALUE = -2.12f;
    private final float mMaxRmsDbValue;
    private final float mMinRmsDbValue;
    private final SpeechRecognizer mSpeechRecognizer;
    private RecognitionListener mRecognitionListener;

    /**
     * Create new instance of DbmHandler with default RMS dB values.
     *
     * @param context instance of context.
     */
    SpeechRecognizerDbmHandler(@NonNull Context context) {
        this(context, MIN_RMS_DB_VALUE, MAX_RMS_DB_VALUE);
    }

    /**
     * Create new instance of DbmHandler and set {@code maxRmsDbValue}.
     *
     * @param context       instance of context
     * @param maxRmsDbValue maximum RMS dB value
     */
    SpeechRecognizerDbmHandler(@NonNull Context context, float minRmsDbValue, float maxRmsDbValue) {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(this);
        mMinRmsDbValue = minRmsDbValue;
        mMaxRmsDbValue = maxRmsDbValue;
    }

    /**
     * Set inner recognition listener.
     *
     * @param innerRecognitionListener inner recognition listener
     */
    public SpeechRecognizerDbmHandler innerRecognitionListener(@Nullable RecognitionListener innerRecognitionListener) {
        mRecognitionListener = innerRecognitionListener;
        return this;
    }

    /**
     * Get inner recognition listener.
     *
     * @return inner recognition listener
     */
    public RecognitionListener innerRecognitionListener() {
        return mRecognitionListener;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onReadyForSpeech(params);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        if (mRecognitionListener != null) {
            mRecognitionListener.onBeginningOfSpeech();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        onDataReceived(rmsdB);
        if (mRecognitionListener != null) {
            mRecognitionListener.onRmsChanged(rmsdB);
        }
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onBufferReceived(buffer);
        }
    }

    @Override
    public void onEndOfSpeech() {
        if (mRecognitionListener != null) {
            mRecognitionListener.onEndOfSpeech();
        }
    }

    @Override
    public void onError(int error) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onError(error);
        }
    }

    @Override
    public void onResults(Bundle results) {
        mSpeechRecognizer.cancel();
        onDataReceived(mMinRmsDbValue);
        calmDownAndStopRendering();
        if (mRecognitionListener != null) {
            mRecognitionListener.onResults(results);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onPartialResults(partialResults);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        if (mRecognitionListener != null) {
            mRecognitionListener.onEvent(eventType, params);
        }
    }

    @Override
    public void release() {
        super.release();
        mSpeechRecognizer.destroy();
    }

    @Override
    protected void onDataReceivedImpl(Float rmsdB, int layersCount, @NonNull float[] dBmArray, @NonNull float[] ampsArray) {
        for (int i = 0; i < layersCount; i++) {
            dBmArray[i] = Utils.normalize(rmsdB, mMinRmsDbValue, mMaxRmsDbValue);
            ampsArray[i] = 1;
        }
    }

    /**
     * Start listening for speech.
     *
     * @param recognitionIntent contains parameters for the recognition to be performed
     * @see SpeechRecognizer#startListening(Intent)
     */
    public void startListening(Intent recognitionIntent) {
        mSpeechRecognizer.startListening(recognitionIntent);
    }

    /**
     * Stop listening for speech.
     *
     * @see SpeechRecognizer#stopListening()
     */
    public void stopListening() {
        mSpeechRecognizer.stopListening();
    }
}
