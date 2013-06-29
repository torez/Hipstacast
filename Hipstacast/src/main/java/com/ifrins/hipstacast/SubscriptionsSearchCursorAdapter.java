package com.ifrins.hipstacast;

import com.ifrins.hipstacast.model.Podcast;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class SubscriptionsSearchCursorAdapter extends ArrayAdapter<Object> {

	private Context context;
	private Podcast[] podcasts;

	public SubscriptionsSearchCursorAdapter(Context context, Podcast[] podcasts) {
		super(context, R.layout.subscriptions_list, podcasts);
		this.context = context;
		this.podcasts = podcasts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.subscriptions_list, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.podcastTitle);
			holder.author = (TextView) convertView.findViewById(R.id.podcastAuthor);
			holder.coverView = (ImageView) convertView .findViewById(R.id.podcastLogo);
			holder.unlistenedCount = convertView.findViewById(R.id.podcastUnlistenedCount);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.title.setText(podcasts[position].title);
		holder.author.setText(podcasts[position].author);
		holder.unlistenedCount.setVisibility(View.GONE);
		Picasso
				.with(context)
				.load(podcasts[position].imageUrl)
				.into(holder.coverView);

		return convertView;
	}
	
	private class ViewHolder {
		TextView title;
		TextView author;
		ImageView coverView;
		View unlistenedCount;
	}

}
