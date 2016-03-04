package com.cleveroad.example;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private static final int REQUEST_CODE = 1;
	private boolean shouldOpenFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
					&& ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
				openFragment();
			} else {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
						|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
					AlertDialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								requestPermissions();
							} else if (which == DialogInterface.BUTTON_NEGATIVE) {
								permissionsNotGranted();
							}
						}
					};
					new AlertDialog.Builder(this)
							.setTitle(getString(R.string.title_permissions))
							.setMessage(Html.fromHtml(getString(R.string.message_permissions)))
							.setPositiveButton(getString(R.string.btn_next), onClickListener)
							.setNegativeButton(getString(R.string.btn_cancel), onClickListener)
							.show();
				} else {
					requestPermissions();
				}
			}
		}
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

	private void openFragment() {
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, MainFragment.newInstance())
				.commit();
	}
}