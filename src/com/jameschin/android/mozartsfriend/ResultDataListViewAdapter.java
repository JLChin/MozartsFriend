package com.jameschin.android.mozartsfriend;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * ResultDataListViewAdapter
 * 
 * @author James Chin <JamesLChin@gmail.com>
 */
public class ResultDataListViewAdapter extends ArrayAdapter<ResultData>{
	private final Context activity;
	private final List<ResultData> results;
	private final Set<String> markedNotes;
	
	public ResultDataListViewAdapter(Context activity, List<ResultData> results, Set<String> markedNotes) {
		super(activity, R.layout.result_data_list_item, results);
		this.activity = activity;
		this.results = results;
		this.markedNotes = markedNotes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ResultDataView resultDataView = null;
 
        if(view == null)
        {
        	LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
            view = inflater.inflate(R.layout.result_data_list_item, null);
 
            // hold the view objects in an object, so they don't need to be re-fetched
            resultDataView = new ResultDataView();
            resultDataView.name = (TextView) view.findViewById(R.id.textview_result_data_name);
            resultDataView.interval = (TextView) view.findViewById(R.id.textview_result_data_interval);
 
            // cache the view objects in the tag, so they can be re-accessed later
            view.setTag(resultDataView);
        } else
        	resultDataView = (ResultDataView) view.getTag();
 
        // set up view
        ResultData currResult = (ResultData) results.get(position);
        resultDataView.name.setText(currResult.noteName);
        resultDataView.interval.setText(currResult.noteInterval);
        
        if (markedNotes.contains(currResult.noteName)) {
        	if (currResult.noteInterval == "Root")
        		view.setBackgroundResource(R.drawable.fretboard_item_background_green);
        	else
        		view.setBackgroundResource(R.drawable.fretboard_item_background_purple);
        } else
        	view.setBackgroundResource(0);
 
        return view;
    }
 
    protected static class ResultDataView {
        protected TextView name;
        protected TextView interval;
    }
}
