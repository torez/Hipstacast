package com.ifrins.hipstacast;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EpisodeListCursorAdapter extends CursorAdapter {

	private static String transformToDuration(String input) {
		int s = Integer.parseInt(input);
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}
	
	public EpisodeListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();

        holder.name.setText(cursor.getString(cursor.getColumnIndex("title")));
        int status = cursor.getInt(cursor.getColumnIndex("status"));
        if (status == 0) {     
        	holder.name.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
        }
        else {
        	holder.name.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium);
        }
        holder.duration.setText(transformToDuration(cursor.getString(cursor.getColumnIndex("duration"))));
        //holder.status.setImageResource(R.drawable.ic_list_new);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(
                R.layout.episodes_list, null);

        Holder holder = new Holder();

        holder.name = (TextView) v.findViewById(R.id.episodeTitle);
        holder.duration= (TextView) v.findViewById(R.id.episodeDuration);

        v.setTag(holder);
        return v;

	}
	
	private class Holder {
        TextView name;
        TextView duration;
    }
}
