package com.cleveroad.audiovisualization;

import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;

/**
 * Audio visualization view interface
 */
public interface AudioVisualization {

	/**
	 * Link view to specific audio session. Pass zero to link to whole output mix.
	 * @param audioSessionId unique audio session id
	 * @see MediaPlayer#getAudioSessionId()
	 * @see AudioTrack#getAudioSessionId()
	 */
	void linkTo(int audioSessionId);

	/**
	 * Link view to media player. At this point view will set itself as
	 * {@link MediaPlayer.OnPreparedListener} and {@link MediaPlayer.OnCompletionListener} of media player.
	 * @param mediaPlayer instance of media player
	 * @see #setInnerOnPreparedListener(MediaPlayer.OnPreparedListener)
	 * @see #setInnerOnCompletionListener(MediaPlayer.OnCompletionListener)
	 */
	void linkTo(MediaPlayer mediaPlayer);

	/**
	 * Set inner listener of view.
	 * @param onPreparedListener instance of listener
	 * @see #linkTo(MediaPlayer)
	 */
	void setInnerOnPreparedListener(@Nullable MediaPlayer.OnPreparedListener onPreparedListener);

	/**
	 * Set inner listener of view.
	 * @param onCompletionListener instance of listener
	 * @see #linkTo(MediaPlayer)
	 *
	 */
	void setInnerOnCompletionListener(@Nullable MediaPlayer.OnCompletionListener onCompletionListener);

	/**
	 * Resume audio visualization.
	 */
	void onResume();

	/**
	 * Pause audio visualization.
	 */
	void onPause();

	/**
	 * Release resources of audio visualization.
	 */
	void release();
}
