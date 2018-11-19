/**
 * Copyright 2013, Haruki Hasegawa
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 */

package com.cleveroad.audiovisualization.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.cleveroad.audiovisualization.R;


public class TunnelPlayerWorkaround {
  private static final String TAG = "TunnelPlayerWorkaround";

  private static final String SYSTEM_PROP_TUNNEL_DECODE_ENABLED = "tunnel.decode";

  private TunnelPlayerWorkaround()
  {
  }

  /**
   * Obtain "tunnel.decode" system property value
   * 
   * @param context Context
   * @return Whether tunnel player is enabled
   */
  public static boolean isTunnelDecodeEnabled(Context context)
  {
    return SystemPropertiesProxy.getBoolean(
        context, SYSTEM_PROP_TUNNEL_DECODE_ENABLED, false);
  }

  /**
   * Create silent MediaPlayer instance to avoid tunnel player issue
   * 
   * @param context Context
   * @return MediaPlayer instance
   */
  public static MediaPlayer createSilentMediaPlayer(Context context)
  {
    boolean result = false;

    MediaPlayer mp = null;
    try {
      mp = MediaPlayer.create(context, R.raw.av_workaround_1min);
      mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

      // NOTE: start() is no needed
      // mp.start();

      result = true;
    } catch (RuntimeException e) {
      Log.e(TAG, "createSilentMediaPlayer()", e);
    } finally {
      if (!result && mp != null) {
        try {
          mp.release();
        } catch (IllegalStateException e) {
        }
      }
    }

    return mp;
  }
}
