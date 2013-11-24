package com.jameschin.android.mozartsfriend;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.TextView;

/**
 * TunerActivity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class TunerActivity extends BaseActivity{
    private static final String[] NOTE_NAME = {"G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"};
    private static final double upRatio1 = 0.04;
    private static final double upRatio2 = 0.25;
    private static final double upRatio3 = 0.35;
    private static final double downRatio1 = -0.09;
    private static final double downRatio2 = -0.25;
    private static final double downRatio3 = -0.35;
    
    // VIEW HOLDERS
    private TextView textViewTunerNote;
    private TextView textViewTunerNotePrev;
    private TextView textViewTunerNoteNext;
    private TextView textViewTunerNoteFrequency;
    private View viewTunerUp1;
    private View viewTunerUp2;
    private View viewTunerUp3;
    private View viewTunerDown1;
    private View viewTunerDown2;
    private View viewTunerDown3;
   
    private WakeLock wakeLock;
    private Thread tunerThread;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuner);
        
        initialize();
    }
    
    private void initialize() {
    	textViewTunerNote = (TextView) findViewById(R.id.textview_tuner_note);
    	textViewTunerNotePrev = (TextView) findViewById(R.id.textview_tuner_note_prev);
    	textViewTunerNoteNext = (TextView) findViewById(R.id.textview_tuner_note_next);
    	textViewTunerNoteFrequency = (TextView) findViewById(R.id.textview_tuner_note_frequency);
    	viewTunerUp1 = (View) findViewById(R.id.view_tuner_indicator_up_1);
    	viewTunerUp2 = (View) findViewById(R.id.view_tuner_indicator_up_2);
    	viewTunerUp3 = (View) findViewById(R.id.view_tuner_indicator_up_3);
    	viewTunerDown1 = (View) findViewById(R.id.view_tuner_indicator_down_1);
    	viewTunerDown2 = (View) findViewById(R.id.view_tuner_indicator_down_2);
    	viewTunerDown3 = (View) findViewById(R.id.view_tuner_indicator_down_3);
    	
    	indicatorClear();
    	
    	PowerManager powermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powermanager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TunerActivity");
		
		synchronized(this) {
			if (!wakeLock.isHeld())
				wakeLock.acquire();
			
			tunerThread = new Thread(new Tuner(this, new Handler()), "Thread - Tuner");
			tunerThread.start();
		}
    }
    
    /**
     * Updates the tuner display, the Tuner class calls this function via this Activity's Handler.
     * @param closestNote the nearest note to the one picked up by the device mic, identified by the Tuner.
     * @param distanceRatio the relative distance from this note (closestNote) to the next closest note. Min 0/Max 1, positive meaning the next higher note and negative meaning the next lower note from closestNote.
     * @param frequency the exact primary frequency picked up by the device mic, identified by the Tuner. 
     */
    protected void updateDisplay(int closestNote, double distanceRatio, double frequency) {
    	indicatorClear();
    	
    	if (closestNote != -1) {
    		textViewTunerNote.setText(NOTE_NAME[closestNote]);
    		
    		if (distanceRatio >= 0) {
    			if (distanceRatio > upRatio1) {
    				viewTunerUp1.setVisibility(View.VISIBLE);
    				if (distanceRatio > upRatio2) {
    					viewTunerUp2.setVisibility(View.VISIBLE);
            			if (distanceRatio > upRatio3)
            				viewTunerUp3.setVisibility(View.VISIBLE);
    				}
    			} else
    				indicatorLock();
    		} else { // (distanceRatio < 0)
    			if (distanceRatio < downRatio1) {
    				viewTunerDown1.setVisibility(View.VISIBLE);
    				if (distanceRatio < downRatio2) {
    					viewTunerDown2.setVisibility(View.VISIBLE);
            			if (distanceRatio < downRatio3)
            				viewTunerDown3.setVisibility(View.VISIBLE);
    				}
    			} else
    				indicatorLock();
    		}
        	
        	textViewTunerNotePrev.setText(NOTE_NAME[closestNote - 1]);
    		textViewTunerNoteNext.setText(NOTE_NAME[closestNote + 1]);
    		textViewTunerNoteFrequency.setText(frequency + " Hz");
    	} else {
    		textViewTunerNote.setText("");
    		textViewTunerNotePrev.setText("");
    		textViewTunerNoteNext.setText("");
    		textViewTunerNoteFrequency.setText("Hz");
    	}
    }
    
    /**
     * Clear the screen and all indicators lights.
     */
    private void indicatorClear() {
    	textViewTunerNote.setBackgroundResource(R.drawable.controls_frame);
    	viewTunerUp1.setVisibility(View.GONE);
    	viewTunerUp2.setVisibility(View.GONE);
    	viewTunerUp3.setVisibility(View.GONE);
    	viewTunerDown1.setVisibility(View.GONE);
    	viewTunerDown2.setVisibility(View.GONE);
    	viewTunerDown3.setVisibility(View.GONE);
    }
    
    /**
     * Light the green Lock indicator.
     */
    private void indicatorLock() {
    	textViewTunerNote.setBackgroundResource(R.drawable.tuner_lock_green);
    }
    
    @Override
	protected void onStop() {
	    super.onStop();
	    synchronized(this) {
	    	if (wakeLock.isHeld())
	    		wakeLock.release();
	    	
	    	tunerThread.interrupt();
	    }
	}
	
	@Override
	protected void onRestart() {
	    super.onRestart();
	    synchronized(this) {
	    	if (!wakeLock.isHeld())
    			wakeLock.acquire();
	    	
	    	tunerThread = new Thread(new Tuner(this, new Handler()));
			tunerThread.start();
	    }
	}
}