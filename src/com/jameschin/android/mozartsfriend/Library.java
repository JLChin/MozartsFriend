package com.jameschin.android.mozartsfriend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Musical Library Engine
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class Library {
	private static final String[] NOTE_NAME = { "C", "D", "E", "F", "G", "A", "B" };
	
	private Map<String, Note> noteMap;
	private Map<String, Sequence> sequenceMap;
	
	private class Note {
		String note;
		String[] aliases;
		Note next;
		Note prev;
		
		Note(String note, String[] aliases) {
			this.note = note;
			this.aliases = aliases;
		}
		
		void setNext(Note n) {
			next = n;
			n.prev = this;
		}
		
		boolean equals(String note) {
			for (String s : aliases) {
				if (s.equals(note))
					return true;
			}
			
			return false;
		}
	}
	
	private class Sequence {
		int[] notes; // 0 - 11
		int[] pattern; // 0 - 7
	}
	
	private class Scale extends Sequence {
		Scale(int[] notes, int[] pattern) {
			this.notes = notes;
			this.pattern = pattern;
		}
	}
	
	private class Mode extends Sequence {
		Mode(int[] notes, int[] pattern) {
			this.notes = notes;
			this.pattern = pattern;
		}
	}
	
	private class Chord extends Sequence {
		Chord(int[] notes, int[] pattern) {
			this.notes = notes;
			this.pattern = pattern;
		}
	}
	
	Library() {
		noteMap = new HashMap<String, Note>();
		sequenceMap = new HashMap<String, Sequence>();
		
		// NOTES
		Note c = new Note("C", new String[] {"B#", "C", "Dbb"});
		Note cSh = new Note(null, new String[] {"B##", "C#", "Db"});
		Note d = new Note("D", new String[] {"C##", "D", "Ebb"});
		Note dSh = new Note(null, new String[] {"D#", "Eb", "Fbb"});
		Note e = new Note("E", new String[] {"D##", "E", "Fb"});
		Note f = new Note("F", new String[] {"E#", "F", "Gbb"});
		Note fSh = new Note(null, new String[] {"E##", "F#", "Gb"});
		Note g = new Note("G", new String[] {"F##", "G", "Abb"});
		Note gSh = new Note(null, new String[] {"G#", "Ab"});
		Note a = new Note("A", new String[] {"G##", "A", "Bbb"});
		Note aSh = new Note(null, new String[] {"A#", "Bb", "Cbb"});
		Note b = new Note("B", new String[] {"A##", "B", "Cb"});
		
		c.setNext(cSh);
		cSh.setNext(d);
		d.setNext(dSh);
		dSh.setNext(e);
		e.setNext(f);
		f.setNext(fSh);
		fSh.setNext(g);
		g.setNext(gSh);
		gSh.setNext(a);
		a.setNext(aSh);
		aSh.setNext(b);
		b.setNext(c);
		
		// NOTE ALIASES
		noteMap.put("Cbb", aSh);
		noteMap.put("Cb", b);
		noteMap.put("C", c);
		noteMap.put("C#", cSh);
		noteMap.put("C##", d);
		
		noteMap.put("Dbb", c);
		noteMap.put("Db", cSh);
		noteMap.put("D", d);
		noteMap.put("D#", dSh);
		noteMap.put("D##", e);
		
		noteMap.put("Ebb", d);
		noteMap.put("Eb", dSh);
		noteMap.put("E", e);
		noteMap.put("E#", f);
		noteMap.put("E##", fSh);
		
		noteMap.put("Fbb", dSh);
		noteMap.put("Fb", e);
		noteMap.put("F", f);
		noteMap.put("F#", fSh);
		noteMap.put("F##", g);
		
		noteMap.put("Gbb", f);
		noteMap.put("Gb", fSh);
		noteMap.put("G", g);
		noteMap.put("G#", gSh);
		noteMap.put("G##", a);
		
		noteMap.put("Abb", g);
		noteMap.put("Ab", gSh);
		noteMap.put("A", a);
		noteMap.put("A#", aSh);
		noteMap.put("A##", b);
		
		noteMap.put("Bbb", a);
		noteMap.put("Bb", aSh);
		noteMap.put("B", b);
		noteMap.put("B#", c);
		noteMap.put("B##", cSh);
		
		// SCALES
		int[] acousticScale = {0, 2, 4, 6, 7, 9, 10};
		int[] adonaiMalakhScale = {0, 2, 4, 5, 7, 8, 10};
		int[] algerianScale = {0, 2, 3, 6, 7, 8, 11};
		int[] alteredScale = {0, 1, 3, 4, 6, 8, 10};
		int[] augmentedScale = {0, 3, 4, 7, 8, 11};
		int[] bebopDominantScale = {0, 2, 4, 5, 7, 9, 10, 11};
		int[] bluesScale = {0, 3, 5, 6, 7, 10};
		int[] chromaticScale = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
		int[] doubleHarmonicScale = {0, 1, 4, 5, 7, 8, 11};
		int[] enigmaticScale = {0, 1, 4, 6, 8, 10, 11};
		int[] flamencoScale = {0, 1, 4, 5, 7, 8, 11};
		int[] gypsyScale = {0, 2, 3, 6, 7, 8, 10};
		int[] halfDiminishedScale = {0, 2, 3, 5, 6, 8, 10};
		int[] harmonicMajorScale = {0, 2, 4, 5, 7, 8, 11};
		int[] harmonicMinorScale = {0, 2, 3, 5, 7, 8, 11};
		int[] hirajoshiScale = {0, 2, 3, 7, 8};
		int[] hungarianMinorScale = {0, 2, 3, 6, 7, 8, 11};
		int[] inScale = {0, 1, 5, 7, 8};
		int[] insenScale = {0, 1, 5, 7, 10};
		int[] istrianScale = {0, 1, 3, 4, 6, 7};
		int[] iwatoScale = {0, 1, 5, 6, 10};
		int[] lydianAugmentedScale = {0, 2, 4, 6, 8, 9, 11};
		int[] majorScale = {0, 2, 4, 5, 7, 9, 11};
		int[] majorBebopScale = {0, 2, 4, 5, 7, 8, 9, 11};
		int[] majorLocrianScale = {0, 2, 4, 5, 6, 8, 10};
		int[] majorPentatonicScale = {0, 2, 4, 7, 9};
		int[] melodicMinorScale = {0, 2, 3, 5, 7, 9, 11};
		int[] minorScale = {0, 2, 3, 5, 7, 8, 10};
		int[] minorPentatonicScale = {0, 3, 5, 7, 10};
		int[] neapolitanMajorScale = {0, 1, 3, 5, 7, 9, 11};
		int[] neapolitanMinorScale = {0, 1, 3, 5, 7, 8, 11};
		int[] persianScale = {0, 1, 4, 5, 6, 8, 11};
		int[] phrygianDominantScale = {0, 1, 4, 5, 7, 8, 10};
		int[] prometheusScale = {0, 2, 4, 6, 9, 10};
		int[] tritoneScale = {0, 1, 4, 6, 7, 10};
		int[] ukranianDorianScale = {0, 2, 3, 6, 7, 9, 10};
		int[] wholeToneScale = {0, 2, 4, 6, 8, 10};
		int[] yoScale = {0, 3, 5, 7, 11};
		
		// SCALE PATTERNS
		int[] patternNormalScale = {0, 1, 2, 3, 4, 5, 6};
		int[] patternPentatonicScale = {0, 1, 2, 4, 5};
		int[] patternBluesScale = {0, 2, 3, 3, 4, 6};
		int[] patternAugmentedScale = {0, 2, 2, 4, 4, 6};
		int[] patternBebopDominantScale = {0, 1, 2, 3, 4, 5, 6, 6};
		int[] patternChromaticScale = {0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 6};
		int[] patternInScale = {0, 1, 3, 4, 5};
		int[] patternInsenScale = {0, 1, 3, 4, 6};
		int[] patternIstrianScale = {0, 1, 2, 3, 4, 4};
		int[] patternMajorBebopScale = {0, 1, 2, 3, 4, 4, 5, 6};
		int[] patternPrometheusScale = {0, 1, 2, 3, 5, 6};
		int[] patternTritoneScale = {0, 1, 2, 4, 4, 6};
		int[] patternWholeToneScale = {0, 1, 2, 4, 5, 6};
		int[] patternYoScale = {0, 2, 3, 4, 6};
		
		sequenceMap.put("Acoustic Scale", new Scale(acousticScale, patternNormalScale));
		sequenceMap.put("Adonai Malakh Scale", new Scale(adonaiMalakhScale, patternNormalScale));
		sequenceMap.put("Algerian Scale", new Scale(algerianScale, patternNormalScale));
		sequenceMap.put("Altered Scale", new Scale(alteredScale, patternNormalScale));
		sequenceMap.put("Augmented Scale", new Scale(augmentedScale, patternAugmentedScale));
		sequenceMap.put("Bebop Dominant Scale", new Scale(bebopDominantScale, patternBebopDominantScale));
		sequenceMap.put("Blues Scale", new Scale(bluesScale, patternBluesScale));
		sequenceMap.put("Chromatic Scale", new Scale(chromaticScale, patternChromaticScale));
		sequenceMap.put("Double Harmonic Scale", new Scale(doubleHarmonicScale, patternNormalScale));
		sequenceMap.put("Enigmatic Scale", new Scale(enigmaticScale, patternNormalScale));
		sequenceMap.put("Flamenco Scale", new Scale(flamencoScale, patternNormalScale));
		sequenceMap.put("Gypsy Scale", new Scale(gypsyScale, patternNormalScale));
		sequenceMap.put("Half Diminished Scale", new Scale(halfDiminishedScale, patternNormalScale));
		sequenceMap.put("Harmonic Major Scale", new Scale(harmonicMajorScale, patternNormalScale));
		sequenceMap.put("Harmonic Minor Scale", new Scale(harmonicMinorScale, patternNormalScale));
		sequenceMap.put("Hirajoshi Scale", new Scale(hirajoshiScale, patternPentatonicScale));
		sequenceMap.put("Hungarian Minor Scale", new Scale(hungarianMinorScale, patternNormalScale));
		sequenceMap.put("In Scale", new Scale(inScale, patternInScale));
		sequenceMap.put("Insen Scale", new Scale(insenScale, patternInsenScale));
		sequenceMap.put("Istrian Scale", new Scale(istrianScale, patternIstrianScale));
		sequenceMap.put("Iwato Scale", new Scale(iwatoScale, patternInsenScale));
		sequenceMap.put("Lydian Augmented Scale", new Scale(lydianAugmentedScale, patternNormalScale));
		sequenceMap.put("Major Scale", new Scale(majorScale, patternNormalScale));
		sequenceMap.put("Major Bebop Scale", new Scale(majorBebopScale, patternMajorBebopScale));
		sequenceMap.put("Major Locrian Scale", new Scale(majorLocrianScale, patternNormalScale));
		sequenceMap.put("Major Pentatonic Scale", new Scale(majorPentatonicScale, patternPentatonicScale));
		sequenceMap.put("Melodic Minor Scale", new Scale(melodicMinorScale, patternNormalScale));
		sequenceMap.put("Minor Scale", new Scale(minorScale, patternNormalScale));
		sequenceMap.put("Minor Pentatonic Scale", new Scale(minorPentatonicScale, patternPentatonicScale));
		sequenceMap.put("Neapolitan Major Scale", new Scale(neapolitanMajorScale, patternNormalScale));
		sequenceMap.put("Neapolitan Minor Scale", new Scale(neapolitanMinorScale, patternNormalScale));
		sequenceMap.put("Persian Scale", new Scale(persianScale, patternNormalScale));
		sequenceMap.put("Phrygian Dominant Scale", new Scale(phrygianDominantScale, patternNormalScale));
		sequenceMap.put("Prometheus Scale", new Scale(prometheusScale, patternPrometheusScale));
		sequenceMap.put("Tritone Scale", new Scale(tritoneScale, patternTritoneScale));
		sequenceMap.put("Ukranian Dorian Scale", new Scale(ukranianDorianScale, patternNormalScale));
		sequenceMap.put("Whole Tone Scale", new Scale(wholeToneScale, patternWholeToneScale));
		sequenceMap.put("Yo Scale", new Scale(yoScale, patternYoScale));
		
		// MODES
		int[] ionian = {0, 2, 4, 5, 7, 9, 11};
		int[] dorian = {0, 2, 3, 5, 7, 9, 10};
		int[] phrygian = {0, 1, 3, 5, 7, 8, 10};
		int[] lydian = {0, 2, 4, 6, 7, 9, 11};
		int[] mixolydian = {0, 2, 4, 5, 7, 9, 10};
		int[] aeolian = {0, 2, 3, 5, 7, 8, 10};
		int[] locrian = {0, 1, 3, 5, 6, 8, 10};
		
		sequenceMap.put("Ionian Mode", new Mode(ionian, patternNormalScale));
		sequenceMap.put("Dorian Mode", new Mode(dorian, patternNormalScale));
		sequenceMap.put("Phrygian Mode", new Mode(phrygian, patternNormalScale));
		sequenceMap.put("Lydian Mode", new Mode(lydian, patternNormalScale));
		sequenceMap.put("Mixolydian Mode", new Mode(mixolydian, patternNormalScale));
		sequenceMap.put("Aeolian Mode", new Mode(aeolian, patternNormalScale));
		sequenceMap.put("Locrian Mode", new Mode(locrian, patternNormalScale));
		
		// CHORDS
		int[] augmentedChord = {0, 4, 8};
		int[] augmented6Chord = {0, 6, 8};
		int[] augmented7Chord = {0, 4, 8, 10};
		int[] augmentedMajor7Chord = {0, 4, 8, 11};
		int[] diminishedChord = {0, 3, 6};
		int[] diminished7Chord = {0, 3, 6, 9};
		int[] diminishedMajor7Chord = {0, 3, 6, 11};
		int[] dominant7Chord = {0, 4, 7, 10};
		int[] dominant7Flat5Chord = {0, 4, 6, 10};
		int[] dreamChord = {0, 5, 6, 7};
		int[] majorChord = {0, 4, 7};
		int[] major6Chord = {0, 4, 7, 9};
		int[] major7Chord = {0, 4, 7, 11};
		int[] minorChord = {0, 3, 7};
		int[] minor6Chord = {0, 3, 7, 9};
		int[] minor7Chord = {0, 3, 7, 10};
		int[] minor7Flat5Chord = {0, 3, 6, 10};
		int[] minorMajor7Chord = {0, 3, 7, 11};
		int[] muChord = {0, 2, 4, 7};
		int[] powerChord = {0, 7};
		int[] sus2Chord = {0, 2, 7};
		int[] sus4Chord = {0, 5, 7};
		int[] seventhSus2Chord = {0, 2, 7, 10};
		int[] seventhSus4Chord = {0, 5, 7, 10};
		int[] sevenSixChord = {0, 4, 7, 9, 10};
		int[] vienneseTrichord = {0, 1, 6};
		
		// CHORD PATTERNS
		int[] patternTriadChord = {0, 2, 4};
		int[] pattern6thChord = {0, 2, 4, 5};
		int[] patternAugmented6thChord = {0, 3, 5};
		int[] pattern7thChord = {0, 2, 4, 6};
		int[] patternDreamChord = {0, 3, 3, 4};
		int[] patternMuChord = {0, 1, 2, 4};
		int[] patternPowerChord = {0, 4};
		int[] patternSus2Chord = {0, 1, 4};
		int[] patternSus4Chord = {0, 3, 4};
		int[] pattern7thSus2Chord = {0, 1, 4, 6};
		int[] pattern7thSus4Chord = {0, 3, 4, 6};
		int[] patternSevenSixChord = {0, 2, 4, 5, 6};
		int[] patternVienneseTrichord = {0, 1, 4};
		
		sequenceMap.put("Augmented Chord", new Chord(augmentedChord, patternTriadChord));
		sequenceMap.put("Augmented 6th Chord", new Chord(augmented6Chord, patternAugmented6thChord));
		sequenceMap.put("Augmented 7th Chord", new Chord(augmented7Chord, pattern7thChord));
		sequenceMap.put("Augmented Major 7th Chord", new Chord(augmentedMajor7Chord, pattern7thChord));
		sequenceMap.put("Diminished Chord", new Chord(diminishedChord, patternTriadChord));
		sequenceMap.put("Diminished 7th Chord", new Chord(diminished7Chord, pattern7thChord));
		sequenceMap.put("Diminished Major 7th Chord", new Chord(diminishedMajor7Chord, pattern7thChord));
		sequenceMap.put("Dominant 7th Chord", new Chord(dominant7Chord, pattern7thChord));
		sequenceMap.put("Dominant 7th Flat 5th Chord", new Chord(dominant7Flat5Chord, pattern7thChord));
		sequenceMap.put("Dream Chord", new Chord(dreamChord, patternDreamChord));
		sequenceMap.put("Major Chord", new Chord(majorChord, patternTriadChord));
		sequenceMap.put("Major 6th Chord", new Chord(major6Chord, pattern6thChord));
		sequenceMap.put("Major 7th Chord", new Chord(major7Chord, pattern7thChord));
		sequenceMap.put("Minor Chord", new Chord(minorChord, patternTriadChord));
		sequenceMap.put("Minor 6th Chord", new Chord(minor6Chord, pattern6thChord));
		sequenceMap.put("Minor 7th Chord", new Chord(minor7Chord, pattern7thChord));
		sequenceMap.put("Minor 7th Flat 5th Chord", new Chord(minor7Flat5Chord, pattern7thChord));
		sequenceMap.put("Minor Major 7th Chord", new Chord(minorMajor7Chord, pattern7thChord));
		sequenceMap.put("Mu Chord", new Chord(muChord, patternMuChord));
		sequenceMap.put("Power Chord", new Chord(powerChord, patternPowerChord));
		sequenceMap.put("Suspended 2nd Chord", new Chord(sus2Chord, patternSus2Chord));
		sequenceMap.put("Suspended 4th Chord", new Chord(sus4Chord, patternSus4Chord));
		sequenceMap.put("7th Suspended 2nd Chord", new Chord(seventhSus2Chord, pattern7thSus2Chord));
		sequenceMap.put("7th Suspended 4th Chord", new Chord(seventhSus4Chord, pattern7thSus4Chord));
		sequenceMap.put("7/6 Chord", new Chord(sevenSixChord, patternSevenSixChord));
		sequenceMap.put("Viennese Trichord", new Chord(vienneseTrichord, patternVienneseTrichord));
	}
	
	/**
	 * Returns a list of ResultData note information for a single instrument string, starting from nutNote.
	 * @param sequence the sequence to return notes for.
	 * @param root the starting note of the sequence.
	 * @param nutNote the starting note of the instrument string.
	 * @param length the length of the instrument string.
	 * @param abbreviated whether to abbreviate the interval labels.
	 * @return a list of ResultData note information for a single instrument string, starting from nutNote.
	 */
	public List<ResultData> getNotes(String sequence, String root, String nutNote, int length, boolean abbreviated) {
		Sequence seq = sequenceMap.get(sequence);
		int[] notes = seq.notes;
		int[] pattern = seq.pattern;
		
		List<ResultData> result = new ArrayList<ResultData>();
		
		// find starting pitchClass at 0th fret
		int pitchClass = 0;
		Note currNote = noteMap.get(root);
		while (!currNote.equals(nutNote)) {
			currNote = currNote.next;
			pitchClass++;
		}
		
		// CONSTRUCT LIST
		for (int i = 0; i < length; i++) {
			int index = containsInterval(notes, pitchClass);
			if (index != -1) {
				// construct note name
				String name = getNoteAnchor(root, pattern[index]); 
				name = addSharpsFlats(name, currNote);
				
				result.add(new ResultData(name, getInterval(notes[index], pattern[index], abbreviated)));
			} else
				result.add(null);
			
			currNote = currNote.next;
			pitchClass++;
			if (pitchClass == 12)
				pitchClass = 0; // octave
		}
		
		return result;
	}
	
	/**
	 * Returns the index that the pitchClass occurs within the specified note sequence, -1 if not.
	 * @param notes the specified note sequence to search for the pitchClass.
	 * @param pitchClass the pitchClass to search for within the specified note sequence.
	 * @return the index that the pitchClass occurs within the specified note sequence, -1 if not.
	 */
	private int containsInterval(int[] notes, int pitchClass) {
		for (int i = 0; i < notes.length; i++) {
			if (notes[i] == pitchClass)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Returns the proper note anchor based on the note distance from the root.
	 * @param root the starting note of the sequence.
	 * @param distance the note distance from the root.
	 * @return the proper note anchor based on the note distance from the root.
	 */
	private String getNoteAnchor(String root, int distance) {
		root = root.substring(0, 1); // trim off sharps if any
		int index = 0;
		
		while (!NOTE_NAME[index].equals(root))
			index++;
		
		index += distance;
		
		while (index > 6) {
			index -= 7;
		}
		
		return NOTE_NAME[index];
	}
	
	/**
	 * Returns the adjusted note name with sharps or flats as necessary.
	 * @param name the current note name consisting of note letter only.
	 * @param currNote the Note object representing the true note.
	 * @return the adjusted note name with sharps or flats as necessary.
	 */
	private String addSharpsFlats(String name, Note currNote) {
		// determine number of sharps/flats, if any
		if (!name.equals(currNote.note)) {
			Note temp = currNote;
			int adjust = 0;

			// check up to two sharps
			while (adjust < 2 && !name.equals(temp.note)) {
				adjust++;
				temp = temp.next;
			}

			// check down to two flats
			if (!name.equals(temp.note)) {
				adjust = 0;
				temp = currNote;

				while (adjust > -2 && !name.equals(temp.note)) {
					adjust--;
					temp = temp.prev;
				}
			}

			// add sharps or flats
			if (adjust < 0) {
				while (adjust++ < 0)
					name += "#";
			} else {
				while (adjust-- > 0)
					name += "b";
			}
		}
		
		// name is now complete
		return name;
	}
	
	/**
	 * Returns the proper interval name between the root and the current pitchClass.
	 * @param pitchClass the specified pitchClass to find the interval for.
	 * @param anchorNote the note distance from the root (2nd, 3rd, 4th etc).
	 * @param abbreviated whether to abbreviate the interval labels.
	 * @return the proper interval name between the root and the current pitchClass.
	 */
	private String getInterval(int pitchClass, int anchorNote, boolean abbreviated) {
		switch (anchorNote) {
		case 0: // Root
			return abbreviated ? "P1" : "Root";
			
		case 1: // 2nd
			switch (pitchClass) {
			case 1: return abbreviated ? "m2" : "Minor 2nd";
			case 2: return abbreviated ? "M2" : "Major 2nd";
			}
			
		case 2: // 3rd
			switch (pitchClass) {
			case 3: return abbreviated ? "m3" : "Minor 3rd";
			case 4: return abbreviated ? "M3" : "Major 3rd";
			}
			
		case 3: // 4th
			switch (pitchClass) {
			case 4: return abbreviated ? "d4" : "Diminished 4th";
			case 5: return abbreviated ? "P4" : "Perfect 4th";
			case 6: return abbreviated ? "A4" : "Augmented 4th";
			}
			
		case 4: // 5th
			switch (pitchClass) {
			case 6: return abbreviated ? "d5" : "Diminished 5th";
			case 7: return abbreviated ? "P5" : "Perfect 5th";
			case 8: return abbreviated ? "A5" : "Augmented 5th";
			}
			
		case 5: // 6th
			switch (pitchClass) {
			case 8: return abbreviated ? "m6" : "Minor 6th";
			case 9: return abbreviated ? "M6" : "Major 6th";
			}
			
		case 6: // 7th
			switch (pitchClass) {
			case 10: return abbreviated? "m7" : "Minor 7th";
			case 11: return abbreviated? "M7" : "Major 7th";
			}
		}
		
		return null;
	}
}
