package com.jameschin.android.mozartsfriend;

import android.app.AlertDialog;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;

/**
 * Chromatic Tuner
 * 
 * Type: Modified Autocorrelation Algorithm
 * Range: Approximately F#0 - A4, 23Hz - 440Hz, varies by configuration
 * Accuracy: +/- 0.84 cent
 * 
 * Space: O(1)
 * Time: O(n^2) where n is the window length.
 * 
 * Tradeoffs:
 *  - Not as fast as Fast Fourier Transform (FFT) algorithm but more accurate and reliable. More resistant to background noise and other signal irregularities.
 * 
 * Notes:
 * BUFFER_SIZE_IN_BYTES should be at least twice as long as any wavelength to be discerned, four times in the case of 16-bit audio since it is two bytes per one sample.
 * SAMPLES_PER_CYCLE = SAMPLE_RATE_IN_HZ / FREQUENCY_IN_HZ.
 * So for 16-bit, (BUFFER_SIZE_IN_BYTES / 4) must be greater than (SAMPLE_RATE_IN_HZ / LOWEST_FREQUENCY_IN_HZ).
 * The lowest frequency discernible is bound by the buffer size.
 * The highest frequency discernible is bound by the sampling rate.
 * 
 * Smoothing Optimizations:
 * - Discard first read after silence, since the attack of musical notes usually overshoots the steady-state range.
 * - Buffer one cycle against an undesirable read that falls outside the JITTER_THRESHOLD.
 * - Buffer a specified number of cycles against drop outs in readings.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class Tuner implements Runnable {
	// DEFAULT SETTINGS
	private static final int AUDIO_SOURCE = AudioSource.CAMCORDER; // CAMCORDER is tuned for noise suppression and far talk, default to main mic otherwise
	private static final int[] SAMPLE_RATE_IN_HZ = {44100, 48000, 22050, 16000};
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_IN_BYTES = 8320; // up from 4096, should handle SGS and basses
    private static final byte MAX_DROPOUT_BUFFER = 2;
    private static final double JITTER_THRESHOLD = 0.9;
    private static final double[] FREQUENCIES = {49.00, 51.91, 55.00, 58.27, 61.74, 65.41, 69.30, 73.42, 77.78, 82.41, 87.31, 92.50, 98.00, 103.83, 110.00, 116.54, 123.47, 130.81, 138.59, 146.83, 155.56, 164.81, 174.61, 185.00, 196.00, 207.65, 220.00, 233.08, 246.94, 261.63, 277.18, 293.66, 311.13, 329.63, 349.23, 369.99};
    
    // STATE VARIABLES
    private int sample_rate;
    private int buffer_size;
    
    // SYSTEM
    private TunerActivity parent;
    private AudioRecord recorder;
    private Handler handler;
    
    public Tuner(TunerActivity parent, Handler handler) {
            this.parent = parent;
            this.handler = handler;
    }
    
    /**
     * Returns the index of the closest note to the current frequency.
     * @param frequency the current discovered frequency.
     * @return the index of the closest note to the current frequency.
     */
    private int getClosestNote(double frequency) {
        double minDist = Double.MAX_VALUE;
        int closestIndex = -1;
        
        for (int i = 1; i < FREQUENCIES.length - 1; i++) {
            double dist = Math.abs(FREQUENCIES[i] - frequency);
            if (dist < minDist) {
                minDist = dist;
                closestIndex = i;
            }
        }
        
        return closestIndex;
    }
    
    /**
     * Returns the normalized distance between the current frequency and the next closest frequency, between -0.5 and +0.5.
     * @param closestNote index of the closest note to the current frequency.
     * @param normalizedFreq normalized value of the current frequency.
     * @return the normalized distance between the current frequency and the next closest frequency, between -0.5 and +0.5.
     */
    private double getDistanceRatio(int closestNote, double normalizedFreq) {
    	double closestFreq = FREQUENCIES[closestNote];
    	
    	double distance = normalizedFreq - closestFreq;
		if (distance >= 0)
			return distance / (FREQUENCIES[closestNote + 1] - closestFreq); // positive value between 0, 0.5
		else
			return distance / (closestFreq - FREQUENCIES[closestNote - 1]); // negative value between 0, -0.5
    }
	
	/**
	 * Returns the normalized frequency within the range of defined frequencies.
	 * @param frequency the current discovered frequency.
	 * @return the normalized frequency within the range of defined frequencies.
	 */
    private double getNormalizedFrequency(double frequency) {
        while ( frequency < 51.91 ) {
        	frequency = 2 * frequency;
        }
        
        while ( frequency > 349.23 ) {
        	frequency = 0.5 * frequency;
        }
        
        return frequency;
    }
    
    /**
     * Initialize AudioRecord instance, iterating through preferred configurations.
     * @return true if successful, false if uninitialized.
     */
    public boolean initializeAudioRecord() {
    	for (int i = 0; i < SAMPLE_RATE_IN_HZ.length; i++) {
			sample_rate = SAMPLE_RATE_IN_HZ[i];
			
			int minBufferSize = AudioRecord.getMinBufferSize(sample_rate, CHANNEL_CONFIG, AUDIO_FORMAT);
			buffer_size = (minBufferSize > BUFFER_SIZE_IN_BYTES) ? minBufferSize : BUFFER_SIZE_IN_BYTES;
			
			recorder = new AudioRecord(AUDIO_SOURCE, sample_rate, CHANNEL_CONFIG, AUDIO_FORMAT, buffer_size);
			if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
				return true;
			
			recorder.release();
		}
    	
    	ShowError(parent.getString(R.string.tuner_initialize_error));
		return false;
    }
    
    /**
     * Main Tuner.
     */
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		if (!initializeAudioRecord())
			return;
		
		double prevFreq = 0.0;
		int prevClosestNote = -1;
		
		int dropoutCounter = 0;
		int lastGoodNote = -1;
		double lastGoodDistanceRatio = 0.0;
		double lastGoodFrequency = 0.0;
		
		
		// MAIN LOOP
		while (!Thread.interrupted()) {
			byte[] buffer = new byte[buffer_size];

			recorder.startRecording();
			int length = recorder.read(buffer, 0, buffer_size);
			recorder.stop();
			
			// convert two bytes into single 16-bit value
			int[] data = new int[length / 2];
			for (int i = 0; i < length; i += 2) {
				int value = (short) ((buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8));
				data[i >> 1] = value;
			}

			double frequency = 0.0;
			double distanceRatio = 0.0;
			int closestNote = -1;
			int windowLength = data.length / 2;
			
			double prevDiff = 0;
			double prevDx = 0;
			double maxDiff = Double.MIN_VALUE;
			
			double minDiff = Double.MAX_VALUE;
			int minDiffIndex = -1;
			boolean acquiringMinDiff = false;
			
			// AUTOCORRELATION ALGORITHM
			for (int i = 0; i < windowLength; i++) {
				double diff = 0;
				
				for (int j = 0; j < windowLength; j++)
					diff += Math.abs(data[i + j] - data[j]);
				
				double dx = diff - prevDiff;
				
				if (!acquiringMinDiff) {
					// local minimum
					if (prevDx < 0 && dx > 0) {
						// after first peak, start looking for minDiff
						if (diff < (0.3 * maxDiff)) {
							acquiringMinDiff = true;
						}
					}
				} else {
					if (diff < minDiff) {
						minDiff = diff;
						minDiffIndex = i - 1;
					}
				}
				
				prevDx = dx;
				prevDiff = diff;
				if (diff > maxDiff)
					maxDiff = diff;
			}
			
			// DATA ACQUIRED
			if (minDiff != Double.MAX_VALUE){
				frequency = ((double) sample_rate / minDiffIndex); // (SAMPLES_PER_SEC / SAMPLES_PER_CYCLE) = CYCLES_PER_SEC = frequency
				
				// INCREASE DATA CONTINUITY: buffer against sudden large changes in frequency beyond JITTER_THRESHOLD
				if (prevFreq != 0.0 && ((frequency / prevFreq) < JITTER_THRESHOLD || (prevFreq / frequency) < JITTER_THRESHOLD)) {
					frequency = prevFreq;
					prevFreq = 0.0;
				} else
					prevFreq = frequency;
				
				// filter out Euro commas that might be introduced
				double normalizedFreq = Double.valueOf(String.valueOf(getNormalizedFrequency(frequency)).replace(',', '.'));
				
				closestNote = getClosestNote(normalizedFreq);
				distanceRatio = getDistanceRatio(closestNote, normalizedFreq);
			}
			
			if (closestNote == -1) {
				// DROPOUT
				if (dropoutCounter <= MAX_DROPOUT_BUFFER) {
					// INCREASE DATA CONTINUITY: temporarily use last known good data during a drop out
					UpdateUI(lastGoodNote, lastGoodDistanceRatio, lastGoodFrequency);
					dropoutCounter++;
					
					if (dropoutCounter == MAX_DROPOUT_BUFFER) {
						lastGoodNote = -1;
						lastGoodDistanceRatio = 0.0;
						lastGoodFrequency = 0.0;
					}
				}
				// when dropoutCounter reaches max, UI is updated only one time with blank data
			} else { // (closestNote != -1)
				if (prevClosestNote != -1) {
					// GOOD READ
					UpdateUI(closestNote, distanceRatio, frequency);
					
					// store current good data in case of temporary drop out
					lastGoodNote = closestNote;
					lastGoodDistanceRatio = distanceRatio;
					lastGoodFrequency = frequency;
					dropoutCounter = 0;
				}
				// INCREASE DATA CONTINUITY: discard the first read after silence, since the attack usually overshoots the steady-state note
			}

			prevClosestNote = closestNote;
		}
		
		recorder.release();
	}
    
    /**
	 * Displays error dialog on the UI via the parent TunerActivity's handler.
	 * @param msg the error message to display.
	 */
	private void ShowError(final String msg) {
		handler.post(new Runnable() {
			public void run() {
				new AlertDialog.Builder(parent).setTitle(R.string.tuner_alert_title).setMessage(msg).show();
			}
		});
	}
    
	/**
     * Updates the UI via the parent TunerActivity's handler.
     * @param closestNote index of the closest note to the current frequency.
     * @param distanceRatio the normalized distance between the current frequency and the next closest frequency, between -0.5 and +0.5.
     * @param frequency the current discovered frequency.
     */
	private void UpdateUI(final int closestNote, final double distanceRatio, final double frequency) {
		handler.post(new Runnable() {
			public void run() {
				parent.updateDisplay(closestNote, distanceRatio, frequency);
			}
		});
	}
}
