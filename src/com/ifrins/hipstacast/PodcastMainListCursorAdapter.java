package com.ifrins.hipstacast;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PodcastMainListCursorAdapter extends CursorAdapter {

	public PodcastMainListCursorAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
        String title = cursor.getString(cursor.getColumnIndex("title"));
        holder.name.setText(title);
        holder.image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex("imageUrl"))));
        holder.author.setText(cursor.getString(cursor.getColumnIndex("author")));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(
                R.layout.podcasts_list, null);

        Holder holder = new Holder();

        holder.name = (TextView) v.findViewById(R.id.podcastTitle);
        holder.image= (ImageView) v.findViewById(R.id.podcastLogo);
        holder.author = (TextView) v.findViewById(R.id.podcastAuthor);

        v.setTag(holder);
        return v;

		
	}
	
	private class Holder {
        TextView name;
        TextView author;
        ImageView image;
    }
}
