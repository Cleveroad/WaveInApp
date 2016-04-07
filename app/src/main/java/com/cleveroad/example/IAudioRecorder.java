package com.cleveroad.example;

/**
 * Interface for audio recorder
 */
interface IAudioRecorder {
    void startRecord();
    void finishRecord();
    boolean isRecording();
}
