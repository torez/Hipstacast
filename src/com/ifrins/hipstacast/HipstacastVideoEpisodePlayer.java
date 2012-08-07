package com.ifrins.hipstacast;

import com.ifrins.hipstacast.HipstacastPlayerService.LocalBinder;
import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.tasks.OnTaskCompleted;
import com.ifrins.hipstacast.utils.PlayerUIUtils;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class HipstacastVideoEpisodePlayer extends Activity {
	
	int show_id;
	int podcast_id;
	int type;
	int start_position;
	int duration;
	Boolean visible = true;
	SeekBar seekBar = null;
	View videoTopView = null;
	View videoBottomView = null;
	HipstacastPlayerService player;
	Boolean bound = false;
	ImageButton playToggleButton;
	ImageButton fastForwardButton;
	ImageButton rewindButton;
	String name;
	Handler seekBarUpdateHandler = new Handler();
	SurfaceView videoSurface = null;

	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("HIP_S", "Service connected");
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			player = binder.getService();
			bound = true;
			if (player != null && player.isPlaying()) {
				playToggleButton.setImageResource(R.drawable.ic_action_pause);
				seekBarUpdateHandler.post(updateRunnable);
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
			Log.d("HIP_S", "Service disconnected");
		}
	};
	
	private OnClickListener playToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d("HIP-PLAY", "Click!");
			if (player != null && !player.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_pause);
				startPlaying();
			} else if (player != null && player.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_play);
				stopPlaying();
			}
		}
	};
	
	private OnClickListener fastForwardClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player != null && player.isPlaying()) {
				int newPosition = player.getCurrentPosition() + 30000;
				player.seekTo(newPosition);
				seekBar.setProgress(newPosition);
			}
		}
	};
	
	private OnClickListener rewindClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player != null && player.isPlaying()) {
				int newPosition = player.getCurrentPosition() - 30000;
				player.seekTo(newPosition);
				seekBar.setProgress(newPosition);
			}
		}
	};
	
	private OnClickListener surfaceClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (player.isPlaying())
				if (visible) {
					visible = false;
					videoTopView.animate().alpha(0);
					videoBottomView.animate().alpha(0);
					seekBarUpdateHandler.removeCallbacks(updateRunnable);
				} else {
					visible = true;
					seekBarUpdateHandler.post(updateRunnable);
					videoTopView.animate().alpha(1);
					videoBottomView.animate().alpha(1);
				}
		}
	};
	
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			if (fromUser) {
				player.seekTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
	};
	
	final Runnable updateRunnable = new Runnable()
	{
	    public void run() 
	    {
	        if (player.isPlaying()) {
	        	seekBar.setProgress(player.getCurrentPosition());
	        	seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
	        }
	    }
	};
	
	public OnTaskCompleted completionListener = new OnTaskCompleted() {

		@Override
		public void onTaskCompleted(String task) {
			Log.d("HIP-TASK", task);
			if (task.equals(Hipstacast.TASK_PLAYBACK_COMPLETED)) {
				playToggleButton.setImageResource(R.drawable.ic_action_play);
				seekBarUpdateHandler.removeCallbacks(updateRunnable);
				seekBar.setProgress(player.getDuration());
				start_position = 0;
				player.destroy();
				PlayerUIUtils.setEpisodeAsListened(getApplicationContext(), podcast_id);
			}
		}
		
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_video);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		show_id = getIntent().getExtras().getInt("show_id");
		podcast_id = getIntent().getExtras().getInt("episode_id");
		type = getIntent().getExtras().getInt("type");
		
		((Hipstacast) getApplicationContext()).trackPageView("/player");
		
		seekBar = (SeekBar)findViewById(R.id.playerSeekBar);
		videoTopView = findViewById(R.id.viewVideoTop);
		videoBottomView = findViewById(R.id.viewVideoBottom);
		playToggleButton = (ImageButton)findViewById(R.id.playToggleButton);
		rewindButton = (ImageButton)findViewById(R.id.playerRewind);
		fastForwardButton = (ImageButton)findViewById(R.id.playerFastForward);
		
		Intent intent = new Intent(this, HipstacastPlayerService.class);

		startService(intent);
		bindService(intent, mConnection, 0);

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		playToggleButton.setOnClickListener(playToggleClickListener);
		fastForwardButton.setOnClickListener(fastForwardClickListener);
		rewindButton.setOnClickListener(rewindClickListener);
		
		Cursor episode = getContentResolver().query(Hipstacast.EPISODES_PROVIDER_URI, new String[] {"_id", "title", "duration",
				"podcast_id", "status", "position",
				"content_url", "shownotes", "guid",
				"donation_url", "type" }, "_id = ?",
		new String[] { String.valueOf(podcast_id) }, null);
		
		episode.moveToFirst();
		duration = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_DURATION));
		seekBar.setMax(duration*1000);
		
		name = episode.getString(episode.getColumnIndex(HipstacastProvider.EPISODE_TITLE));
		
		int pos = episode.getInt(episode.getColumnIndex(HipstacastProvider.EPISODE_CURRENT_POSITION));
		if (pos > 0) {
			start_position = pos * 1000;
			seekBar.setProgress(pos * 1000);
		}
		
		videoSurface = (SurfaceView)findViewById(R.id.videoPlayer);
		videoSurface.setOnClickListener(surfaceClickListener);
		episode.close();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (player != null && !player.isPlaying())
			player.destroy();
			player = null;
		if (bound)
			unbindService(mConnection);
		videoSurface = null;
		seekBarUpdateHandler.removeCallbacks(updateRunnable);		
	}
	
	private void startPlaying() {
		player.clean();
		player.podcastToPlayUrl = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/com.ifrins.hipstacast/files/shows/"
				+ show_id
				+ "/" + podcast_id + ".mp3";
		player.podcast_id = podcast_id;
		player.show_id = show_id;
		player.n = PlayerUIUtils.buildNotification(this, name, show_id, podcast_id, type);
		Log.d("HIP-NW-SP", String.valueOf(start_position));
		player.completionListener = completionListener;
		player.surface = videoSurface.getHolder();
		player.type = 1;
		player.start_position = start_position;
		player.play();
		if (duration == 0)
			fixDuration();
		seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
		Log.d("HIP-STATUS", "Should start");
	}
	
	private void stopPlaying() {
		player.pause();
		start_position = player.mediaPlayer.getCurrentPosition();
		PlayerUIUtils.savePosition(this, podcast_id, start_position);
		Log.d("HIP-STATUS", "Should stop");
	}

	private void fixDuration() {
		
		seekBar.setMax(player.mediaPlayer.getDuration());
				
		PlayerUIUtils.fixDuration(this, podcast_id, player.mediaPlayer.getDuration()/1000);
	}
	

}
