package com.ifrins.hipstacast.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.ListView;

import com.ifrins.hipstacast.EpisodeListCursorAdapter;

public class PlayerUIUtils {
	
	public static String fixCoverPath(String url) {
		if (!url.startsWith("http")) {
			return "file://" + url;
		}
		return url;
	}

	public static String convertSecondsToDuration(int seconds) {
		
		if (seconds < 3600) {
			return String.format("%02d:%02d", seconds / 60, (seconds % 60));
		}
		return String.format("%d:%02d:%02d", seconds / 3600,
				(seconds % 3600) / 60, (seconds % 60));
	}

	public static void fixDuration(Context context, int episode_id, int duration) {
		
		ContentValues c = new ContentValues();
		c.put("duration", duration);
		context.getContentResolver()
				.update(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						c, "_id = ?",
						new String[] { String.valueOf(episode_id) });

	}

	public static void savePosition(Context context, int episode_id,
			int position) {

		ContentValues c = new ContentValues();
		c.put("status", 2);
		c.put("position", position / 1000);
		context.getContentResolver()
				.update(Uri
						.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"),
						c, "_id = ?",
						new String[] { String.valueOf(episode_id) });

	}

	public static void setEpisodeAsListened(Context context, int episode_id) {
		ContentValues c = new ContentValues();
		c.put("position", 0);
		c.put("status", 3);
		if (context != null)
			context.getContentResolver()
					.update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/episodes"), c, "_id = ?",
						new String[] { String.valueOf(episode_id) });

	}
	public static void markAsListenedAndUpdate(Context context, int episode_id, ListView listView) {
		setEpisodeAsListened(context, episode_id);
		((EpisodeListCursorAdapter)listView.getAdapter()).notifyDataSetChanged();
	}

}
