package com.jameschin.android.mozartsfriend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * SelectKeyActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class SelectKeyActivity extends BaseListActivity {
	// CONSTANTS
	public static final String[] KEYS = { "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		initialize();
	}

	private void initialize() {
		initializeListView();

		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, KEYS));
	}

	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent;

				String sequence = getIntent().getStringExtra("sequence");
				boolean arpeggio = getIntent().getBooleanExtra("arpeggio", true);
				intent = new Intent(SelectKeyActivity.this, LibraryActivity.class);
				intent.putExtra("sequence", sequence);
				intent.putExtra("arpeggio", arpeggio);

				String key = (String) ((TextView) view).getText();
				intent.putExtra("key", key);
				startActivity(intent);
			}
		});
	}
}
