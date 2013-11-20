package com.jameschin.android.mozartsfriend;

/**
 * MidiNote
 * 
 * Class representing a MIDI note.
 * 
 * number: 60 = middle C
 * time: 0 = start of track
 * duration: 24 = quarter note
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class MidiNote {
	int number;
	int time;
	int duration;

	MidiNote(int number, int time, int duration) {
		this.number = number;
		this.time = time;
		this.duration = duration;
	}
}
