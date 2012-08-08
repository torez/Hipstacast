package com.ifrins.hipstacast;

import com.ifrins.hipstacast.provider.HipstacastProvider;
import com.ifrins.hipstacast.utils.PlayerUIUtils;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.VideoView;

public class HipstacastVideoEpisodePlayer extends Activity {
	
	int show_id;
	int podcast_id;
	int type;
	int start_position;
	int duration;
	Boolean prepared = false;
	Boolean visible = true;
	SeekBar seekBar = null;
	View videoTopView = null;
	View videoBottomView = null;
	Boolean bound = false;
	ImageButton playToggleButton;
	ImageButton fastForwardButton;
	ImageButton rewindButton;
	String name;
	Handler seekBarUpdateHandler = new Handler();
	VideoView videoView = null;
	
	
	private OnClickListener playToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d("HIP-PLAY", "Click!");
			if (videoView != null && !videoView.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_pause);
				startPlaying();
			} else if (videoView != null && videoView.isPlaying()) {
				((ImageButton)v).setImageResource(R.drawable.ic_action_play);
				stopPlaying();
			}
		}
	};
	
	private OnClickListener fastForwardClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (videoView != null && videoView.isPlaying()) {
				int newPosition = videoView.getCurrentPosition() + 30000;
				videoView.seekTo(newPosition);
				seekBar.setProgress(newPosition);
			}
		}
	};
	
	private OnClickListener rewindClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (videoView != null && videoView.isPlaying()) {
				int newPosition = videoView.getCurrentPosition() - 30000;
				videoView.seekTo(newPosition);
				seekBar.setProgress(newPosition);
			}
		}
	};
	private OnTouchListener surfaceClickListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN && videoView.isPlaying()) {
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
			return true;
		}
	};
	
	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			if (fromUser) {
				videoView.seekTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
	};
	
	private OnPreparedListener onPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			prepared = true;
			Log.d("HIP_POS", String.valueOf(start_position));
			Log.d("HIP_DUR", String.valueOf(videoView.getDuration()));
				
			if (videoView.getDuration() > 0)
				seekBar.setMax(videoView.getDuration());
			videoView.seekTo(start_position);
			videoView.start();
				
		}
	};
	
	final Runnable updateRunnable = new Runnable()
	{
	    public void run() 
	    {
	        if (videoView.isPlaying()) {
	        	seekBar.setProgress(videoView.getCurrentPosition());
	        	seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
	        }
	    }
	};
	
	private final OnCompletionListener onCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			mp.pause();
			playToggleButton.setImageResource(R.drawable.ic_action_play);
			seekBarUpdateHandler.removeCallbacks(updateRunnable);
			seekBar.setProgress(videoView.getDuration());
			start_position = 0;
			PlayerUIUtils.setEpisodeAsListened(getApplicationContext(), podcast_id);
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
		
		videoView = (VideoView)findViewById(R.id.videoPlayer);
		videoView.setOnTouchListener(surfaceClickListener);
		

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
		
		episode.close();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		videoView = null;
		seekBarUpdateHandler.removeCallbacks(updateRunnable);		
	}
	
	private void startPlaying() {
		String videoURI = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/com.ifrins.hipstacast/files/shows/"
				+ show_id
				+ "/" + podcast_id + ".mp3";
		if (videoView != null && !prepared) {
			videoView.setOnPreparedListener(onPreparedListener);
			videoView.setVideoURI(Uri.parse(videoURI));
			videoView.setOnCompletionListener(onCompletionListener);
		}
		else if (videoView != null && prepared) {
			videoView.seekTo(start_position);
			videoView.start();
		}
			
		if (duration == 0)
			fixDuration();
		seekBarUpdateHandler.postDelayed(updateRunnable, 1100);
		Log.d("HIP-STATUS", "Should start");
	}
	
	private void stopPlaying() {
		videoView.pause();
		start_position = videoView.getCurrentPosition();
		PlayerUIUtils.savePosition(this, podcast_id, start_position);
		Log.d("HIP-STATUS", "Should stop");
	}

	private void fixDuration() {
		
		seekBar.setMax(videoView.getDuration());
				
		PlayerUIUtils.fixDuration(this, podcast_id, videoView.getDuration()/1000);
	}
	

}
