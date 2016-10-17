package com.cleveroad.audiovisualization;

import android.support.annotation.NonNull;

/**
 * Audio visualization view interface.
 */
public interface AudioVisualization {

    /**
     * Link view to custom implementation of {@link DbmHandler}.
     *
     * @param dbmHandler instance of DbmHandler
     */
    <T> void linkTo(@NonNull DbmHandler<T> dbmHandler);

    /**
     * Pause audio visualization.
     */
    void onPause();

    /**
     * Resume audio visualization.
     */
    void onResume();

    /**
     * Release resources of audio visualization.
     */
    void release();
}
