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
		((TextView) findViewById(R.id.textview_link_play)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) findViewById(R.id.textview_link_facebook)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) findViewById(R.id.textview_link_email)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
