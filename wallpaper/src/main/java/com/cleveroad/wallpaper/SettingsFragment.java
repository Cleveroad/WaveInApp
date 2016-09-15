package com.cleveroad.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Settings fragment
 */
public class SettingsFragment extends Fragment {

    static final String KEY_COLOR_1 = "COLOR_1";
    static final String KEY_COLOR_2 = "COLOR_2";
    static final String KEY_COLOR_3 = "COLOR_3";
    static final String KEY_COLOR_4 = "COLOR_4";
    static final String KEY_COLOR_5 = "COLOR_5";
    @Bind({R.id.view_1, R.id.view_2, R.id.view_3, R.id.view_4, R.id.view_5})
    View[] views;
    private SharedPreferences preferences;

    public static SettingsFragment instance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final String[] colorsStrings = getResources().getStringArray(R.array.color_preset1);
        final int[] colors = new int[colorsStrings.length];
        for (int i = 0; i < colorsStrings.length; i++) {
            colors[i] = Color.parseColor(colorsStrings[i]);
        }
        colors[0] = preferences.getInt(KEY_COLOR_1, colors[0]);
        colors[1] = preferences.getInt(KEY_COLOR_2, colors[1]);
        colors[2] = preferences.getInt(KEY_COLOR_3, colors[2]);
        colors[3] = preferences.getInt(KEY_COLOR_4, colors[3]);
        colors[4] = preferences.getInt(KEY_COLOR_5, colors[4]);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            views[i].setBackgroundColor(colors[i]);
            views[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialogFragment fragment = ColorPickerDialogFragment.instance(index, colors[index]);
                    fragment.setTargetFragment(SettingsFragment.this, 1);
                    fragment.show(getFragmentManager(), "ColorPicker");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            int index = data.getIntExtra(ColorPickerDialogFragment.KEY_INDEX, -1);
            int color = data.getIntExtra(ColorPickerDialogFragment.KEY_COLOR, 0);
            views[index].setBackgroundColor(color);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        MenuItem preset = menu.findItem(R.id.action_preset);
        Menu m = preset.getSubMenu();
        String[] presets = getResources().getStringArray(R.array.presets);
        for (int i = 0; i < presets.length; i++) {
            final int index = i + 1;
            m.add(Menu.NONE, i, Menu.NONE, presets[i]).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    setPreset(index);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            preferences.edit()
                    .putInt(KEY_COLOR_1, ((ColorDrawable) views[0].getBackground()).getColor())
                    .putInt(KEY_COLOR_2, ((ColorDrawable) views[1].getBackground()).getColor())
                    .putInt(KEY_COLOR_3, ((ColorDrawable) views[2].getBackground()).getColor())
                    .putInt(KEY_COLOR_4, ((ColorDrawable) views[3].getBackground()).getColor())
                    .putInt(KEY_COLOR_5, ((ColorDrawable) views[4].getBackground()).getColor())
                    .apply();
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPreset(int index) {
        int id = getResources().getIdentifier("color_preset" + index, "array", getContext().getPackageName());
        String[] colors = getResources().getStringArray(id);
        for (int i = 0; i < 5; i++) {
            views[i].setBackgroundColor(Color.parseColor(colors[i]));
        }
    }
}
