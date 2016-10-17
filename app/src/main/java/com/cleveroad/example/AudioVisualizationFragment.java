package com.cleveroad.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;

/**
 * Fragment with visualization of audio output.
 */
public class AudioVisualizationFragment extends Fragment {

    private AudioVisualization audioVisualization;

    public static AudioVisualizationFragment newInstance() {
        return new AudioVisualizationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final GLAudioVisualizationView visualizationView = new GLAudioVisualizationView.Builder(getContext())
                .setBubblesSize(R.dimen.bubble_size)
                .setBubblesRandomizeSize(true)
                .setWavesHeight(R.dimen.wave_height)
                .setWavesFooterHeight(R.dimen.footer_height)
                .setWavesCount(7)
                .setLayersCount(4)
                .setBackgroundColorRes(android.R.color.transparent)
                .setLayerColors(R.array.av_colors)
                .setBubblesPerLayer(16)
                .build();
        audioVisualization = visualizationView;
        frameLayout.addView(visualizationView);
//        visualizationView.setZOrderOnTop(true);
        return frameLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioVisualization.linkTo(DbmHandler.Factory.newVisualizerHandler(getContext(), 0));
        getActivity().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
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