package com.ifrins.hipstacast;

import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.webkit.WebView;

public class EpisodePlayer extends Activity {
	HipstacastPlayerService player;
	Boolean bound = false;
	Boolean isPlaying = false;
	int show_id;
	int podcast_id;
	int start_position;
	Boolean complete = false;
	Boolean fromNotification = false;
	SeekBar seek;
	String name;
	String url;
	String donation_url;
	private Handler mHandler = new Handler();
	Notification n;

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if (player != null && player.isPlaying()
					&& player.show_id == show_id
					&& player.podcast_id == podcast_id) {
				seek.setProgress(player.mediaPlayer.getCurrentPosition());
				if (player.mediaPlayer.getDuration() > 500000) {
					mHandler.postDelayed(this, 3000);
				} else {
					mHandler.postDelayed(this, 1000);
				}
			} else if (player != null && !player.isPlaying()
					&& player.show_id == show_id
					&& player.podcast_id == podcast_id) {

				if (player.mediaPlayer != null
						&& player.mediaPlayer.getCurrentPosition() >= player.mediaPlayer
								.getDuration() - 600) {
					setEpisodeAsListened();
					invalidateOptionsMenu();
					player.pause();
					player.clean();
				}
				mHandler.postDelayed(this, 1000);
			} else {
				mHandler.postDelayed(this, 1000);
			}
		}
	};

	private OnSeekBarChangeListener chl = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser && isPlaying) {
				player.mediaPlayer.seekTo(progress);
				seekBar.setProgress(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	};

	private void buildNotification(String title) {
		Intent newIntent = new Intent(this, EpisodePlayer.class);
		newIntent.putExtra("from_notif", true);
		newIntent.putExtra("show_id", show_id);
		newIntent.putExtra("episode_id", podcast_id);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				newIntent, 0);

		n = new Notification.Builder(getApplicationContext())
				.setContentTitle("Hipstacast")
				.setSmallIcon(R.drawable.ic_notification).setContentText(title)
				.setOngoing(true).setContentIntent(contentIntent)
				.getNotification();

	}

	private void setupUIFromLocalData() {
		Log.d("HIP_PID", String.valueOf(podcast_id) + String.valueOf(show_id));
		if (show_id > 0) {
			Cursor p = getContentResolver()
					.query(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
							+ show_id + "/episodes"),
							new String[] { "_id", "title", "duration",
									"podcast_id", "status", "position",
									"content_url", "shownotes", "guid", "donation_url" },
							"_id = ?",
							new String[] { String.valueOf(podcast_id) }, null);
			p.moveToFirst();
			seek = (SeekBar) findViewById(R.id.playerSeekBar);

			if (!fromNotification) {
				start_position = p.getInt(p.getColumnIndex("position")) * 1000;
				seek.setProgress(start_position);
				seek.setOnSeekBarChangeListener(chl);
			}
			TextView title = ((TextView) findViewById(R.id.playerEpisodeName));
			title.setText(p.getString(p.getColumnIndex("title")));
			title.setSelected(true);
			seek.setMax(p.getInt(p.getColumnIndex("duration")) * 1000);

			WebView v = (WebView) findViewById(R.id.playerEpisodeDesc);
			v.loadData(p.getString(p.getColumnIndex("shownotes")), "text/html",
					null);

			isPlaying = false;
			name = p.getString(p.getColumnIndex("title"));
			url = p.getString(p.getColumnIndex("guid"));
			donation_url = p.getString(p.getColumnIndex("donation_url"));
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, 1000);
			buildNotification(p.getString(p.getColumnIndex("title")));
			p.close();
			if (!fromNotification)
				complete = true;
		}
	}

	private void setEpisodeAsListened() {
		ContentValues c = new ContentValues();
		c.put("position", 0);
		c.put("status", 3);
		getContentResolver()
				.update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
						+ show_id + "/episodes/" + podcast_id), c, "_id = ?",
						new String[] { String.valueOf(player.podcast_id) });

		start_position = player.mediaPlayer.getCurrentPosition();

	}

	private void savePosition() {
		ContentValues c = new ContentValues();
		c.put("position", (int) player.mediaPlayer.getCurrentPosition() / 1000);
		c.put("status", 2);
		getContentResolver()
				.update(Uri.parse("content://com.ifrins.hipstacast.provider.HipstacastContentProvider/podcasts/"
						+ show_id + "/episodes/" + podcast_id), c, "_id = ?",
						new String[] { String.valueOf(player.podcast_id) });

		start_position = player.mediaPlayer.getCurrentPosition();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		show_id = getIntent().getExtras().getInt("show_id");
		podcast_id = getIntent().getExtras().getInt("episode_id");
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, HipstacastPlayerService.class);

		getApplicationContext().startService(intent);
		bindService(intent, mConnection, Context.BIND_DEBUG_UNBIND);
		fromNotification = getIntent().getExtras().getBoolean("from_notif");
		setupUIFromLocalData();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("HIP_S", "Service connected");
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			player = binder.getService();
			bound = true;
			if (fromNotification) {
				Log.d("HIP_S", "The url is: " + player.podcastToPlayUrl);
				start_position = player.mediaPlayer.getCurrentPosition();
				SeekBar b = ((SeekBar) findViewById(R.id.playerSeekBar));
				b.setProgress(start_position);
				b.setOnSeekBarChangeListener(chl);
				complete = true;
			} else if (!fromNotification && player.isPlaying()) {
				if (player.show_id != show_id
						&& player.podcast_id != podcast_id) {
					player.pause();
					savePosition();
					player.clean();
					player.podcastToPlayUrl = android.os.Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/Android/data/com.ifrins.hipstacast/files/shows/"
							+ show_id + "/" + podcast_id + ".mp3";
					player.podcast_id = podcast_id;
					player.show_id = show_id;
					player.n = n;
					Log.d("HIP-NW-SP", String.valueOf(start_position));
					player.start_position = start_position;
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
			Log.d("HIP_S", "Service disconnected");
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player, menu);
		MenuItem item = menu.findItem(R.id.menuPlayToggle);
		if (fromNotification && player != null && !player.isPlaying()) {
			item.setTitle(R.string.menu_play);
			item.setIcon(R.drawable.ic_action_play);
		} else if (fromNotification) {
			item.setTitle(R.string.menu_pause);
			item.setIcon(R.drawable.ic_action_pause);
		}
		if (donation_url != "") {
			MenuItem d = menu.findItem(R.id.menuPlayDonate);
			d.setEnabled(true);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuPlayToggle:
			Log.d("HIP-DEB", "player.isPlaying() = ");
			if (!player.isPlaying() && complete) {
				player.clean();
				item.setTitle(R.string.menu_pause);
				item.setIcon(R.drawable.ic_action_pause);
				player.podcastToPlayUrl = android.os.Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/Android/data/com.ifrins.hipstacast/files/shows/"
						+ show_id + "/" + podcast_id + ".mp3";
				player.podcast_id = podcast_id;
				player.show_id = show_id;
				player.n = n;
				Log.d("HIP-NW-SP", String.valueOf(start_position));
				player.start_position = start_position;
				player.play();
				isPlaying = true;
				Log.d("HIP-STATUS", "Should start");
			} else if (player.isPlaying() && complete) {
				item.setTitle(R.string.menu_play);
				item.setIcon(R.drawable.ic_action_play);
				player.pause();
				savePosition();
				isPlaying = false;
				Log.d("HIP-STATUS", "Should stop");
			}

			return true;
		case R.id.menuPlayShare:
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(
					android.content.Intent.EXTRA_TEXT,
					String.format(getString(R.string.share_text), name + " - "
							+ url));
			startActivity(Intent.createChooser(sharingIntent,
					getString(R.string.share)));

			return true;
		case R.id.menuPlayDonate:
			Intent donateIntent = new Intent(Intent.ACTION_VIEW);
			donateIntent.setData(Uri.parse(donation_url));
			startActivity(donateIntent);

			return true;
		case R.id.menuPlayWebsite:
			Intent openIntent = new Intent(Intent.ACTION_VIEW);
			openIntent.setData(Uri.parse(url));
			startActivity(openIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
