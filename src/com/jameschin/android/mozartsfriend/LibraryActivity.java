package com.jameschin.android.mozartsfriend;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jameschin.android.mozartsfriend.Library.ResultData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * LibraryActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class LibraryActivity extends BaseListActivity {	
	// CONSTANTS
	public static final String[] KEYS = { "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B" };
	
	// DEFAULT SETTINGS
	public static final String[] DEFAULT_GUITAR_6_TUNING = { "E", "A", "D", "G", "B", "E" };
	public static final String[] DEFAULT_GUITAR_7_TUNING = { "B", "E", "A", "D", "G", "B", "E" };
	public static final String[] DEFAULT_GUITAR_8_TUNING = { "F#", "B", "E", "A", "D", "G", "B", "E" };
	public static final String[] DEFAULT_BASS_4_TUNING = { "E", "A", "D", "G" };
	public static final String[] DEFAULT_BASS_5_TUNING = { "B", "E", "A", "D", "G" };
	public static final String[] DEFAULT_BASS_6_TUNING = { "B", "E", "A", "D", "G", "C" };
	public static final int DEFAULT_FRETBOARD_LENGTH = 25;
	public static final int DEFAULT_FRET_MARKER_SIZE_IN_DP = 16;
	public static final int DEFAULT_MIDI_VELOCITY = 100;
	public static final String DEFAULT_MIDI_FILE = "midiFile.mid";
	
	// USER PARAMETERS
	private String[] guitar6Tuning;
	private String[] guitar7Tuning;
	private String[] guitar8Tuning;
	private String[] bass4Tuning;
	private String[] bass5Tuning;
	private String[] bass6Tuning;
	private int fretboardLength;
	
	// STATE VARIABLES
	private String sequence;
	private String root;
	private boolean arpeggio;
	private int currentView;
	private Set<String> markedNotes;
	private Map<String, List<View>> fretboardViewCache;
	private Map<String, Integer> midiNoteMap;
	private List<Integer> midiSequence;
	private List<ResultData> results;
	
	// VIEW HOLDERS
	private Button buttonDemo;
	private HorizontalScrollView viewFretboard;
	private TableLayout tableFretboard;
	private ListView listView;
	private RelativeLayout layoutPianoView;
	private Spinner spinnerViewSelect;
	private TextView textViewRoot;
	private TextView textViewSequence;
	private TextView[] textViewPianoKeys;
	
	// SYSTEM
	private Library library;
	private MediaPlayer mediaPlayer;
	private SharedPreferences.Editor sharedPrefEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
		
		// Restore progress after screen orientation change
		@SuppressWarnings("deprecation")
		InstanceData instanceData = (InstanceData) getLastNonConfigurationInstance();
		if (instanceData != null)
			restoreInstance(instanceData);
	}

	private void initialize() {
		library = new Library();
		mediaPlayer = new MediaPlayer();
		
		// Set content based on intent received from previous activity
		Intent intent = getIntent();
		sequence = intent.getStringExtra("sequence");
		root = intent.getStringExtra("key");
		arpeggio = intent.getBooleanExtra("arpeggio", true);

		loadPreferences();
		initializeMidi();
		initializePianoKeys();
		initializeDemo();
		initializeViews();
		initializeListView();
		initializeSpinner();
		initializeNotes();
		supportScreen();
		
		textViewRoot.setText(root);
		textViewSequence.setText(sequence);
		
		// set the user's preferred view upon entry
		spinnerViewSelect.setSelection(currentView);
	}
	
	/**
	 * Loads the ListView based on the current progress.
	 */
	private void initializeListView() {
		listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// NOTE VIEW
				String note = ((TextView) view.findViewById(R.id.textview_result_data_name)).getText().toString();
				String interval = ((TextView) view.findViewById(R.id.textview_result_data_interval)).getText().toString();

				toggleMark(note);

				if (markedNotes.contains(note)) {
					if (interval == "Root")
						view.setBackgroundResource(R.drawable.fretboard_item_background_green);
					else
						view.setBackgroundResource(R.drawable.fretboard_item_background_purple);
				} else
					view.setBackgroundResource(0);
			}
		});
	}
	
	/**
	 * Loads user custom tunings, if any, defaults otherwise.
	 */
	private void loadPreferences() {
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		// restore the user's preferred view upon entry
		currentView = sharedPref.getInt("LIBRARY_VIEW", 0);
		
		guitar6Tuning = new String[6];
		for (int i = 0; i < 6; i++)
			guitar6Tuning[i] = sharedPref.getString("GUITAR_6_TUNING_" + String.valueOf(6 - i), DEFAULT_GUITAR_6_TUNING[i]);
		
		guitar7Tuning = new String[7];
		for (int i = 0; i < 7; i++)
			guitar7Tuning[i] = sharedPref.getString("GUITAR_7_TUNING_" + String.valueOf(7 - i), DEFAULT_GUITAR_7_TUNING[i]);
		
		guitar8Tuning = new String[8];
		for (int i = 0; i < 8; i++)
			guitar8Tuning[i] = sharedPref.getString("GUITAR_8_TUNING_" + String.valueOf(8 - i), DEFAULT_GUITAR_8_TUNING[i]);
		
		bass4Tuning = new String[4];
		for (int i = 0; i < 4; i++)
			bass4Tuning[i] = sharedPref.getString("BASS_4_TUNING_" + String.valueOf(4 - i), DEFAULT_BASS_4_TUNING[i]);
		
		bass5Tuning = new String[5];
		for (int i = 0; i < 5; i++)
			bass5Tuning[i] = sharedPref.getString("BASS_5_TUNING_" + String.valueOf(5 - i), DEFAULT_BASS_5_TUNING[i]);
		
		bass6Tuning = new String[6];
		for (int i = 0; i < 6; i++)
			bass6Tuning[i] = sharedPref.getString("BASS_6_TUNING_" + String.valueOf(6 - i), DEFAULT_BASS_6_TUNING[i]);
		
		fretboardLength = sharedPref.getInt("FRETBOARD_LENGTH", DEFAULT_FRETBOARD_LENGTH);
	}
	
	/**
	 * Attaches various view holders, sets initial layout visibility states.
	 */
	private void initializeViews() {
		viewFretboard = (HorizontalScrollView) findViewById(R.id.view_fretboard);
		tableFretboard = (TableLayout) findViewById(R.id.table_fretboard);
		layoutPianoView = (RelativeLayout) findViewById(R.id.layout_piano_view);
		textViewRoot = (TextView) findViewById(R.id.textview_root);
		textViewSequence = (TextView) findViewById(R.id.textview_sequence);
		
		viewFretboard.setVisibility(View.GONE);
		layoutPianoView.setVisibility(View.GONE);
	}
	
	/**
	 * Set up the result view selection spinner.
	 */
	private void initializeSpinner() {
		spinnerViewSelect = (Spinner) findViewById(R.id.spinner_view_select);
		
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.library_note_view));
		list.add(getString(R.string.library_6_string_guitar_view));
		list.add(getString(R.string.library_7_string_guitar_view));
		list.add(getString(R.string.library_8_string_guitar_view));
		list.add(getString(R.string.library_4_string_bass_view));
		list.add(getString(R.string.library_5_string_bass_view));
		list.add(getString(R.string.library_6_string_bass_view));
		list.add(getString(R.string.library_piano_view));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, list);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		spinnerViewSelect.setAdapter(adapter);
		
		spinnerViewSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// SAVE STATE
				currentView = position;
				
				// save the user's preferred view
				sharedPrefEditor.putInt("LIBRARY_VIEW", position);
				sharedPrefEditor.commit();
				
				generateView(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	/**
	 * Loads the appropriate instrument view.
	 * @param position integer representing the spinner position.
	 */
	private void generateView(int position) {
		switch (position) {
		case (0):
			setNoteViews();
			return;
		case (1):
			setFretboardViews(guitar6Tuning);
			return;
		case (2):
			setFretboardViews(guitar7Tuning);
			return;
		case (3):
			setFretboardViews(guitar8Tuning);
			return;
		case (4):
			setFretboardViews(bass4Tuning);
			return;
		case (5):
			setFretboardViews(bass5Tuning);
			return;
		case (6):
			setFretboardViews(bass6Tuning);
			return;
		case (7):
			setPianoViews();
			return;
		}
	}
	
	/**
	 * Set up the Demo function.
	 */
	private void initializeDemo() {
		buttonDemo = (Button) findViewById(R.id.button_demo);
		buttonDemo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// CONSTRUCT MIDI FILE
				MidiFile midiFile = new MidiFile(LibraryActivity.this);

				// add MIDI events
				if (arpeggio) {
					for (Integer i : midiSequence)
						midiFile.noteOnOffNow(MidiFile.EIGHTH_NOTE, i, DEFAULT_MIDI_VELOCITY);
				} else { // CHORD
					// turn on all notes at start of track
					for (Integer i : midiSequence)
						midiFile.noteOn(0, i, DEFAULT_MIDI_VELOCITY);
					
					// turn off all notes after one whole note
					if (midiSequence.size() != 0)
						midiFile.noteOff(MidiFile.WHOLE_NOTE, midiSequence.get(0));
					for (int i = 1; i < midiSequence.size(); i++)
						midiFile.noteOff(0, midiSequence.get(i));
				}
				
				// write to file
				try {
					midiFile.writeToFile(DEFAULT_MIDI_FILE);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// PLAY MIDI FILE
				File file = new File(getFilesDir(), DEFAULT_MIDI_FILE);
				try {
					FileInputStream inputStream = new FileInputStream(file);
					FileDescriptor fileDescriptor = inputStream.getFD();
					mediaPlayer.reset(); // return MediaPlayer to Idle state
					mediaPlayer.setDataSource(fileDescriptor);
					mediaPlayer.prepare();
					mediaPlayer.start();

					inputStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Generate note data using the Library and store a local copy.
	 * Set up marked notes.
	 */
	private void initializeNotes() {
		results = library.getNotes(sequence, root, root, 12, false);
		
		// remove null gaps between intervals
		for (int i = 0; i < results.size();) {
			if (results.get(i) == null)
				results.remove(i);
			else
				i++;
		}
		
		// set all notes to marked state by default
		markedNotes = new HashSet<String>();
		for (ResultData r : results)
			toggleMark(r.noteName);
		
		storeMidiSequence();
	}
	
	/**
	 * Set up the Note View.
	 */
	private void setNoteViews() {
		setListAdapter(new ResultDataListViewAdapter(this, results, markedNotes));
		showListView();
	}
	
	/**
	 * Store the appropriate MIDI notes to play as demonstration.
	 */
	private void storeMidiSequence() {
		// construct midi notes for demo sequence
		midiSequence = new ArrayList<Integer>();
		
		// special case, Melodic Minor
		int melodicMinor6th = -1;
		int melodicMinor7th = -1;
		
		// ascending notes
		for (int i = 0, prev = Integer.MIN_VALUE; i < results.size(); i++) {
			ResultData currResult = results.get(i);
			String currNote = currResult.noteName;
			int midiNote = midiNoteMap.get(currNote.substring(0, 1));
			
			int remainder = currNote.length() - 1;
			if (remainder != 0) {
				if (currNote.charAt(1) == '#') {
					for (int j = 0; j < remainder; j++)
						midiNote++;
				} else { // currNote.charAt(1) == 'b'
					for (int j = 0; j < remainder; j++)
						midiNote--;
				}
			}
			
			if (midiNote <= prev)
				midiNote += 12;
			
			// special case, Melodic Minor
			if (sequence.equals("Melodic Minor Scale")) {
				if (i == 5)
					melodicMinor6th = midiNote;
				else if (i == 6)
					melodicMinor7th = midiNote;
			}
			
			if (markedNotes.contains(currNote))
				midiSequence.add(midiNote);
			
			prev = midiNote;
		}
		
		// if chord, done
		if (!arpeggio)
			return;
		
		// end ascension with octave note
		if (markedNotes.contains(results.get(0).noteName))
			midiSequence.add(midiSequence.get(0) + 12);
		
		// descending notes
		for (int i = midiSequence.size() - 2; i >= 0; i--) {
			// special case, flat the descending 7th and 6th for Melodic Minor Scale
			if (sequence.equals("Melodic Minor Scale")) {
				if (midiSequence.get(i) == melodicMinor6th)
					midiSequence.add(melodicMinor6th - 1);
				else if (midiSequence.get(i) == melodicMinor7th)
					midiSequence.add(melodicMinor7th - 1);
				else
					midiSequence.add(midiSequence.get(i));
			} else
				midiSequence.add(midiSequence.get(i));
		}
	}
	
	/**
	 * Generate fretboard data using the Library and set up the Fretboard View.
	 * @param tuning information about the instrument, including the number of strings and the tuning for each string.
	 */
	private void setFretboardViews(String[] tuning) {
		// reset TableLayout
		tableFretboard.removeAllViews();
		
		// reset fretboardViewCache
		fretboardViewCache = new HashMap<String, List<View>>();
		
		// get data from library engine
		List<List<ResultData>> array = new ArrayList<List<ResultData>>();
		int numOfStrings = tuning.length;
		for (int i = 0; i < numOfStrings; i++)
			array.add(library.getNotes(sequence, root, tuning[i], fretboardLength, true));
		
		// CONSTRUCT TABLE
		// calculate pixel dimensions for fretmarkers, based on screen density
		float scale = getResources().getDisplayMetrics().density;
		int pixels = (int) (DEFAULT_FRET_MARKER_SIZE_IN_DP * scale + 0.5f);
		int margin = pixels / 6;
		
		// construct row
		for (int i = numOfStrings - 1; i >= 0; i--) {
			List<ResultData> currString = array.get(i);
			TableRow newRow = new TableRow(this);
			
			// construct fret
			for (int j = 0; j < fretboardLength; j++) {
				// inflate vacant fret view
	            View view = getLayoutInflater().inflate(R.layout.fretboard_table_item, null);
	            
	            // set fret numbers on first string
	            if (i == numOfStrings - 1) {
	            	TextView fretNumber = (TextView) view.findViewById(R.id.textview_fret_number_fretboard);
	            	fretNumber.setText(String.valueOf(j));
	            }
	            
	            // set fret markers on last string
	            if (i == 0) {
	            	// add single fret marker
	            	if (j == 3 || j == 5 || j == 7 || j == 9 || j == 12 || j == 15 || j == 17 || j == 19 || j == 21 || j == 24) {
	            		View fretmarker = new View(this);
	            		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(pixels, pixels);
	            		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	            		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	            		params.setMargins(0, 0, margin, margin);
	            		fretmarker.setBackgroundResource(R.drawable.fretboard_marker_circle);
	            		int fretmarker1 = 1;
	            		fretmarker.setId(fretmarker1);
	            		
	            		RelativeLayout fretboardTableItem = (RelativeLayout) view.findViewById(R.id.layout_fretboard_table_item);
	            		fretboardTableItem.addView(fretmarker, params);
	            		
	            		// add second fret marker
	            		if (j == 12 || j == 24) {
	            			View fretmarker2 = new View(this);
		            		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(pixels, pixels);
		            		params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		            		params2.setMargins(0, 0, margin, 0);
		            		params2.addRule(RelativeLayout.ABOVE, fretmarker1);
		            		fretmarker2.setBackgroundResource(R.drawable.fretboard_marker_circle);
		            		
		            		fretboardTableItem.addView(fretmarker2, params2);
	            		}
	            	}
	            }
	            
	            // NON-VACANT FRET
	            ResultData currData = currString.get(j);
	            if (currData != null) {
	            	TextView resultDataInterval = (TextView) view.findViewById(R.id.textview_result_data_interval_fretboard);
		            resultDataInterval.setText(currData.noteInterval);
		            
		    		TextView resultDataName = (TextView) view.findViewById(R.id.textview_result_data_name_fretboard);
		    		resultDataName.setText(currData.noteName);
		    		
		    		// set fret color
		    		if (markedNotes.contains(currData.noteName)) {
						if (currData.noteInterval == "P1")
							view.setBackgroundResource(R.drawable.fretboard_item_background_green);
						else
							view.setBackgroundResource(R.drawable.fretboard_item_background_purple);
					} else
						view.setBackgroundResource(R.drawable.fretboard_item_background_2);
		    		
		    		// save fret information to the view and attach OnClickListener
					view.setTag(new FretData(currData.noteName, currData.noteInterval));
					view.setOnClickListener(new FretboardOnClickListener());
					
					// save view to cache, indexed by note
					if (!fretboardViewCache.containsKey(currData.noteName))
						fretboardViewCache.put(currData.noteName, new ArrayList<View>());
					fretboardViewCache.get(currData.noteName).add(view);
	            }
	            
	            // add fret to row
	    		newRow.addView(view);
			}
			
			// add row to fretboard
			tableFretboard.addView(newRow);
		}
		
		// done - show table
		showHorizontalView();
	}
	
	/**
	 * Class containing the fret data to be cached into the tag of the View representing that fret.
	 * Used to pass information to the OnClickListener
	 */
	private class FretData {
		String note;
		String interval;
		
		FretData(String note, String interval) {
			this.note = note;
			this.interval = interval;
		}
	}
	
	/**
	 * OnClickListener for Views representing the frets, generates a response based on the data cached into the View's tag.
	 * TODO add API level-dependant PopupMenu
	 */
	private class FretboardOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			FretData fretData = (FretData) v.getTag();
			
			// toggle views
			toggleMark(fretData.note);
			for (View view : fretboardViewCache.get(fretData.note)) {
				if (markedNotes.contains(fretData.note)) {
					if (fretData.interval == "P1")
						view.setBackgroundResource(R.drawable.fretboard_item_background_green);
					else
						view.setBackgroundResource(R.drawable.fretboard_item_background_purple);
				} else
					view.setBackgroundResource(R.drawable.fretboard_item_background_2);
			}
		}
	}
	
	/**
	 * Generate note data using the Library and set up the Piano View.
	 */
	private void setPianoViews() {
		List<ResultData> list = library.getNotes(sequence, root, "C", 12, true);

		clearPiano();

		for (int i = 0; i < 12; i++) {
			ResultData curr = list.get(i);
			if (curr != null) {
				textViewPianoKeys[i * 2].setText(curr.noteInterval);
				textViewPianoKeys[i * 2 + 1].setText(curr.noteName);
			}
		}

		showPianoView();
	}
	
	/**
	 * Convenience function that brings in a featured View and dismisses the rest. 
	 */
	private void showListView() {
		listView.setVisibility(View.VISIBLE);
		viewFretboard.setVisibility(View.GONE);
		layoutPianoView.setVisibility(View.GONE);
	}
	
	/**
	 * Convenience function that brings in a featured View and dismisses the rest. 
	 */
	private void showHorizontalView() {
		listView.setVisibility(View.GONE);
		viewFretboard.setVisibility(View.VISIBLE);
		layoutPianoView.setVisibility(View.GONE);
	}
	
	/**
	 * Convenience function that brings in a featured View and dismisses the rest. 
	 */
	private void showPianoView() {
		listView.setVisibility(View.GONE);
		viewFretboard.setVisibility(View.GONE);
		layoutPianoView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Loads the MIDI note map with starting MIDI notes, to be used when generating the MIDI demonstration notes.
	 */
	private void initializeMidi() {
		midiNoteMap = new HashMap<String, Integer>();
		
		midiNoteMap.put("C", 60);
		midiNoteMap.put("D", 62);
		midiNoteMap.put("E", 64);
		midiNoteMap.put("F", 65);
		midiNoteMap.put("G", 67);
		midiNoteMap.put("A", 69);
		midiNoteMap.put("B", 71);
	}
	
	/**
	 * Attaches the view holders for the Piano View into an array.
	 */
	private void initializePianoKeys() {
		textViewPianoKeys = new TextView[24];
		
		textViewPianoKeys[0] = (TextView) findViewById(R.id.textview_piano_key_c_interval);
		textViewPianoKeys[1] = (TextView) findViewById(R.id.textview_piano_key_c_note);
		textViewPianoKeys[2] = (TextView) findViewById(R.id.textview_piano_key_csh_interval);
		textViewPianoKeys[3] = (TextView) findViewById(R.id.textview_piano_key_csh_note);
		textViewPianoKeys[4] = (TextView) findViewById(R.id.textview_piano_key_d_interval);
		textViewPianoKeys[5] = (TextView) findViewById(R.id.textview_piano_key_d_note);
		textViewPianoKeys[6] = (TextView) findViewById(R.id.textview_piano_key_dsh_interval);
		textViewPianoKeys[7] = (TextView) findViewById(R.id.textview_piano_key_dsh_note);
		textViewPianoKeys[8] = (TextView) findViewById(R.id.textview_piano_key_e_interval);
		textViewPianoKeys[9] = (TextView) findViewById(R.id.textview_piano_key_e_note);
		textViewPianoKeys[10] = (TextView) findViewById(R.id.textview_piano_key_f_interval);
		textViewPianoKeys[11] = (TextView) findViewById(R.id.textview_piano_key_f_note);
		textViewPianoKeys[12] = (TextView) findViewById(R.id.textview_piano_key_fsh_interval);
		textViewPianoKeys[13] = (TextView) findViewById(R.id.textview_piano_key_fsh_note);
		textViewPianoKeys[14] = (TextView) findViewById(R.id.textview_piano_key_g_interval);
		textViewPianoKeys[15] = (TextView) findViewById(R.id.textview_piano_key_g_note);
		textViewPianoKeys[16] = (TextView) findViewById(R.id.textview_piano_key_gsh_interval);
		textViewPianoKeys[17] = (TextView) findViewById(R.id.textview_piano_key_gsh_note);
		textViewPianoKeys[18] = (TextView) findViewById(R.id.textview_piano_key_a_interval);
		textViewPianoKeys[19] = (TextView) findViewById(R.id.textview_piano_key_a_note);
		textViewPianoKeys[20] = (TextView) findViewById(R.id.textview_piano_key_ash_interval);
		textViewPianoKeys[21] = (TextView) findViewById(R.id.textview_piano_key_ash_note);
		textViewPianoKeys[22] = (TextView) findViewById(R.id.textview_piano_key_b_interval);
		textViewPianoKeys[23] = (TextView) findViewById(R.id.textview_piano_key_b_note);
	}
	
	/**
	 * Clears all text data in the Piano View.
	 */
	private void clearPiano() {
		for (int i = 0; i < 24; i++)
			textViewPianoKeys[i].setText("");
	}
	
	/**
	 * Toggles the marked state of the specified note and recompiles the MIDI sequence used for audio demonstration.
	 * @param note the specified note to toggle the marked state for. 
	 */
	private void toggleMark(String note) {
		if (markedNotes.contains(note)) {
			markedNotes.remove(note);
			storeMidiSequence();
			return;
		}
		
		markedNotes.add(note);
		storeMidiSequence();
		return;
	}
	
	/**
	 * Fine tune screen for smaller "normal" screens
	 */
	private void supportScreen() {
		DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    double x = Math.pow(dm.widthPixels/dm.xdpi, 2);
	    double y = Math.pow(dm.heightPixels/dm.ydpi, 2);
	    double screenInches = Math.sqrt(x+y);
	    
	    if (screenInches < 4.5) {
	    	for (TextView t : textViewPianoKeys)
	    		t.setTextSize(10);
	    }
	}
	
	/**
	 * Restores the progress saved in an InstanceData object generated during screen orientation change.
	 * @param instanceData object containing progress information about the current Library session.
	 */
	private void restoreInstance(InstanceData instanceData) {
		if (instanceData.savedCurrentView == -1)
			return;
		
		sequence = instanceData.savedSequence;
		root = instanceData.savedRoot;
		currentView = instanceData.savedCurrentView;
		markedNotes = instanceData.savedMarkedNotes;
		
		textViewRoot.setText(root);
		textViewSequence.setText(sequence);
		spinnerViewSelect.setSelection(currentView);
		
		generateView(currentView);
	}
	
	/**
	 * Object containing progress information about the current Library session, to be generated during screen orientation change.
	 */
	private class InstanceData {
		String savedSequence;
		String savedRoot;
		int savedCurrentView;
		Set<String> savedMarkedNotes;
		
		InstanceData() {
			savedSequence = new String(sequence);
			savedRoot = new String(root);
			savedCurrentView = currentView;
			savedMarkedNotes = new HashSet<String>();
			for (String s : markedNotes)
				savedMarkedNotes.add(s);
		}
	}
	
	/**
	 * Return an object containing progress information about the current Library session, to be restored in onCreate().
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
	    return new InstanceData();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mediaPlayer.release();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		mediaPlayer = new MediaPlayer();
	}
	
	/**
	 * Custom ResultData list view adapter.
	 */
	private class ResultDataListViewAdapter extends ArrayAdapter<ResultData> {
		private final Context activity;
		private final List<ResultData> results;
		private final Set<String> markedNotes;
		
		ResultDataListViewAdapter(Context activity, List<ResultData> results, Set<String> markedNotes) {
			super(activity, R.layout.result_data_list_item, results);
			this.activity = activity;
			this.results = results;
			this.markedNotes = markedNotes;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        ResultDataView resultDataView = null;
	 
	        if (view == null) {
	        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
	            view = inflater.inflate(R.layout.result_data_list_item, null);
	 
	            // hold the view objects in an object, so they don't need to be re-fetched
	            resultDataView = new ResultDataView();
	            resultDataView.name = (TextView) view.findViewById(R.id.textview_result_data_name);
	            resultDataView.interval = (TextView) view.findViewById(R.id.textview_result_data_interval);
	 
	            // cache the view objects in the tag, so they can be re-accessed later
	            view.setTag(resultDataView);
	        } else
	        	resultDataView = (ResultDataView) view.getTag();
	 
	        // set up view
	        ResultData currResult = (ResultData) results.get(position);
	        resultDataView.name.setText(currResult.noteName);
	        resultDataView.interval.setText(currResult.noteInterval);
	        
	        if (markedNotes.contains(currResult.noteName)) {
	        	if (currResult.noteInterval == "Root")
	        		view.setBackgroundResource(R.drawable.fretboard_item_background_green);
	        	else
	        		view.setBackgroundResource(R.drawable.fretboard_item_background_purple);
	        } else
	        	view.setBackgroundResource(0);
	 
	        return view;
	    }
	 
	    class ResultDataView {
	        TextView name;
	        TextView interval;
	    }
	}
}
