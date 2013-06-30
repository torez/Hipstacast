package com.ifrins.hipstacast.adapters;

import com.ifrins.hipstacast.R;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.PlayerUIUtils;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class EpisodeListCursorAdapter extends CursorAdapter {

	
	public EpisodeListCursorAdapter(Context context, Cursor c) {
		super(context, c, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();

        holder.name.setText(cursor.getString(cursor.getColumnIndex(HipstacastProvider.EPISODE_TITLE)));

		int totalDuration = cursor.getInt(cursor.getColumnIndex(HipstacastProvider.EPISODE_DURATION));
		int currentPosition = cursor.getInt(cursor.getColumnIndex(HipstacastProvider.EPISODE_CURRENT_POSITION)) / 1000;
		int status = cursor.getInt(cursor.getColumnIndex(HipstacastProvider.EPISODE_STATUS));
        int isDownloaded = cursor.getInt(cursor.getColumnIndex(HipstacastProvider.EPISODE_DOWNLOADED));

        if (isDownloaded == HipstacastProvider.EPISODE_STATUS_DOWNLOADED) {
            holder.name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_downloaded, 0, 0, 0);
        } else {
            holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

		if (status == HipstacastProvider.EPISODE_STATUS_STARTED) {
			holder.duration.setText("-" + PlayerUIUtils.convertSecondsToDuration(totalDuration - currentPosition));
		} else {
			holder.duration.setText(PlayerUIUtils.convertSecondsToDuration(totalDuration));
		}

		if (status == HipstacastProvider.EPISODE_STATUS_FINISHED) {
			int disabledColor = context.getResources().getColor(R.color.episodes_list_color_disabled);
			holder.name.setTextColor(disabledColor);
			holder.duration.setTextColor(disabledColor);
		} else {
			int enabledColor = context.getResources().getColor(R.color.episodes_list_color_primary);
			holder.name.setTextColor(enabledColor);
			holder.duration.setTextColor(enabledColor);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.episodes_list, null);

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
