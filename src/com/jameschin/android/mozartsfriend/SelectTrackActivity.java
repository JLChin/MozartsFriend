package com.jameschin.android.mozartsfriend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * SelectTrackActivity
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class SelectTrackActivity extends BaseListActivity {
	// STATE VARIABLES
	List<TrackInfo> tracks;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		initialize();
	}

	private void initialize() {
		loadTracks();
		initializeListView();
	}
	
	private void initializeListView() {
		ListView listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					int rawResourceID = ((SelectTrackListViewAdapter.TrackInfoView) view.getTag()).resourceID;
					
					Intent intent = new Intent(SelectTrackActivity.this, TrackActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					intent.putExtra("rawResourceID", rawResourceID);
					startActivity(intent);
			}
		});
		
		setListAdapter(new SelectTrackListViewAdapter (this, tracks));
	}
	
	/**
	 * Get track titles and resourceIDs from files in raw folder.
	 * See TrackData.java for file specification.
	 */
	private void loadTracks() {
		tracks = new ArrayList<TrackInfo>();
		
		Field[] fields = R.raw.class.getFields();
	    for(int count = 0; count < fields.length; count++){
	    	int resourceID = -1;
	    	
	    	// GET RAW RESOURCE ID
	    	try {
				resourceID = fields[count].getInt(fields[count]);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
	    	
	    	// GET TITLE FROM FIRST LINE IN EACH FILE
	    	InputStream inputStream = getResources().openRawResource(resourceID);
	    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	    	String line = null;
	    	
	    	try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	String title = line; // line 0 contains track title
	    	
	    	tracks.add(new TrackInfo(title, resourceID));
	    }
	}
	
	/**
	 * Basic track info holder.
	 */
	protected class TrackInfo {
		protected String title;
		protected int resourceID;
		
		TrackInfo(String title, int resourceID) {
			this.title = title;
			this.resourceID = resourceID;
		}
	}
}
