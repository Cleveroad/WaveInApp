package com.cleveroad.wallpaper;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Wallpaper service implementation.
 */
public class AudioVisualizationWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private void readConfiguration(Context context, SharedPreferences preferences, GLAudioVisualizationView.ColorsBuilder builder) {
        final String[] colorsStrings = context.getResources().getStringArray(R.array.color_preset1);
        final int[] colors = new int[colorsStrings.length];
        for (int i = 0; i < colorsStrings.length; i++) {
            colors[i] = Color.parseColor(colorsStrings[i]);
        }
        int bgColor = preferences.getInt(SettingsFragment.KEY_COLOR_1, colors[0]);
        colors[0] = preferences.getInt(SettingsFragment.KEY_COLOR_2, colors[1]);
        colors[1] = preferences.getInt(SettingsFragment.KEY_COLOR_3, colors[2]);
        colors[2] = preferences.getInt(SettingsFragment.KEY_COLOR_4, colors[3]);
        colors[3] = preferences.getInt(SettingsFragment.KEY_COLOR_5, colors[4]);
        builder.setBackgroundColor(bgColor).setLayerColors(colors);
    }

    private class WallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private WallpaperGLSurfaceView audioVisualizationView;
        private DbmHandler dbmHandler;
        private SharedPreferences preferences;
        private GLAudioVisualizationView.AudioVisualizationRenderer renderer;
        private boolean isPermissionsGranted;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            AudioVisualizationWallpaperService context = AudioVisualizationWallpaperService.this;

            if (!(isPermissionsGranted = checkPermissions())) {
                Toast.makeText(context, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
            } else {
                init();
            }
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
            if (visible && !isPermissionsGranted && (isPermissionsGranted = checkPermissions())) {
                init();
            }

            if (audioVisualizationView != null && dbmHandler != null) {
                if (visible) {
                    audioVisualizationView.onResume();
                    dbmHandler.onResume();
                } else {
                    dbmHandler.onPause();
                    audioVisualizationView.onPause();
                }
            }
        }

        @Override
        public void onDestroy() {
            if (preferences != null && dbmHandler != null && audioVisualizationView != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(this);
                dbmHandler.release();
                audioVisualizationView.onDestroy();
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = AudioVisualizationWallpaperService.this;
            GLAudioVisualizationView.ColorsBuilder builder = new GLAudioVisualizationView.ColorsBuilder<>(context);
            readConfiguration(context, sharedPreferences, builder);
            if (renderer != null) {
                renderer.updateConfiguration(builder);
            }
        }

        private boolean checkPermissions() {
            AudioVisualizationWallpaperService context = AudioVisualizationWallpaperService.this;

            return ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }

        private void init() {
            AudioVisualizationWallpaperService context = AudioVisualizationWallpaperService.this;
            audioVisualizationView = new WallpaperGLSurfaceView(context);
            dbmHandler = DbmHandler.Factory.newVisualizerHandler(context, 0);
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.registerOnSharedPreferenceChangeListener(this);
            GLAudioVisualizationView.Builder builder = new GLAudioVisualizationView.Builder(context)
                    .setBubblesSize(R.dimen.bubble_size)
                    .setBubblesRandomizeSize(true)
                    .setWavesHeight(R.dimen.wave_height)
                    .setWavesFooterHeight(R.dimen.footer_height)
                    .setWavesCount(7)
                    .setLayersCount(4)
                    .setBubblesPerLayer(10);
            readConfiguration(context, preferences, builder);
            renderer = new GLAudioVisualizationView.RendererBuilder(builder)
                    .glSurfaceView(audioVisualizationView)
                    .handler(dbmHandler)
                    .build();
            audioVisualizationView.setEGLContextClientVersion(2);
            audioVisualizationView.setRenderer(renderer);
        }


        class WallpaperGLSurfaceView extends GLSurfaceView {

            WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }
}
