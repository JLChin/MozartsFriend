package com.jameschin.android.mozartsfriend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * SelectTrackActivity
 * 
 * @author James Chin <jameslchin@gmail.com>
 */
public class SelectTrackActivity extends BaseListActivity {
	// STATE VARIABLES
	private List<TrackInfo> tracks;
	
	/**
	 * Custom Track list view adapter.
	 */
	private class SelectTrackListViewAdapter extends ArrayAdapter<TrackInfo> {
		class TrackInfoView {
			TextView textViewTitle;
			int resourceID;
		}
		private final List<TrackInfo> tracks;
		
		private final Context activity;
		
		SelectTrackListViewAdapter(Context activity, List<TrackInfo> tracks) {
			super(activity, R.layout.list_item, tracks);
			this.activity = activity;
			this.tracks = tracks;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        TrackInfoView trackInfoView = null;
	 
	        if (view == null) {
	        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
	            view = inflater.inflate(R.layout.list_item, null);
	 
	            // hold the view objects in an object, so they don't need to be re-fetched
	            trackInfoView = new TrackInfoView();
	            trackInfoView.textViewTitle = (TextView) view.findViewById(R.id.textview_list_item);
	 
	            // cache the view objects in the tag, so they can be re-accessed later
	            view.setTag(trackInfoView);
	        } else
	        	trackInfoView = (TrackInfoView) view.getTag();
	 
	        // set up view
	        TrackInfo trackInfo = tracks.get(position);
	        trackInfoView.textViewTitle.setText(trackInfo.title);
	        trackInfoView.resourceID = trackInfo.resourceID;
	 
	        return view;
	    }
	}
	
	/**
	 * Basic track info holder.
	 */
	private class TrackInfo {
		String title;
		int resourceID;
		
		TrackInfo(String title, int resourceID) {
			this.title = title;
			this.resourceID = resourceID;
		}
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
		
		setListAdapter(new SelectTrackListViewAdapter(this, tracks));
	}
	
	/**
	 * Get track titles and resourceIDs from files in raw folder.
	 * See MidiFile.TrackData for file specification.
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		initialize();
	}
}
