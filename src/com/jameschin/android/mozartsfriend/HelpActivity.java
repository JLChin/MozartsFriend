package com.jameschin.android.mozartsfriend;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

/**
 * HelpActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class HelpActivity extends BaseActivity {
	// VIEW HOLDERS
	ScrollView scrollView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		initialize();
	}
	
	private void initialize() {
		scrollView = (ScrollView) findViewById (R.id.scrollview_help);
		
		// BACK TO TOP'S
		int[] backToTopViews = {
				R.id.textview_help_library_back_to_top,
				R.id.textview_help_metronome_back_to_top,
				R.id.textview_help_tuner_back_to_top,
				R.id.textview_help_track_back_to_top
		};
		
		for (int id : backToTopViews) {
			findViewById(id).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					scrollView.smoothScrollTo(0, 0);
				}});
		}
		
		// SHORTCUTS
		findViewById(R.id.textview_help_shortcut_library).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				scrollView.smoothScrollTo(0, findViewById(R.id.textview_help_library_header).getTop());
			}});
		
		findViewById(R.id.textview_help_shortcut_metronome).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				scrollView.smoothScrollTo(0, findViewById(R.id.textview_help_metronome_header).getTop());
			}});
		
		findViewById(R.id.textview_help_shortcut_tuner).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				scrollView.smoothScrollTo(0, findViewById(R.id.textview_help_tuner_header).getTop());
			}});
		
		findViewById(R.id.textview_help_shortcut_track).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				scrollView.smoothScrollTo(0, findViewById(R.id.textview_help_track_header).getTop());
			}});
	}
}
