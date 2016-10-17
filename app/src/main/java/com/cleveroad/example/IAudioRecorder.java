package com.cleveroad.example;

/**
 * Interface for audio recorder
 */
interface IAudioRecorder {
    void finishRecord();

    boolean isRecording();

    void startRecord();
}
