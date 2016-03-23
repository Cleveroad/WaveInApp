package com.cleveroad.example;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Main fragment.
 */
public class MainFragment extends ListFragment implements AdapterView.OnItemClickListener {

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.selection_items, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AudioVisualizationFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        } else if (position == 1) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SpeechRecognitionFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        } else if (position == 2) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AudioRecordingFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        }
    }
}
