package com.jameschin.android.mozartsfriend;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Audio Generator
 * Manages Android AudioTrack operations.
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class AudioGenerator {
	// DEFAULT SETTINGS
	private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_MODE = AudioTrack.MODE_STREAM;

    // STATE VARIABLES
	private int sampleRate;
	private int bufferSize;
	private double volume = 1; // min 0, max 1
	
	// SYSTEM
	private AudioTrack audioTrack;

	public AudioGenerator(int sampleRate, int bufferSize) {
		this.sampleRate = sampleRate;
		this.bufferSize = bufferSize;
	}
	
	/**
	 * Create a new AudioTrack instance, using the minimum buffer size if the default size supplied by Metronome is too small.
	 * @return the AudioTrack output buffer size used.
	 */
	public int createPlayer() {
		int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT);
		if (bufferSize < minBufferSize)
			bufferSize = minBufferSize;
		
		audioTrack = new AudioTrack(AUDIO_STREAM, sampleRate, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize, AUDIO_MODE);
		
		return bufferSize;
	}

	/**
	 * Generate waveform samples for a pure sine wave, representing a fundamental tone.
	 * @param samples number of samples of the sine wave to return.
	 * @param sampleRate the rate at which the sine wave is sampled, in samples per second.
	 * @param frequency the frequency of the sine wave.
	 * @return array of waveform samples.
	 */
	public double[] getSineWave(int samples, int sampleRate, double frequency) {
		double[] sample = new double[samples];
		
		for (int i = 0; i < samples; i++) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency));
		}
		
		return sample;
	}

	/**
	 * Write a buffer length of data to the output stream that is already actively playing.
	 * TODO note that AudioTrack.write() is a blocking function, we want to ensure this is on a separate thread if we go with a less predictable input stream.
	 * @param samples array of waveform sample data.
	 */
	public void writeSound(double[] samples) {
		byte[] outSoundChunk = get16BitPcm(samples);
		audioTrack.write(outSoundChunk, 0, outSoundChunk.length);
	}
	
	/**
	 * Convert array of waveform data into a byte array in 16-bit PCM format.
	 * @param samples array of waveform sample data.
	 * @return byte array in 16-bit PCM format.
	 */
	public byte[] get16BitPcm(double[] samples) {
		byte[] outSoundChunk = new byte[2 * samples.length];
		int index = 0;
		
		for (double sample : samples) {
			// scale to maximum amplitude, modulated by the volume
			short upscaledSample = (short) ((sample * Short.MAX_VALUE * volume));
			
			// Little-endian, first byte is the low order byte
			outSoundChunk[index++] = (byte) (upscaledSample & 0x00ff);
			outSoundChunk[index++] = (byte) ((upscaledSample & 0xff00) >>> 8);
		}
		
		return outSoundChunk;
	}

	/**
	 * Immediately stop playback and flush buffer.
	 * TODO According to Android documentation, AudioTrack.pause() is supposed to immediately stop immediately without finishing playing through the buffered data. This is currently broken, so instead we reduce the buffer size to increase responsiveness.
	 */
	public void stopAudioTrack() {
		audioTrack.pause();
		audioTrack.flush();
	}
	
	/**
	 * Begin playback. Since we are operating in MODE_STREAM, this is only called once at the beginning, before looping write()'s.
	 */
	public void play() {
		audioTrack.play();
	}

	/**
	 * Set playback volume.
	 * @param volume integer value 0-100.
	 */
	public void setVolume(int volume) {
		this.volume = ((double) volume) / 100.0;
	}

	/**
	 * Returns the current playback volume level.
	 * @return integer volume value 0-100.
	 */
	public int getVolume() {
		return (int) (volume * 100);
	}
}