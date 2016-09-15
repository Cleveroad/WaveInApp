package com.cleveroad.wallpaper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Settings activity.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private boolean shouldOpenFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            openFragment();
        } else {
            requestPermissions();
        }
    }

    void openFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, SettingsFragment.instance())
                .commit();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS},
                REQUEST_CODE
        );
    }

    private void permissionsNotGranted() {
        Toast.makeText(this, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean bothGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i]) || Manifest.permission.MODIFY_AUDIO_SETTINGS.equals(permissions[i])) {
                    bothGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            if (bothGranted) {
                shouldOpenFragment = true;
            } else {
                permissionsNotGranted();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldOpenFragment) {
            shouldOpenFragment = false;
            openFragment();
        }
    }
}
