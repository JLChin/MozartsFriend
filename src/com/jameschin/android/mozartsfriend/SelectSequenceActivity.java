package com.jameschin.android.mozartsfriend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectSequenceActivity extends BaseListActivity {
	// CONSTANTS
	public static final String[] SCALES = { "Ionian Mode", "Dorian Mode", "Phrygian Mode", "Lydian Mode", "Mixolydian Mode", "Aeolian Mode", "Locrian Mode", "Acoustic Scale", "Adonai Malakh Scale", "Algerian Scale", "Altered Scale", "Augmented Scale", "Bebop Dominant Scale", "Blues Scale", "Chromatic Scale", "Double Harmonic Scale", "Enigmatic Scale", "Flamenco Scale", "Gypsy Scale", "Half Diminished Scale", "Harmonic Major Scale", "Harmonic Minor Scale", "Hirajoshi Scale", "Hungarian Minor Scale", "In Scale", "Insen Scale", "Istrian Scale", "Iwato Scale", "Lydian Augmented Scale", "Major Scale", "Major Bebop Scale", "Major Locrian Scale", "Major Pentatonic Scale", "Melodic Minor Scale", "Minor Scale", "Minor Pentatonic Scale", "Neapolitan Major Scale", "Neapolitan Minor Scale", "Persian Scale", "Phrygian Dominant Scale", "Prometheus Scale", "Tritone Scale", "Ukranian Dorian Scale", "Whole Tone Scale", "Yo Scale" };
	public static final String[] CHORDS = { "Augmented Chord", "Augmented 6th Chord", "Augmented 7th Chord", "Augmented Major 7th Chord", "Diminished Chord", "Diminished 7th Chord", "Diminished Major 7th Chord", "Dominant 7th Chord", "Dominant 7th Flat 5th Chord", "Dream Chord", "Major Chord", "Major 6th Chord", "Major 7th Chord", "Minor Chord", "Minor 6th Chord", "Minor 7th Chord", "Minor 7th Flat 5th Chord", "Minor Major 7th Chord", "Mu Chord", "Power Chord", "Suspended 2nd Chord", "Suspended 4th Chord", "7th Suspended 2nd Chord", "7th Suspended 4th Chord", "7/6 Chord", "Viennese Trichord" };
	
	// STATE VARIABLES
	private String operation;
	private boolean arpeggio;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		initialize();
	}

	private void initialize() {
		initializeListView();
		
		// Set initial content based on intent received from previous activity
		operation = getIntent().getStringExtra("operation");
		if (operation.equals("SCALES")) {
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, SCALES));
			setTitle(R.string.title_scales);
			arpeggio = true;
		} else if (operation.equals("CHORDS")) {
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, CHORDS));
			setTitle(R.string.title_chords);
			arpeggio = false;
		}
	}
	
	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String sequence = (String) ((TextView) view).getText();
					
					Intent intent = new Intent(SelectSequenceActivity.this, SelectKeyActivity.class);
					intent.putExtra("operation", operation);
					intent.putExtra("sequence", sequence);
					intent.putExtra("arpeggio", arpeggio);
					startActivity(intent);
			}
		});
	}
}
