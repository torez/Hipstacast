package com.ifrins.hipstacast;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.SurfaceView;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.ifrins.hipstacast.utils.HipstacastLogging;
import com.ifrins.hipstacast.utils.PlayerCallbacks;

import com.google.analytics.tracking.android.EasyTracker;


public class HipstacastVideoEpisodePlayer extends Activity {

	int episodeId;
	HipstacastPlayerService player;
	Boolean bound = false;
	SeekBar seekBar;
	SurfaceView videoView;

	Runnable seekbarUpdaterRunnable = new Runnable() {

		@Override
		public void run() {
			HipstacastLogging.log("playbackprogress", player.getCurrentPosition());
			seekBar.setProgress(player.getCurrentPosition());
			seekBar.postDelayed(seekbarUpdaterRunnable, 1500);
		}

	};

	OnClickListener playbackToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ImageButton button = (ImageButton)v;

			if (player.isPlaying()) {
				button.setImageResource(R.drawable.ic_action_play_video);
				player.pause();
				seekBar.removeCallbacks(seekbarUpdaterRunnable);
			} else {
				button.setImageResource(R.drawable.ic_action_pause_video);
				player.play();
				seekBar.postDelayed(seekbarUpdaterRunnable, 3000);
			}
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			HipstacastPlayerService.LocalBinder binder = (HipstacastPlayerService.LocalBinder) service;
			player = binder.getService();
			bound = true;

			player.registerForCallbacks(mPlayerCallbacks);

			if (player.isAlreadyPrepared(episodeId)) {
				player.recover();
			} else {
				player.prepare(episodeId);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound = false;
		}
	};

	SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
			if (fromUser && player != null) {
				player.seekTo(position);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

	PlayerCallbacks mPlayerCallbacks = new PlayerCallbacks() {

		@Override
		public void onStartDoingUIWork() {
			seekBar = (SeekBar)findViewById(R.id.playerSeekBar);
			seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

			videoView = (SurfaceView)findViewById(R.id.videoPlayer);

			ImageButton playbackToogle = (ImageButton) findViewById(R.id.playToggleButton);
			playbackToogle.setOnClickListener(playbackToggleClickListener);

		}

		@Override
		public void onPrepared() {
			player.setPlayerSurface(videoView.getHolder().getSurface());
			seekBar.setMax(player.getMPEpisodeDuration());
			seekBar.setProgress(player.getCurrentPosition());
		}

		@Override
		public void onBufferingUpdate(int progress) {
			double percentage = progress * 0.01;
			int seekPos = (int) (seekBar.getMax() * percentage);
			seekBar.setSecondaryProgress(seekPos);
		}
	};

		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_video);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		episodeId = getIntent().getExtras().getInt("episode_id");

		Intent intent = new Intent(this, HipstacastPlayerService.class);
		this.startService(intent);
		this.bindService(intent, mConnection, 0);

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		videoView = null;
		if (player != null && !player.isPlaying()) {
			player.destroy();
		}

		if (bound) {
			this.unbindService(mConnection);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}
