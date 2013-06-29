package com.ifrins.hipstacast.adapters;

import com.ifrins.hipstacast.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class SubscriptionsCursorAdapter extends CursorAdapter {

	Context ctx;

	public SubscriptionsCursorAdapter(Context context, Cursor c) {
		super(context, c, false);
		ctx = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag();
		int show = cursor.getInt(cursor.getColumnIndex("_id"));
		String title = cursor.getString(cursor.getColumnIndex("title"));
		String imageUrl = cursor.getString(cursor.getColumnIndex("imageUrl"));

		holder.name.setText(title);
		Picasso.with(context).load(imageUrl).into(holder.image);
		holder.author
				.setText(cursor.getString(cursor.getColumnIndex("author")));
		Cursor cur =  context.getContentResolver()
				.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
						+ show + "/episodes"),
						new String[] { "_id", "status" },
						"podcast_id = ? AND status != ? ",
						new String[] { String.valueOf(show), "3"}, null);
		int c = cur.getCount();
		cur.close();
		if (c > 0 && c < 10) {
			holder.listenCount.setText(String.valueOf(c));
			holder.listenCount.setVisibility(View.VISIBLE);
		} else if (c > 9) {
			holder.listenCount.setText("9+");
			holder.listenCount.setVisibility(View.VISIBLE);
		} else {
			holder.listenCount.setVisibility(View.GONE);
		}
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View v = inflater.inflate(R.layout.subscriptions_list, null);

		Holder holder = new Holder();

		holder.name = (TextView) v.findViewById(R.id.podcastTitle);
		holder.image = (ImageView) v.findViewById(R.id.podcastLogo);
		holder.author = (TextView) v.findViewById(R.id.podcastAuthor);
		holder.listenCount = (TextView) v
				.findViewById(R.id.podcastUnlistenedCount);

		v.setTag(holder);
		return v;

	}

	private class Holder {
		TextView name;
		TextView author;
		TextView listenCount;
		ImageView image;
	}
}
