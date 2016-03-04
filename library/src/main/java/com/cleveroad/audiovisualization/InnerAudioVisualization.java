package com.cleveroad.audiovisualization;

import android.support.annotation.Nullable;

/**
 * Created by Александр on 04.03.2016.
 */
interface InnerAudioVisualization {

    void startRendering();

    void stopRendering();

    void calmDownListener(@Nullable CalmDownListener calmDownListener);

    void onDataReceived(float[] dBmArray, float[] ampsArray);

    /**
     * Created by Александр on 04.03.2016.
     */
    interface CalmDownListener {
        void onCalmedDown();
    }
}
