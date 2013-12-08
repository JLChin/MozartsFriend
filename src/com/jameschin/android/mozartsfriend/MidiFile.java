package com.jameschin.android.mozartsfriend;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;

/**
 * MidiFile
 * 
 * Class for generating MIDI files. This is necessary since javax.sound.midi is currently not available on Android.
 * 
 * This class takes parameters from the Library and Jam Track activities and generates a MIDI file in real-time.
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class MidiFile {
	// DEFAULT SETTINGS
	static final int DEFAULT_NOTE = 60; // C4 aka "Middle C"
	static final int DEFAULT_VELOCITY = 100;
	static final int DEFAULT_LOOP_COUNT = 100;
	static final int SIXTEENTH_NOTE_TRIPLET = 4;
	static final int SIXTEENTH_NOTE = 6;
	static final int TRIPLET = 8;
	static final int EIGHTH_NOTE = 12;
	static final int QUARTER_NOTE = 24;
	static final int HALF_NOTE = 48;
	static final int WHOLE_NOTE = 96;
	
	// STATE VARIABLES
	private int key;
	private int tempo;
	private int[] tempoEvent;
	private TrackData track;	
	private Vector<int[]> playEvents;
	
	// SYSTEM
	private Context context;

	/**
	 * MIDI file header for a one-track file.
	 * Includes file header and track header.
	 */
	static final int[] header = { 0x4d, 0x54, 0x68, 0x64, // file header
		0x00, 0x00, 0x00, 0x06, // chunk size of file header, always 6 (the proceeding 6 bytes)
		0x00, 0x00, // single-track format
		0x00, 0x01, // one track
		0x00, 0x18, // 24 ticks per quarter note
		0x4d, 0x54, 0x72, 0x6B }; // track chunk header

	/**
	 * MIDI meta event to signal the end of a track.
	 */
	static final int[] footer = { 0x01,
		0xFF, // meta event
		0x2F, // type
		0x00 // length
	};

	/**
	 * MIDI meta event to set the key signature.
	 */
	static final int[] keySigEvent = { 0x00,
		0xFF, // meta event
		0x59, // type
		0x02, // length
		0x00, // C
		0x00 // major
	};

	/**
	 * MIDI meta event to set the time signature. Included as format but not actually used.
	 */
	static final int[] timeSigEvent = { 0x00,
		0xFF, // meta event
		0x58, // type
		0x04, // length
		0x04, // numerator
		0x02, // denominator, power of two
		0x30, // ticks per click
		0x08 // 32nd notes per quarter note
	};

	/**
	 * Construct a new MidiFile with an empty playback event list.
	 * Used in Library Mode.
	 * @param context parent context.
	 */
	public MidiFile(Context context) {
		this.context = context;
		playEvents = new Vector<int[]>();
		
		setTempoEvent();
	}
	
	/**
	 * Construct a new MidiFile using Jam Track data (modulated by user-specified key and tempo) to fill the playback event list.
	 * Used in Jam Tracks Mode.
	 * @param context parent context.
	 * @param key integer distance from the default key, Middle C.
	 * @param tempo track tempo in beats per minute.
	 * @param track custom container containing all the track information, generated using raw data from the selected Jam Track.
	 */
	public MidiFile(Context context, int key, int tempo, TrackData track) {
		playEvents = new Vector<int[]>();
		this.context = context;
		this.key = key;
		this.tempo = tempo;
		this.track = track;
		
		setTempoEvent();
		constructTrack();
	}
	
	/**
	 * Generate the MIDI meta event to set the track tempo.
	 * Parameter is set as microseconds per quarter note.
	 * Use preset tempo for Library Mode or calculate the user-specified tempo for Jam Tracks Mode.
	 */
	private void setTempoEvent() {
		tempoEvent = new int[7];
		tempoEvent[0] = 0x00;
		tempoEvent[1] = 0xFF; // meta event
		tempoEvent[2] = 0x51; // type
		tempoEvent[3] = 0x03; // length
		
		if (tempo == 0) { // 500,000 microseconds per quarter note, or 120 bpm
			tempoEvent[4] = 0x07;
			tempoEvent[5] = 0xA1;
			tempoEvent[6] = 0x20;
		} else {
			// (microseconds per minute) / (beats per minute) = microseconds per beat
			int mpb = 60000000 / tempo;
			tempoEvent[4] = mpb / 65536;
			tempoEvent[5] = (mpb - tempoEvent[4] * 65536) / 256;
			tempoEvent[6] = (mpb - tempoEvent[4] * 65536 - tempoEvent[5] * 256);
		}
	}
	
	/**
	 * Fill the playback event list with Jam Track data, modulated by user-specified key and tempo.
	 * Used in Jam Tracks mode.
	 */
	private void constructTrack() {
		List<List<MidiEvent>> midiEvents = convertNotesToEvents();
		
		int numOfInstruments = track.instrumentPrograms.length;
		int[] index = new int[numOfInstruments]; // array containing indexes for each instrument
		int emptyCount = 0;
		int time = 0; // current time index
		int program = 0; // current program
		
		while (emptyCount < numOfInstruments) {
			// find the instrument with the event that comes next in chronological order
			int minInstrument = -1;
			int minValue = -1;
			emptyCount = 0;
			
			for (int i = 0; i < numOfInstruments; i++) {
				List<MidiEvent> currInstrument = midiEvents.get(i);
				if (index[i] >= currInstrument.size())
					emptyCount++; // instrument i has no more events
				else {
					int currTime = currInstrument.get(index[i]).time;
					if (currTime <= minValue || minValue == -1) {
						minValue = currTime;
						minInstrument = i;
					}
				}
			}
			
			// minInstrument has the event that comes next, write it
			if (minInstrument != -1) {
				int delta = minValue - time;
				MidiEvent event = midiEvents.get(minInstrument).get(index[minInstrument]);
				
				if (track.instrumentNames[minInstrument].equals("Drums")) {
					if (event.on)
						drumOn(delta, event.number, DEFAULT_VELOCITY);
					else
						drumOff(delta, event.number);
				} else { // regular instrument
					// change to correct instrument if necessary
					int instrument = track.instrumentPrograms[minInstrument];
					if (program != instrument)
						progChange(instrument);
					
					if (event.on)
						noteOn(delta, event.number + key, DEFAULT_VELOCITY);
					else
						noteOff(delta, event.number + key);
				}
				
				time = minValue; // update time counter
				index[minInstrument]++; // advance index of respective instrument
			}
		}
		
		// REPEAT LOOP DATA
		// MediaPlayer.setLooping() results in an unacceptable delay between loops, so we create a longer track instead
		int playEventsSize = playEvents.size();
		
		for (int i = 0; i < DEFAULT_LOOP_COUNT; i++) {
			for (int j = 0; j < playEventsSize; j++)
				playEvents.add(playEvents.get(j));
		}
	}
	
	/**
	 * Convert the Lists of MidiNotes to Lists of MidiEvents, one list per instrument.
	 * @return the result List of List of MidiEvents.
	 */
	private List<List<MidiEvent>> convertNotesToEvents() {
		List<List<MidiEvent>> midiEvents = new ArrayList<List<MidiEvent>>();
		
		for (List<MidiNote> noteList : track.instrumentNotes){
			List<MidiEvent> newEventList = new ArrayList<MidiEvent>();
			
			for (MidiNote note : noteList) {
				newEventList.add(new MidiEvent(note.number, note.time, true));
				newEventList.add(new MidiEvent(note.number, note.time + note.duration, false));
			}
			
			newEventList = sortByTime(newEventList);
			
			midiEvents.add(newEventList);
		}
		
		return midiEvents;
	}
	
	/**
	 * QuickSort algorithm to sort the MidiEvents by time.
	 * Space: O(log n)
	 * Time: O(n log n) where n is the number of MidiEvents.
	 * @param eventList the List of MidiEvents to sort by time.
	 * @return the sorted List of MidiEvents.
	 */
	private List<MidiEvent> sortByTime(List<MidiEvent> eventList) {
		int length = eventList.size();
		MidiEvent[] eventArray = new MidiEvent[length];
		for (int i = 0; i < length; i++)
			eventArray[i] = eventList.get(i);
		
		sort(eventArray, 0, length - 1);
		
		List<MidiEvent> result = new ArrayList<MidiEvent>();
		for (MidiEvent m : eventArray)
			result.add(m);
		
		return result;
	}
	
	/**
	 * sortByTime QuickSort recursive helper function.
	 * @param eventArray the array of MidiEvents to sort by time.
	 * @param start the starting index to be sorted.
	 * @param end the ending index to be sorted.
	 */
	private void sort(MidiEvent[] eventArray, int start, int end) {
		if (start >= end)
			return;
		
		int pivot = partition(eventArray, start, end);
		sort(eventArray, start, pivot - 1);
		sort(eventArray, pivot + 1, end);
	}
	
	/**
	 * sortByTime QuickSort helper function to choose a pivot and sort the index range by greater-than/less-than-equal-to the pivot.
	 * @param eventArray the array of MidiEvents to sort by time.
	 * @param start the starting index to be sorted.
	 * @param end the ending index to be sorted.
	 * @return the final index position of the chosen pivot.
	 */
	private int partition(MidiEvent[] eventArray, int start, int end) {
		// select last entry as pivot
		int pivotValue = eventArray[end].time;
		int pivotIndex = end--;
		
		while (start <= end) {
			if (eventArray[start].time <= pivotValue)
				start++;
			else {
				MidiEvent temp = eventArray[start];
				eventArray[start] = eventArray[end];
				eventArray[end--] = temp;
			}
		}
		
		// swap pivot into final position
		MidiEvent temp = eventArray[pivotIndex];
		eventArray[pivotIndex] = eventArray[start];
		eventArray[start] = temp; 
		
		return start;
	}

	/**
	 * Write the stored MIDI events to a file.
	 * @param filename the name of the MIDI file to be created.
	 * @throws IOException
	 */
	public void writeToFile(String filename) throws IOException {
		FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);

		outputStream.write(intArrayToByteArray(header));

		// calculate track length, including the footer but not the track header
		int size = tempoEvent.length + keySigEvent.length + timeSigEvent.length + footer.length;

		for (int i = 0; i < playEvents.size(); i++)
			size += playEvents.elementAt(i).length;

		// write track data size in big-endian format, note this math is only valid for up to 64k of data
		int high = size / 256;
		int low = size - (high * 256);
		outputStream.write((byte) 0);
		outputStream.write((byte) 0);
		outputStream.write((byte) high);
		outputStream.write((byte) low);

		// write metadata events
		outputStream.write(intArrayToByteArray(tempoEvent));
		outputStream.write(intArrayToByteArray(keySigEvent));
		outputStream.write(intArrayToByteArray(timeSigEvent));

		// write data
		for (int i = 0; i < playEvents.size(); i++) {
			outputStream.write(intArrayToByteArray(playEvents.elementAt(i)));
		}

		outputStream.write(intArrayToByteArray(footer));
		outputStream.close();
	}

	/**
	 * Convert an integer array containing unsigned bytes into a byte array.
	 * @param inArray int input array containing unsigned bytes.
	 * @return converted byte array.
	 */
	private byte[] intArrayToByteArray(int[] inArray) {
		int length = inArray.length;
		byte[] outArray = new byte[length];
		
		for (int i = 0; i < length; i++)
			outArray[i] = (byte) inArray[i];
		
		return outArray;
	}

	/**
	 * Add a note-on event to the track.
	 * @param delta ticks since the last event.
	 * @param note MIDI note number.
	 * @param velocity MIDI note velocity.
	 */
	public void noteOn(int delta, int note, int velocity) {
		int[] data = new int[4];
		data[0] = delta; // Delta Time, ticks since the last event
		data[1] = 0x90; // Note On Event, Channel 1 (0)
		data[2] = note; // Parameter 1
		data[3] = velocity; // Parameter 2
		playEvents.add(data);
	}

	/**
	 * Add a note-off event to the track.
	 * @param delta ticks since the last event.
	 * @param note MIDI note number.
	 */
	public void noteOff(int delta, int note) {
		int[] data = new int[4];
		data[0] = delta; // Delta Time, ticks since the last event
		data[1] = 0x80; // Note Off Event, Channel 1 (0)
		data[2] = note; // Parameter 1
		data[3] = 0; // Parameter 2
		playEvents.add(data);
	}
	
	/**
	 * Add a drum note-on event to the track.
	 * @param delta ticks since the last event.
	 * @param note MIDI note number.
	 * @param velocity MIDI note velocity.
	 */
	public void drumOn(int delta, int note, int velocity) {
		int[] data = new int[4];
		data[0] = delta; // Delta Time, ticks since the last event
		data[1] = 0x99; // Note On Event, Channel 10 (9)
		data[2] = note; // Parameter 1
		data[3] = velocity; // Parameter 2
		playEvents.add(data);
	}

	/**
	 * Add a drum note-off event to the track.
	 * @param delta ticks since the last event.
	 * @param note MIDI note number.
	 */
	public void drumOff(int delta, int note) {
		int[] data = new int[4];
		data[0] = delta; // Delta Time, ticks since the last event
		data[1] = 0x89; // Note Off Event, Channel 10 (9)
		data[2] = note; // Parameter 1
		data[3] = 0; // Parameter 2
		playEvents.add(data);
	}

	/**
	 * Add a program-change event at the current position.
	 * @param prog MIDI program to change to.
	 */
	public void progChange(int prog) {
		int[] data = new int[3];
		data[0] = 0; // Delta Time = 0, do it now
		data[1] = 0xC0; // Program Change Event, Channel 1 (0)
		data[2] = prog; // Parameter 1
		playEvents.add(data);
	}

	/**
	 * Store a note-on event followed by a note-off event a note length later.
	 * There is no delta value — the note is assumed to follow the previous one
	 * with no gap.
	 */
	public void noteOnOffNow(int duration, int note, int velocity) {
		noteOn(0, note, velocity);
		noteOff(duration, note);
	}
	
	/**
	 * Class representing a MIDI note event.
	 */
	private class MidiEvent {
		int number;
		int time;
		boolean on;

		MidiEvent(int number, int time, boolean on) {
			this.number = number;
			this.time = time;
			this.on = on;
		}
	}
	
	/**
	 * MidiNote
	 * 
	 * Class representing a MIDI note.
	 * 
	 * number: 60 = middle C
	 * time: 0 = start of track
	 * duration: 24 = quarter note
	 */
	static class MidiNote {
		int number;
		int time;
		int duration;

		MidiNote(int number, int time, int duration) {
			this.number = number;
			this.time = time;
			this.duration = duration;
		}
	}

	/**
	 * Track Data
	 * 
	 * Default time division = 24 ticks per quarter note
	 * Songs should be in the default key, Middle C, so the program can properly transpose keys as necessary later.
	 * 
	 * 
	 * TRACK FILE SPECIFICATION:
	 * 
	 * Track Name
	 * Default Tempo
	 * Names of Instruments and their Program Numbers, separated by comma. (Piano, 0, Bass, 33, ...)
	 * Instrument0 notes: Note Number, Time of Attack, Duration separated by comma. (60, 0, 48, 63, 0, 48, ...)
	 * Instrument1 notes
	 * ...
	 */
	static class TrackData {
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
}
