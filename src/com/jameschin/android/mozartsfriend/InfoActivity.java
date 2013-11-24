package com.jameschin.android.mozartsfriend;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * InfoActivity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class InfoActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		initialize();
	}
	
	private void initialize() {
		// set hyperlinks
		int[] links = {
				R.id.textview_link_play,
				R.id.textview_link_facebook,
				R.id.textview_link_youtube,
				R.id.textview_link_email
		};
		
		for (int link : links)
			((TextView) findViewById(link)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
