package com.cleveroad.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.AudioVisualization;

/**
 * Fragment with audio visualization view.
 */
public class AudioVisualizationFragment extends Fragment {

	public static AudioVisualizationFragment newInstance() {
		return new AudioVisualizationFragment();
	}

	private AudioVisualization audioVisualization;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gles, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		audioVisualization = (AudioVisualization) view.findViewById(R.id.visualizer_view);
		audioVisualization.linkTo(0);
	}

	@Override
	public void onResume() {
		super.onResume();
		audioVisualization.onResume();
	}

	@Override
	public void onPause() {
		audioVisualization.onPause();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		audioVisualization.release();
		super.onDestroyView();
	}
}
