package com.ifrins.hipstacast.fragments;

import com.ifrins.hipstacast.Hipstacast;
import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EpisodeDetailsFragment extends Fragment {
	int show_id;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		show_id = this.getArguments().getInt("show_id");
		
		View v = inflater.inflate(R.layout.podcast_details, null);
		
		Cursor show = getCursor();
		show.moveToFirst();
		
		((TextView)v.findViewById(R.id.showDetailsTitle)).setText(Html.fromHtml(show.getString(show.getColumnIndex(HipstacastProvider.PODCAST_TITLE))));
		((TextView)v.findViewById(R.id.showDetailsDescription)).setText(Html.fromHtml(show.getString(show.getColumnIndex(HipstacastProvider.PODCAST_DESCRIPTION))));

		return v;
	}
	
	private Cursor getCursor() {
    	return getActivity().getContentResolver().query(Hipstacast.SUBSCRIPTIONS_PROVIDER_URI, new String[] { "_id", "title",
    			"description", HipstacastProvider.PODCAST_FEED}, "_id = ?", new String[] { String.valueOf(show_id) }, null);

	}

}
