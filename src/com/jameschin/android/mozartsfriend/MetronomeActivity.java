package com.jameschin.android.mozartsfriend;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Metronome Activity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class MetronomeActivity extends BaseActivity {
	// DEFAULT SETTINGS
	private static final int MAX_TEMPO = 240;
	private static final int MIN_TEMPO = 30;
	private static final long TAP_DURATION_IN_MILLI = 2000;
	private static final int FLASH_DURATION_IN_MILLI = 125;
	private static final int DEFAULT_TEMPO = 112;
	private static final int DEFAULT_METER = 1; // 2:4
	private static final int DEFAULT_VOLUME = 100;
	private static final int DEFAULT_TONE = 7; // E6
	private static final int DEFAULT_INTERVAL = 4; // P4
	
	// VIEW HOLDERS
	private SeekBar seekBarMetronomeTempo;
	private SeekBar seekBarMetronomeMeter;
	private SeekBar seekBarMetronomeVolume;
	private SeekBar seekBarMetronomeTone;
	private SeekBar seekBarMetronomeInterval;
	private TextView textViewTempo;
	private TextView textViewMeter;
	private TextView textViewVolume;
	private TextView textViewTone;
	private TextView textViewInterval;
	private ToggleButton buttonPlay;
	private Button buttonTempoInc;
	private Button buttonTempoDec;
	private Button buttonTap;
	
	// STATE VARIABLES
	private boolean playing;
	private boolean visualFeedback;
	private long lastTap;
	private int tempo;
	private List<Integer> tapTempos;
	
	// SYSTEM
	private SharedPreferences.Editor sharedPrefEditor;
	private WakeLock wakeLock;
	private Thread metronomeThread;
	private Thread tapIndicatorThread;
	private Thread visualFeedbackThread;
	private Metronome metronome;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.metronome);

		initialize();
	}

	private void initialize() {
		metronome = new Metronome();
		
		PowerManager powermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powermanager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MetronomeActivity");
		
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		sharedPrefEditor = sharedPref.edit();
		
		visualFeedback = sharedPref.getBoolean("METRONOME_VISUAL_FEEDBACK", true);
		
		// PLAY TOGGLE
		buttonPlay = (ToggleButton) findViewById(R.id.button_play);
		buttonPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@SuppressLint("Wakelock")
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				synchronized(this) {
					if (isChecked) {
						if (!wakeLock.isHeld())
							wakeLock.acquire();
						
						if (playing == false) {
							playing = true;
							startMetro();
						}
					} else {
						if (wakeLock.isHeld())
							wakeLock.release();
						
						if (playing == true) {
							playing = false;
							stopMetro();
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
					int newTempo = (int) metronome.getTempo();
					newTempo++;
					
					if (playing == true) stopMetro();
					metronome.setTempo(newTempo);
					tempo = newTempo;
					seekBarMetronomeTempo.setProgress(newTempo - MIN_TEMPO);
					textViewTempo.setText(Integer.toString(newTempo));
					
					if (playing == true) startMetro();
					
					sharedPrefEditor.putInt("TEMPO", newTempo);
					sharedPrefEditor.commit();
				}
			}
		});
		
		// DECREMENT TEMPO BUTTON
		buttonTempoDec = (Button) findViewById(R.id.button_tempo_dec);
		buttonTempoDec.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				synchronized (this) {
					int newTempo = (int) metronome.getTempo();
					newTempo--;
					
					if (playing == true) stopMetro();
					metronome.setTempo(newTempo);
					tempo = newTempo;
					seekBarMetronomeTempo.setProgress(newTempo - MIN_TEMPO);
					textViewTempo.setText(Integer.toString(newTempo));
					
					if (playing == true) startMetro();
					
					sharedPrefEditor.putInt("TEMPO", newTempo);
					sharedPrefEditor.commit();
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
					tapIndicatorThread = new Thread(new TapIndicator(new Handler()));
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

						if (playing == true) stopMetro();
						metronome.setTempo(newTempo);
						tempo = newTempo;
						seekBarMetronomeTempo.setProgress(newTempo - MIN_TEMPO);
						textViewTempo.setText(Integer.toString(newTempo));

						if (playing == true) startMetro();
						
						sharedPrefEditor.putInt("TEMPO", newTempo);
						sharedPrefEditor.commit();
					}
				}
			}
		});
		
		try {
			// TEMPO CONTROL FRAME
			seekBarMetronomeTempo = (SeekBar) findViewById(R.id.seekbar_tempo);
			textViewTempo = (TextView) findViewById(R.id.textview_tempo);
			seekBarMetronomeTempo.setMax(MAX_TEMPO - MIN_TEMPO);
			int savedTempo = sharedPref.getInt("TEMPO", DEFAULT_TEMPO);
			seekBarMetronomeTempo.setProgress(savedTempo - MIN_TEMPO);
			metronome.setTempo(savedTempo);
			textViewTempo.setText(String.valueOf(savedTempo));
			tempo = savedTempo;
			seekBarMetronomeTempo.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If metronome is already playing, restart with new parameters
					synchronized(this) {
						sharedPrefEditor.putInt("TEMPO", (int) metronome.getTempo());
						sharedPrefEditor.commit();
						
						if (playing == true) startMetro();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) {
					if (playing == true) stopMetro();
				}
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						int newTempo = progress + MIN_TEMPO;
						metronome.setTempo(newTempo);
						tempo = newTempo;
						textViewTempo.setText(String.valueOf(newTempo));
					}
				}
			});

			// METER CONTROL FRAME
			seekBarMetronomeMeter = (SeekBar) findViewById(R.id.seekbar_meter);
			textViewMeter = (TextView) findViewById(R.id.textview_meter);
			seekBarMetronomeMeter.setMax(7);
			int savedMeter = sharedPref.getInt("METER", DEFAULT_METER);
			seekBarMetronomeMeter.setProgress(savedMeter);
			metronome.setMeter(savedMeter);
			textViewMeter.setText(metronome.getMeter());
			seekBarMetronomeMeter.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If metronome is already playing, restart with new parameters
					synchronized(this) {
						if (playing == true) startMetro();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) {
					if (playing == true) stopMetro();
				}
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						metronome.setMeter(progress);
						textViewMeter.setText(metronome.getMeter());
						
						sharedPrefEditor.putInt("METER", progress);
						sharedPrefEditor.commit();
					}
				}
			});
			
			// VOLUME CONTROL FRAME
			seekBarMetronomeVolume = (SeekBar) findViewById(R.id.seekbar_volume);
			textViewVolume = (TextView) findViewById(R.id.textview_volume);
			seekBarMetronomeVolume.setMax(100);
			int savedVolume = sharedPref.getInt("VOLUME", DEFAULT_VOLUME);
			seekBarMetronomeVolume.setProgress(savedVolume);
			metronome.setVolume(savedVolume);
			textViewVolume.setText(String.valueOf(metronome.getVolume()));
			seekBarMetronomeVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If metronome is already playing, restart with new parameters
					synchronized(this) {
						sharedPrefEditor.putInt("VOLUME", metronome.getVolume());
						sharedPrefEditor.commit();
						
						if (playing == true) startMetro();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) {
					if (playing == true) stopMetro();
				}
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						metronome.setVolume(progress);
						textViewVolume.setText(String.valueOf(metronome.getVolume()));
					}	
				}
			});
			
			// TONE CONTROL FRAME
			seekBarMetronomeTone = (SeekBar) findViewById(R.id.seekbar_tone);
			textViewTone = (TextView) findViewById(R.id.textview_tone);
			seekBarMetronomeTone.setMax(12);
			int savedTone = sharedPref.getInt("TONE", DEFAULT_TONE);
			seekBarMetronomeTone.setProgress(savedTone);
			metronome.setTone(savedTone);
			textViewTone.setText(metronome.getTone());
			seekBarMetronomeTone.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If metronome is already playing, restart with new parameters
					synchronized(this) {
						if (playing == true) startMetro();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) {
					if (playing == true) stopMetro();
				}
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						metronome.setTone(progress);
						textViewTone.setText(metronome.getTone());
						
						sharedPrefEditor.putInt("TONE", progress);
						sharedPrefEditor.commit();
					}
				}
			});

			// INTERVAL CONTROL FRAME
			seekBarMetronomeInterval = (SeekBar) findViewById(R.id.seekbar_interval);
			textViewInterval = (TextView) findViewById(R.id.textview_interval);
			seekBarMetronomeInterval.setMax(11);
			int savedInterval = sharedPref.getInt("INTERVAL", DEFAULT_INTERVAL);
			seekBarMetronomeInterval.setProgress(savedInterval);
			metronome.setInterval(savedInterval);
			textViewInterval.setText(metronome.getInterval());
			seekBarMetronomeInterval.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar arg0) {
					// If metronome is already playing, restart with new parameters
					synchronized(this) {
						if (playing == true) startMetro();
					}
				}
				public void onStartTrackingTouch(SeekBar arg0) {
					if (playing == true) stopMetro();
				}
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					synchronized(this) {
						metronome.setInterval(progress);
						textViewInterval.setText(metronome.getInterval());
						
						sharedPrefEditor.putInt("INTERVAL", progress);
						sharedPrefEditor.commit();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		supportScreen();
	}
	
	/**
	 * Fine tune screen for smaller "normal" screens
	 */
	private void supportScreen() {
		TextView[] fontGroup1 = {textViewTempo, textViewMeter, textViewVolume, textViewTone, textViewInterval};
		TextView[] fontGroup2 = {
			(TextView) findViewById(R.id.textview_tempo_tempo),
			(TextView) findViewById(R.id.textview_meter_meter),
			(TextView) findViewById(R.id.textview_volume_volume),
			(TextView) findViewById(R.id.textview_tone_tone),
			(TextView) findViewById(R.id.textview_interval_interval),
			(TextView) findViewById(R.id.textview_volume_percent),
			(TextView) findViewById(R.id.textview_meter_time),
			(TextView) findViewById(R.id.textview_tempo_bpm)
		};
		RelativeLayout layoutMetronomeTop = (RelativeLayout) findViewById(R.id.layout_metronome_top);
		
		DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    double x = Math.pow(dm.widthPixels/dm.xdpi, 2);
	    double y = Math.pow(dm.heightPixels/dm.ydpi, 2);
	    double screenInches = Math.sqrt(x+y);
	    
	    if (screenInches < 4.5) {
	    	if (layoutMetronomeTop != null)
	    		layoutMetronomeTop.setVisibility(View.GONE);
	    	
	    	for (TextView t : fontGroup1) {
	    		t.setGravity(Gravity.CENTER);
	    		t.setTextSize(40);
	    	}
	    		
	    	for (TextView t : fontGroup2)
	    		t.setTextSize(10);
	    }
	}
	
	/**
	 * Calculates parameters and then starts new metronome playback and visual feedback threads.
	 */
	private void startMetro() {
		// calculate visual feedback parameters first
		int beatDurationInMilli = 60000 / tempo; // (milliseconds per minute) / (beats per minute) = (milliseconds per beat)
		int silenceDurationInMilli = beatDurationInMilli - FLASH_DURATION_IN_MILLI;
		
		// calculate the millisecond duration of one measure
		String meter = metronome.getMeter();
		int numOfBeats = Integer.valueOf(meter.substring(0, 1)); // first number TODO beats above 9?
		int beatDivision = Integer.valueOf(meter.substring(meter.length() - 1)); // last number
		int measureDurationInMilli = (beatDivision == 8) ? (numOfBeats * beatDurationInMilli / 2) : (numOfBeats * beatDurationInMilli);
		
		// ensure old threads are dead before starting new ones
		if (visualFeedbackThread != null && visualFeedbackThread.isAlive())
			try {
				visualFeedbackThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		if (metronomeThread != null && metronomeThread.isAlive())
			try {
				metronomeThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		// START NEW TASKS
		metronomeThread = new Thread(new MetronomeThread());
		metronomeThread.start();
		
		if (visualFeedback) { // if visual feedback is enabled in Settings
			visualFeedbackThread = new Thread(new VisualFeedback(measureDurationInMilli, silenceDurationInMilli, new Handler()));
			visualFeedbackThread.start();
		}
	}
	
	/**
	 * Stops metronome playback and interrupts the visual feedback thread. Both threads then naturally terminate.
	 */
	private void stopMetro() {
		metronome.stop();
		
		if (visualFeedbackThread != null)
			visualFeedbackThread.interrupt();
	}
	
	/**
	 * Thread to separate metronome operation from the UI thread.
	 * When parameters change, the old thread is killed and a new one is initiated with the new parameters.
	 */
	private class MetronomeThread implements Runnable {
		public void run() {
			metronome.play();
		}
	}
	
	/**
	 * Thread to turn the Tap indicator light off after the tap acquisition time window has elapsed.
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
	 * Thread to control the visual feedback for the beat.
	 */
	private class VisualFeedback implements Runnable {
		Handler handler;
		int measureDurationInMilli; // millisecond duration of the total loop
		int silenceDurationInMilli; // milliseconds between the end of the beat flash and the beginning of the next flash
		
		VisualFeedback(int measureDurationInMilli, int silenceDurationInMilli, Handler handler){
			this.measureDurationInMilli = measureDurationInMilli;
			this.silenceDurationInMilli = silenceDurationInMilli;
			this.handler = handler;
		}

		public void run() {
			int remainingMilli;
			boolean firstBeat;
			boolean silence;
			
			while (!Thread.currentThread().isInterrupted()) {
				remainingMilli = measureDurationInMilli;
				firstBeat = true;
				silence = false;
				
				while (remainingMilli > 0) {
					if (!silence) {
						if (firstBeat) {
							setPlayIndicator(R.drawable.button_background_red); // primary ON indicator
							firstBeat = false;
						} else
							setPlayIndicator(R.drawable.tuner_lock_green); // secondary ON indicator
						
						try {
							Thread.sleep(FLASH_DURATION_IN_MILLI);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); // propagate interrupt
							break;
						}
						
						setPlayIndicator(R.drawable.button_background); // OFF
						
						remainingMilli -= FLASH_DURATION_IN_MILLI;
						silence = true;
					} else { // silence, period during which light is off
						int silenceTimeInMilli = (remainingMilli >= silenceDurationInMilli) ? silenceDurationInMilli : remainingMilli;
						
						try {
							Thread.sleep(silenceTimeInMilli);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); // propagate interrupt
							break;
						}
						
						remainingMilli -= silenceTimeInMilli;
						silence = false;
					}
				}
			}
			
			// Thread interrupted, revert Play indicator
			setPlayIndicator(R.drawable.button_background);
		}
		
		/**
		 * Light (or unlight) the Play Button.
		 * @param resID the drawable resource to use for the Play Button background.
		 */
		void setPlayIndicator(final int resID) {
			handler.post(new Runnable() {
				public void run() {
					buttonPlay.setBackgroundResource(resID);
				}			
			});
		}
	}

	@Override
	protected void onStop() {
	    super.onStop();
	    synchronized(this) {
	    	if (wakeLock.isHeld())
	    		wakeLock.release();
	    	
	    	if (playing == true) stopMetro();
	    }
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    synchronized(this) {
	    	if (playing == true) {
	    		if (!wakeLock.isHeld())
	    			wakeLock.acquire();
	    		
	    		startMetro();
	    	}
	    }
	}
}