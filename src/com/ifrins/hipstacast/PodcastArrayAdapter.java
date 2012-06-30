package com.ifrins.hipstacast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.TextView;

import com.ifrins.hipstacast.model.Podcast;

public class PodcastArrayAdapter extends ArrayAdapter<Podcast> {
	private final Context ctx;
	private final Podcast[] podcsts;

	public PodcastArrayAdapter(Context context, Podcast[] podcasts) {
		super(context, R.layout.podcasts_list, podcasts);
		ctx = context;
		podcsts = podcasts;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.podcasts_list, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.podcastTitle);
		//ImageView imageView = (ImageView) rowView.findViewById(R.id.podcastLogo);
		textView.setText(podcsts[position].title);
 
		// Change icon based on name
		 
		return rowView;
	}

	
}
