package com.jameschin.android.mozartsfriend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Jam Track Activity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class TrackActivity extends BaseActivity {
	// DEFAULT SETTINGS
	private static final int MAX_TEMPO = 240;
	private static final int MIN_TEMPO = 10;
	private static final long TAP_DURATION_IN_MILLI = 2000;
	private static final int DEFAULT_VOLUME = 100;
	private static final int DEFAULT_TONE = 4; // E6
	public static final int DEFAULT_MIDI_VELOCITY = 100;
	public static final String DEFAULT_MIDI_FILE = "midiFile.mid";
	
	// VIEW HOLDERS
	private SeekBar seekBarTempo;
	private SeekBar seekBarVolume;
	private SeekBar seekBarTone;
	private TextView textViewTempo;
	private TextView textViewVolume;
	private TextView textViewTone;
	private TextView textViewTitle;
	private View viewTitle;
	private ToggleButton buttonPlay;
	private Button buttonTempoInc;
	private Button buttonTempoDec;
	private Button buttonTap;

	// STATE VARIABLES
	private boolean playing;
	private String title;
	private int key;
	private int tempo;
	private int volume;
	private long lastTap;
	private List<Integer> tapTempos;
	private TrackData track;
	
	// SYSTEM
	private MediaPlayer mediaPlayer;
	private SharedPreferences.Editor sharedPrefEditor;
	private Thread tapIndicatorThread;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_player);

		loadTrackData();
		initialize();
	}
	
	/**
	 * Get track data from the selected res/raw file.
	 */
	private void loadTrackData() {
		int rawResourceID = getIntent().getIntExtra("rawResourceID", -1);

		InputStream inputStream = getResources().openRawResource(rawResourceID);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;

		try {
			line = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int lineCounter = 0;
		String[] instrumentNames = null;
		int[] instrumentPrograms = null;
		int[] instrumentChannels = null;
		List<List<MidiNote>> instrumentNotes = new ArrayList<List<MidiNote>>();

		while (line != null) {
			switch(lineCounter) {
			case(0):
				// TRACK NAME
				title = line;
				break;
			case(1):
				// TRACK TEMPO
				tempo = Integer.valueOf(line);
				break;
			case(2):
				// TRACK INSTRUMENTS
				String[] instruments = line.split(",");
				int numOfInstruments = instruments.length / 2;
				instrumentNames = new String[numOfInstruments];
				instrumentPrograms = new int[numOfInstruments];
				instrumentChannels = new int[numOfInstruments];

				for (int i = 0, index = 0; i < instruments.length; i += 2, index++) {
					instrumentNames[index] = instruments[i];
					instrumentPrograms[index] = Integer.valueOf(instruments[i + 1]);
					instrumentChannels[index] = (instrumentNames[index].equals("Drums")) ? 9 : 0;
				}
				break;
			default:
				// TRACK NOTE EVENTS
				instrumentNotes.add(new ArrayList<MidiNote>());
				String[] notes = line.split(",");

				int currInstrument = lineCounter - 3;
				List<MidiNote> curr = instrumentNotes.get(currInstrument);

				for (int i = 0; i < notes.length; i += 3) {
					int number = Integer.valueOf(notes[i]);
					int time = Integer.valueOf(notes[i + 1]);
					int duration = Integer.valueOf(notes[i + 2]);
					curr.add(new MidiNote(number, time, duration));
				}
			}

			lineCounter++;
			
			try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		track = new TrackData(title, tempo, instrumentNames, instrumentPrograms, instrumentChannels, instrumentNotes);
	}
	
	private void initialize() {
		mediaPlayer = new MediaPlayer();
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		// TITLE FRAME
		textViewTitle = (TextView) findViewById(R.id.textview_track_title);
		textViewTitle.setText(title);
		viewTitle = (View) findViewById(R.id.view_track_title);
		viewTitle.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		// PLAY TOGGLE
		buttonPlay = (ToggleButton) findViewById(R.id.button_play);
		buttonPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				synchronized (this) {
					if (isChecked) {
						if (playing == false) {
							playing = true;
							play();
						}
					} else {
						if (playing == true) {
							playing = false;
							stop();
						}
					}
				}
			}
		});
		
		// INCREMENT TEMPO BUTTON
		buttonTempoInc = (Button) findViewById(R.id.button_tempo_inc);
		buttonTempoInc.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				synchronized (this) {
					if (tempo < MAX_TEMPO)
						tempo++;
					
					seekBarTempo.setProgress(tempo - MIN_TEMPO);
					textViewTempo.setText(Integer.toString(tempo));
					
					if (playing == true) play();
				}
			}
		});
		
		// DECREMENT TEMPO BUTTON
		buttonTempoDec = (Button) findViewById(R.id.button_tempo_dec);
		buttonTempoDec.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				synchronized (this) {
					if (tempo > MIN_TEMPO)
						tempo--;
					
					seekBarTempo.setProgress(tempo - MIN_TEMPO);
					textViewTempo.setText(Integer.toString(tempo));
					
					if (playing == true) play();
				}
			}
		});
		
		// TAP BUTTON
		buttonTap = (Button) findViewById(R.id.button_tap);
		buttonTap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				synchronized (this) {
					// turn indicator on
					if (tapIndicatorThread != null)
						tapIndicatorThread.interrupt();
					buttonTap.setBackgroundResource(R.drawable.button_background_red);
					tapIndicatorThread = new Thread(new TapIndicator(new Handler()), "Thread - Track Tap Tempo Timer");
					tapIndicatorThread.start();
					
					long newTap = System.currentTimeMillis();
					long gap = newTap - lastTap;
					lastTap = newTap;
						
					if (gap > TAP_DURATION_IN_MILLI || gap < 1)
						tapTempos = new ArrayList<Integer>();
					else {
						tapTempos.add((int) (60 / ((double) gap / 1000)));

						// set BPM with average tap tempo
						int newTempo = 0;
						for (int i : tapTempos)
							newTempo += i;
						newTempo /= tapTempos.size();

						tempo = newTempo;
						seekBarTempo.setProgress(tempo - MIN_TEMPO);
						textViewTempo.setText(Integer.toString(tempo));

						if (playing == true) play();
					}
				}
			}
		});
		
		try {
			// TEMPO CONTROL FRAME
			seekBarTempo = (SeekBar) findViewById(R.id.seekbar_tempo);
			textViewTempo = (TextView) findViewById(R.id.textview_tempo);
			seekBarTempo.setMax(MAX_TEMPO - MIN_TEMPO);
			seekBarTempo.setProgress(tempo - MIN_TEMPO);
			textViewTempo.setText(String.valueOf(tempo));
			seekBarTempo.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If already playing, restart with new parameters
					synchronized(this) {
						if (playing == true)
							play();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) { }
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						tempo = progress + MIN_TEMPO;
						textViewTempo.setText(String.valueOf(tempo));
					}
				}
			});
			
			// VOLUME CONTROL FRAME
			seekBarVolume = (SeekBar) findViewById(R.id.seekbar_volume);
			textViewVolume = (TextView) findViewById(R.id.textview_volume);
			seekBarVolume.setMax(100);
			volume = sharedPref.getInt("VOLUME", DEFAULT_VOLUME);
			seekBarVolume.setProgress(volume);
			float vol = (float) volume / 100;
			mediaPlayer.setVolume(vol, vol);
			textViewVolume.setText(String.valueOf(volume));
			seekBarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If already playing, restart with new parameters
					synchronized(this) {
						sharedPrefEditor.putInt("VOLUME", volume);
						sharedPrefEditor.commit();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) { }
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						volume = progress;
						textViewVolume.setText(String.valueOf(volume));
						float vol = (float) volume / 100;
						mediaPlayer.setVolume(vol, vol);
					}	
				}
			});
			
			// TONE CONTROL FRAME
			seekBarTone = (SeekBar) findViewById(R.id.seekbar_tone);
			textViewTone = (TextView) findViewById(R.id.textview_tone);
			seekBarTone.setMax(11);
			key = sharedPref.getInt("TRACK_TONE", DEFAULT_TONE);
			seekBarTone.setProgress(key);
			textViewTone.setText(getTone(key));
			seekBarTone.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If already playing, restart with new parameters
					synchronized(this) {
						if (playing == true)
							play();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) { }
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						key = progress;
						textViewTone.setText(getTone(key));
						
						sharedPrefEditor.putInt("TRACK_TONE", progress);
						sharedPrefEditor.commit();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void play() {
		// CONSTRUCT MIDI FILE
		MidiFile midiFile = new MidiFile(this, key, tempo, track);
		
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
	
	/**
	 * Simple thread to turn the Tap indicator light off after the acquisition time window has elapsed.
	 */
	private class TapIndicator implements Runnable {
		Handler handler;
		
		TapIndicator(Handler handler) {
			this.handler = handler;
		}
		
		public void run() {
			try {
				Thread.sleep(TAP_DURATION_IN_MILLI);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate interrupt
			}
			
			// note Thread.interrupted() is a static method 
			if (!Thread.currentThread().isInterrupted()) {
				// revert background after tap tempo acquire duration has elapsed
				handler.post(new Runnable() {
					public void run() {
						buttonTap.setBackgroundResource(R.drawable.button_background);
					}
				});
			}
		}
	}
	
	/**
	 * Returns the String representation of the current key.
	 * @param key integer stored as offset from middle C (MIDI value of 60)
	 * @return the String representation of the current key.
	 */
	private String getTone(int value) {
		switch(value) {
		case(0): return "C4";
		case(1): return "C#4";
		case(2): return "D4";
		case(3): return "D#4";
		case(4): return "E4";
		case(5): return "F4";
		case(6): return "F#4";
		case(7): return "G4";
		case(8): return "G#4";
		case(9): return "A4";
		case(10): return "A#4";
		case(11): return "B4";
		}
		
		return "??";
	}
	
	private void stop() {
		mediaPlayer.stop();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		synchronized(this) {
			mediaPlayer.release();
	    }
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		synchronized(this) {
			mediaPlayer = new MediaPlayer();
			float vol = (float) volume / 100;
			mediaPlayer.setVolume(vol, vol);
	    }
	}
}
