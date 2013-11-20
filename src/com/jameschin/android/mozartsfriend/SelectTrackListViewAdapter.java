package com.jameschin.android.mozartsfriend;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * SelectTrackListViewAdapter
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class SelectTrackListViewAdapter extends ArrayAdapter<SelectTrackActivity.TrackInfo>{
	private final Context activity;
	private final List<SelectTrackActivity.TrackInfo> tracks;
	
	public SelectTrackListViewAdapter(Context activity, List<SelectTrackActivity.TrackInfo> tracks) {
		super(activity, R.layout.list_item, tracks);
		this.activity = activity;
		this.tracks = tracks;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        TrackInfoView trackInfoView = null;
 
        if(view == null)
        {
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
        SelectTrackActivity.TrackInfo trackInfo = tracks.get(position);
        trackInfoView.textViewTitle.setText(trackInfo.title);
        trackInfoView.resourceID = trackInfo.resourceID;
 
        return view;
    }
	
	protected static class TrackInfoView {
		protected TextView textViewTitle;
		protected int resourceID;
	}
}