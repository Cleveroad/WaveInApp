package com.cleveroad.example;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.SpeechRecognizerDbmHandler;

/**
 * Fragment with visualization of speech recognition.
 */
public class SpeechRecognitionFragment extends Fragment {

    public static SpeechRecognitionFragment newInstance() {
        return new SpeechRecognitionFragment();
    }

    private AudioVisualization audioVisualization;
    private Button btnRecognize;
    private SpeechRecognizerDbmHandler handler;
    private boolean recognizing;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gles, container, false);
        audioVisualization = (AudioVisualization) view.findViewById(R.id.visualizer_view);
        btnRecognize = (Button) view.findViewById(R.id.btn_recognize);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recognizing) {
                    handler.stopListening();
                } else {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
                    handler.startListening(intent);
                }
                btnRecognize.setEnabled(false);
            }
        });
        handler = DbmHandler.Factory.newSpeechRecognizerHandler(getContext());
        handler.innerRecognitionListener(new SimpleRecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                super.onReadyForSpeech(params);
                onStartRecognizing();
            }

            @Override
            public void onResults(Bundle results) {
                super.onResults(results);
                onStopRecognizing();
            }

            @Override
            public void onError(int error) {
                super.onError(error);
                onStopRecognizing();

            }
        });
        audioVisualization.linkTo(handler);
    }

    private void onStopRecognizing() {
        recognizing = false;
        btnRecognize.setText(R.string.start_recognition);
        btnRecognize.setEnabled(true);
    }

    private void onStartRecognizing() {
        btnRecognize.setText(R.string.stop_recognition);
        btnRecognize.setEnabled(true);
        recognizing = true;
    }

    @Override
    public void onDestroyView() {
        audioVisualization.release();
        super.onDestroyView();
    }

    private static class SimpleRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
        }

        @Override
        public void onResults(Bundle results) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }
}