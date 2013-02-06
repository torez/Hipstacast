package com.ifrins.hipstacast.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ListView;

import com.ifrins.hipstacast.EpisodePlayer;
import com.ifrins.hipstacast.EpisodeListCursorAdapter;
import com.ifrins.hipstacast.HipstacastPlayerService;
import com.ifrins.hipstacast.R;

public class PlayerUIUtils {

	public static void startPlaying(HipstacastPlayerService player,
			int show_id, int episode_id, int type, Notification n,
			int start_position, SurfaceView surface) {
		
		player.clean();
		player.podcastToPlayUrl = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/com.ifrins.hipstacast/files/shows/"
				+ show_id
				+ "/" + episode_id + ".mp3";
		player.type = type;
		if (type == 1) {
			player.surface = surface.getHolder();
		}
		player.podcast_id = episode_id;
		player.show_id = show_id;
		player.n = n;
		Log.d("HIP-NW-SP", String.valueOf(start_position));
		player.start_position = start_position;
		player.play();
		Log.d("HIP-STATUS", "Should start");

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

	public static Notification buildNotification(Context context, String title,
			int show_id, int episode_id, int type) {

		Intent newIntent = new Intent(context, EpisodePlayer.class);
		newIntent.putExtra("from_notif", true);
		newIntent.putExtra("show_id", show_id);
		newIntent.putExtra("episode_id", episode_id);
		newIntent.putExtra("type", type);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				newIntent, 0);

		Notification n = new Notification.Builder(context)
				.setContentTitle("Hipstacast")
				.setSmallIcon(R.drawable.ic_stat_playing).setContentText(title)
				.setOngoing(true).setContentIntent(contentIntent)
				.setTicker(title).getNotification();

		return n;

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
