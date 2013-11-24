package com.jameschin.android.mozartsfriend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/**
 * SettingsActivity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class SettingsActivity extends BaseActivity {
	// DEFAULT SETTINGS
	static final int MAX_FRET_START = 20;
	static final int MAX_FRET_RANGE = 8;
	static final int VISUAL_FEEDBACK_MODE_ENABLED = 0;
	static final int VISUAL_FEEDBACK_MODE_FIRST_BEAT_ONLY = 1;
	static final int VISUAL_FEEDBACK_MODE_DISABLED = 2;
	
	// SYSTEM
	private SharedPreferences.Editor sharedPrefEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		initialize();
	}
	
	private void initialize() {
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, LibraryActivity.KEYS);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		
		// 6-STRING GUITAR SETTINGS
		Spinner[] spinnersGuitar6Tuning = new Spinner[6];
		DefaultData guitar6Defaults = new DefaultData(6);
		guitar6Defaults.spinnersToReset = spinnersGuitar6Tuning;
		guitar6Defaults.defaultTuning = LibraryActivity.DEFAULT_GUITAR_6_TUNING;
		for (int i = 0; i < 6; i++) {
			int index = 6 - i;
			String name = "GUITAR_6_TUNING_" + index;
			spinnersGuitar6Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_guitar6_tuning_" + index, "id", getPackageName()));
			spinnersGuitar6Tuning[i].setAdapter(adapter);
			spinnersGuitar6Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersGuitar6Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, guitar6Defaults.defaultTuning[i])));
			spinnersGuitar6Tuning[i].setTag(name);
			guitar6Defaults.prefKeysToRemove[i] = name;
		}
		
		Button buttonGuitar6Default = (Button) findViewById(R.id.button_settings_guitar6_tuning_default);
		buttonGuitar6Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonGuitar6Default.setTag(guitar6Defaults);
		
		// 7-STRING GUITAR SETTINGS
		Spinner[] spinnersGuitar7Tuning = new Spinner[7];
		DefaultData guitar7Defaults = new DefaultData(7);
		guitar7Defaults.spinnersToReset = spinnersGuitar7Tuning;
		guitar7Defaults.defaultTuning = LibraryActivity.DEFAULT_GUITAR_7_TUNING;
		for (int i = 0; i < 7; i++) {
			int index = 7 - i;
			String name = "GUITAR_7_TUNING_" + index;
			spinnersGuitar7Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_guitar7_tuning_" + index, "id", getPackageName()));
			spinnersGuitar7Tuning[i].setAdapter(adapter);
			spinnersGuitar7Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersGuitar7Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, guitar7Defaults.defaultTuning[i])));
			spinnersGuitar7Tuning[i].setTag(name);
			guitar7Defaults.prefKeysToRemove[i] = name;
		}

		Button buttonGuitar7Default = (Button) findViewById(R.id.button_settings_guitar7_tuning_default);
		buttonGuitar7Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonGuitar7Default.setTag(guitar7Defaults);
		
		// 8-STRING GUITAR SETTINGS
		Spinner[] spinnersGuitar8Tuning = new Spinner[8];
		DefaultData guitar8Defaults = new DefaultData(8);
		guitar8Defaults.spinnersToReset = spinnersGuitar8Tuning;
		guitar8Defaults.defaultTuning = LibraryActivity.DEFAULT_GUITAR_8_TUNING;
		for (int i = 0; i < 8; i++) {
			int index = 8 - i;
			String name = "GUITAR_8_TUNING_" + index;
			spinnersGuitar8Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_guitar8_tuning_" + index, "id", getPackageName()));
			spinnersGuitar8Tuning[i].setAdapter(adapter);
			spinnersGuitar8Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersGuitar8Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, guitar8Defaults.defaultTuning[i])));
			spinnersGuitar8Tuning[i].setTag(name);
			guitar8Defaults.prefKeysToRemove[i] = name;
		}

		Button buttonGuitar8Default = (Button) findViewById(R.id.button_settings_guitar8_tuning_default);
		buttonGuitar8Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonGuitar8Default.setTag(guitar8Defaults);
		
		// 4-STRING BASS SETTINGS
		Spinner[] spinnersBass4Tuning = new Spinner[4];
		DefaultData bass4Defaults = new DefaultData(4);
		bass4Defaults.spinnersToReset = spinnersBass4Tuning;
		bass4Defaults.defaultTuning = LibraryActivity.DEFAULT_BASS_4_TUNING;
		for (int i = 0; i < 4; i++) {
			int index = 4 - i;
			String name = "BASS_4_TUNING_" + index;
			spinnersBass4Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_bass4_tuning_" + index, "id", getPackageName()));
			spinnersBass4Tuning[i].setAdapter(adapter);
			spinnersBass4Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersBass4Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, bass4Defaults.defaultTuning[i])));
			spinnersBass4Tuning[i].setTag(name);
			bass4Defaults.prefKeysToRemove[i] = name;
		}

		Button buttonBass4Default = (Button) findViewById(R.id.button_settings_bass4_tuning_default);
		buttonBass4Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonBass4Default.setTag(bass4Defaults);
		
		// 5-STRING BASS SETTINGS
		Spinner[] spinnersBass5Tuning = new Spinner[5];
		DefaultData bass5Defaults = new DefaultData(5);
		bass5Defaults.spinnersToReset = spinnersBass5Tuning;
		bass5Defaults.defaultTuning = LibraryActivity.DEFAULT_BASS_5_TUNING;
		for (int i = 0; i < 5; i++) {
			int index = 5 - i;
			String name = "BASS_5_TUNING_" + index;
			spinnersBass5Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_bass5_tuning_" + index, "id", getPackageName()));
			spinnersBass5Tuning[i].setAdapter(adapter);
			spinnersBass5Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersBass5Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, bass5Defaults.defaultTuning[i])));
			spinnersBass5Tuning[i].setTag(name);
			bass5Defaults.prefKeysToRemove[i] = name;
		}

		Button buttonBass5Default = (Button) findViewById(R.id.button_settings_bass5_tuning_default);
		buttonBass5Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonBass5Default.setTag(bass5Defaults);
		
		// 6-STRING BASS SETTINGS
		Spinner[] spinnersBass6Tuning = new Spinner[6];
		DefaultData bass6Defaults = new DefaultData(6);
		bass6Defaults.spinnersToReset = spinnersBass6Tuning;
		bass6Defaults.defaultTuning = LibraryActivity.DEFAULT_BASS_6_TUNING;
		for (int i = 0; i < 6; i++) {
			int index = 6 - i;
			String name = "BASS_6_TUNING_" + index;
			spinnersBass6Tuning[i] = (Spinner) findViewById(getResources().getIdentifier("spinner_settings_bass6_tuning_" + index, "id", getPackageName()));
			spinnersBass6Tuning[i].setAdapter(adapter);
			spinnersBass6Tuning[i].setOnItemSelectedListener(new TuningSpinnerListener());
			spinnersBass6Tuning[i].setSelection(getKeyPosition(sharedPref.getString(name, bass6Defaults.defaultTuning[i])));
			spinnersBass6Tuning[i].setTag(name);
			bass6Defaults.prefKeysToRemove[i] = name;
		}

		Button buttonBass6Default = (Button) findViewById(R.id.button_settings_bass6_tuning_default);
		buttonBass6Default.setOnClickListener(new DefaultTuningButtonListener());
		buttonBass6Default.setTag(bass6Defaults);
		
		// FRETBOARD LENGTH SETTINGS
		List<String> fretLengthList = new ArrayList<String>();
		for (int i = 0; i <= MAX_FRET_RANGE; i++)
			fretLengthList.add(String.valueOf(MAX_FRET_START + i) + "-Fret");
		ArrayAdapter<String> adapterFretLength = new ArrayAdapter<String>(this, R.layout.spinner_item, fretLengthList);
		adapterFretLength.setDropDownViewResource(R.layout.spinner_dropdown_item);
		Spinner spinnerFretLength = (Spinner) findViewById(R.id.spinner_settings_fretboard_length);
		spinnerFretLength.setAdapter(adapterFretLength);
		spinnerFretLength.setSelection(sharedPref.getInt("FRETBOARD_LENGTH", LibraryActivity.DEFAULT_FRETBOARD_LENGTH) - MAX_FRET_START - 1);
		spinnerFretLength.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				sharedPrefEditor.putInt("FRETBOARD_LENGTH", MAX_FRET_START + position + 1);
				sharedPrefEditor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		// METRONOME VISUAL FEEDBACK SETTINGS
		String[] metronomeVisualFeedbackList = { getString(R.string.string_metronome_visual_feedback_mode_enabled), getString(R.string.string_metronome_visual_feedback_mode_firstbeatonly), getString(R.string.string_metronome_visual_feedback_mode_disabled) };
		ArrayAdapter<String> adapterMetronomeVisualFeedback = new ArrayAdapter<String>(this, R.layout.spinner_item, metronomeVisualFeedbackList);
		adapterMetronomeVisualFeedback.setDropDownViewResource(R.layout.spinner_dropdown_item);
		Spinner spinnerMetronomeVisualFeedback = (Spinner) findViewById(R.id.spinner_settings_metronome_visual_feedback);
		spinnerMetronomeVisualFeedback.setAdapter(adapterMetronomeVisualFeedback);
		spinnerMetronomeVisualFeedback.setSelection(sharedPref.getInt("METRONOME_VISUAL_FEEDBACK_MODE", VISUAL_FEEDBACK_MODE_ENABLED));
		spinnerMetronomeVisualFeedback.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				sharedPrefEditor.putInt("METRONOME_VISUAL_FEEDBACK_MODE", position);
				sharedPrefEditor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	private class TuningSpinnerListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			String name = (String) parent.getTag();
			String key = LibraryActivity.KEYS[position];
			sharedPrefEditor.putString(name, key);
			sharedPrefEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}
	
	private class DefaultTuningButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			DefaultData defaultData = (DefaultData) v.getTag();
			
			// remove SharedPreference data
			for (String s : defaultData.prefKeysToRemove)
				sharedPrefEditor.remove(s);
			sharedPrefEditor.commit();
			
			// restore spinners to default tuning
			for (int i = 0; i < defaultData.spinnersToReset.length; i++)
				defaultData.spinnersToReset[i].setSelection(getKeyPosition(defaultData.defaultTuning[i]));
		}
	}
	
	/**
	 * Class containing data to facilitate restoring default values.
	 */
	private class DefaultData {
		Spinner[] spinnersToReset;
		String[] prefKeysToRemove;
		String[] defaultTuning;
		
		DefaultData(int size) {
			spinnersToReset = new Spinner[size];
			prefKeysToRemove = new String[size];
		}
	}
	
	/**
	 * Returns the spinner index position of the specified key.
	 * @param string the musical key to find the spinner index position for.
	 * @return the spinner index position of the specified key.
	 */
	private int getKeyPosition(String string) {
		for (int i = 0; i < LibraryActivity.KEYS.length; i++) {
			if (LibraryActivity.KEYS[i].equals(string))
				return i;
		}
		
		return -1;
	}
}
