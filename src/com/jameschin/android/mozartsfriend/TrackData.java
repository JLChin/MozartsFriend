package com.jameschin.android.mozartsfriend;

import java.util.List;

/**
 * TRACK FILE SPECIFICATION:
 * 
 * Track Name
 * Default Tempo
 * Names of Instruments and their Program Numbers, separated by comma. (Piano, 0, Bass, 33, ...)
 * Instrument0 notes: Note Number, Time of Attack, Duration separated by comma. (60, 0, 48, 63, 0, 48, ...)
 * Instrument1 notes
 * ...
 */

/**
 * Track Data
 * 
 * Default time division = 24 ticks per quarter note
 * Songs should be in the default key, Middle C, so the program can properly transpose keys as necessary later.
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class TrackData {
	// TRACK META-DATA
	String name;
	int defaultTempo;
	
	// INSTRUMENT DATA
	String[] instrumentNames;
	int[] instrumentPrograms;
	int[] instrumentChannels;
	List<List<MidiNote>> instrumentNotes;
	
	TrackData (String name, int defaultTempo, String[] instrumentNames, int[] instrumentPrograms, int[] instrumentChannels, List<List<MidiNote>> instrumentNotes) {
		this.name = name;
		this.defaultTempo = defaultTempo;
		this.instrumentNames = instrumentNames;
		this.instrumentPrograms = instrumentPrograms;
		this.instrumentChannels = instrumentChannels;
		this.instrumentNotes = instrumentNotes;
	}
}
