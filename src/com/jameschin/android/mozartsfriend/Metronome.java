package com.jameschin.android.mozartsfriend;

/**
 * Metronome
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class Metronome {
	// DEFAULT SETTINGS
	private static final int SAMPLE_RATE_IN_HZ = 8000;
	private static final int SAMPLES_PER_TICK = 1000; // 0.125 sec
	private static final int BUFFER_SIZE_IN_BYTES = 800; // higher for more compatibility/stability, lower for better responsiveness
	
	// STATE VARIABLES
	private int tempo = 112; // BPM
	private double beatSound = 1318.51; // E6
	private double sound = 1760; // A6 P4
	private double intervalMultiplier = 1.334840; // P4
	private byte beats = 2; // 2:4
	private byte interval = 4;
	private int bufferSize;
	private boolean oddTime = false;
	private boolean play = true;

	// SYSTEM
	private AudioGenerator audioGenerator;

	public Metronome() {
		audioGenerator = new AudioGenerator(SAMPLE_RATE_IN_HZ, BUFFER_SIZE_IN_BYTES);
		bufferSize = audioGenerator.createPlayer();
	}

	/**
	 * Main thread loop, repeatedly write()'s to AudioTrack's output stream while (play == true).
	 * TODO note that AudioTrack.write() is a blocking function, we want to ensure this is on a separate thread if we go with a less predictable input stream.
	 */
	public void play() {
		// CALCULATE SILENCE
		int samplesPerBeat = (int) ((60 / tempo) * SAMPLE_RATE_IN_HZ);
		int samplesPerSilence = samplesPerBeat - SAMPLES_PER_TICK;
		int samplesPerHalfSilence = samplesPerBeat / 2 - SAMPLES_PER_TICK;
		
		// INITIALIZE WAVEFORMS
		double silence = 0;
		double[] offbeat = audioGenerator.getSineWave(SAMPLES_PER_TICK, SAMPLE_RATE_IN_HZ, beatSound);
		double[] firstbeat = audioGenerator.getSineWave(SAMPLES_PER_TICK, SAMPLE_RATE_IN_HZ, sound);
		
		// LOOP
		double[] sound = new double[bufferSize];
		int t = 0, s = 0, b = 0;
		play = true;
		audioGenerator.play();
		
		do {
			for (int i = 0; i < sound.length && play; i++) {
				if (t < SAMPLES_PER_TICK) {
					if (b == 0)
						sound[i] = firstbeat[t];
					else
						sound[i] = offbeat[t];
					t++;
				} else {
					sound[i] = silence;
					s++;
					if (s == samplesPerSilence || (s == samplesPerHalfSilence && b == beats - 1 && oddTime)) {
						t = 0;
						s = 0;
						b++;
						if (b == beats)
							b = 0;
					}
				}
			}
			
			audioGenerator.writeSound(sound);
		} while (play);
	}

	/**
	 * Terminates this thread's play() loop. Immediately stops the AudioTrack playback and flushes its buffer.
	 * TODO According to Android documentation, AudioTrack.pause() is supposed to immediately stop immediately without finishing playing through the buffered data. This is currently broken, so instead we reduce the buffer size to increase responsiveness.
	 */
	public void stop() {
		play = false;
		audioGenerator.stopAudioTrack();
	}

	/**
	 * Returns the current tempo.
	 * @return the current tempo.
	 */
	public int getTempo() {
		return tempo;
	}
	
	/**
	 * Sets the tempo, takes effect after play() is restarted.
	 * @param newTempo the new tempo.
	 */
	public void setTempo(int newTempo) {
		tempo = newTempo;
	}
	
	/**
	 * Returns the current meter.
	 * @return the current meter.
	 */
	public String getMeter() {
		switch (beats) {
		case 1: return "1:4";
		case 2: return "2:4";
		case 3: return oddTime ? "5:8" : "3:4";
		case 4: return oddTime ? "7:8" : "4:4";
		case 5: return oddTime ? "9:8" : "5:4";
		}
		
		return "...";
	}
	
	/**
	 * Sets the meter, takes effect after play() is restarted.
	 * @param newMeter the new meter.
	 */
	public void setMeter(int newMeter) {
		switch (newMeter) {
		case 0: beats = 1; oddTime = false; return;
		case 1: beats = 2; oddTime = false; return;
		case 2: beats = 3; oddTime = true; return;
		case 3: beats = 3; oddTime = false; return;
		case 4: beats = 4; oddTime = true; return;
		case 5: beats = 4; oddTime = false; return;
		case 6: beats = 5; oddTime = true; return;
		case 7: beats = 5; oddTime = false; return;
		}
	}
	
	/**
	 * Returns the current tone.
	 * @return the current tone.
	 */
	public String getTone() {
		switch ((int) beatSound) {
		case 880: return "A5";
		case 932: return "A#5";
		case 988: return "B5";
		case 1047: return "C6";
		case 1109: return "C#6";
		case 1175: return "D6";
		case 1245: return "D#6";
		case 1319: return "E6";
		case 1397: return "F6";
		case 1480: return "F#6";
		case 1568: return "G6";
		case 1661: return "G#6";
		case 1760: return "A6";
		}
		
		return "...";
	}
	
	/**
	 * Sets the tone, takes effect after play() is restarted.
	 * @param newTone the new tone.
	 */
	public void setTone(int newTone) {
		switch (newTone) {
		case 0: beatSound = 880; break; //A5
		case 1: beatSound = 932; break; //A#5
		case 2: beatSound = 988; break; //B5
		case 3: beatSound = 1047; break; //C6
		case 4: beatSound = 1109; break; //C#6
		case 5: beatSound = 1175; break; //D6
		case 6: beatSound = 1245; break; //D#6
		case 7: beatSound = 1319; break; //E6
		case 8: beatSound = 1397; break; //F6
		case 9: beatSound = 1480; break; //F#6
		case 10: beatSound = 1568; break; //G6
		case 11: beatSound = 1661; break; //G#6
		case 12: beatSound = 1760; break; //A6
		default: beatSound = 880; //A5
		}
		
		sound = beatSound * intervalMultiplier;
	}
	
	/**
	 * Returns the current interval.
	 * @return the current interval.
	 */
	public String getInterval() {
		switch (interval) {
		case 0: return "m2";
		case 1: return "M2";
		case 2: return "m3";
		case 3: return "M3";
		case 4: return "P4";
		case 5: return "d5";
		case 6: return "P5";
		case 7: return "m6";
		case 8: return "M6";
		case 9: return "m7";
		case 10: return "M7";
		case 11: return "P8";
		}
		
		return "...";
	}
	
	/**
	 * Sets the interval, takes effect after play() is restarted.
	 * @param newInterval the new interval.
	 */
	public void setInterval(int newInterval) {
		switch (newInterval) {
		case 0: interval = 0; intervalMultiplier = 1.059463; break;
		case 1: interval = 1; intervalMultiplier = 1.122462; break;
		case 2: interval = 2; intervalMultiplier = 1.189207; break;
		case 3: interval = 3; intervalMultiplier = 1.259921; break;
		case 4: interval = 4; intervalMultiplier = 1.334840; break;
		case 5: interval = 5; intervalMultiplier = 1.414214; break;
		case 6: interval = 6; intervalMultiplier = 1.498307; break;
		case 7: interval = 7; intervalMultiplier = 1.587401; break;
		case 8: interval = 8; intervalMultiplier = 1.681793; break;
		case 9: interval = 9; intervalMultiplier = 1.781797; break;
		case 10: interval = 10; intervalMultiplier = 1.887749; break;
		case 11: interval = 11; intervalMultiplier = 2; break;
		default: interval = 4; intervalMultiplier = 2;
		}
		
		sound = beatSound * intervalMultiplier; 
	}
	
	/**
	 * Returns the current volume.
	 * @return the current volume.
	 */
	public int getVolume() {
		return audioGenerator.getVolume();
	}
	
	/**
	 * Sets the volume, takes effect after play() is restarted.
	 * @param newVolume the new volume.
	 */
	public void setVolume(int newVolume) {
		audioGenerator.setVolume(newVolume);
	}
}