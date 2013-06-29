package com.ifrins.hipstacast.adapters;

import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.model.PodcastEpisode;
import com.ifrins.hipstacast.utils.PlayerUIUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RemoteEpisodesArrayAdapter extends ArrayAdapter<PodcastEpisode> {

	private Context context;
	private PodcastEpisode[] episodes;

	public RemoteEpisodesArrayAdapter(Context context, PodcastEpisode[] episodes) {
		super(context, R.layout.subscriptions_list, episodes);
		this.context = context;
		this.episodes = episodes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.episodes_list, parent, false);

		((TextView)rowView.findViewById(R.id.episodeTitle)).setText(episodes[position].title);
		((TextView)rowView.findViewById(R.id.episodeDuration)).setText(PlayerUIUtils.convertSecondsToDuration(episodes[position].duration));
		return rowView;
	}


}
